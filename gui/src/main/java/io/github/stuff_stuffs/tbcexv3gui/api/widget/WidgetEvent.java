package io.github.stuff_stuffs.tbcexv3gui.api.widget;

import io.github.stuff_stuffs.tbcexv3gui.api.Point2d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public sealed interface WidgetEvent permits WidgetEvent.GainedFocusEvent, WidgetEvent.LostFocusEvent, WidgetEvent.MouseMoveEvent, WidgetEvent.MousePressEvent, WidgetEvent.MouseScrollEvent, WidgetEvent.TickEvent {
    final class LostFocusEvent implements WidgetEvent {
    }

    final class GainedFocusEvent implements WidgetEvent {
    }

    record TickEvent(int tickCount, Optional<Point2d> mousePos) implements WidgetEvent {
        @Override
        public Optional<WidgetEvent> transform(final Function<Point2d, Point2d> positionTransformer, final Function<Point2d, Point2d> vectorTransformer) {
            final Optional<Point2d> transformedMousePos = mousePos.map(positionTransformer);
            return Optional.of(new TickEvent(tickCount, transformedMousePos));
        }
    }

    record MouseMoveEvent(Point2d start, Point2d end, float time) implements WidgetEvent {
        @Override
        public boolean alwaysPass() {
            return true;
        }

        @Override
        public Optional<WidgetEvent> transform(final Function<Point2d, Point2d> positionTransformer, final Function<Point2d, Point2d> vectorTransformer) {
            final Point2d transformedStart = positionTransformer.apply(start);
            if (transformedStart == null) {
                return Optional.empty();
            }
            final Point2d transformedEnd = positionTransformer.apply(end);
            if (transformedEnd == null) {
                return Optional.empty();
            }
            return Optional.of(new MouseMoveEvent(transformedStart, transformedEnd, time));
        }
    }

    record MousePressEvent(Point2d point, int button, float time) implements WidgetEvent {
        @Override
        public Optional<WidgetEvent> transform(final Function<Point2d, Point2d> positionTransformer, final Function<Point2d, Point2d> vectorTransformer) {
            final Point2d transformedPoint = positionTransformer.apply(point);
            if (transformedPoint == null) {
                return Optional.empty();
            }
            return Optional.of(new MousePressEvent(transformedPoint, button, time));
        }
    }

    record MouseScrollEvent(Point2d point, double amount) implements WidgetEvent {
        @Override
        public Optional<WidgetEvent> transform(final Function<Point2d, @Nullable Point2d> positionTransformer, final Function<Point2d, @Nullable Point2d> vectorTransformer) {
            final Point2d transformedPoint = positionTransformer.apply(point);
            if (transformedPoint == null) {
                return Optional.empty();
            }
            return Optional.of(new MouseScrollEvent(transformedPoint, amount));
        }
    }

    default boolean alwaysPass() {
        return false;
    }

    default Optional<WidgetEvent> transform(final Function<Point2d, @Nullable Point2d> positionTransformer, final Function<Point2d, @Nullable Point2d> vectorTransformer) {
        return Optional.of(this);
    }
}
