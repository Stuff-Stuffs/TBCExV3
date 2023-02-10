package io.github.stuff_stuffs.tbcexv3model.impl.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelRenderPartContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public record ModelRenderPartContextImpl(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        Vector3fc cameraPos,
        Quaternionfc cameraLook,
        Model model,
        Identifier bone,
        Identifier id
) implements ModelRenderPartContext {
    //TODO light sampling
    @Override
    public int sampleLight(float x, float y, float z) {
        return LightmapTextureManager.MAX_LIGHT_COORDINATE;
    }
}
