package org.kie.drl.engine.compilation.model;

import org.drools.util.io.FileSystemResource;
import org.kie.dar.compilationmanager.api.model.DARFileSetResource;
import org.kie.dar.compilationmanager.api.model.DARResource;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractDrlFileSetResource extends DARFileSetResource implements DARResource<Set<File>> {

    private final Set<FileSystemResource> fileSystemResources;

    protected AbstractDrlFileSetResource(Set<File> modelFiles, String basePath) {
        super(modelFiles, "drl", basePath);
        this.fileSystemResources =
                modelFiles.stream()
                        .map(FileSystemResource::new)
                        .collect(Collectors.toSet());
    }


    public Set<FileSystemResource> getFileSystemResource() {
        return fileSystemResources;
    }


}
