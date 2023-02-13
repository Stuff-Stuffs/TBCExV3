package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public interface BattleAnimationContext {
    static Identifier toModelId(final BattleParticipantHandle handle) {
        return TBCExV3Core.createId("model/" + handle.getUuid().toString().replace('-', '_'));
    }

    BattleStateView state();

    BlockPos toLocal(BlockPos global);

    BlockPos toGlobal(BlockPos local);

    Vec3d toLocal(Vec3d global);

    Vec3d toGlobal(Vec3d local);

    default Box toLocal(final Box global) {
        final Vec3d min = new Vec3d(global.minX, global.minY, global.minZ);
        final Vec3d max = new Vec3d(global.maxX, global.maxY, global.maxZ);
        return new Box(toLocal(min), toLocal(max));
    }


    default Box toGlobal(final Box local) {
        final Vec3d min = new Vec3d(local.minX, local.minY, local.minZ);
        final Vec3d max = new Vec3d(local.maxX, local.maxY, local.maxZ);
        return new Box(toGlobal(min), toGlobal(max));
    }
}
