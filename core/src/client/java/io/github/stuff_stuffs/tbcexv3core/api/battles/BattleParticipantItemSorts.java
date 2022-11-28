package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.battles.BattleParticipantItemSortsImpl;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.NonExtendable
public interface BattleParticipantItemSorts {
    void register(Identifier id, BattleParticipantItemSort sort, Predicate<BattleParticipantStateView> predicate);

    Iterable<BattleParticipantItemSort> sorts();

    Iterable<BattleParticipantItemSort> sorts(BattleParticipantStateView stateView);

    BattleParticipantItemSort defaultSort();

    static BattleParticipantItemSorts instance() {
        return BattleParticipantItemSortsImpl.INSTANCE;
    }
}
