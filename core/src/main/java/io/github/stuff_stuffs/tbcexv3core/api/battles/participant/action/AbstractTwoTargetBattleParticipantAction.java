package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractTwoTargetBattleParticipantAction<T0 extends BattleParticipantActionTarget, T1 extends BattleParticipantActionTarget> implements BattleParticipantAction {
    protected final BattleParticipantActionTargetType<T0> firstType;
    protected final BattleParticipantActionTargetType<T1> secondType;
    protected final Function<State<T0, T1>, ? extends BattleAction> actionFactory;
    private final BattleParticipantActionBuilder.TargetProviderFactory<State<T0, T1>> targetProviderFactory;

    protected AbstractTwoTargetBattleParticipantAction(final BattleParticipantActionTargetType<T0> firstType, final BattleParticipantActionTargetType<T1> secondType, final Function<State<T0, T1>, BattleAction> factory, final BattleParticipantActionBuilder.TargetProviderFactory<State<T0, T1>> providerFactory) {
        this.firstType = firstType;
        this.secondType = secondType;
        actionFactory = factory;
        targetProviderFactory = wrapProviderFactory(providerFactory, firstType, secondType);
    }

    @Override
    public BattleParticipantActionBuilder builder(final BattleParticipantStateView stateView, final Consumer<BattleAction> consumer) {
        return BattleParticipantActionBuilder.create(stateView, s -> s.firstValue != null && s.secondValue != null, actionFactory, new State<>(stateView), targetProviderFactory, (state, target) -> {
            if (state.firstValue == null) {
                if (target.type() != firstType) {
                    throw new IllegalArgumentException("Type mismatch!");
                }
                state.firstValue = (T0) target;
            } else if (state.secondValue != null) {
                if (target.type() != secondType) {
                    throw new IllegalArgumentException("Type mismatch!");
                }
                state.secondValue = (T1) target;
            } else {
                throw new IllegalStateException("Too many targets!");
            }
        }, consumer);
    }

    private static <T0 extends BattleParticipantActionTarget, T1 extends BattleParticipantActionTarget> BattleParticipantActionBuilder.TargetProviderFactory<State<T0, T1>> wrapProviderFactory(final BattleParticipantActionBuilder.TargetProviderFactory<State<T0, T1>> factory, final BattleParticipantActionTargetType<T0> firstType, final BattleParticipantActionTargetType<T1> secondType) {
        return (stateView, state, targetConsumer) -> {
            if (state.firstValue == null) {
                return BattleParticipantActionBuilder.TargetProvider.typeCheck(factory.build(stateView, state, targetConsumer), firstType);
            }
            if (state.secondValue == null) {
                return BattleParticipantActionBuilder.TargetProvider.typeCheck(factory.build(stateView, state, targetConsumer), secondType);
            }
            return BattleParticipantActionBuilder.TargetProvider.empty();
        };
    }

    protected static final class State<T0, T1> {
        private final BattleParticipantStateView stateView;
        private T0 firstValue;
        private T1 secondValue;

        private State(final BattleParticipantStateView view) {
            stateView = view;
        }

        public BattleParticipantStateView state() {
            return stateView;
        }

        public T0 firstValue() {
            return firstValue;
        }

        public T1 secondValue() {
            return secondValue;
        }
    }
}
