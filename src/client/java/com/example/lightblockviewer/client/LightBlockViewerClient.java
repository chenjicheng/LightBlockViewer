package com.example.lightblockviewer.client;

import com.example.lightblockviewer.client.render.LightBlockOverlayRenderer;
import com.example.lightblockviewer.network.ToggleSyncPayload;
import com.example.lightblockviewer.state.PlayerToggleState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class LightBlockViewerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side toggle check for use by common mixins
        PlayerToggleState.setClientEnabledCheck(
                player -> player == Minecraft.getInstance().player && ClientToggleState.isEnabled()
        );

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::onClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(LightBlockOverlayRenderer.getInstance()::tick);

        // The server answers every toggle request with the state it stored, so the client never
        // predicts a state the server does not have.
        ClientPlayNetworking.registerGlobalReceiver(ToggleSyncPayload.TYPE, (payload, context) -> {
            ClientToggleState.confirm(payload.enabled());

            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.displayClientMessage(
                        Component.translatable(payload.enabled()
                                ? "lightblockviewer.config.enabled"
                                : "lightblockviewer.config.disabled"),
                        true);
            }
        });

        // Server-side state is dropped on disconnect, so the client starts each world disabled.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientToggleState.reset());

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(LightBlockOverlayRenderer.getInstance()::onBeforeTranslucent);
    }
}
