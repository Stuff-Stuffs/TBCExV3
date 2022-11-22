package io.github.stuff_stuffs.tbcexv3core.internal.client.screen;

import io.github.stuff_stuffs.tbcexv3_gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3_gui.api.screen.GuiScreen;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Trapezoid;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.*;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.OneHotContainerWidget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Function;

public class BattleMenuScreen extends GuiScreen<BackgroundWidget<BattleMenuScreen.RootData>, BattleMenuScreen.RootData> {
    public BattleMenuScreen(final BattleHandle handle) {
        super(Text.of("BattleScreen"), create(handle), new RootData());
    }

    private static BackgroundWidget<RootData> create(final BattleHandle handle) {
        final BackgroundWidget<RootData> background = new BackgroundWidget<>(data -> 0.0, WidgetRenderUtils.basicPanelTerminal(0xF70F0F0F));
        final OneHotContainerWidget<RootData> oneHot = new OneHotContainerWidget<>();
        final OneHotContainerWidget.Handle mainHandle = oneHot.addChild(createMain(handle), Function.identity());
        final OneHotContainerWidget.Handle inventoryHandle = oneHot.addChild(createInventory(handle), Function.identity());
        final OneHotContainerWidget.Handle actionsHandle = oneHot.addChild(createActions(handle), Function.identity());
        final OneHotContainerWidget.Handle selfStatsHandle = oneHot.addChild(createSelfStats(handle), Function.identity());
        final OneHotContainerWidget.Handle otherStatsHandle = oneHot.addChild(createOtherStats(handle), Function.identity());
        oneHot.activeScreenGetter(data -> switch (data.menuState) {
            case MAIN -> mainHandle;
            case ACTIONS -> actionsHandle;
            case INVENTORY -> inventoryHandle;
            case SELF_STATS -> selfStatsHandle;
            case OTHER_STATS -> otherStatsHandle;
        });
        background.setChild(oneHot, Function.identity());
        return background;
    }

    private static Widget<RootData> createMain(final BattleHandle handle) {
        final SelectionWheelWidget<RootData> selectionWheel = new SelectionWheelWidget<>(SelectionWheelWidget.RadiusSizer.max(0.25, 0.85), StateUpdater.none());
        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>((data, bounds, hoverBounds, event) -> {
                    if(event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mouseMove.end();
                        data.hovered = b.isIn(mouse.x(), mouse.y());
                    } else if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mousePress.point();
                        if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && b.isIn(mouse.x(), mouse.y())) {
                            data.rootData.menuState = MenuState.INVENTORY;
                            return true;
                        }
                    }
                    return false;
                },
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("INVENTORY").asOrderedText()))),
                context -> WidgetContext.standalone(context, new SectionState(context.getData()))
        );

        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>((data, bounds, hoverBounds, event) -> {
                    if(event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mouseMove.end();
                        data.hovered = b.isIn(mouse.x(), mouse.y());
                    } else if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mousePress.point();
                        if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && b.isIn(mouse.x(), mouse.y())) {
                            data.rootData.menuState = MenuState.ACTIONS;
                            return true;
                        }
                    }
                    return false;
                },
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("ACTIONS").asOrderedText()))),
                context -> WidgetContext.standalone(context, new SectionState(context.getData()))
        );

        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>((data, bounds, hoverBounds, event) -> {
                    if(event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mouseMove.end();
                        data.hovered = b.isIn(mouse.x(), mouse.y());
                    } else if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mousePress.point();
                        if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && b.isIn(mouse.x(), mouse.y())) {
                            data.rootData.menuState = MenuState.SELF_STATS;
                            return true;
                        }
                    }
                    return false;
                },
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("SELF STATS").asOrderedText()))),
                context -> WidgetContext.standalone(context, new SectionState(context.getData()))
        );

        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>((data, bounds, hoverBounds, event) -> {
                    if(event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mouseMove.end();
                        data.hovered = b.isIn(mouse.x(), mouse.y());
                    } else if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                        final Trapezoid b = data.hovered ? hoverBounds : bounds;
                        final Point2d mouse = mousePress.point();
                        if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && b.isIn(mouse.x(), mouse.y())) {
                            data.rootData.menuState = MenuState.OTHER_STATS;
                            return true;
                        }
                    }
                    return false;
                },
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("OTHER STATS").asOrderedText()))),
                context -> WidgetContext.standalone(context, new SectionState(context.getData()))
        );

        return selectionWheel;
    }

    private static Widget<RootData> createActions(final BattleHandle handle) {
        return new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(-1), Sizer.max());
    }

    private static Widget<RootData> createOtherStats(final BattleHandle handle) {
        return new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(-1), Sizer.max());
    }

    private static Widget<RootData> createInventory(final BattleHandle handle) {
        return new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(-1), Sizer.max());
    }

    private static Widget<RootData> createSelfStats(final BattleHandle handle) {
        return new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(-1), Sizer.max());
    }

    public static final class RootData {
        public MenuState menuState = MenuState.MAIN;
    }

    public enum MenuState {
        MAIN,
        INVENTORY,
        ACTIONS,
        SELF_STATS,
        OTHER_STATS
    }

    public static final class SectionState {
        public boolean hovered = false;
        public final RootData rootData;

        public SectionState(final RootData data) {
            rootData = data;
        }
    }
}
