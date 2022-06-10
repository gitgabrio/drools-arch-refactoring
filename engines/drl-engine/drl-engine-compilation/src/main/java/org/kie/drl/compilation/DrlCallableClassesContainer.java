package org.kie.drl.compilation;

import org.kie.dar.common.api.model.FRI;
import org.kie.dar.compilationmanager.api.model.DARCallableOutputClassesContainer;

import java.util.Map;

public class DrlCallableClassesContainer extends DARCallableOutputClassesContainer {
    protected DrlCallableClassesContainer(FRI fri, String fullClassName, Map<String, byte[]> compiledClassMap) {
        super(fri, fullClassName, compiledClassMap);
    }
}
