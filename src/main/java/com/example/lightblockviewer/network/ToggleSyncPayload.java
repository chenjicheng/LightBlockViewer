package com.example.lightblockviewer.network;

import com.example.lightblockviewer.LightBlockViewer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ToggleSyncPayload(boolean enabled) implements CustomPacketPayload {

    public static final Identifier TOGGLE_SYNC_ID =
            Identifier.fromNamespaceAndPath(LightBlockViewer.MOD_ID, "toggle_sync");

    public static final CustomPacketPayload.Type<ToggleSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(TOGGLE_SYNC_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, ToggleSyncPayload::enabled,
                    ToggleSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
