package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class WrapperComponent<C extends Component> extends WrappingParentComponent<C> {
    private final C empty;

    public WrapperComponent(final Sizing horizontalSizing, final Sizing verticalSizing, final C empty) {
        super(horizontalSizing, verticalSizing, empty);
        this.empty = empty;
    }

    @Override
    public WrappingParentComponent<C> child(final C newChild) {
        return super.child(newChild == null ? empty : newChild);
    }

    @Override
    public void draw(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks, final float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        if (child != null) {
            drawChildren(matrices, mouseX, mouseY, partialTicks, delta, List.of(child));
        }
    }
}
