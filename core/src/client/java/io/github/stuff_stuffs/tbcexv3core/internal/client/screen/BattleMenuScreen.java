package io.github.stuff_stuffs.tbcexv3core.internal.client.screen;

import io.github.stuff_stuffs.tbcexv3_gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3_gui.api.screen.GuiScreen;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Trapezoid;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.Axis;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.*;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.AbstractListLikeContainerWidget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.ExpandableListContainerWidget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.OneHotContainerWidget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.ScrollingContainerWidget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Function;

public class BattleMenuScreen extends GuiScreen<BackgroundWidget<BattleMenuScreen.RootData>, BattleMenuScreen.RootData> {
    public BattleMenuScreen(final BattleHandle handle) {
        super(Text.of("BattleScreen"), create(handle), new RootData());
    }

    private static BackgroundWidget<RootData> create(final BattleHandle handle) {
        final BackgroundWidget<RootData> background = new BackgroundWidget<>(data -> 0.0, WidgetRenderUtils.basicPanelTerminal(0xD70F0F0F));
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
                    if (event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
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
                    if (event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
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
                    if (event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
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
                    if (event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
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
        final ExpandableListContainerWidget<RootData> list = new ExpandableListContainerWidget<>(WidgetRenderUtils.Renderer.empty(), (data, startA1, maxA1, remainingA1, rowIndex, maxIndex, axis) -> 0.5, AbstractListLikeContainerWidget.Justification.UPPER, Axis.Y, true);
        final ScrollingContainerWidget<InventoryScrollState> sortScroller = new ScrollingContainerWidget<>(Axis.X, false, Sizer.max(), new ScrollingContainerWidget.ScrollbarInfo<InventoryScrollState>() {
            @Override
            public double calculateScrollbarSize(final InventoryScrollState data, final Rectangle bounds) {
                return 0;
            }

            @Override
            public double getScrollAmount(final InventoryScrollState data, final Rectangle bounds, final Rectangle innerBounds) {
                return data.getScrollAmount();
            }

            @Override
            public double getScrollbarLength(final InventoryScrollState data, final Rectangle bounds, final Rectangle innerBounds) {
                return 0;
            }
        }, ScrollingContainerWidget.ScrollbarState.stateUpdater(Axis.X), (data, renderContext, bounds, innerBounds, axis) -> {
        });
        list.add(sortScroller, context -> WidgetContext.standalone(context, new InventoryScrollState(context.getData())));
        final ExpandableListContainerWidget<SortState> sortList = new ExpandableListContainerWidget<>(WidgetRenderUtils.Renderer.empty(), (data, startA1, maxA1, remainingA1, rowIndex, maxIndex, axis) -> 0.5, AbstractListLikeContainerWidget.Justification.LOWER, Axis.X, true);
        sortScroller.setChild(sortList, context -> WidgetContext.standalone(context, new SortState(context.getData())));
        for (int i = 0; i < 10; i++) {
            sortList.add(new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(0xFF000000 | HashCommon.murmurHash3(HashCommon.murmurHash3(i))), Sizer.min()), WidgetContext::stateless);
        }
        return list;
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

    public static final class SortState implements ScrollingContainerWidget.ScrollbarState {
        private final InventoryScrollState state;
        private int selected = 0;
        private double scrollAmount = 0;
        private Rectangle bounds;
        private Rectangle innerBounds;

        public SortState(final InventoryScrollState state) {
            this.state = state;
        }

        @Override
        public Rectangle innerBounds() {
            return innerBounds;
        }

        @Override
        public void setInnerBounds(final Rectangle rectangle) {
            innerBounds = rectangle;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public double getScrollAmount() {
            return scrollAmount;
        }

        @Override
        public void setScrollAmount(final double scrollAmount) {
            this.scrollAmount = scrollAmount;
        }
    }

    public static final class InventoryScrollState implements ScrollingContainerWidget.ScrollbarState {
        private final RootData rootData;
        private double scrollAmount = 0;
        private Rectangle bounds;
        private Rectangle innerBounds;

        public InventoryScrollState(final RootData data) {
            rootData = data;
        }

        @Override
        public Rectangle innerBounds() {
            return innerBounds;
        }

        @Override
        public void setInnerBounds(final Rectangle rectangle) {
            innerBounds = rectangle;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public double getScrollAmount() {
            return scrollAmount;
        }

        @Override
        public void setScrollAmount(final double scrollAmount) {
            this.scrollAmount = scrollAmount;
        }
    }
}
