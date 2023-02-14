package io.github.stuff_stuffs.tbcexv3model.impl.scene;

import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationFactory;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelRenderPartContext;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Bone;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.impl.animation.AnimationManagerImpl;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AnimationSceneImpl<T> implements AnimationScene<T> {
    private final AnimationManagerImpl<T> manager = new AnimationManagerImpl<>();
    private final Map<Identifier, Model> models = new Object2ReferenceOpenHashMap<>();
    private final Map<TokenImpl<T>, VertexBuffer> buffers = new Object2ReferenceOpenHashMap<>();
    private int nextBufferId = 0;

    @Override
    public AnimationManager<T> manager() {
        return manager;
    }

    @Override
    public BufferToken upload(final BufferBuilder.BuiltBuffer buffer) {
        final VertexBuffer vertexBuffer = new VertexBuffer();
        vertexBuffer.bind();
        vertexBuffer.upload(buffer);
        final TokenImpl<T> token = new TokenImpl<>(nextBufferId++, this);
        buffers.put(token, vertexBuffer);
        VertexBuffer.unbind();
        return token;
    }

    @Override
    public Set<Identifier> models() {
        return Collections.unmodifiableSet(models.keySet());
    }

    @Override
    public Model getModel(final Identifier id) {
        return models.get(id);
    }

    @Override
    public void removeModel(final Identifier id) {
        models.remove(id);
    }

    @Override
    public void addModel(final Identifier id, final ModelType type, final Map<Identifier, ModelAnimationFactory<T>> defaultFactories) {
        if (!models.containsKey(id)) {
            validate(type, defaultFactories.keySet());
            models.put(id, Model.create(type));
            manager.addModel(id, type, defaultFactories);
        }
    }

    private static void validate(final ModelType type, final Set<Identifier> ids) {
        if (!ids.containsAll(type.requiredAnimations())) {
            throw new IllegalArgumentException();
        }
        for (final ModelType parent : type.parents()) {
            validate(parent, ids);
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

    @Override
    public void update(final double time, final T data) {
        manager.update(time, data, this);
    }

    @Override
    public void close() {
        buffers.values().forEach(VertexBuffer::close);
        buffers.clear();
    }

    private record TokenImpl<T>(int id, AnimationSceneImpl<T> scene) implements BufferToken {
        @Override
        public boolean isValid() {
            return scene.buffers.containsKey(this);
        }

        @Override
        public VertexBuffer getBuffer() {
            final VertexBuffer vertexBuffer = scene.buffers.get(this);
            if (vertexBuffer == null) {
                throw new RuntimeException();
            }
            return vertexBuffer;
        }

        @Override
        public void destroy() {
            scene.buffers.remove(this).close();
        }
    }
}
