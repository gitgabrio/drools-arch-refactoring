Compilation Manager
===================

The code in these modules will be responsible for transformations of original model to executable representation; most of the time this is represented by code-generation and class-compilation, but exceptions should be considered as well.

The code in `compilation-manager-api` should be the only one visible outside the `core` of the system, while the code inside `compilation-manager-common` should be considered **private** and hidden from outside.