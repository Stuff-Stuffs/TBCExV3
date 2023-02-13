package io.github.stuff_stuffs.tbcexv3model.api.model.modelpart;

import io.github.stuff_stuffs.tbcexv3model.api.model.ModelGuiRenderPartContext;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelRenderPartContext;
import net.minecraft.client.util.math.MatrixStack;

public interface ModelPart {
    void render(ModelRenderPartContext context, double time);

    void renderInGui(ModelGuiRenderPartContext context);

    static ModelPart offset(final ModelPart modelPart, final float x, final float y, final float z) {
        return new ModelPart() {
            @Override
            public void render(final ModelRenderPartContext context, final double time) {
                final MatrixStack matrices = context.matrices();
                matrices.push();
                matrices.translate(x, y, z);
                modelPart.render(context, time);
                matrices.pop();
            }

            @Override
            public void renderInGui(final ModelGuiRenderPartContext context) {
                final MatrixStack matrices = context.matrices();
                matrices.push();
                matrices.translate(x, y, z);
                modelPart.renderInGui(context);
                matrices.pop();
            }
        };
    }
}
