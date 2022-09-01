package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class WidgetUtils {
    public static <T> StateUpdaterBuilder<T> builder() {
        return new StateUpdaterBuilder<>();
    }

    public static final class StateUpdaterBuilder<T> {
        private final List<StateUpdater<? super T>> stateUpdaters = new ArrayList<>();

        private StateUpdaterBuilder() {
        }

        public StateUpdaterBuilder<T> add(final StateUpdater<? super T> stateUpdater) {
            stateUpdaters.add(stateUpdater);
            return this;
        }

        public <K> StateUpdaterBuilder<T> addTransforming(final Function<? super T, ? extends K> transformer, final StateUpdater<K> stateUpdater) {
            stateUpdaters.add(new StateUpdater<>() {
                @Override
                public boolean event(final WidgetEvent event, final T data) {
                    return stateUpdater.event(event, transformer.apply(data));
                }

                @Override
                public void updateBounds(final Rectangle bounds, final T data) {
                    stateUpdater.updateBounds(bounds, transformer.apply(data));
                }
            });
            return this;
        }

        public StateUpdater<T> build() {
            final List<StateUpdater<? super T>> defensiveCopy = List.copyOf(stateUpdaters);
            return new StateUpdater<T>() {
                @Override
                public boolean event(final WidgetEvent event, final T data) {
                    for (final StateUpdater<? super T> stateUpdater : defensiveCopy) {
                        if (stateUpdater.event(event, data)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void updateBounds(final Rectangle bounds, final T data) {
                    for (final StateUpdater<? super T> stateUpdater : defensiveCopy) {
                        stateUpdater.updateBounds(bounds, data);
                    }
                }
            };
        }
    }

    public interface BoundsHolder {
        Rectangle bounds();
    }

    public interface MutableBoundsHolder extends BoundsHolder {
        void setBounds(Rectangle bounds);

        static <T> WidgetContext<MutableBoundsHolder> standalone(final WidgetContext<T> parentContext) {
            return WidgetContext.standalone(parentContext, new MutableBoundsHolder() {
                private Rectangle bounds;

                @Override
                public void setBounds(final Rectangle bounds) {
                    this.bounds = bounds;
                }

                @Override
                public Rectangle bounds() {
                    return bounds;
                }
            });
        }

        static StateUpdater<MutableBoundsHolder> stateUpdater() {
            return new StateUpdater<>() {
                @Override
                public boolean event(final WidgetEvent event, final MutableBoundsHolder data) {
                    return false;
                }

                @Override
                public void updateBounds(final Rectangle bounds, final MutableBoundsHolder data) {
                    data.setBounds(bounds);
                }
            };
        }
    }

    public interface ButtonStateHolder extends MutableBoundsHolder {
        BasicWidgets.ButtonState state();
    }

    public interface MutableButtonStateHolder extends ButtonStateHolder {
        void setState(BasicWidgets.ButtonState state);

        static <T> WidgetContext<MutableButtonStateHolder> standalone(final WidgetContext<T> parentContext) {
            return WidgetContext.standalone(parentContext, new MutableButtonStateHolder() {
                private Rectangle bounds;
                private BasicWidgets.ButtonState state;

                @Override
                public void setState(final BasicWidgets.ButtonState state) {
                    this.state = state;
                }

                @Override
                public BasicWidgets.ButtonState state() {
                    return state;
                }

                @Override
                public void setBounds(final Rectangle bounds) {
                    this.bounds = bounds;
                }

                @Override
                public Rectangle bounds() {
                    return bounds;
                }
            });
        }

        static StateUpdater<MutableButtonStateHolder> stateUpdater() {
            return new StateUpdater<>() {
                @Override
                public boolean event(final WidgetEvent event, final MutableButtonStateHolder data) {
                    if (event instanceof WidgetEvent.TickEvent tickEvent) {
                        if (data.state() == BasicWidgets.ButtonState.PRESSED) {
                            if (tickEvent.mousePos().isPresent() && data.bounds().contains(tickEvent.mousePos().get())) {
                                data.setState(BasicWidgets.ButtonState.HOVER);
                            } else {
                                data.setState(BasicWidgets.ButtonState.DEFAULT);
                            }
                            return true;
                        }
                    } else if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                        if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && data.bounds().contains(mousePress.point())) {
                            data.setState(BasicWidgets.ButtonState.PRESSED);
                            return true;
                        }
                    } else if (event instanceof WidgetEvent.MouseMoveEvent mouseMoveEvent) {
                        final Rectangle bounds = data.bounds();
                        if (bounds.contains(mouseMoveEvent.start()) || bounds.contains(mouseMoveEvent.end())) {
                            data.setState(BasicWidgets.ButtonState.HOVER);
                        } else {
                            data.setState(BasicWidgets.ButtonState.DEFAULT);
                        }
                        return true;
                    }
                    return false;
                }

                @Override
                public void updateBounds(final Rectangle bounds, final MutableButtonStateHolder data) {
                    data.setBounds(bounds);
                }
            };
        }
    }

    private WidgetUtils() {
    }
}
