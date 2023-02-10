package io.github.stuff_stuffs.tbcexv3model.impl.scene;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelRenderPartContext;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Bone;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AnimationSceneImpl implements AnimationScene {
    private final Map<Identifier, Model> models = new Object2ReferenceOpenHashMap<>();

    @Override
    public Set<Identifier> models() {
        return Collections.unmodifiableSet(models.keySet());
    }

    @Override
    public Model model(final Identifier id) {
        return models.get(id);
    }

    @Override
    public void removeModel(final Identifier id) {
        models.remove(id);
    }

    @Override
    public void addModel(final Identifier id) {
        if (!models.containsKey(id)) {
            models.put(id, Model.create());
        }
    }

    @Override
    public void render(final MatrixStack matrices, final VertexConsumerProvider vertexConsumer, final Vec3d cameraPos, final Quaternionfc cameraLook, final double time) {
        for (final Map.Entry<Identifier, Model> entry : models.entrySet()) {
            final Model model = entry.getValue();
            for (final Identifier boneId : model.skeleton().bones()) {
                final Bone bone = model.skeleton().bone(boneId);
                final Model.ModelAttachedPart modelAttached = model.get(boneId);
                Vector3fc cameraPosTransformed = null;
                for (final Identifier partId : modelAttached.parts()) {
                    final ModelPart part = modelAttached.part(partId);
                    if (cameraPosTransformed == null) {
                        matrices.push();
                        final Matrix4f completeTransform = bone.completeTransform();
                        matrices.multiplyPositionMatrix(completeTransform);
                        final Vector4f vec = new Vector4f((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, 1.0F);
                        completeTransform.transform(vec);
                        cameraPosTransformed = new Vector3f(vec.x() / vec.w(), vec.y() / vec.w(), vec.z() / vec.w());
                    }
                    part.render(ModelRenderPartContext.of(matrices, vertexConsumer, cameraPosTransformed, cameraLook, model, boneId, partId), time);
                }
                if (cameraPosTransformed != null) {
                    matrices.pop();
                }
            }
        }
    }
}
