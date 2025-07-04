package enterprises.iwakura.sigewine.core;

import enterprises.iwakura.sigewine.core.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.core.extension.SigewineConstellation;
import enterprises.iwakura.sigewine.core.utils.Preconditions;
import enterprises.iwakura.sigewine.core.utils.collections.TypedCollection;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Main entry point to the Sigewine Dependency Injection (DI) API.
 * <p>
 * This class provides functionality for scanning, registering, and injecting beans
 * (classes or methods annotated with {@link RomaritimeBean} or the extension of it) into a dependency graph.
 * </p>
 * <p>
 * You may add additional functionality by implementing the {@link SigewineConstellation} interface and adding it to the Sigewine instance.
 * </p>
 * <p>
 * <b>Usage:</b>
 * <ol>
 *   <li>Create an instance of {@link SigewineOptions} to configure logging and other options.</li>
 *   <li>Instantiate {@link Sigewine} with the options.</li>
 *   <li>Use {@link #treatment(Class)} or {@link #treatment(String, ClassLoader)} to scan packages for beans.</li>
 *   <li>Use {@link #syringe(Class)} to inject dependencies into a class.</li>
 * </ol>
 */
@Data
@Slf4j
public class Sigewine {

    /**
     * Sigewine options.
     */
    protected final SigewineOptions sigewineOptions;
    /**
     * List of constellations to extend the functionality of Sigewine.
     */
    protected final List<SigewineConstellation> constellations = new ArrayList<>(List.of());
    /**
     * Map of beans registered in the DI container.
     */
    protected final Map<BeanDefinition, Object> singletonBeans = new HashMap<>();
    /**
     * List of classes that have method beans that need to be initialized later.
     */
    protected final List<Class<?>> initializeLaterMethodBeans = new ArrayList<>();
    /**
     * Cache for method bean declaring classes.
     */
    protected final Map<Class<?>, Object> methodBeanDeclaringClassCache = new HashMap<>();
    /**
     * Map of original beans that are proxied. This is used to keep track of the original bean instances
     * when they are proxied by the AOP constellation or any other constellation that creates proxies.
     */
    protected final Map<BeanDefinition, Object> proxiedOriginalBeans = new HashMap<>();
    /**
     * Constructor for Sigewine.
     *
     * @param sigewineOptions Sigewine options
     */
    public Sigewine(SigewineOptions sigewineOptions) {
        this.sigewineOptions = sigewineOptions;
    }

    /**
     * Default constructor for Sigewine with default options.
     * <p>
     * This constructor uses the default {@link SigewineOptions} for logging and other configurations.
     * </p>
     */
    public Sigewine() {
        this(new SigewineOptions());
    }

    /**
     * Invokes {@link #treatment(String, ClassLoader)} with the package name of the class and its class loader
     *
     * @param clazz Class to get the package name from
     */
    public void treatment(Class<?> clazz) {
        treatment(clazz.getPackageName(), clazz.getClassLoader());
    }

    /**
     * Scans the package for beans and registers them.
     * <p>
     * This method performs the following steps:
     * <ol>
     *     <li>Scans for methods annotated with {@link RomaritimeBean} (or the extension of it) and registers their return values as beans.</li>
     *     <li>Scans for classes annotated with {@link RomaritimeBean} (or the extension of it) and registers them as beans.</li>
     *     <li>Injects beans into fields of type {@link TypedCollection}.</li>
     * </ol>
     *
     * @param packageName The package name to scan.
     * @param classLoader The class loader to use for scanning.
     */
    @SneakyThrows
    public synchronized void treatment(String packageName, ClassLoader classLoader) {
        log.info("Scanning package '{}' for classes annotated with @Romaritime", packageName);

        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName, classLoader))
                .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated)
                .filterInputsBy(new FilterBuilder().includePackage(packageName));
        config.setClassLoaders(new ClassLoader[] {classLoader});
        final var reflections = new Reflections(config);
        final var annotatedClasses = reflections.getTypesAnnotatedWith(RomaritimeBean.class);
        final var annotatedMethods = reflections.getMethodsAnnotatedWith(RomaritimeBean.class);

        log.info("Found '{}' classes annotated with bean annotation", annotatedClasses.size());
        log.info("Found '{}' methods annotated with bean annotation", annotatedMethods.size());

        var beanDefinitions = new HashSet<BeanDefinition>();
        annotatedClasses.forEach(clazz -> beanDefinitions.add(BeanDefinition.of(clazz)));
        annotatedMethods.forEach(method -> beanDefinitions.add(BeanDefinition.of(method)));

        log.debug("Sorting bean definitions...");
        //@formatter:off
        var sortedBeanDefinitions = beanDefinitions.stream()
                .peek(beanDefinition -> beanDefinition.computeBeanScore(beanDefinitions))
                .sorted(Comparator.comparingLong(BeanDefinition::getBeanScore))
                .toList();
        //@formatter:on

        for (BeanDefinition beanDefinition : sortedBeanDefinitions) {
            log.debug("Registering bean definition '{}'", beanDefinition);

            if (beanDefinition.getMethod() != null) {
                registerMethodBean(beanDefinition);
            } else {
                registerClassBean(beanDefinition);
            }
        }

        // Process constellations
        log.debug("Processing constellations...");
        constellations.stream()
            .sorted(Comparator.comparingInt(SigewineConstellation::getPriority))
            .forEach(constellation -> {
                log.debug("Processing constellation '{}' with priority '{}'", constellation.getClass().getSimpleName(), constellation.getPriority());
                constellation.processBeans(this);
            });

        // Inject beans into collections
        log.debug("Going through beans to inject beans into TypedCollections");
        for (Map.Entry<BeanDefinition, Object> beanEntry : singletonBeans.entrySet()) {
            final var beanDefinition = beanEntry.getKey();
            // Prefer original bean since it might be a proxy and we need the original instance
            final var bean = Optional.ofNullable(proxiedOriginalBeans.get(beanDefinition)).orElse(beanEntry.getValue());
            log.debug("Going through bean '{}': '{}'", beanDefinition, bean);
            final var declaredFields = getAllFields(bean.getClass());

            for (var field : declaredFields) {
                var annotationPresent = field.isAnnotationPresent(RomaritimeBean.class);
                var isCollection = Collection.class.isAssignableFrom(field.getType());

                if (annotationPresent && isCollection) {
                    var wasAccessible = field.canAccess(bean);
                    field.setAccessible(true);

                    var collectionObject = field.get(bean);

                    if (!(collectionObject instanceof TypedCollection<?> collection)) {
                        log.debug("Field '{}' is collection, but is not instance of TypedCollection. Ignoring", field.getName());
                        continue;
                    }

                    var collectionType = collection.getType();

                    if (collectionType == null) {
                        throw new IllegalArgumentException("Field " + field.getName() + " must have a type");
                    }

                    if (collectionType.isAssignableFrom(TypedCollection.class)) {
                        throw new IllegalArgumentException("Type of TypedCollection " + field.getName() + " cannot be a TypedCollection");
                    }

                    var beansToInject = getAllBeansThatAreAssignableFrom(collectionType);
                    log.debug("Injecting '{}' beans into bean named '{}' for collection named '{}' of type '{}'",
                        beansToInject.size(), beanDefinition, field.getName(), collectionType.getName()
                    );
                    beansToInject.forEach(collection::addTypedObject);
                    field.setAccessible(wasAccessible);
                }
            }
        }

        log.debug("Going through beans to inject itself");
        for (Map.Entry<BeanDefinition, Object> beanEntry : singletonBeans.entrySet()) {
            final var beanDefinition = beanEntry.getKey();
            final var originalBean = Optional.ofNullable(proxiedOriginalBeans.get(beanDefinition)).orElse(beanEntry.getValue());
            final var proxiedBean = beanEntry.getValue();
            log.debug("Going through bean '{}': '{}'", beanDefinition, originalBean);
            final var declaredFields = getAllFields(originalBean.getClass());

            for (var field : declaredFields) {
                if (field.isAnnotationPresent(RomaritimeBean.class) && field.getType().isAssignableFrom(originalBean.getClass())) {
                    log.debug("Injecting self into field '{}' of class '{}'", field.getName(), originalBean.getClass().getName());
                    field.setAccessible(true);
                    // Set the field to the original bean instance, not the proxied one,
                    // hover use the proxied bean for the field value
                    field.set(originalBean, proxiedBean);
                }
            }
        }

        log.debug("Cleaning bean definitions...");
        beanDefinitions.forEach(beanDefinition -> beanDefinition.getConstructorParameters().clear());

        log.info("Finished scanning package '{}', singleton bean count: '{}'", packageName, singletonBeans.size());
    }

    /**
     * Injects dependencies into the class.
     *
     * @param clazz Class to inject dependencies into
     * @param <T>   Type of the class
     *
     * @return Instance of the class with dependencies injected
     */
    @SneakyThrows
    public <T> T syringe(Class<T> clazz) {
        // Get bean name for the class
        final var beanDefinition = BeanDefinition.of(clazz);

        // If bean is already registered, return it
        if (isBeanRegistered(beanDefinition)) {
            final var beanObject = getRegisteredBean(beanDefinition);
            if (!clazz.isAssignableFrom(beanObject.getClass())) {
                throw new IllegalArgumentException("Bug! Bean " + beanDefinition + " is not of type " + clazz.getName());
            }
            log.debug("Returning registered bean '{}' of class '{}': '{}'", beanDefinition, clazz.getName(), beanObject);
            return clazz.cast(getRegisteredBean(beanDefinition));
        }

        // Inject dependencies
        log.debug("Injecting beans into class bean '{}' of class '{}'", beanDefinition, clazz.getName());
        Preconditions.checkOneConstructor(clazz);

        final var constructor = clazz.getConstructors()[0];
        final var parameters = constructor.getParameters();
        final var args = new Object[parameters.length];

        log.debug("Found '{}' parameters for bean '{}' of class '{}': '{}'", parameters.length, beanDefinition, clazz.getName(), parameters);

        for (int i = 0; i < parameters.length; i++) {
            final var parameter = parameters[i];
            final var parameterType = parameter.getType();
            final var parameterName = parameter.getName();
            final var parameterBeanDefinition = BeanDefinition.of(parameter);

            Object argInstance;

            if (isBeanRegistered(parameterBeanDefinition)) {
                argInstance = getRegisteredBean(parameterBeanDefinition);
            } else {
                throw new IllegalArgumentException("No bean found for parameter '" + parameterName + "' of type '" + parameterType.getName() + "' in class '" + clazz.getName() + "'");
            }

            args[i] = argInstance;
        }

        log.debug("Resolved '{}' args for bean '{}' of class '{}', creating instance: '{}'", args.length, beanDefinition, clazz.getName(), args);
        return clazz.cast(constructor.newInstance(args));
    }

    /**
     * Adds a constellation to the Sigewine instance.
     *
     * @param constellation Constellation to add
     */
    public void addConstellation(@NonNull SigewineConstellation constellation) {
        if (constellations.stream().anyMatch(c -> c.getClass().equals(constellation.getClass()))) {
            throw new IllegalArgumentException("Constellation " + constellation.getClass().getSimpleName() + " is already registered");
        }
        log.debug("Adding constellation '{}' with priority '{}'", constellation.getClass().getSimpleName(), constellation.getPriority());
        constellations.add(constellation);
    }

    /**
     * Registers a method bean.
     *
     * @param beanDefinition Bean definition to register
     */
    @SneakyThrows
    protected void registerMethodBean(BeanDefinition beanDefinition) {
        final var method = beanDefinition.getMethod();
        final var declaringClass = method.getDeclaringClass();
        final var returnType = method.getReturnType();

        Preconditions.checkNoVoidReturnType(method);
        Preconditions.checkNoPrimitiveReturnType(method);

        if (isBeanRegistered(beanDefinition)) {
            throw new IllegalArgumentException("Class " + returnType.getName() + " already registered as " + beanDefinition);
        }

        log.debug("Registering method bean '{}' of class '{}'", beanDefinition, declaringClass.getName());

        //@formatter:off
        final var beanClassInstance = methodBeanDeclaringClassCache.computeIfAbsent(declaringClass, clazz -> {
            var classBeanDefinition = BeanDefinition.of(clazz);

            if (isBeanRegistered(classBeanDefinition)) {
                return getRegisteredBean(classBeanDefinition);
            }

            boolean hasNoArgConstructor = Arrays.stream(clazz.getConstructors()).anyMatch(constructor -> constructor.getParameterCount() == 0);

            if (hasNoArgConstructor) {
                try {
                    return clazz.getConstructor().newInstance();
                } catch (Exception exception) {
                    throw new RuntimeException("Failed to create instance of " + clazz.getName() + " for bean " + beanDefinition + " (no no-args constructor?)", exception);
                }
            }

            return syringe(clazz);
        });
        //@formatter:on

        final var beanInstance = method.invoke(beanClassInstance);

        if (beanInstance == null) {
            throw new IllegalArgumentException("Method " + method + " cannot return null");
        }

        registerBeanWithInstance(returnType, beanDefinition, beanInstance);
    }

    /**
     * Registers a class bean.
     *
     * @param beanDefinition Bean definition to register
     */
    @SneakyThrows
    protected void registerClassBean(BeanDefinition beanDefinition) {
        final var constructorBeanDefinitions = beanDefinition.getConstructorBeanDefinitions();
        final var beanClass = beanDefinition.getClazz();
        Object beanInstance;

        if (constructorBeanDefinitions.isEmpty()) {
            log.debug("Creating bean instance for class bean '{}'", beanDefinition);
            beanInstance = beanClass.getConstructor().newInstance();
        } else {
            log.debug("Creating bean instance for class bean '{}' with constructor arguments: '{}'", beanDefinition, constructorBeanDefinitions);
            final var constructor = beanClass.getConstructors()[0];
            var parameters = constructor.getParameters();
            var args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                final var parameterBeanDefinition = constructorBeanDefinitions.get(i);
                if (isBeanRegistered(parameterBeanDefinition)) {
                    args[i] = getRegisteredBean(parameterBeanDefinition);
                } else {
                    throw new IllegalArgumentException("No bean found for constructor argument '" + parameterBeanDefinition + "' of class '" + beanClass.getName() + "'");
                }
            }

            Collections.addAll(beanDefinition.getConstructorParameters(), args);
            beanInstance = constructor.newInstance(args);
        }

        log.debug("Processing constellations for class bean '{}'", beanDefinition);
        for (SigewineConstellation constellation : constellations) {
            beanInstance = constellation.processCreatedBeanInstance(beanInstance, beanDefinition, this);
        }

        log.debug("Registering bean instance for class bean '{}' of class '{}'", beanDefinition, beanClass.getName());
        registerBeanWithInstance(beanClass, beanDefinition, beanInstance);
    }

    /**
     * Registers a bean with an instance. Checks if the bean is already registered.
     *
     * @param clazz          Class of the bean
     * @param beanDefinition Bean definition
     * @param instance       Instance of the bean
     */
    protected void registerBeanWithInstance(Class<?> clazz, BeanDefinition beanDefinition, Object instance) {
        if (isBeanRegistered(beanDefinition)) {
            throw new IllegalArgumentException("Class " + instance.getClass().getName() + " already registered as " + beanDefinition);
        }
        log.debug("Registering bean '{}' of class '{}'", beanDefinition, clazz.getName());
        singletonBeans.put(beanDefinition, instance);
    }

    /**
     * Gets all beans that are assignable from the specified class.
     *
     * @param clazz Class to check
     *
     * @return Set of beans that are assignable from the specified class
     */
    protected Set<Object> getAllBeansThatAreAssignableFrom(Class<?> clazz) {
        final var beans = new HashSet<>();
        for (var entry : this.singletonBeans.entrySet()) {
            if (clazz.isAssignableFrom(entry.getValue().getClass())) {
                beans.add(entry.getValue());
            }
        }
        return beans;
    }

    /**
     * Checks if the bean is already registered.
     *
     * @param beanDefinition Bean definition to check
     *
     * @return True if the bean is already registered, false otherwise
     */
    protected boolean isBeanRegistered(BeanDefinition beanDefinition) {
        return singletonBeans.entrySet().stream().anyMatch(entry -> entry.getKey().is(beanDefinition));
    }

    /**
     * Gets the bean name from the annotation or class name.
     *
     * @param beanDefinition Bean definition to check
     *
     * @return Bean name
     */
    protected Object getRegisteredBean(BeanDefinition beanDefinition) {
        return singletonBeans.entrySet().stream()
            .filter(entry -> entry.getKey().is(beanDefinition))
            .map(Map.Entry::getValue)
            .findFirst()
            .map(beanDefinition.getClazz()::cast)
            .orElseThrow(() -> new IllegalArgumentException("No bean found for " + beanDefinition));
    }

    /**
     * Gets all fields of the class and its superclasses.
     *
     * @param clazz Class to get the fields from
     *
     * @return Array of fields
     */
    protected Field[] getAllFields(Class<?> clazz) {
        final var fields = new ArrayList<Field>();
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields.toArray(new Field[0]);
    }
}
