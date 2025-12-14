# Sigewine

> Sigewine is a Java library for simple and lightweight dependency injection.

![Coverage](.github/badges/jacoco.svg) [![Build & Test](https://github.com/iwakura-enterprises/sigewine/actions/workflows/build.yml/badge.svg)](https://github.com/iwakura-enterprises/sigewine/actions/workflows/build.yml)

[Source Code](https://github.com/iwakura-enterprises/sigewine) —
[Documentation](https://docs.iwakura.enterprises/sigewine.html) —
[Maven Central](https://central.sonatype.com/artifact/enterprises.iwakura/sigewine-core)

## Project structure

As of version 2.0.0, the project is split into multiple modules:

| Name   | Description                                                                                                                   | Version                                                                |
|--------|-------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------|
| `core` | The core library that provides the dependency injection functionality.                                                        | <a id="sigewine_core_version" href="https://central.sonatype.com/artifact/enterprises.iwakura/sigewine-core"><img src="https://maven-badges.sml.io/sonatype-central/enterprises.iwakura/sigewine-core/badge.png?style=for-the-badge" alt=""></img></a> |
| `aop`  | Contains AOP-like Proxy functionality to allow wrap beans. For example, to log method calls or to add transaction management. | <a id="sigewine_aop_version" href="https://central.sonatype.com/artifact/enterprises.iwakura/sigewine-aop"><img src="https://maven-badges.sml.io/sonatype-central/enterprises.iwakura/sigewine-aop/badge.png?style=for-the-badge" alt=""></img></a>  |
| `aop-sentry` | Provides integration with Sentry for AOP-like method interception. This module is optional and requires `sigewine-aop`. | <a id="sigewine_aop_sentry_version" href="https://central.sonatype.com/artifact/enterprises.iwakura/sigewine-aop-sentry"><img src="https://maven-badges.sml.io/sonatype-central/enterprises.iwakura/sigewine-aop-sentry/badge.png?style=for-the-badge" alt=""></img></a> |

Each module has its own maven artifact. The versions between them are aligned, so you can use the same version for all
modules.

> More modules may be added in the future, such as `sigewine-irminsul` and `sigewine-sentry`.
> {style="note"}

<warning title="AOP module size">
    AOP module is quite large, as it contains ByteBuddy dependency. It is recommended to use it only if you need
    AOP-like method interception functionality.
</warning>

## Installation

### Gradle
```groovy
implementation 'enterprises.iwakura:sigewine-core:VERSION'
implementation 'enterprises.iwakura:sigewine-aop:VERSION'
// Required for core module
implementation 'org.reflections:reflections:0.10.2'
// Required for AOP module
implementation 'net.bytebuddy:byte-buddy:1.17.5'
// Extension for AOP module
implementation 'enterprises.iwakura:sigewine-aop-sentry:VERSION'
```

### Maven
```xml
<dependency>
    <groupId>enterprises.iwakura</groupId>
    <artifactId>sigewine-core</artifactId>
    <version>VERSION</version>
</dependency>
<dependency>
    <groupId>enterprises.iwakura</groupId>
    <artifactId>sigewine-aop</artifactId>
    <version>VERSION</version>
</dependency>
<!-- Required for core module -->
<dependency>
    <groupId>org.reflections</groupId>
    <artifactId>reflections</artifactId>
    <version>0.10.2</version>
</dependency>
<!-- Required for AOP module -->
<dependency>
    <groupId>net.bytebuddy</groupId>
    <artifactId>byte-buddy</artifactId>
    <version>1.17.5</version>
</dependency>
<!-- Extension for AOP module -->
<dependency>
    <groupId>enterprises.iwakura</groupId>
    <artifactId>sigewine-aop-sentry</artifactId>
    <version>VERSION</version>
</dependency>
```

**The minimum required Java version is 21**.

## Dependency injection

Sigewine allows you to easily inject beans into your classes using annotations. In simple terms, you can define
a class instance as a "bean" and then inject it into other classes that require it. This automates the process of
managing class instances and passing them around, making your code cleaner and more maintainable.

In the end, Sigewine: a) creates instances of your classes, b) injects them into other classes that require them using
constructors.

## Features

- Automatic bean injection
- Automatic bean scanning
- Custom bean names
- Typed array list for bean injection
- Extensions (Constellations) for additional functionality

<warning title="Limitations">
<ul>
    <li>Cyclic dependencies are not supported.</li>
    <li>No lazy loading.</li>
    <li>All beans are singleton, for now.</li>
</ul>
</warning>

## Usage

1. Specify beans using `@RomaritimeBean` annotation.
2. Inject beans using `@RomaritimeBean` annotation in constructor.

<procedure title="Simple example" id="simple-example" collapsible="true" default-state="expanded">

```java
// Define a bean with @RomaritimeBean annotation
@RomaritimeBean
public class Config {

    private int someValue = 10;
}

// Define a class that uses the bean
@RomaritimeBean
public class SomeService {

    private final Config config;

    // Sigewine automatically injects the Config bean
    public SomeService(Config config) {
        this.config = config;
    }

    public void doSomething() {
        System.out.println("Config value: " + config.getSomeValue());
    }
}

// Create instance of SomeService
public static void main(String[] args) {
    // Create instance of Sigewine
    Sigewine sigewine = new Sigewine(new SigewineOptions());
    // Scan for beans in current package
    sigewine.treatment("your.package.name"); // or specify a class

    // Getting the beans
    SomeService service = sigewine.syringe(SomeService.class);
    service.doSomething(); // Outputs: Config value: 10
}
```

The `Sigewine#treatment()` method scans the specified package for classes and methods annotated with `@RomaritimeBean`
and
registers them as beans.

</procedure>

<procedure title="Method injection" id="method-injection" collapsible="true">

You may create beans using methods annotated with `@RomaritimeBean`. This allows you to creat beans from classes, which
cannot be annotated with `@RomaritimeBean` directly.

```java
public class SomeTreatment {

    @RomaritimeBean
    public ExternalClass externalClass() {
        return new ExternalClass();
    }
}
```

This will allow you to inject instance of `ExternalClass` as it was annotated with `@RomaritimeBean`. Method classes
may require other beans as constructor parameters and Sigewine will automatically inject them.

</procedure>

<procedure title="Custom named beans" id="custom-named-beans" collapsible="true">

Sometimes you may want to specify a custom name for your bean. You can do this by using the `name` attribute of the
`@RomaritimeBean` annotation. This is useful when you have multiple beans of the same type and you want to
differentiate them. When using them, you must specify the name in the constructor parameter.

```java
public class Configs {

    @RomaritimeBean(name = "customProductionConfig")
    public Config productionConfig() {
        return new Config();
    }

    @RomaritimeBean(name = "customDevelopmentConfig")
    public Config developmentConfig() {
        return new Config();
    }
}

public class ProductionService {

    private final Config config;

    // Injects the bean with custom name
    public ProductionService(
            @RomaritimeBean(name = "customProductionConfig") Config config
    ) {
        this.config = productionConfig;
    }
}

public class DevelopmentService {

    private final Config config;

    // Injects the bean with custom name
    public DevelopmentService(
            @RomaritimeBean(name = "customDevelopmentConfig") Config config
    ) {
        this.config = config;
    }
}
```

</procedure>

<procedure title="Abstract bean definitions" id="abstract-bean-definitions" collapsible="true">

Sometimes you have a bean that implements an interface or an abstract class. Sigewine respects this and allows you to
specify bean by the abstract class. The injected class will be the implementation of the abstract class or interface.

```java
// An abstract class that defines a service
public abstract class BaseService {

    public abstract void performAction();
}

// An implementation of the abstract class
@RomaritimeBean
public class ConcreteService extends BaseService {

    @Override
    public void performAction() {
        System.out.println("Action performed by ConcreteService");
    }
}

@RomaritimeBean
public class ServiceConsumer {

    private final BaseService service;

    // Sigewine automatically injects the ConcreteService bean
    public ServiceConsumer(BaseService service) {
        this.service = service;
    }

    public void useService() {
        service.performAction(); // Outputs: Action performed by ConcreteService
    }
}
```

</procedure>

<procedure title="Collection of beans" id="collection-of-beans" collapsible="true">

If you have multiple beans that extend a common base class or implement a common interface, you can inject
them as a collection.

```java
// Define a base entity interface
public interface BaseEntity {
    // Common properties and methods for all entities
}

// Define player entity
@RomaritimeBean
public class PlayerEntity implements BaseEntity {
    // Player specific properties and methods
}

// Define NPC entity
@RomaritimeBean
public class NpcEntity implements BaseEntity {
    // NPC specific properties and methods
}

// Define a service that uses all BaseEntity beans
@RomaritimeBean
@RequiredArgsConstructor
public class GameWorld {

    // Injects all beans that extend BaseEntity
    private final List<BaseEntity> entities = new TypedArrayList<>(BaseEntity.class);
}
```

</procedure>

<procedure title="Self-injected beans" id="self-injected-beans" collapsible="true">

You may self-inject beans into themselves. This is useful for beans that need to call their own methods and for
the AOP-like method interception.

> For more information regarding AOP functionalities within Sigewine, please refer to the
[AOP subpage](AOP.md).

```java
@RomaritimeBean
@RequiredArgsConstructor
public class SomeService {

    @RomaritimeBean
    private SomeService self; // Self-injected bean
}
```

<warning title="Self-injection field">
    The self-injection field must be annotated with <code>@RomaritimeBean</code> annotation and be <b>non-final</b>.
</warning>

> If you self-inject a abstract class or an interface, Sigewine will automatically inject the implementation of that class or interface.

</procedure>

### Lombok

I recommend using Lombok's `@RequiredArgsConstructor` to avoid boilerplate code.
Keep in mind that you won't be able to specify per-parameter bean names with it.

### Extensions -- Constellations

Sigewine supports extensions that allows you to process beans when they are registered. They are named <b>
Constellations</b>.

One of the existing implementation is **AOP extension**, that allows you to wrap methods of beans with additional
functionality. Please, check the [AOP subpage](AOP.md) for more information.

<procedure title="Defining constellation" id="defining-constellation" collapsible="true">

```java
public class CustomConstellation extends SigewineConstellation {

    public CustomConstellation() {
        super(10); // Priority. Smaller values are processed first.
    }

    @Override
    public void processBeans(Sigewine sigewine) {
        // This method is called when beans are registered.
        // You can process them here, for example, by adding additional properties or modifying them.

        // Example: Print all beans
        sigewine.getSingletonBeans().forEach((definition, bean) -> {
            System.out.println("Processing bean %s of type %s".formatted(
                definition.getName(),
                bean.getClass().getName()
            ));
        });
    }
}

// Registering the constellation
public static void main(String[] args) {
    // Create instance of Sigewine
    Sigewine sigewine = new Sigewine(new SigewineOptions());

    // Add custom constellation
    sigewine.addConstellation(new CustomConstellation());

    // Scan for beans in current package
    sigewine.treatment("your.package.name");
}
```

</procedure>
