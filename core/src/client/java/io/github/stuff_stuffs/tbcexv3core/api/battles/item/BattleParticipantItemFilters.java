package io.github.stuff_stuffs.tbcexv3core.api.battles.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.battles.item.BattleParticipantItemFiltersImpl;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.NonExtendable
public interface BattleParticipantItemFilters {
    void register(Identifier id, BattleParticipantItemFilter filter, Predicate<BattleParticipantStateView> predicate);

    Iterable<BattleParticipantItemFilter> filters();

    Iterable<BattleParticipantItemFilter> filters(BattleParticipantStateView stateView);

    BattleParticipantItemFilter defaultFilter();

    static BattleParticipantItemFilters instance() {
        return BattleParticipantItemFiltersImpl.INSTANCE;
    }
}
