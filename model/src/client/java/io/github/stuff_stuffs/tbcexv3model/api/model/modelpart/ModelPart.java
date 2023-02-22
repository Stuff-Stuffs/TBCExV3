package io.github.stuff_stuffs.tbcexv3model.api.model.modelpart;

import io.github.stuff_stuffs.tbcexv3model.api.model.Bone;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyKey;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.Optional;

public interface ModelPart {
    void render(ModelPartRenderContext context);

    static void renderTransformed(final ModelPartRenderContext context, final TransformedRenderer renderer) {
        final ObjectArrayList<Identifier> boneStack = new ObjectArrayList<>();
        boneStack.add(context.bone());
        while (true) {
            final Bone bone = context.model().bone(boneStack.top());
            if (bone.parentId().isEmpty()) {
                break;
            } else {
                boneStack.add(bone.parentId().get());
            }
        }
        final MatrixStack matrices = context.matrices();
        matrices.push();
        double translucency = 1;
        if (!context.isInGui()) {
            final Vec3d position = context.properties().getProperty(ModelPropertyKey.POSITION).map(Interpable.InterpableVec3d::value).orElse(Vec3d.ZERO);
            matrices.translate(position.x, position.y, position.z);
            final Optional<Quaternionfc> rot = context.properties().getProperty(ModelPropertyKey.ROTATION).map(Interpable.InterpableQuaternionF::value);
            if (rot.isPresent()) {
                matrices.multiply(new Quaternionf(rot.get()));
            }
            final Vec3d scale = context.properties().getProperty(ModelPropertyKey.SCALE).map(Interpable.InterpableVec3d::value).orElse(new Vec3d(1, 1, 1));
            matrices.scale((float) scale.x, (float) scale.y, (float) scale.z);
            translucency = translucency * context.properties().getProperty(ModelPropertyKey.TRANSLUCENCY).map(Interpable.InterpableDouble::value).orElse(1.0);
        }
        while (!boneStack.isEmpty()) {
            final Identifier boneId = boneStack.pop();
            final Vec3d position = context.properties().getProperty(ModelPropertyKey.BoneKey.position(boneId)).map(Interpable.InterpableVec3d::value).orElse(Vec3d.ZERO);
            matrices.translate(position.x, position.y, position.z);
            final Optional<Quaternionfc> rot = context.properties().getProperty(ModelPropertyKey.BoneKey.rotation(boneId)).map(Interpable.InterpableQuaternionF::value);
            if (rot.isPresent()) {
                matrices.multiply(new Quaternionf(rot.get()));
            }
            final Vec3d scale = context.properties().getProperty(ModelPropertyKey.BoneKey.scale(boneId)).map(Interpable.InterpableVec3d::value).orElse(new Vec3d(1, 1, 1));
            matrices.scale((float) scale.x, (float) scale.y, (float) scale.z);
            translucency = translucency * context.properties().getProperty(ModelPropertyKey.BoneKey.translucency(boneId)).map(Interpable.InterpableDouble::value).orElse(1.0);
        }
        renderer.render(context, translucency);
        matrices.pop();
    }

    interface TransformedRenderer {
        void render(ModelPartRenderContext context, double translucency);
    }

    static ModelPart offset(final ModelPart modelPart, final float x, final float y, final float z) {
        return context -> {
            final MatrixStack matrices = context.matrices();
            matrices.push();
            matrices.translate(x, y, z);
            modelPart.render(context);
            matrices.pop();
        };
    }

    static ModelPart rotate(final ModelPart modelPart, final Quaternionfc rotation) {
        final Quaternionf copy = new Quaternionf(rotation);
        return context -> {
            final MatrixStack matrices = context.matrices();
            matrices.push();
            matrices.multiply(copy);
            modelPart.render(context);
            matrices.pop();
        };
    }

    static ModelPart scale(final ModelPart modelPart, final double x, final double y, final double z) {
        return context -> {
            final MatrixStack matrices = context.matrices();
            matrices.push();
            matrices.scale((float) x, (float) y, (float) z);
            modelPart.render(context);
            matrices.pop();
        };
    }
}
