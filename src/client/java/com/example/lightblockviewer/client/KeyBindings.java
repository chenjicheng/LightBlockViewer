package com.example.lightblockviewer.client;

import com.example.lightblockviewer.config.ModConfig;
import com.example.lightblockviewer.network.TogglePayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    private static boolean wasPressed = false;

    private KeyBindings() {}

    public static void onClientTick(Minecraft client) {
        if (client.getWindow() == null || client.screen != null) return;

        com.mojang.blaze3d.platform.Window window = client.getWindow();
        ModConfig config = ModConfig.get();

        boolean modifierHeld = isModifierHeld(window, config.getModifier());
        boolean keyDown = InputConstants.isKeyDown(window, config.getKeyCode());
        boolean pressed = modifierHeld && keyDown && !wasPressed;
        wasPressed = modifierHeld && keyDown;

        if (pressed && client.player != null) {
            boolean newState = !ClientToggleState.isEnabled();
            ClientToggleState.setEnabled(newState);
            ClientPlayNetworking.send(new TogglePayload(newState));

            // Show on-screen notification
            client.player.displayClientMessage(
                    Component.translatable(newState
                            ? "lightblockviewer.config.enabled"
                            : "lightblockviewer.config.disabled"),
                    true);
        }
    }

    private static boolean isModifierHeld(com.mojang.blaze3d.platform.Window window, ModConfig.Modifier modifier) {
        return switch (modifier) {
            case NONE -> true;
            case CTRL -> InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                    || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
            case ALT -> InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
                    || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
            case SHIFT -> InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                    || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        };
    }
}
