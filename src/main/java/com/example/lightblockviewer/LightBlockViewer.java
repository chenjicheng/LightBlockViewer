package com.example.lightblockviewer;

import com.example.lightblockviewer.network.ModNetworking;
import com.example.lightblockviewer.state.PlayerToggleState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LightBlock;

public class LightBlockViewer implements ModInitializer {

    public static final String MOD_ID = "lightblockviewer";
    public static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModNetworking.registerPayloads();
        ModNetworking.registerServerReceivers();

        // Clean up player state on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlayerToggleState.removePlayer(handler.getPlayer().getUUID());
        });

        // Drop light block item when broken by a player with mod enabled
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof LightBlock
                    && player instanceof ServerPlayer serverPlayer
                    && !serverPlayer.isCreative()
                    && PlayerToggleState.isEnabled(serverPlayer)) {
                int lightLevel = state.getValue(LightBlock.LEVEL);
                ItemStack lightItem = new ItemStack(Items.LIGHT);
                LightBlock.setLightOnStack(lightItem, lightLevel);
                Block.popResource(world, pos, lightItem);
            }
        });
    }
}
