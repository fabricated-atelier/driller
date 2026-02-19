package net.driller.datagen.util;

public enum Language {
    ENGLISH("en_us"),
    GERMAN("de_de");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Language getOrDefault(String code) {
        for (Language entry : Language.values()) {
            if (entry.getCode().equals(code)) return entry;
        }
        return ENGLISH;
    }
}
