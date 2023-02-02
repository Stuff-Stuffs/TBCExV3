package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

public class InterceptingComponent<C extends Component> extends WrappingParentComponent<C> {
    private final Runnable onDraw;

    public InterceptingComponent(final Sizing horizontalSizing, final Sizing verticalSizing, final Runnable draw) {
        super(horizontalSizing, verticalSizing, null);
        onDraw = draw;
    }

    @Override
    public void draw(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks, final float delta) {
        onDraw.run();
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        drawChildren(matrices, mouseX, mouseY, partialTicks, delta, children());
    }
}
