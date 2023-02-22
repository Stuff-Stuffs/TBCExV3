package io.github.stuff_stuffs.tbcexv3model.api.model.modelpart;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.scene.property.ScenePropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.impl.model.part.ModelPartRenderContextImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public interface ModelPartRenderContext {
    boolean isInGui();

    Model model();

    Identifier bone();

    ModelPropertyContainer properties();

    ScenePropertyContainer sceneProperties();

    MatrixStack matrices();

    VertexConsumerProvider vertexConsumers();

    int light(double x, double y, double z);

    static ModelPartRenderContext create(final Model model, final Identifier bone, final ModelPropertyContainer properties, final ScenePropertyContainer sceneProperties, final MatrixStack matrices, final VertexConsumerProvider consumers, @Nullable final BlockRenderView renderView) {
        return new ModelPartRenderContextImpl(model, bone, properties, sceneProperties, matrices, consumers, renderView);
    }
}
