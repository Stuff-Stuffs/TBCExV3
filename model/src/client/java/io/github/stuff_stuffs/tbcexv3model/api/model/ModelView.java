package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.SkeletonView;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface ModelView {
    ModelType type();

    SkeletonView skeleton();

    ModelAttachedPartView get(Identifier bone);

    void renderInGui(MatrixStack matrices, int light, VertexConsumerProvider vertex, float width, float height);

    interface ModelAttachedPartView {
        Identifier bone();

        Set<Identifier> parts();

        ModelPart part(Identifier id);
    }
}
