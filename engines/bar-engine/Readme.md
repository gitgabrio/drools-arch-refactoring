bar-engine
==========

This engine is an example of a "mixed" engine:
1. if the model file name starts with "Redirect", the model is compiled to an "intermediate" artifact and its execution requires the foo-engine to complete
2. otherwise, the model is compiled to a "final" artifact and its execution is completely self-contained