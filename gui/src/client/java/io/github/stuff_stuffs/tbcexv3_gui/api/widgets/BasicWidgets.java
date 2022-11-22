package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.AbstractSingleChildWidget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.ModifiableWidget;
import net.minecraft.client.render.RenderLayer;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class BasicWidgets {
    public static <T> AbstractSingleChildWidget<T> hide(final Predicate<? super T> visibility, final StateUpdater<? super T> stateUpdater, final ModifiableWidget.WidgetEventPhase eventPhase) {
        return new ModifiableWidget<>(new ModifiableWidget.Animation<>() {
            @Override
            public Optional<WidgetRenderContext> animate(final T data, final WidgetRenderContext parent) {
                return visibility.test(data) ? Optional.of(parent) : Optional.empty();
            }

            @Override
            public Optional<WidgetEvent> animateEvent(final T data, final WidgetEvent event) {
                return visibility.test(data) ? Optional.of(event) : Optional.empty();
            }
        }, stateUpdater, eventPhase);
    }

    public static <T> Widget<T> button(final Function<? super T, ? extends WidgetUtils.MutableButtonStateHolder> stateGetter, final WidgetRenderUtils.Renderer<T> renderer, final Sizer<? super T> sizer) {
        final ModifiableWidget<T> widget = new ModifiableWidget<>(new ModifiableWidget.Animation<>() {
            @Override
            public Optional<WidgetRenderContext> animate(final T data, final WidgetRenderContext parent) {
                return Optional.of(parent);
            }

            @Override
            public Optional<WidgetEvent> animateEvent(final T data, final WidgetEvent event) {
                return Optional.of(event);
            }
        }, WidgetUtils.<T>builder().addTransforming(stateGetter, WidgetUtils.MutableButtonStateHolder.stateUpdater()).build(), ModifiableWidget.WidgetEventPhase.POST_CHILD);
        widget.setChild(new TerminalWidget<>(StateUpdater.none(), renderer, sizer), Function.identity());
        return widget;
    }

    public static Widget<Void> basicPanel(final int color, final RenderLayer renderLayer, final Sizer<Void> sizer) {
        return basicPanel(v -> color, renderLayer, sizer);
    }

    public static <T> Widget<T> basicPanel(final ToIntFunction<? super T> colourGetter, final RenderLayer renderLayer, final Sizer<? super T> sizer) {
        return new TerminalWidget<>(new StateUpdater<>() {
            @Override
            public boolean event(final WidgetEvent event, final T data) {
                return false;
            }

            @Override
            public void updateBounds(final Rectangle bounds, final T data) {

            }
        }, WidgetRenderUtils.basicPanelTerminal(colourGetter), sizer);
    }


    public enum ButtonState {
        DEFAULT,
        HOVER,
        PRESSED
    }

    private BasicWidgets() {
    }
}
