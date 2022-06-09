/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.pmml.commons.model.expressions;

import org.kie.pmml.commons.model.KiePMMLExtension;
import org.kie.pmml.commons.model.abstracts.AbstractKiePMMLComponent;

import java.util.List;
import java.util.Optional;

/**
 * @see <a href=http://dmg.org/pmml/v4-4-1/Transformations.html#xsdElement_DiscretizeBin>DiscretizeBin</a>
 */
public class KiePMMLDiscretizeBin extends AbstractKiePMMLComponent {

    private static final long serialVersionUID = -6437255657731885594L;
    private final String binValue;
    private final KiePMMLInterval interval;

    public KiePMMLDiscretizeBin(String name, List<KiePMMLExtension> extensions, String binValue, KiePMMLInterval interval) {
        super(name, extensions);
        this.binValue = binValue;
        this.interval = interval;
    }

    public String getBinValue() {
        return binValue;
    }

    public KiePMMLInterval getInterval() {
        return interval;
    }

    public Optional<String> evaluate(Number toEvaluate) {
        return interval.isIn(toEvaluate) ? Optional.of(binValue) : Optional.empty();
    }
}
