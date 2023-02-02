package io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemSort;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemSorts;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.gui.DataWrapperComponent;
import io.github.stuff_stuffs.tbcexv3core.api.gui.InterceptingComponent;
import io.github.stuff_stuffs.tbcexv3core.api.gui.SortedFlowLayout;
import io.github.stuff_stuffs.tbcexv3core.api.gui.TBCExGUI;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.Observable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public final class InventoryPart {
    private static final int ITEM_ENTRY_NAME_WIDTH = 96;
    private static final int ITEM_ENTRY_COUNT_WIDTH = 32;
    private static final int ITEM_ENTRY_RARITY_WIDTH = 56;
    private static final int ITEM_ENTRY_INSET = 4;
    private static final int ITEM_ENTRY_HEIGHT = 24;

    private static DataWrapperComponent<BattleParticipantInventoryHandle> itemStackEntry(final BattleParticipantInventoryHandle inventoryHandle, final InventoryState state, final BattleParticipantItemStack stack, final BattleParticipantStateView participant, final boolean odd, final Runnable select) {
        final Insets insets = Insets.of(ITEM_ENTRY_INSET, ITEM_ENTRY_INSET, ITEM_ENTRY_INSET, ITEM_ENTRY_INSET);
        final GridLayout layout = Containers.grid(Sizing.content(), Sizing.content(), 1, 3);
        final LabelComponent label = Components.label(stack.getItem().name(participant));
        label.maxWidth(ITEM_ENTRY_NAME_WIDTH - ITEM_ENTRY_INSET * 2);
        label.sizing(Sizing.fixed(ITEM_ENTRY_NAME_WIDTH - ITEM_ENTRY_INSET * 2), Sizing.fill(ITEM_ENTRY_HEIGHT - 2 * ITEM_ENTRY_INSET));
        final ScrollContainer<LabelComponent> component = Containers.verticalScroll(Sizing.fixed(ITEM_ENTRY_NAME_WIDTH), Sizing.fixed(ITEM_ENTRY_HEIGHT), label);
        component.positioning(Positioning.relative(0, 50));
        component.padding(insets);
        component.sizing(Sizing.fixed(ITEM_ENTRY_NAME_WIDTH), Sizing.fixed(ITEM_ENTRY_HEIGHT));
        layout.child(component, 0, 0);
        final LabelComponent count = Components.label(Text.of("" + stack.getCount()));
        count.sizing(Sizing.fixed(ITEM_ENTRY_COUNT_WIDTH), Sizing.fixed(ITEM_ENTRY_HEIGHT));
        count.margins(insets);
        layout.child(count, 0, 1);
        final LabelComponent rarity = Components.label(stack.getItem().rarity().getAsText());
        rarity.margins(insets);
        rarity.sizing(Sizing.fixed(ITEM_ENTRY_RARITY_WIDTH), Sizing.fixed(ITEM_ENTRY_HEIGHT));
        layout.child(rarity, 0, 2);
        final Surface surface = odd ? TBCExGUI.DARK_SURFACE : TBCExGUI.LIGHT_SURFACE;
        layout.surface(surface);
        layout.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                select.run();
                return true;
            }
            return false;
        });
        state.selected.observe(handle -> {
            if (handle.isEmpty()) {
                layout.surface(surface);
            } else {
                if (handle.get().equals(inventoryHandle)) {
                    layout.surface(TBCExGUI.SELECTED_SURFACE);
                } else {
                    layout.surface(surface);
                }
            }
        });
        return new DataWrapperComponent<>(Sizing.content(), Sizing.content(), layout, inventoryHandle);
    }

    private static Component sorterBar(final InventoryState state, final BattleParticipantHandle handle, final BattleWorld world) {
        final GridLayout layout = Containers.grid(Sizing.content(), Sizing.content(), 2, 3);
        final LabelComponent label = Components.label(Text.of("Name"));
        final LabelComponent count = Components.label(Text.of("Count"));
        final LabelComponent rarity = Components.label(Text.of("Rarity"));
        label.sizing(Sizing.fixed(ITEM_ENTRY_NAME_WIDTH), Sizing.fixed(ITEM_ENTRY_HEIGHT));
        count.sizing(Sizing.fixed(ITEM_ENTRY_COUNT_WIDTH), Sizing.fixed(ITEM_ENTRY_HEIGHT));
        rarity.sizing(Sizing.fixed(ITEM_ENTRY_RARITY_WIDTH), Sizing.fixed(ITEM_ENTRY_HEIGHT));

        layout.child(label, 1, 0);
        layout.child(count, 1, 1);
        layout.child(rarity, 1, 2);
        return layout;
    }

    public static Component inventory(final BattleParticipantHandle handle) {
        final InventoryState state = new InventoryState();
        final FlowLayout outer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        outer.child(sorterBar(state, handle, (BattleWorld) MinecraftClient.getInstance().world));
        final FlowLayout flowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.fill(100));
        flowLayout.surface(TBCExGUI.DEFAULT_SURFACE);
        final SortedFlowLayout<DataWrapperComponent<BattleParticipantInventoryHandle>> itemList = new SortedFlowLayout<>(Sizing.fill(100), Sizing.fixed(256), FlowLayout.Algorithm.VERTICAL, (o1, o2) -> {
            final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(o1.getData().get().getParentHandle().getParent());
            if (view == null) {
                return 0;
            }
            final BattleParticipantStateView participant = view.getState().getParticipantByHandle(o1.getData().get().getParentHandle());
            if (participant == null) {
                return 0;
            }
            final Optional<BattleParticipantItemStack> firstStack = participant.getInventory().getStack(o1.getData().get());
            final Optional<BattleParticipantItemStack> secondStack = participant.getInventory().getStack(o2.getData().get());
            if (firstStack.isPresent() && secondStack.isPresent()) {
                return state.sort.get().compare(firstStack.get(), secondStack.get(), participant);
            }
            return 0;
        }, (Class<DataWrapperComponent<BattleParticipantInventoryHandle>>) (Object) DataWrapperComponent.class);
        state.sort.observe(sort -> itemList.sort());
        final InterceptingComponent<FlowLayout> inventoryUpdater = new InterceptingComponent<>(Sizing.content(), Sizing.content(), () -> {
            final BattleView battleView = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(handle.getParent());
            if (battleView != null) {
                if (state.updateHandles(handle, battleView.getState())) {
                    itemList.clearChildren();
                    if (state.handles.isEmpty()) {
                        return;
                    }
                    final BattleParticipantStateView participant = battleView.getState().getParticipantByHandle(handle);
                    final BattleParticipantInventoryView inventory = participant.getInventory();
                    int counter = 0;
                    for (final BattleParticipantInventoryHandle inventoryHandle : state.handles) {
                        final Optional<BattleParticipantItemStack> stack = inventory.getStack(inventoryHandle);
                        if (stack.isPresent()) {
                            final ParentComponent child = itemStackEntry(inventoryHandle, state, stack.get(), participant, (counter & 1) == 1, () -> state.updateSelected(Optional.of(inventoryHandle)));
                            itemList.child(child);
                            counter++;
                        }
                    }
                }
            }
        });
        itemList.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                state.updateSelected(Optional.empty());
                return true;
            }
            return false;
        });
        inventoryUpdater.child(itemList);
        final ScrollContainer<InterceptingComponent<?>> itemListScroller = Containers.verticalScroll(Sizing.content(), Sizing.fill(100), inventoryUpdater);
        flowLayout.child(itemListScroller);
        outer.child(flowLayout);
        return outer;
    }

    private static final class InventoryState {
        private final Set<BattleParticipantInventoryHandle> handles;
        private final Observable<Optional<BattleParticipantInventoryHandle>> selected;
        private final Observable<BattleParticipantItemSort> sort = Observable.of(BattleParticipantItemSorts.instance().defaultSort());
        private final Set<BattleParticipantItemSort> sorts;

        private InventoryState() {
            handles = new ObjectOpenHashSet<>();
            sorts = new ObjectOpenHashSet<>();
            sorts.add(sort.get());
            selected = Observable.of(Optional.empty());
        }

        public boolean updateSortsAndFilters(final BattleParticipantHandle handle, final BattleStateView view) {
            final BattleParticipantStateView stateView = view.getParticipantByHandle(handle);
            if (stateView != null) {
                final Iterable<BattleParticipantItemSort> sorts = BattleParticipantItemSorts.instance().sorts(stateView);
                final Set<BattleParticipantItemSort> newSorts = new ObjectOpenHashSet<>();
                for (final BattleParticipantItemSort itemSort : sorts) {
                    newSorts.add(itemSort);
                }
                if (!this.sorts.equals(newSorts)) {
                    this.sorts.clear();
                    this.sorts.addAll(newSorts);
                    if (!this.sorts.contains(sort.get())) {
                        sort.set(BattleParticipantItemSorts.instance().defaultSort());
                    }
                    return true;
                }
            } else if (sorts.size() > 1) {
                sorts.clear();
                sort.set(BattleParticipantItemSorts.instance().defaultSort());
                sorts.add(sort.get());
                return true;
            }
            return false;
        }

        public boolean updateHandles(final BattleParticipantHandle handle, final BattleStateView view) {
            final BattleParticipantStateView participant = view.getParticipantByHandle(handle);
            if (participant != null) {
                final Iterator<BattleParticipantInventoryHandle> handles = participant.getInventory().getHandles();
                final Set<BattleParticipantInventoryHandle> newHandles = new ObjectOpenHashSet<>();
                handles.forEachRemaining(newHandles::add);
                if (!newHandles.equals(this.handles)) {
                    this.handles.clear();
                    this.handles.addAll(newHandles);
                    return true;
                }
            } else {
                if (!handles.isEmpty()) {
                    handles.clear();
                    return true;
                }
            }
            return false;
        }

        public void updateSelected(final Optional<BattleParticipantInventoryHandle> handle) {
            if (handle.isPresent() && !handles.contains(handle.get())) {
                selected.set(Optional.empty());
            } else {
                selected.set(handle);
            }
            UISounds.playButtonSound();
        }
    }

    private InventoryPart() {
    }
}
