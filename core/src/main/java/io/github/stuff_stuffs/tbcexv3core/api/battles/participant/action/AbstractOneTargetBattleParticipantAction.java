package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractOneTargetBattleParticipantAction<R, T extends BattleParticipantActionTarget> implements BattleParticipantAction {
    protected final BattleParticipantActionTargetType<T> type;
    protected final Function<State<T>, BattleAction> actionFactory;
    private final BattleParticipantActionBuilder.TargetProviderFactory<State<T>> targetProviderFactory;
    private final R data;
    private final BiFunction<R, State<T>, R> renderDataUpdater;

    protected AbstractOneTargetBattleParticipantAction(final BattleParticipantActionTargetType<T> type, final Function<State<T>, BattleAction> factory, final BattleParticipantActionBuilder.TargetProviderFactory<State<T>> providerFactory, final R data, final BiFunction<R, State<T>, R> updater) {
        this.type = type;
        actionFactory = factory;
        targetProviderFactory = BattleParticipantActionBuilder.TargetProviderFactory.orEmpty(providerFactory, state -> state.value == null);
        this.data = data;
        renderDataUpdater = updater;
    }

    @Override
    public BattleParticipantActionBuilder<R> builder(final BattleParticipantStateView stateView, final Consumer<BattleAction> consumer) {
        return BattleParticipantActionBuilder.create(stateView, s -> s.value != null, actionFactory, new State<>(stateView), targetProviderFactory, (state, target) -> {
            if (state.value != null) {
                throw new IllegalStateException();
            } else if (target.type() != type) {
                throw new IllegalArgumentException("Type mismatch!");
            }
            state.value = (T) target;
        }, consumer, data, renderDataUpdater);
    }

    protected static final class State<T> {
        private final BattleParticipantStateView stateView;
        private T value;

        private State(final BattleParticipantStateView view) {
            stateView = view;
        }

        public BattleParticipantStateView state() {
            return stateView;
        }

        public T value() {
            return value;
        }
    }
}
