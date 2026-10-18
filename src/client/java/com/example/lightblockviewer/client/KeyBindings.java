package com.example.lightblockviewer.client;

import com.example.lightblockviewer.config.ModConfig;
import com.example.lightblockviewer.network.TogglePayload;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    private static boolean wasPressed = false;

    private KeyBindings() {}

    public static void onClientTick(Minecraft client) {
        Window window = client.getWindow();
        if (window == null) return;

        ModConfig config = ModConfig.get();
        boolean modifierHeld = isModifierHeld(window, config.getModifier());
        boolean keyDown = InputConstants.isKeyDown(window, config.getKeyCode());
        boolean pressed = modifierHeld && keyDown && !wasPressed;
        // Tracked even while a screen is open, so the key has to be released again to toggle.
        wasPressed = modifierHeld && keyDown;

        if (client.player == null) return;

        // An unanswered request means the server did not accept it.
        if (ClientToggleState.tickPending()) {
            client.player.displayClientMessage(
                    Component.translatable("lightblockviewer.message.noServerSupport"), false);
        }

        if (client.screen != null) return;

        if (pressed) {
            ClientPlayNetworking.send(new TogglePayload(ClientToggleState.requestToggle()));
        }
    }

    private static boolean isModifierHeld(Window window, ModConfig.Modifier modifier) {
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
