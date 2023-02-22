package io.github.stuff_stuffs.tbcexv3model.api.util;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class ModelMathUtil {
    public static Quaternionf interpolate(final Quaternionfc[] quaternions, final float[] weights) {
        if (quaternions.length != weights.length || quaternions.length == 0) {
            throw new IllegalArgumentException();
        }
        if (quaternions.length == 1) {
            return new Quaternionf(quaternions[0]);
        } else if (quaternions.length == 2) {
            return quaternions[0].slerp(quaternions[1], weights[1] / (weights[0] + weights[1]), new Quaternionf());
        }
        final Quaternionf scratch = new Quaternionf();
        final Quaternionf m = new Quaternionf().identity();
        for (int i = 0; i < quaternions.length; i++) {
            final Quaternionfc quaternion = quaternions[i];
            scratch.set(quaternion);
            scratch.scale(weights[i]);
            m.add(quaternion);
        }
        m.normalize();
        final Quaternionf e = new Quaternionf();
        for (int i = 0; i < 8; i++) {
            e.set(0, 0, 0, 0);
            for (int j = 0; j < quaternions.length; j++) {
                scratch.set(0, 0, 0, 0);
                scratch.set(quaternions[i]);
                log(scratch);
                scratch.mul(weights[i]);
                e.add(scratch);
            }
            exp(e);
            m.mul(e);
        }
        return m.normalize();
    }

    private static void exp(final Quaternionf quaternion) {
        final float scale = (float) Math.exp(quaternion.w);
        final float lengthV = Math.sqrt(quaternion.x * quaternion.x + quaternion.y * quaternion.y + quaternion.z * quaternion.z);
        final float invLengthV = 1 / lengthV;
        final float s = Math.sin(lengthV);
        final float c = Math.cosFromSin(s, lengthV);
        quaternion.set(quaternion.x * invLengthV * s * scale, quaternion.y * invLengthV * s * scale, quaternion.z * invLengthV * s * scale, c * scale);
    }

    private static void log(final Quaternionf quaternion) {
        final float lengthQ = Math.sqrt(quaternion.lengthSquared());
        final float a = (float) java.lang.Math.log(lengthQ);
        final float lengthV = Math.sqrt(quaternion.x * quaternion.x + quaternion.y * quaternion.y + quaternion.z * quaternion.z);
        final float invLengthV = 1 / lengthV;
        final float ac = Math.acos(quaternion.w / lengthQ);
        quaternion.set(quaternion.x * invLengthV * ac, quaternion.y * invLengthV * ac, quaternion.z * invLengthV * ac, a);
    }

    private ModelMathUtil() {
    }
}
