package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.wispforest.owo.ui.base.BaseComponent;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

public class ModelPreviewComponent extends BaseComponent {
    private final Identifier modelId;
    private final AnimationScene<?, ?> scene;
    private float rotationSpeed = 1 / 80.0F;
    private float mouseSensitivity = (float) Math.PI * 2.0F;
    private boolean rotating = true;
    private boolean mouseEffects = true;
    private final Quaternionf rotation = new Quaternionf().identity();

    public ModelPreviewComponent(final Identifier id, final AnimationScene<?, ?> scene) {
        modelId = id;
        this.scene = scene;
    }

    public void setMouseSensitivity(final float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public void setRotationSpeed(final float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setRotating(final boolean rotating) {
        this.rotating = rotating;
    }

    public void setMouseEffects(final boolean mouseEffects) {
        this.mouseEffects = mouseEffects;
    }

    public void resetRotation() {
        rotation.identity();
    }

    @Override
    public boolean canFocus(final FocusSource source) {
        return true;
    }

    @Override
    public boolean onMouseDrag(final double mouseX, final double mouseY, final double deltaX, final double deltaY, final int button) {
        if (super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button)) {
            return true;
        } else {
            if (mouseEffects) {
                rotation.rotateLocalY((float) deltaX * mouseSensitivity * 0.001F);
                rotation.rotateLocalX((float) deltaY * -mouseSensitivity * 0.001F);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void draw(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks, final float delta) {
        final Model model = scene.getModel(modelId);
        if (model != null) {
            if (rotating) {
                rotation.rotateY(delta * rotationSpeed);
            }
            matrices.push();
            matrices.translate(x + width / 2f, y + height, 250);
            final int i = Math.min(width, height);
            matrices.multiply(rotation);
            matrices.scale(i * 0.25F, -i * 0.25F, i * 0.25F);
            final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(buffer);
            scene.render(modelId, matrices, immediate);
            immediate.draw();
            matrices.pop();
        }
    }
}
