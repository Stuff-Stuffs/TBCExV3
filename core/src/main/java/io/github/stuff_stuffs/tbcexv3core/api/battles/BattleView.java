package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleView {
    BlockPos origin();

    BattleStateView getState();

    int getActionCount();

    BattleAction getAction(int index);

    TracerView<ActionTrace> tracer();

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
