package enterprises.iwakura.sigewine;

import enterprises.iwakura.sigewine.annotations.RomaritimeBean;
import enterprises.iwakura.sigewine.utils.Preconditions;
import enterprises.iwakura.sigewine.utils.collections.TypedCollection;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * Main entry point to the Sigewine Dependency Injection (DI) API.
 * <p>
 * This class provides functionality for scanning, registering, and injecting beans
 * (classes or methods annotated with {@link RomaritimeBean}) into a dependency graph.
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
     * Map of beans registered in the DI container.
     */
    protected final Map<String, Object> beans = new HashMap<>();

    /**
     * List of classes that have method beans that need to be initialized later.
     */
    protected final List<Class<?>> initializeLaterMethodBeans = new ArrayList<>();

    /**
     * Cache for method bean declaring classes.
     */
    protected final Map<Class<?>, Object> methodBeanDeclaringClassCache = new HashMap<>();

    /**
     * Constructor for Sigewine.
     *
     * @param sigewineOptions Sigewine options
     */
    public Sigewine(SigewineOptions sigewineOptions) {
        this.sigewineOptions = sigewineOptions;
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
     *     <li>Scans for methods annotated with {@link RomaritimeBean} and registers them as beans.</li>
     *     <li>Scans for classes annotated with {@link RomaritimeBean} and registers them as beans.</li>
     *     <li>Injects beans into fields of type {@link TypedCollection}.</li>
     * </ol>
     *
     * @param packageName The package name to scan.
     * @param classLoader The class loader to use for scanning.
     */
    @SneakyThrows
    public synchronized void treatment(String packageName, ClassLoader classLoader) {
        log.atLevel(sigewineOptions.getLogLevel()).log("Scanning package '{}' for classes annotated with @Romaritime", packageName);

        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName, classLoader))
                .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated)
                .filterInputsBy(new FilterBuilder().includePackage(packageName));
        config.setClassLoaders(new ClassLoader[] { classLoader });
        final var reflections = new Reflections(config);
        final var annotatedClasses = reflections.getTypesAnnotatedWith(RomaritimeBean.class);
        final var annotatedMethods = reflections.getMethodsAnnotatedWith(RomaritimeBean.class);

        log.atLevel(sigewineOptions.getLogLevel()).log("Found '{}' classes annotated with @Romaritime", annotatedClasses.size());
        log.atLevel(sigewineOptions.getLogLevel()).log("Found '{}' methods annotated with @Romaritime", annotatedMethods.size());

        // Register method beans
        log.atLevel(sigewineOptions.getLogLevel()).log("Registering methods annotated with @Romaritime");
        for (Method method : annotatedMethods) {
            registerMethodBean(method);
        }

        // Initialize method beans that were not initialized yet
        log.atLevel(sigewineOptions.getLogLevel()).log("Initializing later-initialization method beans");
        for (Class<?> clazz : initializeLaterMethodBeans) {
            log.atLevel(sigewineOptions.getLogLevel()).log("Initializing method beans for class '{}'", clazz.getName());
            registerClassBean(clazz, false);

            log.atLevel(sigewineOptions.getLogLevel()).log("Going through methods of class '{}'", clazz.getName());
            final var beanMethods = clazz.getDeclaredMethods();
            for (Method beanMethod : beanMethods) {
                if (beanMethod.isAnnotationPresent(RomaritimeBean.class)) {
                    registerMethodBean(beanMethod);
                }
            }
        }
        initializeLaterMethodBeans.clear();

        // Register class beans
        log.atLevel(sigewineOptions.getLogLevel()).log("Registering classes annotated with @Romaritime");
        for (Class<?> clazz : annotatedClasses) {
            registerClassBean(clazz, false);
        }

        // Inject beans into collections
        log.atLevel(sigewineOptions.getLogLevel()).log("Going through beans to inject beans into TypedCollections");
        for (Map.Entry<String, Object> namedBeanEntry : beans.entrySet()) {
            final var beanName = namedBeanEntry.getKey();
            final var bean = namedBeanEntry.getValue();
            log.atLevel(sigewineOptions.getLogLevel()).log("Going through bean '{}': '{}'", beanName, bean);
            final var declaredFields = bean.getClass().getDeclaredFields();

            for (var field : declaredFields) {
                var annotationPresent = field.isAnnotationPresent(RomaritimeBean.class);
                var isCollection = Collection.class.isAssignableFrom(field.getType());

                if (annotationPresent && isCollection) {
                    var wasAccessible = field.canAccess(bean);
                    field.setAccessible(true);

                    var collectionObject = field.get(bean);

                    if (!(collectionObject instanceof TypedCollection<?> collection)) {
                        log.atLevel(sigewineOptions.getLogLevel())
                           .log("Field '{}' is collection, but is not instance of TypedCollection. Ignoring", field.getName());
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
                    log.atLevel(sigewineOptions.getLogLevel()).log("Injecting '{}' beans into bean named '{}' for collection named '{}' of type '{}'",
                                                                   beansToInject.size(), beanName, field.getName(), collectionType.getName()
                    );
                    beansToInject.forEach(collection::addTypedObject);
                    field.setAccessible(wasAccessible);
                }
            }
        }

        log.atLevel(sigewineOptions.getLogLevel()).log("Finished scanning package '{}', bean count: '{}'", packageName, beans.size());
    }

    /**
     * Injects dependencies into the specified class.
     * <p>
     * This method resolves dependencies for the class and creates an instance
     * with all dependencies injected.
     * </p>
     *
     * @param clazz The class to inject dependencies into.
     * @param <T>   The type of the class.
     * @return An instance of the class with dependencies injected.
     */
    public <T> T syringe(Class<T> clazz) {
        return syringeInternal(clazz, (c) -> {
                                   throw new IllegalArgumentException("No bean found for class " + c.getName());
                               }
        );
    }

    /**
     * Injects dependencies into the class.
     *
     * @param clazz         Class to inject dependencies into
     * @param onUnknownBean Function to call when a bean is not found. Should return a non-null bean or throw an exception.
     * @param <T>           Type of the class
     *
     * @return Instance of the class with dependencies injected
     */
    @SneakyThrows
    protected <T> T syringeInternal(Class<T> clazz, Function<Class<?>, Object> onUnknownBean) {
        // Get bean name for the class
        final var beanName = getBeanName(clazz.getAnnotation(RomaritimeBean.class), clazz.getName());

        // If bean is already registered, return it
        if (beans.containsKey(beanName)) {
            final var beanObject = beans.get(beanName);
            if (!clazz.isAssignableFrom(beanObject.getClass())) {
                throw new IllegalArgumentException("Bug! Bean " + beanName + " is not of type " + clazz.getName());
            }
            log.atLevel(sigewineOptions.getLogLevel())
               .log("Returning registered bean '{}' of class '{}': '{}'", beanName, clazz.getName(), beanObject);
            return clazz.cast(beans.get(beanName));
        }

        // Inject dependencies
        log.atLevel(sigewineOptions.getLogLevel()).log("Injecting beans into class bean '{}' of class '{}'", beanName, clazz.getName());
        Preconditions.checkOneConstructor(clazz);

        final var constructor = clazz.getConstructors()[0];
        final var parameters = constructor.getParameters();
        final var args = new Object[parameters.length];

        log.atLevel(sigewineOptions.getLogLevel()).log("Found '{}' parameters for bean '{}' of class '{}': '{}'", parameters.length, beanName, clazz.getName(), parameters);

        for (int i = 0; i < parameters.length; i++) {
            final var parameter = parameters[i];
            final var romaritimeAnnotation = parameter.getAnnotation(RomaritimeBean.class);
            final var parameterType = parameter.getType();
            final var parameterName = parameter.getName();

            Object argInstance;

            if (romaritimeAnnotation != null
                    && !romaritimeAnnotation.name().isBlank()
                    && beans.containsKey(romaritimeAnnotation.name())) {
                argInstance = beans.get(romaritimeAnnotation.name());
            } else if (beans.containsKey(parameterType.getName())) {
                argInstance = beans.get(parameterType.getName());
            } else if (beans.containsKey(parameterName)) {
                argInstance = beans.get(parameterName);
            } else {
                log.atLevel(sigewineOptions.getLogLevel())
                   .log("No bean found for parameter '{}' of type '{}' in class '{}', using onUnknownBean function", parameterName, parameterType.getName(), clazz.getName());
                argInstance = onUnknownBean.apply(parameterType);
            }

            args[i] = argInstance;
        }

        log.atLevel(sigewineOptions.getLogLevel())
           .log("Resolved '{}' args for bean '{}' of class '{}', creating instance: '{}'", args.length, beanName, clazz.getName(), args);
        return clazz.cast(constructor.newInstance(args));
    }

    /**
     * Registers a method bean.
     *
     * @param method Method to register
     */
    @SneakyThrows
    protected void registerMethodBean(Method method) {
        final var romaritime = method.getAnnotation(RomaritimeBean.class);
        final var declaringClass = method.getDeclaringClass();
        final var returnType = method.getReturnType();

        Preconditions.checkNoVoidReturnType(method);
        Preconditions.checkNoPrimitiveReturnType(method);

        final var beanName = getBeanName(romaritime, returnType.getName());
        log.atLevel(sigewineOptions.getLogLevel())
           .log("Registering method '{}' of declaring class '{}' as bean '{}'", method, method.getDeclaringClass(), beanName);

        if (beans.containsKey(beanName)) {
            throw new IllegalArgumentException("Class " + returnType.getName() + " already registered as " + beanName);
        }

        final var beanClassInstance = methodBeanDeclaringClassCache.computeIfAbsent(declaringClass, clazz -> {
            boolean hasNoArgConstructor = Arrays.stream(clazz.getConstructors()).anyMatch(constructor -> constructor.getParameterCount() == 0);

            if (hasNoArgConstructor) {
                try {
                    return clazz.getConstructor().newInstance();
                } catch (Exception exception) {
                    throw new RuntimeException("Failed to create instance of " + clazz.getName() + " for bean " + beanName + " (nos no-args constructor?)", exception);
                }
            }

            try {
                // Check if the class already has a bean, if yes, return
                return syringeInternal(clazz, (otherClass) -> {
                    throw new IllegalStateException();
                });
            } catch (IllegalStateException ignored) {
                // Catch the exception and return null
                log.atLevel(sigewineOptions.getLogLevel()).log("Class '{}' has no no-args constructor, will be initialized later", clazz.getName());
                initializeLaterMethodBeans.add(clazz);
                return null;
            }
        });

        // In case of later initialization, we will not register the bean yet
        if (beanClassInstance != null) {
            final var beanInstance = method.invoke(beanClassInstance);

            if (beanInstance == null) {
                throw new IllegalArgumentException("Method " + method + " cannot return null");
            }

            registerBeanWithInstance(returnType, beanName, beanInstance);
        }
    }

    /**
     * Registers a class bean.
     *
     * @param clazz            Class to register
     * @param ignoreDuplicates Ignore duplicates
     */
    @SneakyThrows
    protected void registerClassBean(Class<?> clazz, boolean ignoreDuplicates) {
        final var beanName = getBeanName(clazz.getAnnotation(RomaritimeBean.class), clazz.getName());

        if (beans.containsKey(beanName)) {
            if (ignoreDuplicates) {
                log.atLevel(sigewineOptions.getLogLevel())
                   .log("Class '{}' already registered as '{}', ignoring per ignoreDuplicates", clazz.getName(), beanName);
                return;
            }

            if (beans.get(beanName).getClass() == clazz) {
                log.atLevel(sigewineOptions.getLogLevel())
                   .log("Class '{}' already registered as '{}', ignoring per same class instance", clazz.getName(), beanName);
                return;
            }

            throw new IllegalArgumentException("Class " + clazz.getName() + " already registered as " + beanName);
        }

        var constructors = clazz.getConstructors();
        if (constructors.length == 1 && constructors[0].getParameters().length == 0) {
            registerBeanWithInstance(clazz, beanName, clazz.getConstructor().newInstance());
            return;
        }

        Preconditions.checkOneConstructor(clazz);

        final var beanClassInstance = syringeInternal(clazz, this::recursiveOnUnknownBean);

        if (beanClassInstance == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " cannot return null");
        }

        registerBeanWithInstance(clazz, beanName, beanClassInstance);
    }

    /**
     * Registers a bean with an instance. Checks if the bean is already registered.
     *
     * @param clazz    Class of the bean
     * @param beanName Name of the bean
     * @param instance Instance of the bean
     */
    protected void registerBeanWithInstance(Class<?> clazz, String beanName, Object instance) {
        if (beans.containsKey(beanName)) {
            throw new IllegalArgumentException("Class " + instance.getClass().getName() + " already registered as " + beanName);
        }
        log.atLevel(sigewineOptions.getLogLevel()).log("Registering bean '{}' of class '{}'", beanName, clazz.getName());
        beans.put(beanName, instance);
    }

    /**
     * Recursively injects dependencies into the class.
     *
     * @param clazz Class to inject dependencies into
     *
     * @return Instance of the class with dependencies injected
     */
    protected Object recursiveOnUnknownBean(Class<?> clazz) {
        if (Collection.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Cannot inject collection of type " + clazz.getName() + ", please use TypedCollection");
        }
        Preconditions.checkAnnotated(clazz, RomaritimeBean.class);
        final var beanName = getBeanName(clazz.getAnnotation(RomaritimeBean.class), clazz.getName());
        log.atLevel(sigewineOptions.getLogLevel()).log("Recursively injecting beans into class bean '{}' of class '{}'", beanName, clazz.getName());
        if (beans.containsKey(beanName)) {
            log.atLevel(sigewineOptions.getLogLevel())
               .log("Returning already registered bean '{}' of class '{}': '{}'", beanName, clazz.getName(), beans.get(beanName));
            return beans.get(beanName);
        }
        registerClassBean(clazz, true);
        return syringeInternal(clazz, this::recursiveOnUnknownBean);
    }

    /**
     * Gets the bean name from the class.
     *
     * @param romaritimeBean Romaritime annotation
     * @param defaultName    Default name to use if no name is specified
     *
     * @return Bean name
     */
    protected String getBeanName(RomaritimeBean romaritimeBean, String defaultName) {
        if (romaritimeBean != null && !romaritimeBean.name().isBlank()) {
            return romaritimeBean.name();
        }

        return defaultName;
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
        for (var entry : this.beans.entrySet()) {
            if (clazz.isAssignableFrom(entry.getValue().getClass())) {
                beans.add(entry.getValue());
            }
        }
        return beans;
    }
}
