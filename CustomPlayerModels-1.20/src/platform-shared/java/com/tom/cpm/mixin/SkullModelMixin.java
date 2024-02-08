package com.tom.cpm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.cpm.client.PlayerRenderManager.RedirectModelRendererBase;

@Mixin(SkullModel.class)
public class SkullModelMixin {
	public @Shadow ModelPart head;

	@Inject(at = @At("HEAD"), method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", cancellable = true)
	public void renderToBuffer(PoseStack ps, VertexConsumer p_103816_, int p_103817_, int p_103818_, float p_103819_, float p_103820_, float p_103821_, float p_103822_, CallbackInfo cbi) {
		if (head instanceof RedirectModelRendererBase) {
			head.render(ps, p_103816_, p_103817_, p_103818_, p_103819_, p_103820_, p_103821_, p_103822_);
			cbi.cancel();
		}
	}
}
