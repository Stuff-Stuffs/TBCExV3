package io.github.stuff_stuffs.tbcexv3core.internal.client.screen;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleActionHudRegistry;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;


public final class BattleActionHudScreen extends BaseOwoScreen<ParentComponent> implements MouseUnlockingScreen {
    private final BiFunction<Sizing, Sizing, ParentComponent> factory;
    private final List<Runnable> toCleanup = new ArrayList<>();
    private boolean unlocked = false;

    private BattleActionHudScreen(final BiFunction<BattleActionHudRegistry.MouseLocker, Consumer<Runnable>, BiFunction<Sizing, Sizing, ParentComponent>> factory) {
        final Mouse mouse = MinecraftClient.getInstance().mouse;
        final BattleActionHudRegistry.MouseLocker mouseLocker = new BattleActionHudRegistry.MouseLocker() {
            @Override
            public void lockMouse() {
                unlocked = true;
                passEvents = true;
                mouse.lockCursor();
            }

            @Override
            public void unlockMouse() {
                unlocked = false;
                passEvents = false;
                mouse.unlockCursor();
            }
        };
        this.factory = factory.apply(mouseLocker, toCleanup::add);
        passEvents = true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }


    @Override
    public void removed() {
        toCleanup.forEach(Runnable::run);
        super.removed();
    }

    @Override
    protected @NotNull OwoUIAdapter<ParentComponent> createAdapter() {
        return OwoUIAdapter.create(this, factory);
    }

    @Override
    protected void build(final ParentComponent rootComponent) {
    }

    @Override
    public boolean skipCloseFromCursorUnlock() {
        return unlocked;
    }

    public static void setup(final BattleParticipantHandle handle, final Identifier id, final ClientPlayerEntity entity) {
        MinecraftClient.getInstance().setScreen(new BattleActionHudScreen((mouseLocker, cleanup) -> create(id, mouseLocker, cleanup)));
    }

    private static BiFunction<Sizing, Sizing, ParentComponent> create(final Identifier id, final BattleActionHudRegistry.MouseLocker mouseLocker, final Consumer<Runnable> consumer) {
        return BattleActionHudRegistry.INSTANCE.get(id, mouseLocker, consumer);
    }
}
