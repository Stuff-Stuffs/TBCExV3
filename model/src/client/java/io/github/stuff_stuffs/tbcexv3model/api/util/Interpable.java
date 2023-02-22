package io.github.stuff_stuffs.tbcexv3model.api.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public interface Interpable<T extends Interpable<T>> {
    T interpolate(T next, double alpha);

    record InterpableDouble(double value) implements Interpable<InterpableDouble> {
        @Override
        public InterpableDouble interpolate(final InterpableDouble next, final double alpha) {
            return new InterpableDouble(MathHelper.lerp(alpha, value, next.value));
        }
    }

    record InterpableVec3d(Vec3d value) implements Interpable<InterpableVec3d> {
        @Override
        public InterpableVec3d interpolate(final InterpableVec3d next, final double alpha) {
            final double x = MathHelper.lerp(alpha, value.x, next.value.x);
            final double y = MathHelper.lerp(alpha, value.y, next.value.y);
            final double z = MathHelper.lerp(alpha, value.z, next.value.z);
            return new InterpableVec3d(new Vec3d(x, y, z));
        }
    }

    record InterpableQuaternionF(Quaternionfc value) implements Interpable<InterpableQuaternionF> {
        @Override
        public InterpableQuaternionF interpolate(final InterpableQuaternionF next, final double alpha) {
            return new InterpableQuaternionF(value.slerp(next.value, (float) alpha, new Quaternionf()));
        }
    }
}
