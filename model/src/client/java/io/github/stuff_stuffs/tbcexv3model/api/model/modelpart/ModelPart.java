package io.github.stuff_stuffs.tbcexv3model.api.model.modelpart;

import io.github.stuff_stuffs.tbcexv3model.api.model.ModelRenderContext;
import net.minecraft.client.util.math.MatrixStack;

public interface ModelPart {
    void render(ModelRenderContext context);

    static ModelPart offset(final ModelPart modelPart, final float x, final float y, final float z) {
        return context -> {
            final MatrixStack matrices = context.matrices();
            matrices.push();
            matrices.translate(x, y, z);
            modelPart.render(context);
            matrices.pop();
        };
    }
}
