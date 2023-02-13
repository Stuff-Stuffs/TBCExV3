package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public class WrapperComponent<C extends Component> extends WrappingParentComponent<C> {
    private final EventStream<PreDraw> preDrawStream;
    public final EventSource<PreDraw> preDraw;
    private final C empty;

    public WrapperComponent(final Sizing horizontalSizing, final Sizing verticalSizing, final C empty) {
        super(horizontalSizing, verticalSizing, empty);
        this.empty = empty;
        preDrawStream = new EventStream<>(preDraws -> delta -> {
            for (final PreDraw preDraw : preDraws) {
                preDraw.preDraw(delta);
            }
        });
        preDraw = preDrawStream.source();
    }

    @Override
    public WrappingParentComponent<C> child(final C newChild) {
        return super.child(newChild == null ? empty : newChild);
    }

    @Override
    public void draw(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks, final float delta) {
        preDrawStream.sink().preDraw(delta);
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        if (child != null) {
            drawChildren(matrices, mouseX, mouseY, partialTicks, delta, List.of(child));
        }
    }

    public interface PreDraw {
        void preDraw(double delta);
    }
}
