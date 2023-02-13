package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Skeleton;
import io.github.stuff_stuffs.tbcexv3model.impl.model.ModelImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface Model {
    Skeleton skeleton();

    ModelAttachedPart get(Identifier bone);

    void renderInGui(MatrixStack matrices, int light, VertexConsumerProvider vertex, float width, float height);

    static Model create() {
        return new ModelImpl();
    }

    interface ModelAttachedPart {
        Identifier bone();

        Set<Identifier> parts();

        ModelPart part(Identifier id);

        boolean addPart(Identifier id, ModelPart part);
    }
}
