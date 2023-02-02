package io.github.stuff_stuffs.tbcexv3core.internal.client.screen;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.gui.SelectionWheelComponent;
import io.github.stuff_stuffs.tbcexv3core.api.gui.WrapperComponent;
import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts.InventoryPart;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class BattleMenuScreen extends BaseOwoScreen<FlowLayout> {
    private MenuState state;
    private final BattleParticipantHandle handle;
    private WrapperComponent<Component> selected;

    public BattleMenuScreen(final BattleParticipantHandle handle) {
        this.handle = handle;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(final FlowLayout rootComponent) {
        selected = new WrapperComponent<>(Sizing.content(), Sizing.content(), Containers.grid(Sizing.content(), Sizing.content(), 0, 0));
        selected.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (state != MenuState.MAIN && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                updateState(MenuState.MAIN);
                return true;
            }
            return false;
        });
        selected.child(main());
        rootComponent.child(selected);
    }

    private Component main() {
        final SelectionWheelComponent component = new SelectionWheelComponent(0.15, 1.25, Sizing.fill(100), Sizing.fill(100));
        for (int i = 0; i < 5; i++) {
            component.addChild().setWrappedText(Text.of("Inventory")).setClickAction(() -> {
                updateState(MenuState.INVENTORY);
            });
        }
        return component;
    }

    private void updateState(final MenuState state) {
        if (state != this.state) {
            this.state = state;
            selected.child(switch (state) {
                case INVENTORY -> InventoryPart.inventory(handle);
                default -> null;
            });
        }
    }

    public enum MenuState {
        MAIN,
        INVENTORY,
        ACTIONS,
        SELF_STATS,
        OTHER_STATS
    }
}
