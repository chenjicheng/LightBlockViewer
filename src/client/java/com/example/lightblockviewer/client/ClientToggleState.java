package com.example.lightblockviewer.client;

public final class ClientToggleState {

    private static boolean enabled = false;

    private ClientToggleState() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void toggle() {
        enabled = !enabled;
    }
}
