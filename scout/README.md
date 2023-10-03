# Scout (Gradle Project)

This directory contains gradle project with library sources. There are also tests, benchmarks and samples. Project consists of bunch of modules, grouped by purposes.

## Group: scout
Primary library sources located in `scout` directory (modules `core`, `inspector`, `validator`).

### Module: scout/core
Contains implementation of library for usage in production code sources.

### Module: scout/inspector
Contains utility for inspecting the scope content. This utility used by validator but you can build custom tools based on it.

### Module: scout/validator
Contains implementation of validator and common checks. There are also some useful classes to build your own checks, component producers, etc.

## Group: tools
Secondary library sources with tools for library users (modules `classgraph-collector`, `scout-gradle-plugin`).

### Module: tools/classgraph-collector
Contains implementation of `ComponentProducer` based on [Class Graph library](https://github.com/classgraph/classgraph).

### Module: tools/scout-gradle-plugin
Contains implementaion of gradle plugin for project using Scout library.

## Group: measures
Auxiliary sources for benchmarks and companion tools (modules `jvm-benchmarks`, `graph-generator-plugin`).

### Module: measures/jvm-benchmarks
Contains platform and set of benchmarks for change assessment and library comparison.

### Module: measures/graph-generator-plugin
Contains sample graph generator and source generators for popular DI-frameworks (koin, kodein, katana, dagger).

## Group: samples
Sample sources with library usage showcase (modules `coffee-maker`).

### Module: coffee-maker
Contains the simplest example of library usage.
