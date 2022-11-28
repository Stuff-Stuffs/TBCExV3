package io.github.stuff_stuffs.tbcexv3core.internal.client.screen.menuparts;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3_gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.Axis;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.TerminalWidget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.WidgetRenderUtils;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.WidgetUtils;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.BattleMenuScreen;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Stream;

public final class InventoryWidget {
    public static Widget<InventoryState> createInventory(final BattleParticipantHandle handle) {
        final GridContainerWidget<InventoryState> container = new GridContainerWidget<>(new GridContainerWidget.GridContainerSizer<InventoryState>() {
            @Override
            public double xSize(final InventoryState data, final int index, final double max, final double total) {
                return switch (index) {
                    case 0 -> total * 0.15;
                    case 1 -> total * (1 - 0.15) / 2.0;
                    case 2 -> max;
                    default -> throw new IllegalStateException("Unexpected value: " + index);
                };
            }

            @Override
            public double ySize(final InventoryState data, final int index, final double max, final double total) {
                return switch (index) {
                    case 0 -> total * 0.15;
                    case 1 -> total * (1 - 0.15) / 2.0;
                    case 2 -> max;
                    default -> throw new IllegalStateException("Unexpected value: " + index);
                };
            }

            @Override
            public int xCellCount(final InventoryState data) {
                return 3;
            }

            @Override
            public int yCellCount(final InventoryState data) {
                return 3;
            }
        });
        container.add(createSorter(handle), context -> WidgetContext.dependent(context, SorterScrollState::new), 1, 0);
        container.add(createFilter(handle), context -> WidgetContext.dependent(context, FilterScrollState::new), 0, 1);
        container.add(createInventoryItemList(handle), context -> WidgetContext.dependent(context, InventoryScrollState::new), 1, 1);
        return container;
    }

    private static Widget<SorterScrollState> createSorter(final BattleParticipantHandle handle) {
        final ScrollingContainerWidget<SorterScrollState> sortScroller = new ScrollingContainerWidget<>(Axis.X, false, Sizer.max(), ScrollingContainerWidget.ScrollbarInfo.basic(0, 0, Axis.X), ScrollingContainerWidget.ScrollbarState.stateUpdater(Axis.X), ScrollingContainerWidget.ScrollbarRenderer.none());
        final ExpandableListContainerWidget<SorterScrollState> sortList = new ExpandableListContainerWidget<>(WidgetRenderUtils.Renderer.empty(), (data, startA1, maxA1, remainingA1, rowIndex, maxIndex, axis) -> 0.5, AbstractListLikeContainerWidget.Justification.LOWER, Axis.X, true);
        sortScroller.setChild(sortList, WidgetContext::passthrough);
        final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(handle.getParent());
        final Iterable<BattleParticipantItemSort> sorts;
        if (view != null) {
            final BattleParticipantStateView participant = view.getState().getParticipantByHandle(handle);
            if (participant != null) {
                sorts = BattleParticipantItemSorts.instance().sorts(participant);
            } else {
                sorts = Collections.emptyList();
            }
        } else {
            sorts = Collections.emptyList();
        }
        for (final BattleParticipantItemSort sort : sorts) {
            sortList.add(new TerminalWidget<>(((StateUpdater<SortButtonState>) (event, data) -> {
                if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                    final Point2d mouse = mousePress.point();
                    if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && data.bounds.contains(mouse)) {
                        data.parent.parent.sort = data.sort;
                        return true;
                    }
                }
                return false;
            }).compose(WidgetUtils.MutableBoundsHolder.stateUpdater()), WidgetRenderUtils.Renderer.compound(WidgetRenderUtils.basicPanelTerminal(data -> data.parent.parent.sort == data.sort ? 0xFF7F7F7F : 0x3F3F3F3F), WidgetRenderUtils.centeredText(data -> 0xFF7F7F7F, data -> sort.name())), Sizer.min()), context -> WidgetContext.dependent(context, data -> new SortButtonState(data, sort)));
        }
        return sortScroller;
    }

    private static Widget<FilterScrollState> createFilter(final BattleParticipantHandle handle) {
        final ScrollingContainerWidget<FilterScrollState> filterScroller = new ScrollingContainerWidget<>(Axis.Y, false, Sizer.max(), ScrollingContainerWidget.ScrollbarInfo.basic(0, 0, Axis.Y), ScrollingContainerWidget.ScrollbarState.stateUpdater(Axis.Y), ScrollingContainerWidget.ScrollbarRenderer.none());
        final ExpandableListContainerWidget<FilterScrollState> filterList = new ExpandableListContainerWidget<>(WidgetRenderUtils.Renderer.empty(), (data, startA1, maxA1, remainingA1, rowIndex, maxIndex, axis) -> 0.125, AbstractListLikeContainerWidget.Justification.LOWER, Axis.Y, true);
        filterScroller.setChild(filterList, WidgetContext::passthrough);
        final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(handle.getParent());
        final Iterable<BattleParticipantItemFilter> filters;
        if (view != null) {
            final BattleParticipantStateView participant = view.getState().getParticipantByHandle(handle);
            if (participant != null) {
                filters = BattleParticipantItemFilters.instance().filters(participant);
            } else {
                filters = Collections.emptyList();
            }
        } else {
            filters = Collections.emptyList();
        }
        for (final BattleParticipantItemFilter filter : filters) {
            filterList.add(new TerminalWidget<>(((StateUpdater<FilterButtonState>) (event, data) -> {
                if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                    final Point2d mouse = mousePress.point();
                    if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && data.bounds.contains(mouse)) {
                        data.parent.parent.filter = data.filter;
                        return true;
                    }
                }
                return false;
            }).compose(WidgetUtils.MutableBoundsHolder.stateUpdater()), WidgetRenderUtils.Renderer.compound(WidgetRenderUtils.basicPanelTerminal(data -> data.parent.parent.filter == data.filter ? 0xFF7F7F7F : 0x3F3F3F3F), WidgetRenderUtils.centeredText(data -> 0xFF7F7F7F, data -> filter.name())), Sizer.min()), context -> WidgetContext.dependent(context, data -> new FilterButtonState(data, filter)));
        }
        return filterScroller;
    }

    private static Widget<InventoryScrollState> createInventoryItemList(final BattleParticipantHandle handle) {
        final ScrollingContainerWidget.ScrollbarInfo<InventoryScrollState> info = ScrollingContainerWidget.ScrollbarInfo.basic(0.05, 0.05, Axis.Y);
        final ExpandableListContainerWidget<InventoryScrollState> itemList = new ExpandableListContainerWidget<>(WidgetRenderUtils.Renderer.empty(), (data, startA1, maxA1, remainingA1, rowIndex, maxIndex, axis) -> 0.1, AbstractListLikeContainerWidget.Justification.LOWER, Axis.Y, true);
        final ScrollingContainerWidget<InventoryScrollState> scroller = new ScrollingContainerWidget<>(Axis.Y, false, Sizer.max(), info, ((StateUpdater<InventoryScrollState>) (event, data) -> {
            final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(handle.getParent());
            boolean b = false;
            if (view != null) {
                final BattleParticipantStateView participant = view.getState().getParticipantByHandle(handle);
                if (participant != null) {
                    b = true;
                    final List<Pair<BattleParticipantInventoryHandle, BattleParticipantItemStack>> stacks = data.state.stacks(MinecraftClient.getInstance().world).toList();
                    boolean invalidated = false;
                    if (stacks.size() != data.cache.size()) {
                        invalidated = true;
                    } else {
                        for (int i = 0; i < stacks.size(); i++) {
                            final Pair<BattleParticipantInventoryHandle, BattleParticipantItemStack> stack = stacks.get(i);
                            final Pair<BattleParticipantInventoryHandle, BattleParticipantItemStack> old = data.cache.get(i);
                            if (!old.getFirst().equals(stack.getFirst())) {
                                invalidated = true;
                                break;
                            } else if (!old.getSecond().matches(stack.getSecond())) {
                                invalidated = true;
                                break;
                            }
                        }
                    }
                    if (invalidated) {
                        data.cache.clear();
                        data.handles.forEach(ExpandableListContainerWidget.Handle::remove);
                        data.handles.clear();
                        data.cache.addAll(stacks);
                        for (final Pair<BattleParticipantInventoryHandle, BattleParticipantItemStack> stack : stacks) {
                            data.handles.add(itemList.add(createItemEntry(), context -> WidgetContext.dependent(context, d -> new InventoryItemEntryState(stack.getFirst(), stack.getSecond(), d))));
                        }
                    }
                }
            }
            if (!b) {
                data.cache.clear();
                data.handles.forEach(ExpandableListContainerWidget.Handle::remove);
                data.state.parent.menuState = BattleMenuScreen.MenuState.MAIN;
            }
            return false;
        }).compose(ScrollingContainerWidget.ScrollbarState.stateUpdater(Axis.Y)), ScrollingContainerWidget.ScrollbarRenderer.basicScrollbarRenderer(true, info, data -> 0xFF3F3F3F));
        scroller.setChild(itemList, WidgetContext::passthrough);
        return scroller;
    }

    private static Widget<InventoryItemEntryState> createItemEntry() {
        final GridContainerWidget<InventoryItemEntryState> container = new GridContainerWidget<>(new GridContainerWidget.GridContainerSizer<>() {
            @Override
            public double xSize(final InventoryItemEntryState data, final int index, final double max, final double total) {
                return max / 2.0;
            }

            @Override
            public double ySize(final InventoryItemEntryState data, final int index, final double max, final double total) {
                return max;
            }

            @Override
            public int xCellCount(final InventoryItemEntryState data) {
                return 2;
            }

            @Override
            public int yCellCount(final InventoryItemEntryState data) {
                return 1;
            }
        });
        container.add(new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.Renderer.compound(WidgetRenderUtils.basicPanelTerminal(state -> state.hovered ? 0xFF00FF00 : 0x7F9F9F9F), WidgetRenderUtils.centeredText(state -> -1, state -> {
            final BattleParticipantHandle handle = state.state.state.parent.handle;
            final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(handle.getParent());
            if (view != null) {
                final BattleParticipantStateView participant = view.getState().getParticipantByHandle(handle);
                if (participant != null) {
                    return state.stack.getItem().name(participant);
                }
            }
            return Text.empty().asOrderedText();
        })), Sizer.max()), WidgetContext::passthrough, 0, 0);
        container.add(new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.Renderer.compound(WidgetRenderUtils.basicPanelTerminal(state -> state.hovered ? 0xFF00FF00 : 0x7F9F9F9F), WidgetRenderUtils.centeredText(state -> -1, state -> {
            final BattleParticipantHandle handle = state.state.state.parent.handle;
            final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(handle.getParent());
            if (view != null) {
                final BattleParticipantStateView participant = view.getState().getParticipantByHandle(handle);
                if (participant != null) {
                    return state.stack.getItem().rarity().getAsText().asOrderedText();
                }
            }
            return Text.empty().asOrderedText();
        })), Sizer.max()), WidgetContext::passthrough, 1, 0);
        final ModifiableWidget<InventoryItemEntryState> outer = new ModifiableWidget<>(ModifiableWidget.Animation.none(), ((StateUpdater<InventoryItemEntryState>) (event, data) -> {
            if (event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
                final Point2d mouse = mouseMove.end();
                return data.hovered = data.bounds.contains(mouse);
            } else if (event instanceof WidgetEvent.TickEvent tick) {
                final Point2d mouse = tick.mousePos().orElse(null);
                if (mouse != null) {
                    return data.hovered = data.bounds.contains(mouse);
                }
            } else if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                final Point2d mouse = mousePress.point();
                if (data.bounds.contains(mouse)) {
                    data.hovered = true;
                    final InventoryState inventoryState = data.state.state;
                    if (inventoryState.selectedIndex.isPresent() && inventoryState.selectedIndex.get().equals(data.handle)) {
                        inventoryState.selectedIndex = Optional.empty();
                    } else {
                        inventoryState.selectedIndex = Optional.of(data.handle);
                    }
                    return true;
                }
            }
            return false;
        }).compose(WidgetUtils.MutableBoundsHolder.stateUpdater()), ModifiableWidget.WidgetEventPhase.POST_CHILD);
        outer.setChild(container, WidgetContext::passthrough);
        return outer;
    }

    public static final class InventoryItemEntryState implements WidgetUtils.MutableBoundsHolder {
        private final BattleParticipantInventoryHandle handle;
        private final BattleParticipantItemStack stack;
        private final InventoryScrollState state;
        private boolean hovered = false;
        private Rectangle bounds;

        public InventoryItemEntryState(final BattleParticipantInventoryHandle handle, final BattleParticipantItemStack stack, final InventoryScrollState state) {
            this.handle = handle;
            this.stack = stack;
            this.state = state;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
        }
    }

    public static final class SortButtonState implements WidgetUtils.MutableBoundsHolder {
        private final SorterScrollState parent;
        private final BattleParticipantItemSort sort;
        private Rectangle bounds;

        public SortButtonState(final SorterScrollState parent, final BattleParticipantItemSort sort) {
            this.parent = parent;
            this.sort = sort;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
        }
    }

    public static final class SorterScrollState implements ScrollingContainerWidget.ScrollbarState {
        private final InventoryState parent;
        private double scrollAmount = 0;
        private Rectangle bounds;
        private Rectangle innerBounds;

        public SorterScrollState(final InventoryState parent) {
            this.parent = parent;
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

    public static final class FilterButtonState implements WidgetUtils.MutableBoundsHolder {
        private final FilterScrollState parent;
        private final BattleParticipantItemFilter filter;
        private Rectangle bounds;

        public FilterButtonState(final FilterScrollState parent, final BattleParticipantItemFilter filter) {
            this.parent = parent;
            this.filter = filter;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
        }
    }

    public static final class FilterScrollState implements ScrollingContainerWidget.ScrollbarState {
        private final InventoryState parent;
        private double scrollAmount = 0;
        private Rectangle bounds;
        private Rectangle innerBounds;

        public FilterScrollState(final InventoryState parent) {
            this.parent = parent;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
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
        public double getScrollAmount() {
            return scrollAmount;
        }

        @Override
        public void setScrollAmount(final double scrollAmount) {
            this.scrollAmount = scrollAmount;
        }
    }

    public static final class InventoryScrollState implements ScrollingContainerWidget.ScrollbarState {
        private final InventoryState state;
        private final List<Pair<BattleParticipantInventoryHandle, BattleParticipantItemStack>> cache = new ArrayList<>();
        private final List<ExpandableListContainerWidget.Handle> handles = new ArrayList<>();
        private double scrollAmount = 0;
        private Rectangle bounds;
        private Rectangle innerBounds;

        public InventoryScrollState(final InventoryState state) {
            this.state = state;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
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
        public double getScrollAmount() {
            return scrollAmount;
        }

        @Override
        public void setScrollAmount(final double scrollAmount) {
            this.scrollAmount = scrollAmount;
        }
    }

    public static final class InventoryState {
        public final BattleMenuScreen.RootData parent;
        private Optional<BattleParticipantInventoryHandle> selectedIndex = Optional.empty();
        private BattleParticipantItemFilter filter = BattleParticipantItemFilters.instance().defaultFilter();
        private BattleParticipantItemSort sort = BattleParticipantItemSorts.instance().defaultSort();

        public InventoryState(final BattleMenuScreen.RootData parent) {
            this.parent = parent;
        }

        public Stream<Pair<BattleParticipantInventoryHandle, BattleParticipantItemStack>> stacks(final World world) {
            final BattleView view = ((BattleWorld) world).tryGetBattleView(parent.handle.getParent());
            if (view == null) {
                return Stream.empty();
            }
            final BattleParticipantStateView participant = view.getState().getParticipantByHandle(parent.handle);
            if (participant == null) {
                return Stream.empty();
            }
            final BattleParticipantInventoryView inventory = participant.getInventory();
            return ObjectIterators.pour(inventory.getHandles()).stream().filter(handle -> filter.accepted(inventory.getStack(handle).get(), participant)).sorted((o1, o2) -> sort.compare(inventory.getStack(o1).get(), inventory.getStack(o2).get(), participant)).map(handle -> Pair.of(handle, inventory.getStack(handle).get()));
        }

    }

    private InventoryWidget() {
    }
}
