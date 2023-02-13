package io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts;

import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectRenderInfo;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectRenderInfoRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStat;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatMapView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.gui.ModelPreviewComponent;
import io.github.stuff_stuffs.tbcexv3core.api.gui.TBCExGUI;
import io.github.stuff_stuffs.tbcexv3core.api.gui.WrapperComponent;
import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorld;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.Observable;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class StatsPart {
    public static Component stats(final BattleParticipantHandle target) {
        final GridLayout gridLayout = Containers.grid(Sizing.fill(75), Sizing.fill(75), 2, 2);
        gridLayout.positioning(Positioning.relative(50, 50));
        gridLayout.surface(TBCExGUI.DEFAULT_SURFACE);
        final AnimationScene scene = ((ClientBattleWorld) MinecraftClient.getInstance().world).tbcex$getScene(target.getParent());
        final Component modelPreview;
        if (scene != null) {
            modelPreview = new ModelPreviewComponent(BattleAnimationContext.toModelId(target), scene);
            modelPreview.sizing(Sizing.fill(50), Sizing.fill(50));
        } else {
            modelPreview = Components.box(Sizing.fill(50), Sizing.fill(50));
        }
        gridLayout.child(modelPreview, 0, 0);
        gridLayout.child(statPart(target), 1, 1);
        gridLayout.child(effectPart(target), 1, 0);
        return gridLayout;
    }

    private static Component statPart(final BattleParticipantHandle target) {
        final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(target.getParent());
        final FlowLayout stats = Containers.verticalFlow(Sizing.fill(50), Sizing.fill(50));
        stats.child(new WrapperComponent<>(Sizing.fill(100), Sizing.content(2), Components.label(Text.of("Stats"))).surface(TBCExGUI.LIGHT_SURFACE));
        final Observable<Object2DoubleMap<BattleParticipantStat>> observable = Observable.of(new Object2DoubleOpenHashMap<>());
        final WrapperComponent<FlowLayout> wrapper = new WrapperComponent<>(Sizing.content(), Sizing.content(), stats);
        wrapper.preDraw.subscribe(delta -> {
            if (view != null) {
                final BattleParticipantStateView stateView = view.getState().getParticipantByHandle(target);
                final Object2DoubleMap<BattleParticipantStat> map = new Object2DoubleOpenHashMap<>();
                final BattleParticipantStatMapView statMap = stateView.getStatMap();
                for (final BattleParticipantStat stat : BattleParticipantStat.REGISTRY) {
                    map.put(stat, statMap.compute(stat, null));
                }
                if (!observable.get().equals(map)) {
                    observable.set(map);
                }
            }
        });
        final FlowLayout statLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        final ScrollContainer<FlowLayout> statScroller = Containers.verticalScroll(Sizing.content(), Sizing.fill(100), statLayout);
        stats.child(statScroller);
        observable.observe(types -> {
            statLayout.clearChildren();
            if (view != null) {
                boolean odd = false;
                for (final Object2DoubleMap.Entry<BattleParticipantStat> entry : observable.get().object2DoubleEntrySet()) {
                    final String prefix = BattleParticipantStat.REGISTRY.getId(entry.getKey()).toString() + ": ";
                    final WrapperComponent<LabelComponent> wrapperComponent = new WrapperComponent<>(Sizing.fill(100), Sizing.content(2), Components.label(Text.of(prefix + entry.getDoubleValue())));
                    wrapperComponent.surface(odd ? TBCExGUI.LIGHT_SURFACE : TBCExGUI.DARK_SURFACE);
                    odd = !odd;
                    statLayout.child(wrapperComponent);
                }
            }
        });
        return wrapper;
    }

    private static Component effectPart(final BattleParticipantHandle target) {
        final BattleView view = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(target.getParent());
        final FlowLayout effects = Containers.verticalFlow(Sizing.fill(50), Sizing.fill(50));
        effects.child(new WrapperComponent<>(Sizing.fill(100), Sizing.content(2), Components.label(Text.of("Active Effects"))).surface(TBCExGUI.LIGHT_SURFACE));
        final WrapperComponent<FlowLayout> wrapper = new WrapperComponent<>(Sizing.content(), Sizing.content(), effects);
        final Observable<Set<BattleParticipantEffectType<?, ?>>> observable = Observable.of(new ObjectOpenHashSet<>());
        wrapper.preDraw.subscribe(delta -> {
            if (view != null) {
                final BattleParticipantStateView stateView = view.getState().getParticipantByHandle(target);
                final Set<BattleParticipantEffectType<?, ?>> set = new ObjectOpenHashSet<>();
                stateView.getEffects().forEachRemaining(set::add);
                if (!observable.get().equals(set)) {
                    observable.set(set);
                }
            }
        });
        final FlowLayout effectLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        final ScrollContainer<FlowLayout> effectScroller = Containers.verticalScroll(Sizing.content(), Sizing.fill(100), effectLayout);
        effects.child(effectScroller);
        observable.observe(types -> {
            effectLayout.clearChildren();
            if (view != null) {
                final BattleParticipantStateView stateView = view.getState().getParticipantByHandle(target);
                boolean odd = false;
                for (final BattleParticipantEffectType<?, ?> type : types) {
                    final Component child = effectEntry(type, odd, stateView);
                    if (child == null) {
                        continue;
                    }
                    effectLayout.child(child);
                    odd = !odd;
                }
            }
        });
        return wrapper;
    }

    private static <View extends BattleParticipantEffect, Effect extends View> @Nullable Component effectEntry(final BattleParticipantEffectType<View, Effect> type, final boolean odd, final BattleParticipantStateView state) {
        final BattleParticipantEffectRenderInfo<View, Effect> info = BattleParticipantEffectRenderInfoRegistry.INSTANCE.get(type);
        if (info == null) {
            return null;
        }
        final View view = state.getEffectView(type).get();
        if (!info.shown(state)) {
            return null;
        }
        final WrapperComponent<LabelComponent> wrapperComponent = new WrapperComponent<>(Sizing.fill(100), Sizing.content(2), Components.label(info.name(view, state)));
        wrapperComponent.surface(odd ? TBCExGUI.LIGHT_SURFACE : TBCExGUI.DARK_SURFACE);
        wrapperComponent.tooltip(info.description(view, state).texts());
        return wrapperComponent;
    }

    private StatsPart() {
    }
}
