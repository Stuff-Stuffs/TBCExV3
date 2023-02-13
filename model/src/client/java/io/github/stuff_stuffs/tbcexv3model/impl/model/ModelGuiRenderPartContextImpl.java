package io.github.stuff_stuffs.tbcexv3model.impl.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelGuiRenderPartContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public record ModelGuiRenderPartContextImpl(MatrixStack matrices,
                                            VertexConsumerProvider vertexConsumers,
                                            Model model,
                                            int light,
                                            Identifier bone,
                                            Identifier id,
                                            float width,
                                            float height
) implements ModelGuiRenderPartContext {
}
