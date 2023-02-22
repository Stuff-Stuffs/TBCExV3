package io.github.stuff_stuffs.tbcexv3model.api.model.properties;

import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import net.minecraft.util.Identifier;

public sealed interface ModelPropertyKey<T extends Interpable<T>> {
    Identifier POSITION_ID = TBCExV3Model.id("position");
    Identifier ROTATION_ID = TBCExV3Model.id("rotation");
    Identifier SCALE_ID = TBCExV3Model.id("scale");
    Identifier TRANSLUCENCY_ID = TBCExV3Model.id("visible");
    ModelPropertyKey<Interpable.InterpableVec3d> POSITION = new ModelKey<>(ModelPropertyKey.POSITION_ID, Interpable.InterpableVec3d.class);
    ModelPropertyKey<Interpable.InterpableQuaternionF> ROTATION = new ModelKey<>(ModelPropertyKey.ROTATION_ID, Interpable.InterpableQuaternionF.class);
    ModelPropertyKey<Interpable.InterpableVec3d> SCALE = new ModelKey<>(ModelPropertyKey.SCALE_ID, Interpable.InterpableVec3d.class);
    ModelPropertyKey<Interpable.InterpableDouble> TRANSLUCENCY = new ModelKey<>(ModelPropertyKey.TRANSLUCENCY_ID, Interpable.InterpableDouble.class);

    Class<T> type();

    record ModelKey<T extends Interpable<T>>(Identifier id, Class<T> type) implements ModelPropertyKey<T> {
    }

    record BoneKey<T extends Interpable<T>>(
            Identifier bone,
            Identifier id,
            Class<T> type
    ) implements ModelPropertyKey<T> {
        public static BoneKey<Interpable.InterpableVec3d> position(final Identifier bone) {
            return new BoneKey<>(bone, POSITION_ID, Interpable.InterpableVec3d.class);
        }

        public static BoneKey<Interpable.InterpableQuaternionF> rotation(final Identifier bone) {
            return new BoneKey<>(bone, POSITION_ID, Interpable.InterpableQuaternionF.class);
        }

        public static BoneKey<Interpable.InterpableVec3d> scale(final Identifier bone) {
            return new BoneKey<>(bone, SCALE_ID, Interpable.InterpableVec3d.class);
        }

        public static BoneKey<Interpable.InterpableDouble> translucency(final Identifier bone) {
            return new BoneKey<>(bone, TRANSLUCENCY_ID, Interpable.InterpableDouble.class);
        }
    }
}
