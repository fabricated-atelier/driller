package net.driller;

import net.driller.datagen.DrillerTranslationGenerator;
import net.driller.datagen.util.Language;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DrillerDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider((output, future) ->
                new DrillerTranslationGenerator(output, Language.ENGLISH.getCode(), future));
        pack.addProvider((output, future) ->
                new DrillerTranslationGenerator(output, Language.GERMAN.getCode(), future));
    }
}
