package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.github.stuff_stuffs.tbcexv3util.api.util.IntBiPredicate;
import io.wispforest.owo.ui.event.KeyPress;
import io.wispforest.owo.ui.event.MouseDown;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;

import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public final class DoublePressHandler<C> {
    private final EventStream<DoublePress<C>> eventStream;
    public final EventSource<DoublePress<C>> doublePress;
    private double timeSinceLastPress = Double.POSITIVE_INFINITY;
    private int maxDelay = 3;

    public <T> DoublePressHandler(final EventSource<T> singlePress, final Predicate<C> filter, final Function<Predicate<C>, T> factory) {
        eventStream = new EventStream<>(presses -> context -> {
            for (final DoublePress<C> press : presses) {
                if (press.onDoublePress(context)) {
                    return true;
                }
            }
            return false;
        });
        doublePress = eventStream.source();
        singlePress.subscribe(factory.apply(context -> {
            if (!filter.test(context)) {
                return false;
            }
            if (timeSinceLastPress <= maxDelay) {
                eventStream.sink().onDoublePress(context);
                timeSinceLastPress = maxDelay + 1;
                return true;
            } else {
                timeSinceLastPress = 0;
                return false;
            }
        }));
    }

    public void setMaxDelay(final int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public void update(final double delta) {
        timeSinceLastPress = timeSinceLastPress + delta;
    }

    public interface DoublePress<T> {
        boolean onDoublePress(T context);
    }

    public static DoublePressHandler<MouseDownContext> mousePress(final EventSource<MouseDown> mouse, final IntPredicate predicate) {
        return new DoublePressHandler<>(
                mouse,
                context -> predicate.test(context.button()),
                consumer ->
                        (mouseX, mouseY, b) ->
                                consumer.test(
                                        new MouseDownContext(
                                                mouseX,
                                                mouseY,
                                                b
                                        )
                                )
        );
    }

    public static DoublePressHandler<MouseDownContext> mousePress(final EventSource<MouseDown> mouse, final int button) {
        return mousePress(mouse, i -> i == button);
    }

    public static DoublePressHandler<KeyPressContext> keyPress(final EventSource<KeyPress> keyboard, final IntBiPredicate predicate) {
        return new DoublePressHandler<>(
                keyboard,
                press -> predicate.test(press.keyCode(), press.modifiers()),
                consumer ->
                        (keyCode, scanCode, modifiers) ->
                                consumer.test(
                                        new KeyPressContext(
                                                keyCode,
                                                scanCode,
                                                modifiers
                                        )
                                )
        );
    }

    public record MouseDownContext(double x, double y, int button) {
    }

    public record KeyPressContext(int keyCode, int scanCode, int modifiers) {
    }
}
