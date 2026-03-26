package com.example.lightblockviewer.network;

import com.example.lightblockviewer.state.PlayerToggleState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {

    private ModNetworking() {}

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(TogglePayload.TYPE, TogglePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ToggleSyncPayload.TYPE, ToggleSyncPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TogglePayload.TYPE, (payload, context) -> {
            PlayerToggleState.setEnabled(context.player(), payload.enabled());
            ServerPlayNetworking.send(context.player(), new ToggleSyncPayload(payload.enabled()));
        });
    }
}
