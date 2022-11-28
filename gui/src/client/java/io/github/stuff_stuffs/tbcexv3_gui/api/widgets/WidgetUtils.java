package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import org.lwjgl.glfw.GLFW;

public final class WidgetUtils {
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
