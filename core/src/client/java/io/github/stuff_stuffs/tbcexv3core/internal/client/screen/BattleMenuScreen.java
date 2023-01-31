package io.github.stuff_stuffs.tbcexv3core.internal.client.screen;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.gui.SelectionWheelComponent;
import io.github.stuff_stuffs.tbcexv3core.api.gui.TBCExGridLayout;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BattleMenuScreen extends BaseOwoScreen<GridLayout> {
    public BattleMenuScreen(BattleParticipantHandle handle) {

    }

    @Override
    protected @NotNull OwoUIAdapter<GridLayout> createAdapter() {
        return OwoUIAdapter.create(this, (hor, ver) -> new TBCExGridLayout(hor, ver, 1, 1));
    }

    @Override
    protected void build(final GridLayout rootComponent) {
        final SelectionWheelComponent component = new SelectionWheelComponent(0.15, 1.25);
        for (int i = 0; i < 5; i++) {
            component.addChild().setWrappedText(Text.of("Hello1Hello2"));
        }
        rootComponent.child(component, 0, 0);
    }
}
