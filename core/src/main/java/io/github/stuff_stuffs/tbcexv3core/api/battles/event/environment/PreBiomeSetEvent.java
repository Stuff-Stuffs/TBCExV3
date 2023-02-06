package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public interface PreBiomeSetEvent {
    boolean preBiomeSet(BlockPos pos, BattleState state, RegistryEntry<Biome> newBiome, Tracer<ActionTrace> tracer);

    interface View {
        void preBiomeSet(BlockPos pos, BattleStateView state, RegistryEntry<Biome> newBiome, TracerView<ActionTrace> tracer);
    }

    static PreBiomeSetEvent convert(final PreBiomeSetEvent.View view) {
        return (pos, state, newBiome, tracer) -> {
            view.preBiomeSet(pos, state, newBiome, tracer);
            return true;
        };
    }

    static PreBiomeSetEvent invoker(final PreBiomeSetEvent[] events, final Runnable enter, final Runnable exit) {
        return (pos, state, newBiome, tracer) -> {
            boolean b = true;
            enter.run();
            for (final PreBiomeSetEvent event : events) {
                b &= event.preBiomeSet(pos, state, newBiome, tracer);
            }
            exit.run();
            return b;
        };
    }
}
