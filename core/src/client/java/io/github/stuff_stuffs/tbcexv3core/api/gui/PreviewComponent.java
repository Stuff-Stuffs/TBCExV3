package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.base.BaseComponent;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;

public class PreviewComponent extends BaseComponent {
    private float rotationSpeed = 1 / 80.0F;
    private float mouseSensitivity = (float) Math.PI * 2.0F;
    private boolean rotating = true;
    private boolean mouseEffects = true;
    private final Quaternionf rotation = new Quaternionf().identity();
    private final Renderable renderable;

    public PreviewComponent(final Renderable renderable) {
        this.renderable = renderable;
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
        if (rotating) {
            rotation.rotateY(delta * rotationSpeed);
        }
        matrices.push();
        matrices.translate(x + this.width / 2f, y + this.height / 2f, -250);
        final int i = Math.min(width, height);
        matrices.multiply(rotation);
        matrices.scale(i * 0.75F, -i * 0.75F, i * 0.75F);
        renderable.render(matrices, partialTicks, delta, width(), height());
        matrices.pop();
    }

    public interface Renderable {
        void render(MatrixStack matrices, float partialTicks, float delta, int width, int height);
    }
}
