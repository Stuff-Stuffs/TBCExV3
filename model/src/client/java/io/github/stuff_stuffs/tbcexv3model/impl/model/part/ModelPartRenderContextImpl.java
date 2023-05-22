package io.github.stuff_stuffs.tbcexv3model.impl.model.part;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPartRenderContext;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.scene.property.ScenePropertyContainer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;

public class ModelPartRenderContextImpl implements ModelPartRenderContext {
    private final Model model;
    private final Identifier bone;
    private final ModelPropertyContainer modelProperties;
    private final ScenePropertyContainer sceneProperties;
    private final MatrixStack matrices;
    private final VertexConsumerProvider vertexConsumers;
    private final LightRetriever retriever;
    private final boolean gui;

    public ModelPartRenderContextImpl(final Model model, final Identifier bone, final ModelPropertyContainer properties, final ScenePropertyContainer sceneProperties, final MatrixStack matrices, final VertexConsumerProvider consumers, @Nullable final BlockRenderView renderView) {
        this.model = model;
        this.bone = bone;
        modelProperties = properties;
        this.sceneProperties = sceneProperties;
        this.matrices = matrices;
        vertexConsumers = consumers;
        retriever = renderView == null ? new ConstantLight() : new WorldLight(renderView);
        gui = renderView == null;
    }

    @Override
    public boolean isInGui() {
        return gui;
    }

    @Override
    public Model model() {
        return model;
    }

    @Override
    public Identifier bone() {
        return bone;
    }

    @Override
    public ModelPropertyContainer properties() {
        return modelProperties;
    }

    @Override
    public ScenePropertyContainer sceneProperties() {
        return sceneProperties;
    }

    @Override
    public MatrixStack matrices() {
        return matrices;
    }

    @Override
    public VertexConsumerProvider vertexConsumers() {
        return vertexConsumers;
    }

    @Override
    public int light(final double x, final double y, final double z) {
        return retriever.sample(x, y, z);
    }

    private interface LightRetriever {
        int sample(double x, double y, double z);
    }

    private static final class ConstantLight implements LightRetriever {
        @Override
        public int sample(final double x, final double y, final double z) {
            return LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }
    }

    private static final class WorldLight implements LightRetriever {
        private final BlockRenderView renderView;

        private WorldLight(final BlockRenderView view) {
            renderView = view;
        }

        @Override
        public int sample(final double x, final double y, final double z) {
            final BlockPos pos = BlockPos.ofFloored(x, y, z);
            return LightmapTextureManager.pack(renderView.getLightLevel(LightType.BLOCK, pos), renderView.getLightLevel(LightType.SKY, pos));
        }
    }
}
