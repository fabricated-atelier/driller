package net.driller.datagen;

import net.driller.DrillerMain;
import net.driller.datagen.util.Language;
import net.driller.init.ItemInit;
import net.driller.init.TagsInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DrillerTranslationGenerator extends FabricLanguageProvider {
    private final Language language;

    public DrillerTranslationGenerator(FabricDataOutput dataOutput, String languageCode, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, languageCode, registryLookup);
        this.language = Language.getOrDefault(languageCode);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder builder) {
        builder.add(ItemInit.WRENCH, switch (language) {
            case ENGLISH -> "Wrench";
            case GERMAN -> "Schraubenschlüssel";
        });
        builder.add("item.driller.item_group", switch (language) {
            case ENGLISH, GERMAN -> "Driller";
        });
        builder.add("entity.driller.connection.added", switch (language) {
            case ENGLISH -> "Connection Added";
            case GERMAN -> "Verlinkung erstellt";
        });
        builder.add("entity.driller.connection.removed", switch (language) {
            case ENGLISH -> "Connection Removed";
            case GERMAN -> "Verlinkung entfernt";
        });
        builder.add("entity.driller.drill.on", switch (language) {
            case ENGLISH -> "Drill Enabled";
            case GERMAN -> "Bohrer angeschaltet";
        });
        builder.add("entity.driller.drill.off", switch (language) {
            case ENGLISH -> "Drill Disabled";
            case GERMAN -> "Bohrer ausgeschaltet";
        });
        builder.add("entity.driller.connection.error_link_self", switch (language) {
            case ENGLISH -> "No self-links allowed";
            case GERMAN -> "Keine verbindung mit sich selbst möglich";
        });
        builder.add("entity.driller.connection.parent_selected", switch (language) {
            case ENGLISH -> "Parent Selected";
            case GERMAN -> "Zuglore ausgewählt";
        });
        builder.add("entity.driller.connection.error_no_parent", switch (language) {
            case ENGLISH -> "No Parent found";
            case GERMAN -> "Zuglore nicht vorhanden";
        });

        for (TagKey<?> entry : TagsInit.ALL_TAGS) {
            builder.add(entry, getReadable(entry.location(), false));
        }


        if (this.language == Language.ENGLISH) {
            try {
                Path existingFilePath = dataOutput.getModContainer().findPath("assets/%s/lang/en_us.manual.json".formatted(DrillerMain.MOD_ID)).orElseThrow();
                builder.add(existingFilePath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add existing language file!", e);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String getReadable(ResourceLocation location, boolean reverse) {
        List<String> split = List.of(location.getPath().split("/"));
        List<String> words = Arrays.asList(split.getLast().split("_"));
        if (reverse) Collections.reverse(words);

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            char capitalized = Character.toUpperCase(word.charAt(0));
            output.append(capitalized).append(word.substring(1));
            if (i < words.size() - 1) {
                output.append(" ");
            }
        }
        return output.toString();
    }
}
