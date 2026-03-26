package com.example.lightblockviewer.client.mixin;

import com.example.lightblockviewer.client.render.LightBlockOverlayRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "close", at = @At("RETURN"))
    private void lightblockviewer$cleanupResources(CallbackInfo ci) {
        LightBlockOverlayRenderer.getInstance().close();
    }
}
