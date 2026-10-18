package com.example.lightblockviewer.client;

import com.example.lightblockviewer.config.ModConfig;
import com.example.lightblockviewer.network.TogglePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private boolean listeningForKey = false;
    private Button keyBindButton;
    private Button modifierButton;
    private Button toggleButton;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("lightblockviewer.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        listeningForKey = false;
        int centerX = width / 2;
        int startY = height / 4;
        int buttonWidth = 200;
        int spacing = 28;

        // Toggle on/off, applied by the server
        toggleButton = addRenderableWidget(Button.builder(getToggleText(), button -> requestToggle())
                .bounds(centerX - buttonWidth / 2, startY, buttonWidth, 20).build());

        // Modifier key selector
        modifierButton = addRenderableWidget(Button.builder(getModifierText(), button -> {
            ModConfig config = ModConfig.get();
            config.setModifier(config.getModifier().next());
            config.save();
            button.setMessage(getModifierText());
            keyBindButton.setMessage(getKeyBindText());
        }).bounds(centerX - buttonWidth / 2, startY + spacing, buttonWidth, 20).build());

        // Key bind button (click to listen, click again to cancel)
        keyBindButton = addRenderableWidget(Button.builder(getKeyBindText(), button -> {
            listeningForKey = !listeningForKey;
            button.setMessage(listeningForKey ? getListeningText() : getKeyBindText());
        }).bounds(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, 20).build());

        // Done
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            onClose();
        }).bounds(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, 20).build());
    }

    @Override
    public void tick() {
        super.tick();

        // The toggle is only available in a world, and its state comes back from the server.
        toggleButton.active = isConnected();
        toggleButton.setMessage(getToggleText());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listeningForKey) {
            int keyCode = event.key();

            // Ignore modifier-only presses
            if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL
                    || keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT
                    || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningForKey = false;
                keyBindButton.setMessage(getKeyBindText());
                return true;
            }

            ModConfig config = ModConfig.get();
            config.setKeyCode(keyCode);
            config.save();
            listeningForKey = false;
            keyBindButton.setMessage(getKeyBindText());
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, height / 4 - 20, 0xFFFFFF);
    }

    private void requestToggle() {
        if (!isConnected()) {
            return;
        }
        ClientPlayNetworking.send(new TogglePayload(ClientToggleState.requestToggle()));
    }

    private boolean isConnected() {
        return minecraft != null && minecraft.getConnection() != null;
    }

    private Component getToggleText() {
        if (!isConnected()) {
            return Component.translatable("lightblockviewer.config.notConnected");
        }
        return Component.translatable(ClientToggleState.isEnabled()
                ? "lightblockviewer.config.enabled"
                : "lightblockviewer.config.disabled");
    }

    private Component getModifierText() {
        return Component.translatable("lightblockviewer.config.modifier")
                .append(Component.literal(": "))
                .append(Component.translatable(ModConfig.get().getModifier().getTranslationKey()));
    }

    private Component getKeyBindText() {
        ModConfig config = ModConfig.get();
        MutableComponent binding = Component.literal("");
        if (config.getModifier() != ModConfig.Modifier.NONE) {
            binding.append(Component.translatable(config.getModifier().getTranslationKey()))
                    .append(Component.literal(" + "));
        }
        binding.append(Component.literal(config.getKeyName()));

        return Component.translatable("lightblockviewer.config.keybind")
                .append(Component.literal(": [ "))
                .append(binding)
                .append(Component.literal(" ]"));
    }

    private Component getListeningText() {
        return Component.literal("> ")
                .append(Component.translatable("lightblockviewer.config.pressKey"))
                .append(Component.literal(" <"));
    }
}
