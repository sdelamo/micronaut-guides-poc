package io.micronaut.guides.feature;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.application.generator.GeneratorContext;
import io.micronaut.starter.build.dependencies.Dependency;
import io.micronaut.starter.feature.Feature;

import javax.inject.Singleton;

@Singleton
public class SpreadsheetBuilder implements Feature {

    @NonNull
    @Override
    public String getName() {
        return "spreadsheet-builder";
    }

    @Override
    public boolean supports(ApplicationType applicationType) {
        return true;
    }

    @Override
    public void apply(GeneratorContext generatorContext) {
        generatorContext.addDependency(Dependency.builder().groupId("builders.dsl").lookupArtifactId("spreadsheet-builder-poi").compile());
    }
}