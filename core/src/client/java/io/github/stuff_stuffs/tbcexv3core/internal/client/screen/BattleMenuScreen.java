package io.github.stuff_stuffs.tbcexv3core.internal.client.screen;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionSource;
import io.github.stuff_stuffs.tbcexv3core.api.gui.SelectionWheelComponent;
import io.github.stuff_stuffs.tbcexv3core.api.gui.WrapperComponent;
import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts.ActionPart;
import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts.InventoryPart;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class BattleMenuScreen extends BaseOwoScreen<FlowLayout> {
    private final BattleParticipantHandle handle;
    private WrapperComponent<Component> selected;
    private final Stack<Component> stack = new ObjectArrayList<>();

    public BattleMenuScreen(final BattleParticipantHandle handle) {
        this.handle = handle;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void build(final FlowLayout rootComponent) {
        selected = new WrapperComponent<>(Sizing.content(), Sizing.content(), Containers.grid(Sizing.content(), Sizing.content(), 0, 0));
        rootComponent.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (!stack.isEmpty()) {
                    selected.child(stack.pop());
                } else {
                    close();
                }
                return true;
            }
            return false;
        });
        stack.push(main());
        selected.child(stack.top());
        rootComponent.child(selected);
    }

    private Component main() {
        final SelectionWheelComponent component = new SelectionWheelComponent(0.15, 1.25, Sizing.fill(200), Sizing.fill(200));
        component.addChild().setWrappedText(Text.of("Inventory")).setClickAction(() -> {
            updateState(MenuState.INVENTORY);
        });
        component.addChild().setWrappedText(Text.of("Actions")).setClickAction(() -> {
            updateState(MenuState.ACTIONS);
        });
        component.addChild().setWrappedText(Text.of("Self Stats")).setClickAction(() -> {
            updateState(MenuState.SELF_STATS);
        });
        component.addChild().setWrappedText(Text.of("Other Stats"));
        component.addChild().setWrappedText(Text.of("Test button!"));
        return component;
    }

    private void updateState(final MenuState state) {
        updateState(state, component -> {
            stack.push(selected.child());
            selected.child(component);
        });
    }

    private void updateState(final MenuState state, final Consumer<Component> push) {
        final Component child = switch (state) {
            case INVENTORY -> InventoryPart.inventory(handle, push);
            case ACTIONS -> ActionPart.action(handle, source -> source instanceof BattleParticipantActionSource.Default || source instanceof BattleParticipantActionSource.Effect || source instanceof BattleParticipantActionSource.Equipped, push);
            default -> null;
        };
        push.accept(child);
    }

    public enum MenuState {
        MAIN,
        INVENTORY,
        ACTIONS,
        SELF_STATS,
        OTHER_STATS
    }
}
