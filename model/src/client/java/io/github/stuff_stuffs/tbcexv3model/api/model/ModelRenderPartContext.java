package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.impl.model.ModelRenderPartContextImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public interface ModelRenderPartContext {
    default int sampleLight(final Vector3fc vector, final Matrix4fc posMat) {
        final Vector4f scratch = new Vector4f(vector.x(), vector.y(), vector.z(), 1.0F);
        posMat.transform(scratch);
        scratch.mul(1.0F / scratch.w());
        return sampleLight(scratch.x(), scratch.y(), scratch.z());
    }

    default int sampleLight(final Vector3fc vector) {
        return sampleLight(vector.x(), vector.y(), vector.z());
    }

    int sampleLight(float x, float y, float z);

    MatrixStack matrices();

    VertexConsumerProvider vertexConsumers();

    Vector3fc cameraPos();

    //TODO fix this
    Quaternionfc cameraLook();

    Model model();

    Identifier bone();

    Identifier id();

    static ModelRenderPartContext of(final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final Vector3fc cameraPos, final Quaternionfc cameraLook, final Model model, final Identifier bone, final Identifier id) {
        return new ModelRenderPartContextImpl(matrices, vertexConsumers, cameraPos, cameraLook, model, bone, id);
    }
}
