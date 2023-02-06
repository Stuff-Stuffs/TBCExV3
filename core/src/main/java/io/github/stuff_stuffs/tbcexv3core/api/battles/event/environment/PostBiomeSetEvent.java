package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public interface PostBiomeSetEvent {
    void postBiomeSet(BlockPos pos, BattleState state, RegistryEntry<Biome> oldBiome, Tracer<ActionTrace> tracer);

    interface View {
        void postBiomeSet(BlockPos pos, BattleStateView state, RegistryEntry<Biome> oldBiome, TracerView<ActionTrace> tracer);
    }

    static PostBiomeSetEvent convert(final PostBiomeSetEvent.View view) {
        return view::postBiomeSet;
    }

    static PostBiomeSetEvent invoker(final PostBiomeSetEvent[] events, final Runnable enter, final Runnable exit) {
        return (pos, state, oldBiome, tracer) -> {
            enter.run();
            for (final PostBiomeSetEvent event : events) {
                event.postBiomeSet(pos, state, oldBiome, tracer);
            }
            exit.run();
        };
    }
}
