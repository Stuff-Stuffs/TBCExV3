package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

import java.util.Iterator;
import java.util.function.*;

public class BattleParticipantActionBuilderImpl<R, S> implements BattleParticipantActionBuilder<R> {
    private final BattleParticipantStateView stateView;
    private final Predicate<S> buildPredicate;
    private final Function<S, ? extends BattleAction> builder;
    private final S state;
    private final TargetProviderFactory<S> targetProviderFactory;
    private final BiConsumer<S, BattleParticipantActionTarget> stateUpdater;
    private final Consumer<BattleAction> consumer;
    private final BiFunction<R, S, R> renderDataUpdater;
    private R renderData;
    private int targetCount = 0;
    private TargetProvider currentProvider;

    public BattleParticipantActionBuilderImpl(final BattleParticipantStateView view, final Predicate<S> predicate, final Function<S, ? extends BattleAction> builder, final S state, final TargetProviderFactory<S> factory, final BiConsumer<S, BattleParticipantActionTarget> updater, final Consumer<BattleAction> consumer, final R data, final BiFunction<R, S, R> renderDataUpdater) {
        stateView = view;
        buildPredicate = predicate;
        this.builder = builder;
        this.state = state;
        targetProviderFactory = factory;
        stateUpdater = updater;
        this.consumer = consumer;
        this.renderDataUpdater = renderDataUpdater;
        renderData = data;
        setupProvider();
    }

    @Override
    public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
        return currentProvider.targets(type);
    }

    @Override
    public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type) {
        return currentProvider.raycastTargets(type);
    }

    @Override
    public R renderData() {
        return renderData;
    }

    @Override
    public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
        return currentProvider.types();
    }

    private void update(final BattleParticipantActionTarget target, final int token) {
        if (token != targetCount) {
            throw new IllegalArgumentException("Tried to add target with wrong token!");
        }
        targetCount++;
        stateUpdater.accept(state, target);
        renderData = renderDataUpdater.apply(renderData, state);
        setupProvider();
    }

    private void setupProvider() {
        final int t = targetCount;
        currentProvider = targetProviderFactory.build(stateView, state, action -> update(action, t));
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
