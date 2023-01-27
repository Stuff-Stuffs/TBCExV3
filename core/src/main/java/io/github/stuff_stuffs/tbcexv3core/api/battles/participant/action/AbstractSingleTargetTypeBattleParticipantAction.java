package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractSingleTargetTypeBattleParticipantAction<T extends BattleParticipantActionTarget> implements BattleParticipantAction {
    protected final BattleParticipantActionTargetType<T> type;
    protected final Function<State<T>, BattleAction> actionFactory;
    protected final Predicate<State<T>> readyForBuild;
    private final BattleParticipantActionBuilder.TargetProviderFactory<State<T>> providerFactory;

    protected AbstractSingleTargetTypeBattleParticipantAction(final BattleParticipantActionTargetType<T> type, final Function<State<T>, BattleAction> factory, final Predicate<State<T>> build, final BattleParticipantActionBuilder.TargetProviderFactory<State<T>> providerFactory) {
        this.type = type;
        actionFactory = factory;
        readyForBuild = build;
        this.providerFactory = providerFactory;
    }

    @Override
    public BattleParticipantActionBuilder builder(final BattleParticipantStateView stateView, final Consumer<BattleAction> consumer) {
        return BattleParticipantActionBuilder.create(stateView, readyForBuild, actionFactory, new State<>(stateView), (stateView1, state, targetConsumer) -> BattleParticipantActionBuilder.TargetProvider.typeCheck(providerFactory.build(stateView1, state, targetConsumer), type), (state, target) -> {
            if (target.type() != type) {
                throw new IllegalArgumentException("Type mismatch!");
            }
            state.value.add((T) target);
        }, consumer);
    }

    protected static final class State<T> {
        private final BattleParticipantStateView stateView;
        private final List<T> value = new ArrayList<>();

        private State(final BattleParticipantStateView view) {
            stateView = view;
        }

        public BattleParticipantStateView state() {
            return stateView;
        }

        public List<T> value() {
            return Collections.unmodifiableList(value);
        }
    }
}
