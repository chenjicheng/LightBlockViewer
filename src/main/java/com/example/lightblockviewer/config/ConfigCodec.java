package com.example.lightblockviewer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON mapping for the config file.
 *
 * <p>Kept free of Minecraft and Fabric types so the fallback behaviour for damaged or partial
 * config files can be tested outside the game.
 */
final class ConfigCodec {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigCodec() {}

    /** Raw config values; {@code keyCode} is 0 and {@code modifier} is {@code null} when absent. */
    record Data(int keyCode, String modifier) {}

    /**
     * Reads config data from JSON text.
     *
     * @return the parsed data, or {@code null} if the text is not a usable config file
     */
    static Data parse(String json) {
        try {
            return GSON.fromJson(json, Data.class);
        } catch (RuntimeException e) {
            // Gson throws unchecked exceptions for malformed or truncated files.
            return null;
        }
    }

    /**
     * Resolves a modifier name read from a config file.
     *
     * @return the matching modifier, or {@code null} if the name is missing or unknown
     */
    static ModConfig.Modifier parseModifier(String name) {
        if (name == null) {
            return null;
        }

        try {
            return ModConfig.Modifier.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static String toJson(Data data) {
        return GSON.toJson(data);
    }
}
