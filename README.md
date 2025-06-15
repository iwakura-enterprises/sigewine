# Sigewine
Sigewine is a Java library for simple and lightweight dependency injection.

![Coverage](.github/badges/jacoco.svg) [![Build & Test](https://github.com/iwakura-enterprises/sigewine/actions/workflows/build.yml/badge.svg)](https://github.com/iwakura-enterprises/sigewine/actions/workflows/build.yml)

> Disclaimer: The library name is not related to any character in any game or media.

## Project structure
- `sigewine-core`: The core library that provides the dependency injection functionality.
- `sigewine-aop`: Contains AOP-like Proxy functionality to allow wrap beans. For example, to log method calls or to add transaction management.
- `sigewine-aop-sentry`: Provides integration with Sentry for AOP-like method interception. This module is optional and requires `sigewine-aop`.

More modules may be added in the future, such as `sigewine-aop-irminsul`.

## Documentation
Documentation is available at the [Central iwakura.enterprises documentations](https://docs.iwakura.enterprises/sigewine.html)

## Disclaimers
- Cyclic dependencies are not supported.
- No lazy loading.
- All beans are singleton, for now.
