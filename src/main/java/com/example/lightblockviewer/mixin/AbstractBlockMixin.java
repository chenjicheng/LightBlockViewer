package com.example.lightblockviewer.mixin;

import com.example.lightblockviewer.state.PlayerToggleState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {

    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    private void lightblockviewer$allowBreaking(BlockState state, Player player, BlockGetter level,
                                                 BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof LightBlock) {
            if (isPlayerEnabled(player)) {
                cir.setReturnValue(0.1f);
            }
        }
    }

    private static boolean isPlayerEnabled(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return PlayerToggleState.isEnabled(serverPlayer);
        }
        return PlayerToggleState.isClientEnabled(player);
    }
}
