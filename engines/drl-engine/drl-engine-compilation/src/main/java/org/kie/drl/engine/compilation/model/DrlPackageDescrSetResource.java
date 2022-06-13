package org.kie.drl.engine.compilation.model;

import org.drools.drl.ast.descr.PackageDescr;
import org.kie.dar.compilationmanager.api.model.DARSetResource;

import java.util.Set;

public class DrlPackageDescrSetResource extends DARSetResource<PackageDescr> implements DrlResource<Set<PackageDescr>> {

    private String basePath;

    public DrlPackageDescrSetResource(Set<PackageDescr> packageDescrs, String basePath) {
        super(packageDescrs, "drl");
        this.basePath = basePath;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

}
