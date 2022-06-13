package org.kie.drl.engine.compilation.model;

import org.drools.util.io.FileSystemResource;
import org.kie.dar.compilationmanager.api.model.DARFileSetResource;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractDrlFileSetResource extends DARFileSetResource implements DrlResource<Set<File>> {

    private final Set<FileSystemResource> fileSystemResources;

    private String basePath;

    public AbstractDrlFileSetResource(Set<File> modelFiles, String basePath) {
        super(modelFiles, "drl");
        this.basePath = basePath;
        this.fileSystemResources =
                modelFiles.stream()
                        .map(FileSystemResource::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    public Set<FileSystemResource> getFileSystemResource() {
        return fileSystemResources;
    }


}
