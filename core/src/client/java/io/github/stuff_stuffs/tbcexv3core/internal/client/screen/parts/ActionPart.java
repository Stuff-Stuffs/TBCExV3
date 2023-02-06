package io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionSource;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionUtil;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.battles.participant.action.BattleActionHudRegistryImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.entity.TBCExClientPlayerExtensions;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleTryActionSender;
import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.BattleActionHudScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ActionPart {
    private static Component entry(final BattleParticipantAction action, final BattleParticipantStateView state) {
        final Text name = action.name(state);
        final LabelComponent labelComponent = Components.label(name);
        labelComponent.sizing(Sizing.fill(100), Sizing.content());
        labelComponent.tooltip(action.description(state).texts());
        labelComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                final ClientPlayerEntity player = MinecraftClient.getInstance().player;
                ((TBCExClientPlayerExtensions) player).tbcexcore$action$setCurrent(action.builder(state, handle -> actionSender(state.getBattleState().getHandle(), handle)), name);
                BattleActionHudScreen.setup(state.getHandle(), action.renderer(state).orElse(BattleActionHudRegistryImpl.DEFAULT_RENDERER_ID), player);
                return true;
            }
            return false;
        });
        return labelComponent;
    }

    private static void actionSender(final BattleHandle handle, final BattleAction action) {
        BattleTryActionSender.send(handle, action);
        MinecraftClient.getInstance().setScreen(null);
    }

    public static Component action(final BattleParticipantHandle handle, final Predicate<BattleParticipantActionSource> filter, final Consumer<Component> push) {
        final FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        final FlowLayout actionList = Containers.verticalFlow(Sizing.content(), Sizing.content());
        actionList.gap(4);
        final ScrollContainer<FlowLayout> scroller = Containers.verticalScroll(Sizing.fixed(50), Sizing.fill(100), actionList);
        layout.child(scroller);
        final ClientWorld world = MinecraftClient.getInstance().world;
        final BattleView battleView = ((BattleWorld) world).tryGetBattleView(handle.getParent());
        if (battleView != null) {
            final BattleParticipantStateView participantView = battleView.getState().getParticipantByHandle(handle);
            if (participantView != null) {
                final List<Pair<BattleParticipantActionSource, BattleParticipantAction>> actions = BattleParticipantActionUtil.gather(participantView);
                final List<Pair<BattleParticipantActionSource, BattleParticipantAction>> filtered = actions.stream().filter(p -> filter.test(p.getFirst())).toList();
                for (final Pair<BattleParticipantActionSource, BattleParticipantAction> pair : filtered) {
                    actionList.child(entry(pair.getSecond(), participantView));
                }
            }
        }
        scroller.surface(Surface.PANEL);
        return scroller;
    }

    private ActionPart() {
    }
}
