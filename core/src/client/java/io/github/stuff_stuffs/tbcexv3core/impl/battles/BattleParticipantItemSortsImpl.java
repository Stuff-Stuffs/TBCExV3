package io.github.stuff_stuffs.tbcexv3core.impl.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemSort;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemSorts;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Predicate;

public final class BattleParticipantItemSortsImpl implements BattleParticipantItemSorts {
    public static final BattleParticipantItemSortsImpl INSTANCE = new BattleParticipantItemSortsImpl();
    private final Map<Identifier, Entry> map = new Object2ReferenceOpenHashMap<>();

    private BattleParticipantItemSortsImpl() {
    }

    @Override
    public void register(final Identifier id, final BattleParticipantItemSort sort, final Predicate<BattleParticipantStateView> predicate) {
        if (map.put(id, new Entry(sort, predicate)) != null) {
            throw new IllegalArgumentException("Duplicate id: " + id);
        }
    }

    @Override
    public Iterable<BattleParticipantItemSort> sorts() {
        return () -> map.values().stream().map(entry -> entry.sort).iterator();
    }

    @Override
    public Iterable<BattleParticipantItemSort> sorts(final BattleParticipantStateView stateView) {
        return () -> map.values().stream().filter(entry -> entry.predicate.test(stateView)).map(entry -> entry.sort).iterator();
    }

    @Override
    public BattleParticipantItemSort defaultSort() {
        return map.get(TBCExV3Core.createId("count")).sort;
    }

    private record Entry(BattleParticipantItemSort sort, Predicate<BattleParticipantStateView> predicate) {
    }
}
