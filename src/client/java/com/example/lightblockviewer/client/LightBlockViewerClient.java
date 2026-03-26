package com.example.lightblockviewer.client;

import com.example.lightblockviewer.client.render.LightBlockOverlayRenderer;
import com.example.lightblockviewer.network.ToggleSyncPayload;
import com.example.lightblockviewer.state.PlayerToggleState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class LightBlockViewerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side toggle check for use by common mixins
        PlayerToggleState.setClientEnabledCheck(
                player -> player == Minecraft.getInstance().player && ClientToggleState.isEnabled()
        );

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::onClientTick);

        ClientPlayNetworking.registerGlobalReceiver(ToggleSyncPayload.TYPE, (payload, context) -> {
            ClientToggleState.setEnabled(payload.enabled());
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(LightBlockOverlayRenderer.getInstance()::onBeforeTranslucent);
    }
}
