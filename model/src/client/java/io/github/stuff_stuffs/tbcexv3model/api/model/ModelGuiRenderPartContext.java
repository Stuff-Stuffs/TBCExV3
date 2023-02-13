package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.impl.model.ModelGuiRenderPartContextImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public interface ModelGuiRenderPartContext {
    int light();

    MatrixStack matrices();

    VertexConsumerProvider vertexConsumers();

    Model model();

    Identifier bone();

    Identifier id();

    float width();

    float height();

    static ModelGuiRenderPartContext of(
            final MatrixStack matrices,
            final VertexConsumerProvider vertexConsumers,
            final Model model,
            final int light,
            final Identifier bone,
            final Identifier id,
            final float width,
            final float height
    ) {
        return new ModelGuiRenderPartContextImpl(matrices, vertexConsumers, model, light, bone, id, width, height);
    }
}
