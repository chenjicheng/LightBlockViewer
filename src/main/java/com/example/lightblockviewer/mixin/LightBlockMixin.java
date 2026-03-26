package com.example.lightblockviewer.mixin;

import com.example.lightblockviewer.state.PlayerToggleState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightBlock.class)
public abstract class LightBlockMixin extends Block {

    protected LightBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void lightblockviewer$modifyShape(BlockState state, BlockGetter level, BlockPos pos,
                                               CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (context instanceof EntityCollisionContext entityCtx) {
            Entity entity = entityCtx.getEntity();
            if (entity instanceof Player player && isPlayerEnabled(player)) {
                cir.setReturnValue(Shapes.block());
            }
        }
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void lightblockviewer$allowCycling(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult,
                                                CallbackInfoReturnable<InteractionResult> cir) {
        if (isPlayerEnabled(player)) {
            if (!level.isClientSide()) {
                int currentLevel = state.getValue(LightBlock.LEVEL);
                int nextLevel = (currentLevel + 1) % 16;
                level.setBlock(pos, state.setValue(LightBlock.LEVEL, nextLevel), Block.UPDATE_ALL);
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    private static boolean isPlayerEnabled(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return PlayerToggleState.isEnabled(serverPlayer);
        }
        return PlayerToggleState.isClientEnabled(player);
    }
}
