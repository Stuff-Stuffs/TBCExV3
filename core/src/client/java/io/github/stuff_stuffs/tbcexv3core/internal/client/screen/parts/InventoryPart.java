package io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemModel;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemModelRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemSort;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemSorts;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionSource;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.gui.*;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import io.github.stuff_stuffs.tbcexv3core.internal.client.TBCExV3CoreClient;
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
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class InventoryPart {
    private static final int ITEM_ENTRY_NAME_WIDTH = 96;
    private static final int ITEM_ENTRY_COUNT_WIDTH = 32;
    private static final int ITEM_ENTRY_RARITY_WIDTH = 56;
    private static final int ITEM_ENTRY_INSET = 4;
    private static final int ITEM_ENTRY_HEIGHT = 24;

    private static DataWrapperComponent<BattleParticipantInventoryHandle> itemStackEntry(final BattleParticipantInventoryHandle inventoryHandle, final InventoryState state, final BattleParticipantItemStack stack, final BattleParticipantStateView participant, final boolean odd, final Runnable select, final Consumer<Component> push) {
        final Insets insets = Insets.of(ITEM_ENTRY_INSET, ITEM_ENTRY_INSET, ITEM_ENTRY_INSET, ITEM_ENTRY_INSET);
        final GridLayout layout = Containers.grid(Sizing.content(), Sizing.content(), 1, 3);
        final LabelComponent label = Components.label(stack.getItem().name(participant));
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
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                push.accept(ActionPart.action(inventoryHandle.getParentHandle(), source -> {
                    if (source instanceof BattleParticipantActionSource.Item item) {
                        return item.handle().equals(inventoryHandle);
                    } else if (source instanceof BattleParticipantActionSource.Equipped equipped) {
                        final Optional<BattleParticipantEquipmentSlot> slot = participant.getInventory().getSlot(inventoryHandle);
                        return slot.isPresent() && equipped.slot() == slot.get();
                    }
                    return false;
                }, push));
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

    private static Component preview(final InventoryState state, final BattleParticipantHandle handle, final BattleWorld world) {
        final FlowLayout layout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        final PreviewComponent previewComponent = new PreviewComponent((matrices, partialTicks, delta, width, height) -> {
            final Optional<BattleParticipantInventoryHandle> inventoryHandle = state.selected.get();
            if (inventoryHandle.isPresent()) {
                final BattleView battleView = world.tryGetBattleView(inventoryHandle.get().getParentHandle().getParent());
                if (battleView == null) {
                    return;
                }
                final BattleParticipantStateView participant = battleView.getState().getParticipantByHandle(inventoryHandle.get().getParentHandle());
                if (participant == null) {
                    return;
                }
                final Optional<BattleParticipantItemStack> stack = participant.getInventory().getStack(inventoryHandle.get());
                if (stack.isEmpty()) {
                    return;
                }
                final Matrix4f copyPos = new Matrix4f();
                copyPos.set(matrices.peek().getPositionMatrix());
                final Matrix3f copyNormal = new Matrix3f();
                copyNormal.set(matrices.peek().getNormalMatrix());
                final BattleParticipantItemModel model = BattleParticipantItemModelRegistry.INSTANCE.get(stack.get().getItem());
                TBCExV3CoreClient.defer(context -> {
                    final MatrixStack copied = new MatrixStack();
                    final Matrix4f saved = new Matrix4f(RenderSystem.getProjectionMatrix());
                    final Window window = MinecraftClient.getInstance().getWindow();
                    RenderSystem.disableDepthTest();
                    RenderSystem.disableCull();
                    final Matrix4f matrix4f = new Matrix4f()
                            .setOrtho(
                                    0.0F,
                                    (float) ((double) window.getFramebufferWidth() / window.getScaleFactor()),
                                    (float) ((double) window.getFramebufferHeight() / window.getScaleFactor()),
                                    0.0F, 10.0F,
                                    1000.0F
                            );
                    RenderSystem.setProjectionMatrix(matrix4f);
                    final MatrixStack viewStack = RenderSystem.getModelViewStack();
                    viewStack.push();
                    viewStack.peek().getNormalMatrix().set(copyNormal);
                    viewStack.peek().getPositionMatrix().set(copyPos);
                    RenderSystem.applyModelViewMatrix();
                    viewStack.pop();
                    final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                    model.render(copied, stack.get(), participant, immediate, context.world(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
                    final float start = RenderSystem.getShaderFogStart();
                    //FIXME ugly hack
                    RenderSystem.setShaderFogStart(100000.0F);
                    immediate.draw();
                    RenderSystem.setShaderFogStart(start);
                    RenderSystem.setProjectionMatrix(saved);
                    RenderSystem.applyModelViewMatrix();
                });
            }
        });
        previewComponent.sizing(Sizing.fill(50), Sizing.fill(50));
        layout.child(previewComponent);
        final FlowLayout text = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        text.margins(Insets.of(0, 0, 2, 2));
        state.selected.observe(handle1 -> {
            final Optional<BattleParticipantInventoryHandle> inventoryHandle = state.selected.get();
            if (inventoryHandle.isPresent()) {
                final BattleView battleView = world.tryGetBattleView(inventoryHandle.get().getParentHandle().getParent());
                text.clearChildren();
                if (battleView == null) {
                    return;
                }
                final BattleParticipantStateView participant = battleView.getState().getParticipantByHandle(inventoryHandle.get().getParentHandle());
                if (participant == null) {
                    return;
                }
                final Optional<BattleParticipantItemStack> stack = participant.getInventory().getStack(inventoryHandle.get());
                if (stack.isEmpty()) {
                    return;
                }
                final TooltipText description = stack.get().getItem().description(participant);
                for (final Text t : description.texts()) {
                    final LabelComponent labelComponent = Components.label(t);
                    labelComponent.sizing(Sizing.fill(100), Sizing.content());
                    text.child(labelComponent);
                }
            } else {
                text.clearChildren();
            }
        });
        final ScrollContainer<FlowLayout> tooltipScroll = Containers.verticalScroll(Sizing.fill(49), Sizing.fill(39), text);
        tooltipScroll.margins(Insets.both(2, 2));
        tooltipScroll.surface(TBCExGUI.TOOLTIP_SURFACE);
        layout.child(tooltipScroll);
        return layout;
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

    public static Component inventory(final BattleParticipantHandle handle, final Consumer<Component> push) {
        final InventoryState state = new InventoryState();
        final FlowLayout outer = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        outer.child(sorterBar(state, handle, (BattleWorld) MinecraftClient.getInstance().world));
        final FlowLayout flowLayout = Containers.horizontalFlow(Sizing.content(), Sizing.fill(100));
        flowLayout.surface(TBCExGUI.DEFAULT_SURFACE);
        final IndexedFlowLayout<BattleParticipantInventoryHandle> itemList = new IndexedFlowLayout<>(Sizing.fill(50), Sizing.content(), (o1, o2) -> {
            final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(o1.getParentHandle().getParent());
            if (view == null) {
                return 0;
            }
            final BattleParticipantStateView participant = view.getState().getParticipantByHandle(o1.getParentHandle());
            if (participant == null) {
                return 0;
            }
            final Optional<BattleParticipantItemStack> firstStack = participant.getInventory().getStack(o1);
            final Optional<BattleParticipantItemStack> secondStack = participant.getInventory().getStack(o2);
            if (firstStack.isPresent() && secondStack.isPresent()) {
                return state.sort.get().compare(firstStack.get(), secondStack.get(), participant);
            }
            return 0;
        }, IndexedFlowLayout.Algorithm.VERTICAL);
        state.sort.observe(sort -> itemList.updateSort());
        final InterceptingComponent<IndexedFlowLayout<BattleParticipantInventoryHandle>> inventoryUpdater = new InterceptingComponent<>(Sizing.content(), Sizing.content(), () -> {
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
                            final ParentComponent child = itemStackEntry(inventoryHandle, state, stack.get(), participant, (counter & 1) == 1, () -> state.updateSelected(Optional.of(inventoryHandle)), push);
                            itemList.child(inventoryHandle, child);
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
        final ScrollContainer<InterceptingComponent<?>> itemListScroller = Containers.verticalScroll(Sizing.content(), Sizing.fill(90), inventoryUpdater);
        flowLayout.child(itemListScroller);
        flowLayout.child(preview(state, handle, (BattleWorld) MinecraftClient.getInstance().world));
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
