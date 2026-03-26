package com.example.lightblockviewer.config;

import com.example.lightblockviewer.LightBlockViewer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(LightBlockViewer.MOD_ID + ".json");

    private static ModConfig instance;

    private int keyCode = GLFW.GLFW_KEY_L;
    private Modifier modifier = Modifier.CTRL;

    public enum Modifier {
        NONE("无 / None"),
        CTRL("Ctrl"),
        ALT("Alt"),
        SHIFT("Shift");

        private final String displayName;

        Modifier(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Modifier next() {
            Modifier[] values = values();
            return values[(ordinal() + 1) % values.length];
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

    public String getKeyDisplayName() {
        String keyName = GLFW.glfwGetKeyName(keyCode, 0);
        if (keyName == null) {
            keyName = getSpecialKeyName(keyCode);
        } else {
            keyName = keyName.toUpperCase();
        }
        if (modifier == Modifier.NONE) {
            return keyName;
        }
        return modifier.getDisplayName() + " + " + keyName;
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
            Files.writeString(CONFIG_PATH, GSON.toJson(new ConfigData(keyCode, modifier.name())));
        } catch (IOException e) {
            LightBlockViewer.LOGGER.error("Failed to save config", e);
        }
    }

    private static ModConfig load() {
        ModConfig config = new ModConfig();
        if (Files.exists(CONFIG_PATH)) {
            try {
                ConfigData data = GSON.fromJson(Files.readString(CONFIG_PATH), ConfigData.class);
                if (data != null) {
                    config.keyCode = data.keyCode;
                    try {
                        config.modifier = Modifier.valueOf(data.modifier);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            } catch (IOException e) {
                LightBlockViewer.LOGGER.error("Failed to load config", e);
            }
        }
        return config;
    }

    private record ConfigData(int keyCode, String modifier) {}
}
