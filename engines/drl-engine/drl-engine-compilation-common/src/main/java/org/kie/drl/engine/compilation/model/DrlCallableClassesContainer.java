package org.kie.drl.engine.compilation.model;

import org.kie.dar.common.api.model.FRI;
import org.kie.dar.compilationmanager.api.model.DARCallableOutputClassesContainer;

import java.util.List;
import java.util.Map;

public class DrlCallableClassesContainer extends DARCallableOutputClassesContainer {
    public DrlCallableClassesContainer(FRI fri, List<String> fullClassNames, Map<String, byte[]> compiledClassMap) {
        super(fri, fullClassNames, compiledClassMap);
    }
}
