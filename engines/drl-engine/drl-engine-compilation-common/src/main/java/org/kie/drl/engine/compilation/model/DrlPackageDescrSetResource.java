package org.kie.drl.engine.compilation.model;

import org.drools.drl.ast.descr.PackageDescr;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.model.DARSetResource;

import java.util.Set;

public class DrlPackageDescrSetResource extends DARSetResource<PackageDescr> implements DARResource<Set<PackageDescr>> {

    public DrlPackageDescrSetResource(Set<PackageDescr> packageDescrs, String basePath) {
        super(packageDescrs, "drl", basePath);
    }


}
