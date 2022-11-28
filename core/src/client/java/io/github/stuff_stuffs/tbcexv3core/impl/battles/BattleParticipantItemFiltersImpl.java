package io.github.stuff_stuffs.tbcexv3core.impl.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemFilter;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemFilters;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Predicate;

public final class BattleParticipantItemFiltersImpl implements BattleParticipantItemFilters {
    public static final BattleParticipantItemFiltersImpl INSTANCE = new BattleParticipantItemFiltersImpl();
    private final Map<Identifier, Entry> map = new Object2ReferenceOpenHashMap<>();

    private BattleParticipantItemFiltersImpl() {
    }

    @Override
    public void register(final Identifier id, final BattleParticipantItemFilter filter, final Predicate<BattleParticipantStateView> predicate) {
        if (map.put(id, new Entry(filter, predicate)) != null) {
            throw new IllegalArgumentException("Duplicate id: " + id);
        }
    }

    @Override
    public Iterable<BattleParticipantItemFilter> filters() {
        return () -> map.values().stream().map(entry -> entry.filter).iterator();
    }

    @Override
    public Iterable<BattleParticipantItemFilter> filters(final BattleParticipantStateView stateView) {
        return () -> map.values().stream().filter(entry -> entry.predicate.test(stateView)).map(entry -> entry.filter).iterator();
    }

    @Override
    public BattleParticipantItemFilter defaultFilter() {
        return map.get(TBCExV3Core.createId("all")).filter();
    }

    private record Entry(BattleParticipantItemFilter filter, Predicate<BattleParticipantStateView> predicate) {
    }
}
