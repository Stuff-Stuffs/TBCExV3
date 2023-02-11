package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public record BattleUpdate(
        BattleHandle handle,
        List<BattleAction> actions,
        int offset,
        Optional<InitialData> initialData
) {

    public static Codec<BattleUpdate> codec(final Registry<Biome> biomeRegistry) {
        return RecordCodecBuilder.create(instance -> instance.group(BattleHandle.codec().fieldOf("handle").forGetter(BattleUpdate::handle), Codec.list(BattleAction.NETWORK_CODEC).fieldOf("actions").forGetter(BattleUpdate::actions), Codec.INT.fieldOf("offset").forGetter(BattleUpdate::offset), InitialData.codec(biomeRegistry).optionalFieldOf("initialData").forGetter(BattleUpdate::initialData)).apply(instance, BattleUpdate::new));
    }

    public record InitialData(BattleEnvironmentImpl.Initial initialEnvironment, BlockPos origin) {
        public static Codec<InitialData> codec(final Registry<Biome> biomeRegistry) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    BattleEnvironmentImpl.Initial.codec(biomeRegistry).fieldOf("initialEnvironment").forGetter(InitialData::initialEnvironment),
                    BlockPos.CODEC.fieldOf("origin").forGetter(InitialData::origin)
            ).apply(instance, InitialData::new));
        }
    }
}
