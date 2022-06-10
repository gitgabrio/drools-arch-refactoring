package org.kie.drl.compilation;

import org.drools.util.io.FileSystemResource;
import org.kie.dar.compilationmanager.api.model.DARFileCollectionResource;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public class DrlFileCollectionResource extends DARFileCollectionResource {

    private final Set<FileSystemResource> fileSystemResources;

    public DrlFileCollectionResource(Set<File> modelFiles) {
        super(modelFiles, "drl");
        this.fileSystemResources =
                modelFiles.stream()
                        .map(FileSystemResource::new)
                        .collect(Collectors.toSet());
    }

    Set<FileSystemResource> getDroolsResources() {
        return fileSystemResources;
    }
}
