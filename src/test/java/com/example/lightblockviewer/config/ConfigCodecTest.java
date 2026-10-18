package com.example.lightblockviewer.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigCodecTest {

    @Test
    void parsesCompleteConfig() {
        ConfigCodec.Data data = ConfigCodec.parse("{\"keyCode\":76,\"modifier\":\"ALT\"}");

        assertNotNull(data);
        assertEquals(76, data.keyCode());
        assertEquals(ModConfig.Modifier.ALT, ConfigCodec.parseModifier(data.modifier()));
    }

    @Test
    void truncatedFileIsIgnoredInsteadOfThrowing() {
        // A truncated write used to crash the client tick loop with a JsonSyntaxException.
        assertNull(ConfigCodec.parse("{\"keyCode\":76,\"modi"));
    }

    @Test
    void emptyFileIsIgnored() {
        assertNull(ConfigCodec.parse(""));
    }

    @Test
    void fileWithoutModifierKeepsTheDefault() {
        // A missing modifier used to throw a NullPointerException from Modifier.valueOf.
        ConfigCodec.Data data = ConfigCodec.parse("{\"keyCode\":76}");

        assertNotNull(data);
        assertNull(ConfigCodec.parseModifier(data.modifier()));
    }

    @Test
    void unknownModifierNameKeepsTheDefault() {
        assertNull(ConfigCodec.parseModifier("HYPER"));
        assertNull(ConfigCodec.parseModifier(null));
    }
}
