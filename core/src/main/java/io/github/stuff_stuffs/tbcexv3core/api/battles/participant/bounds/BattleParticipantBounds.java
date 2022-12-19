package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.bounds.BattleParticipantBoundsImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Optional;

public interface BattleParticipantBounds {
    Identifier BODY = TBCExV3Core.createId("body");
    Codec<BattleParticipantBounds> CODEC = BattleParticipantBoundsImpl.CASTED_CODEC;

    Iterator<Part> parts();

    Optional<Part> byId(Identifier id);

    BlockPos center();

    interface Part {
        Identifier id();

        Box box();
    }

    static Builder builder(final BlockPos center) {
        return new BattleParticipantBoundsImpl.BuilderImpl(center);
    }

    static BattleParticipantBounds basic(final Entity entity) {
        final BlockPos blockPos = entity.getBlockPos();
        final Box box = entity.getDimensions(entity.getPose()).getBoxAt(Vec3d.ofBottomCenter(blockPos));
        return builder(blockPos).add(BODY, box).build();
    }

    static BattleParticipantBounds move(final BlockPos center, final BattleParticipantBounds bounds) {
        final BlockPos oldCenter = bounds.center();
        final Vec3d delta = Vec3d.of(center.subtract(oldCenter));
        final Builder builder = builder(center);
        bounds.parts().forEachRemaining(part -> builder.add(part.id(), part.box().offset(delta.x, delta.y, delta.z)));
        return builder.build();
    }

    interface Builder {
        Builder add(Identifier id, Box box);

        boolean contains(Identifier id);

        BlockPos center();

        BattleParticipantBounds build();
    }
}
