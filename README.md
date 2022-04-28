# drools-arch-refactoring
-------------------------

This project aims at re-design the drools overall framework.
By itself it has to be considered as a to-be-thrown PoC: as soon as a good design has been achieved and agreed upon,
such design should be iteratively introduced inside `drool` repository.

The main focus of this project is on:
1. clear separation between compile-time and runtime
2. uniform overall management of different models/engines

Regarding the overall module organization/relationship, this project should follow the guidelines of the "Clean Architecture" principle.
As per the design style, the microkernel-style will be featured, where each model/engine will represent a specific `plugin`.

Each plugin should provide a `compilation` and a `runtime` component.

That [MIRO](https://miro.com/app/board/uXjVO7-T2EE=/) page will be used as "brainstorm" place to share idea, suggestions, notes...

The most important decisions (reason and choice) will be documented [here](https://docs.google.com/document/d/1n9rKcMh0qnP7R4DUb3xqanFZcN0q7SL8aBRoAdQDSH0).

The [documentation](./documentation) folder contains diagrams (PUML, Archimate), images, and other resources related to design/architectural choices.

This effort can not be full-filled by a single person, since it involve decisions at every level and it will have an impact on the all `drools` codebase; everyone may be interested will be warmly welcomed for ideas, suggestions, criticism, contribution.



