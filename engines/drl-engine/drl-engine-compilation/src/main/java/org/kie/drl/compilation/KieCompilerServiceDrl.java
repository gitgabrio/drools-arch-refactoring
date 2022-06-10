package org.kie.drl.compilation;

import org.drools.compiler.builder.impl.BuildResultCollector;
import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.resources.DrlResourceHandler;
import org.drools.compiler.lang.descr.CompositePackageDescr;
import org.drools.drl.ast.descr.PackageDescr;
import org.drools.drl.parser.DroolsParserException;
import org.drools.model.project.codegen.KogitoPackageSources;
import org.drools.model.project.codegen.RuleCodegenError;
import org.drools.modelcompiler.builder.GeneratedFile;
import org.drools.modelcompiler.tool.ExplicitCanonicalModelCompiler;
import org.kie.api.io.Resource;
import org.kie.dar.common.api.model.FRI;
import org.kie.dar.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.dar.compilationmanager.api.model.DARCompilationOutput;
import org.kie.dar.compilationmanager.api.model.DARResource;
import org.kie.dar.compilationmanager.api.service.KieCompilerService;
import org.kie.internal.builder.KnowledgeBuilderResult;
import org.kie.memorycompiler.JavaConfiguration;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KieCompilerServiceDrl implements KieCompilerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieCompilerServiceDrl.class);

    @Override
    public <T extends DARResource> boolean canManageResource(T toProcess) {
        return toProcess instanceof DrlFileCollectionResource;
    }

    @Override
    public <T extends DARResource, E extends DARCompilationOutput> List<E> processResource(T toProcess, KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader) {
        if (!canManageResource(toProcess)) {
            throw new KieCompilerServiceException(String.format("%s can not process %s",
                    this.getClass().getName(),
                    toProcess.getClass().getName()));
        }


        DrlFileCollectionResource resources = (DrlFileCollectionResource) toProcess;

        KnowledgeBuilderConfigurationImpl knowledgeBuilderConfiguration =
                new KnowledgeBuilderConfigurationImpl();

        DrlResourceHandler drlResourceHandler =
                new DrlResourceHandler(knowledgeBuilderConfiguration);

        Map<String, CompositePackageDescr> packages = new HashMap<>();

        for (Resource resource : resources.getDroolsResources()) {
            parseAndAdd(drlResourceHandler, resource, packages);
        }

        ExplicitCanonicalModelCompiler<KogitoPackageSources> compiler =
                ExplicitCanonicalModelCompiler.of(
                        packages.values(),
                        knowledgeBuilderConfiguration,
                        KogitoPackageSources::dumpSources);

        compiler.process();
        BuildResultCollector buildResults = compiler.getBuildResults();

        if (buildResults.hasErrors()) {
            for (KnowledgeBuilderResult error : buildResults.getResults()) {
                LOGGER.error(error.toString());
            }
            throw new RuleCodegenError(buildResults.getAllResults());
        }

        Collection<KogitoPackageSources> packageSources = compiler.getPackageSources();

        List<GeneratedFile> legacyModelFiles = new ArrayList<>();

        for (KogitoPackageSources pkgSources : packageSources) {
            pkgSources.collectGeneratedFiles(legacyModelFiles);
        }


        Map<String, String> sourceCode = legacyModelFiles.stream()
                .collect(Collectors.toMap(generatedFile -> generatedFile.getPath()
                        .replace(".java", "")
                        .replace('/', '.'),
                        generatedFile -> new String(generatedFile.getData(), StandardCharsets.UTF_8)));

        Map<String, byte[]> compiledClasses = compileClasses(sourceCode, memoryCompilerClassLoader);

        return (List<E>) Collections.singletonList(new DrlCallableClassesContainer(new FRI("my.rule", "drl"), "my/Rule", compiledClasses));
    }


    private void parseAndAdd(DrlResourceHandler drlResourceHandler, Resource resource, Map<String, CompositePackageDescr> packages) {
        try {
            PackageDescr packageDescr = drlResourceHandler.process(resource);
            CompositePackageDescr compositePackageDescr =
                    packages.computeIfAbsent(packageDescr.getNamespace(), CompositePackageDescr::new);
            compositePackageDescr.addPackageDescr(resource, packageDescr);
        } catch (DroolsParserException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Map<String, byte[]> compileClasses(Map<String, String> sourcesMap, KieMemoryCompiler.MemoryCompilerClassLoader memoryClassLoader) {
        return KieMemoryCompiler.compileNoLoad(sourcesMap, memoryClassLoader, JavaConfiguration.CompilerType.NATIVE);
    }


}
