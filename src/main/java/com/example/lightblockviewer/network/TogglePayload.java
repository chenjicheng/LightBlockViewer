package com.example.lightblockviewer.network;

import com.example.lightblockviewer.LightBlockViewer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TogglePayload(boolean enabled) implements CustomPacketPayload {

    public static final Identifier TOGGLE_ID =
            Identifier.fromNamespaceAndPath(LightBlockViewer.MOD_ID, "toggle");

    public static final CustomPacketPayload.Type<TogglePayload> TYPE =
            new CustomPacketPayload.Type<>(TOGGLE_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, TogglePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, TogglePayload::enabled,
                    TogglePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
