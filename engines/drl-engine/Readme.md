DRL Engine
==========

The rule(DRL) engine feature different use cases/code paths.

As per the compilation phase, currently only the `ExecutableModel` is implemented, and relevant code is present in `drl-engine-compilation-common` (to be inherited by all drl-engine implementations)

As per the runtime phase, there are at least four different scenarios:

1. direct usage of kiesession in local mode -> `drl-engine-kiesession-local`
2. usage of kiesession (via proxy) in remote mode -> to be implemented
3. usage of map of objects (for inter-engine communications)
4. ruleunit usage (e.g. for Rest endpoints ?)


At Runtime, the `FRI` should univocally define
1. the kind of runtime
2. (eventually) the session id

