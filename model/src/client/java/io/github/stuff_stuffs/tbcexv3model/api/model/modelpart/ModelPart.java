package io.github.stuff_stuffs.tbcexv3model.api.model.modelpart;

import io.github.stuff_stuffs.tbcexv3model.api.model.ModelRenderPartContext;
import net.minecraft.client.util.math.MatrixStack;

public interface ModelPart {
    void render(ModelRenderPartContext context, double time);

    static ModelPart offset(final ModelPart modelPart, final float x, final float y, final float z) {
        return (context, time) -> {
            final MatrixStack matrices = context.matrices();
            matrices.push();
            matrices.translate(x, y, z);
            modelPart.render(context, time);
            matrices.pop();
        };
    }
}
