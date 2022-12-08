package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.action;

import com.google.common.collect.Iterators;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.*;

public class BattleParticipantActionBuilderImpl<S> implements BattleParticipantActionBuilder {
    private final BattleParticipantStateView stateView;
    private final Predicate<S> buildPredicate;
    private final Function<S, BattleAction> builder;
    private final S state;
    private final TargetProviderFactory<S> targetProviderFactory;
    private final BiConsumer<S, BattleParticipantActionTarget> stateUpdater;
    private final Consumer<BattleAction> consumer;
    private int targetCount = 0;
    private TargetProvider currentProvider;

    public BattleParticipantActionBuilderImpl(final BattleParticipantStateView view, final Predicate<S> predicate, final Function<S, BattleAction> builder, final S state, final TargetProviderFactory<S> factory, final BiConsumer<S, BattleParticipantActionTarget> updater, final Consumer<BattleAction> consumer) {
        stateView = view;
        buildPredicate = predicate;
        this.builder = builder;
        this.state = state;
        targetProviderFactory = factory;
        stateUpdater = updater;
        this.consumer = consumer;
        setupProvider();
    }

    @Override
    public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
        return currentProvider.targets(type);
    }

    @Override
    public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type, final Vec3d start, final Vec3d end) {
        return currentProvider.raycastTargets(type, start, end);
    }

    @Override
    public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
        return currentProvider.types();
    }

    private void update(final BattleParticipantActionTarget target) {
        targetCount++;
        stateUpdater.accept(state, target);
        setupProvider();
    }

    private void setupProvider() {
        final int t = targetCount;
        currentProvider = targetProviderFactory.build(stateView, state, this::update, () -> t == targetCount);
    }

    @Override
    public boolean canBuild() {
        return buildPredicate.test(state);
    }

    @Override
    public void build() {
        if (!canBuild()) {
            throw new IllegalStateException("Tried to build BattleParticipantAction without checking canBuild!");
        }
        consumer.accept(builder.apply(state));
    }
}
