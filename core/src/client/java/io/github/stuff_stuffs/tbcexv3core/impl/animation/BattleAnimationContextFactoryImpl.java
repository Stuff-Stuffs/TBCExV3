package io.github.stuff_stuffs.tbcexv3core.impl.animation;

import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContextFactory;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleParticipantAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleSceneAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BattleAnimationContextFactoryImpl implements BattleAnimationContextFactory {
    private final BattleView battle;

    public BattleAnimationContextFactoryImpl(final BattleView battle) {
        this.battle = battle;
    }

    @Override
    public BattleStateView state() {
        return battle.getState();
    }

    @Override
    public BlockPos toLocal(final BlockPos global) {
        return battle.toLocal(global);
    }

    @Override
    public BlockPos toGlobal(final BlockPos local) {
        return battle.toGlobal(local);
    }

    @Override
    public Vec3d toLocal(final Vec3d global) {
        return battle.toLocal(global);
    }

    @Override
    public Vec3d toGlobal(final Vec3d local) {
        return battle.toLocal(local);
    }

    @Override
    public BattleParticipantAnimationContext forChild(final BattleParticipantHandle handle, final TracerView.Node<ActionTrace> action) {
        return new BattleParticipantAnimationContext() {
            @Override
            public BattleAnimationContextFactory parent() {
                return BattleAnimationContextFactoryImpl.this;
            }

            @Override
            public BattleParticipantHandle handle() {
                return handle;
            }

            @Override
            public TracerView.Node<ActionTrace> action() {
                return action;
            }
        };
    }

    @Override
    public BattleSceneAnimationContext forScene(final TracerView.Node<ActionTrace> action) {
        return new BattleSceneAnimationContext() {
            @Override
            public BattleAnimationContextFactory parent() {
                return BattleAnimationContextFactoryImpl.this;
            }

            @Override
            public TracerView.Node<ActionTrace> action() {
                return action;
            }
        };
    }
}
