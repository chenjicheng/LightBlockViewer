package com.example.lightblockviewer.config;

import com.example.lightblockviewer.LightBlockViewer;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ModConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(LightBlockViewer.MOD_ID + ".json");

    private static ModConfig instance;

    private int keyCode = GLFW.GLFW_KEY_L;
    private Modifier modifier = Modifier.CTRL;

    public enum Modifier {
        NONE,
        CTRL,
        ALT,
        SHIFT;

        public Modifier next() {
            Modifier[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        /** Translation key of the display name, e.g. {@code lightblockviewer.config.modifier.ctrl}. */
        public String getTranslationKey() {
            return "lightblockviewer.config.modifier." + name().toLowerCase(Locale.ROOT);
        }
    }

    public static ModConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    /** Display name of the configured key on its own, e.g. {@code L} or {@code F5}. */
    public String getKeyName() {
        String keyName = GLFW.glfwGetKeyName(keyCode, 0);
        if (keyName == null) {
            return getSpecialKeyName(keyCode);
        }
        return keyName.toUpperCase(Locale.ROOT);
    }

    private static String getSpecialKeyName(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW.GLFW_KEY_DELETE -> "Delete";
            case GLFW.GLFW_KEY_INSERT -> "Insert";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_PAGE_UP -> "PageUp";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PageDown";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            default -> "Key" + keyCode;
        };
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, ConfigCodec.toJson(new ConfigCodec.Data(keyCode, modifier.name())));
        } catch (IOException e) {
            LightBlockViewer.LOGGER.error("Failed to save config", e);
        }
    }

    private static ModConfig load() {
        ModConfig config = new ModConfig();
        if (!Files.exists(CONFIG_PATH)) {
            return config;
        }

        try {
            ConfigCodec.Data data = ConfigCodec.parse(Files.readString(CONFIG_PATH));
            if (data == null) {
                LightBlockViewer.LOGGER.warn("Config {} is not a valid config file, using defaults", CONFIG_PATH);
            } else {
                config.apply(data);
            }
        } catch (IOException e) {
            LightBlockViewer.LOGGER.error("Failed to read config, using defaults", e);
        }

        return config;
    }

    /** Applies stored values, keeping the defaults for anything missing or unknown. */
    private void apply(ConfigCodec.Data data) {
        if (data.keyCode() > 0) {
            keyCode = data.keyCode();
        }

        Modifier storedModifier = ConfigCodec.parseModifier(data.modifier());
        if (storedModifier != null) {
            modifier = storedModifier;
        }
    }
}
