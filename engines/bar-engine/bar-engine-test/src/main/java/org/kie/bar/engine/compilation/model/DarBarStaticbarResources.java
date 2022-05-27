package org.kie.bar.engine.compilation.model;

import org.kie.bar.engine.api.model.BarResources;
import org.kie.foo.engine.api.model.FooResources;

import java.util.Arrays;

public class DarBarStaticbarResources extends BarResources {

    public DarBarStaticbarResources() {
        super(Arrays.asList("org.kie.bar.engine.compilation.model.DarBarStaticbar"));
    }
}
