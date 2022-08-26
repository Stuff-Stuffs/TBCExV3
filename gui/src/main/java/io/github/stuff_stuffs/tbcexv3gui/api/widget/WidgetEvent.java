package io.github.stuff_stuffs.tbcexv3gui.api.widget;

import io.github.stuff_stuffs.tbcexv3gui.api.Point2d;

public sealed interface WidgetEvent permits WidgetEvent.GainedFocusEvent, WidgetEvent.LostFocusEvent, WidgetEvent.MouseMoveEvent, WidgetEvent.MousePressEvent, WidgetEvent.TickEvent {
    final class LostFocusEvent implements WidgetEvent {
        @Override
        public boolean shouldPassToChildren() {
            return false;
        }

        @Override
        public boolean mustHandle() {
            return true;
        }
    }

    final class GainedFocusEvent implements WidgetEvent {
        @Override
        public boolean shouldPassToChildren() {
            return false;
        }

        @Override
        public boolean mustHandle() {
            return true;
        }
    }

    record TickEvent(int tickCount, Point2d mousePos) implements WidgetEvent {
        @Override
        public boolean shouldPassToChildren() {
            return true;
        }

        @Override
        public boolean mustHandle() {
            return false;
        }
    }

    record MouseMoveEvent(Point2d start, Point2d end, float time) implements WidgetEvent {
        @Override
        public boolean shouldPassToChildren() {
            return true;
        }

        @Override
        public boolean mustHandle() {
            return false;
        }

        @Override
        public boolean alwaysPass() {
            return true;
        }
    }

    record MousePressEvent(Point2d point, int button, float time) implements WidgetEvent {
        @Override
        public boolean shouldPassToChildren() {
            return true;
        }

        @Override
        public boolean mustHandle() {
            return false;
        }
    }

    default boolean alwaysPass() {
        return false;
    }

    boolean shouldPassToChildren();

    boolean mustHandle();
}
