package io.github.stuff_stuffs.tbcexv3core.api.battles;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface Battle extends BattleView {
    @Override
    BattleState getState();

    void trimActions(int size);

    void pushAction(BattleAction action);

    boolean tryPushAction(BattleAction action);

    static Encoder<Battle> encoder(final Registry<Biome> registry) {
        return CodecUtil.castedEncoder(BattleImpl.encoder(registry), BattleImpl.class, Battle.class);
    }

    static Decoder<Factory> decoder(final Registry<Biome> registry) {
        return BattleImpl.decoder(registry);
    }

    interface Factory {
        Battle create(BattleHandle handle, BattleStateMode mode, World world, BlockPos pos, Runnable worldReset);

        BattleEnvironmentImpl.Initial environment();
    }
}
