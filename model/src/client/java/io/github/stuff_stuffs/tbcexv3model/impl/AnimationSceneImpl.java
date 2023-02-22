package io.github.stuff_stuffs.tbcexv3model.impl;

import io.github.stuff_stuffs.tbcexv3model.api.animation.*;
import io.github.stuff_stuffs.tbcexv3model.api.model.BoneAttachedModelParts;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPartRenderContext;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationSceneAddModelEvent;
import io.github.stuff_stuffs.tbcexv3model.api.scene.property.ScenePropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpolation;
import io.github.stuff_stuffs.tbcexv3model.api.util.Transition;
import io.github.stuff_stuffs.tbcexv3model.impl.model.BoneAttachedModelPartsImpl;
import io.github.stuff_stuffs.tbcexv3model.impl.model.ModelImpl;
import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnimationSceneImpl<SC, MC> implements AnimationScene<SC, MC> {
    private final Map<Identifier, Entry> models = new Object2ReferenceOpenHashMap<>();
    private final SortedMap<Identifier, SceneAnimationWithTransition> animationMap = new Object2ObjectAVLTreeMap<>(SceneAnimation.ANIMATION_LAYER.phaseComparator());
    private final Map<Identifier, Queue<Runnable>> animationCallbackQueues = new Object2ObjectOpenHashMap<>();
    private final List<Identifier> animationCallbacks = new ArrayList<>();
    private ScenePropertyContainer properties = ScenePropertyContainer.builder().build();
    private double time;

    public AnimationSceneImpl(final double time) {
        this.time = time;
    }

    @Override
    public Set<Identifier> models() {
        return Collections.unmodifiableSet(models.keySet());
    }

    @Override
    public void render(final Identifier model, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers) {
        final Entry entry = models.get(model);
        if (entry == null) {
            throw new NullPointerException();
        }
        renderModel(entry, matrices, vertexConsumers, null);
    }

    @Override
    public void addModel(final Identifier id, final ModelBuilder model, final ModelType type) {
        AnimationSceneAddModelEvent.EVENT.invoker().onAddModel(model, type);
        final ModelBuilderImpl builder = (ModelBuilderImpl) model;
        builder.verify();
        models.put(id, new Entry(new ModelImpl(builder.ids, type)));
    }

    @Override
    public Model getModel(final Identifier id) {
        final Entry entry = models.get(id);
        if (entry != null) {
            return entry.model;
        }
        return null;
    }

    @Override
    public Optional<ModelAnimation> findModelAnimation(final Identifier id, final ModelType type, final MC data, final double offset) {
        ModelType cursor = type;
        while (true) {
            final Optional<ModelAnimation> animation = FindModelAnimationEvent.EVENT.invoker().find(id, FindModelAnimationEvent.createContext(time + offset, cursor, data));
            if (animation.isPresent()) {
                return animation;
            }
            if (cursor.parent().isPresent()) {
                cursor = cursor.parent().get();
            } else {
                TBCExV3Model.LOGGER.warn("Could not find animation with id {} for model type {}!", id, type);
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<SceneAnimation> findSceneAnimation(final Identifier id, final SC data, final double offset) {
        return FindSceneAnimationEvent.EVENT.invoker().find(id, FindSceneAnimationEvent.createContext(time + offset, data));
    }

    @Override
    public void removeModel(final Identifier id) {
        models.remove(id);
    }

    @Override
    public void setModelAnimation(final Identifier modelId, final Identifier layer, final ModelAnimation animation, final Transition transition) {
        models.get(modelId).animationMap.put(layer, new ModelAnimationWithTransition(animation, transition));
    }

    @Override
    public Transition createTransition(final Interpolation interpolation, final double duration, final double offset) {
        return Transition.fromInterpolation(time + offset, duration, interpolation);
    }

    @Override
    public void setSceneAnimation(final Identifier layer, final SceneAnimation animation, final Transition transition) {
        animationMap.put(layer, new SceneAnimationWithTransition(animation, transition));
    }

    @Override
    public void render(final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final BlockRenderView world, final double time) {
        final double oldTime = this.time;
        this.time = time;
        for (final Identifier callback : animationCallbacks) {
            final Queue<Runnable> runnables = animationCallbackQueues.get(callback);
            if (runnables != null && !runnables.isEmpty()) {
                runnables.remove().run();
            }
        }
        if (!animationMap.isEmpty()) {
            ScenePropertyContainer container = properties;
            for (final Iterator<Map.Entry<Identifier, SceneAnimationWithTransition>> iterator = animationMap.entrySet().iterator(); iterator.hasNext(); ) {
                final Map.Entry<Identifier, SceneAnimationWithTransition> entry = iterator.next();
                final SceneAnimationWithTransition animation = entry.getValue();
                final SceneAnimationKeyFrame current = animation.animation.current(time);
                if (animation.animation().beforeStart(oldTime) || animation.animation.current(oldTime).start() != current.start()) {
                    current.addOrRemoveModels(this);
                }
                final SceneAnimationKeyFrame next = animation.animation.next(time);
                if (next == null) {
                    iterator.remove();
                    animationCallbacks.add(entry.getKey());
                    container = ScenePropertyContainer.blend(container, current.properties(), animation.transition().alpha(time));
                } else {
                    final double duration = next.start() - current.start();
                    if (animation.animation.beforeStart(oldTime)) {
                        container = ScenePropertyContainer.blend(properties, current.properties(), animation.transition().alpha(time));
                    } else {
                        container = current.interpolatedProperties(time, duration, properties, next.properties());
                    }
                }
            }
            properties = ScenePropertyContainer.copyFallback(container, properties);
        }
        for (final Entry entry : models.values()) {
            if (!entry.animationMap.isEmpty()) {
                ModelPropertyContainer container = ModelPropertyContainer.copy(entry.container);
                for (final Iterator<Map.Entry<Identifier, ModelAnimationWithTransition>> iterator = entry.animationMap.entrySet().iterator(); iterator.hasNext(); ) {
                    final Map.Entry<Identifier, ModelAnimationWithTransition> animationWithTransition = iterator.next();
                    final ModelAnimationWithTransition animation = animationWithTransition.getValue();
                    final ModelAnimationKeyFrame current = animation.animation.current(time);
                    if (animation.animation.beforeStart(oldTime) || animation.animation.current(oldTime).start() != current.start()) {
                        current.addOrRemoveParts(entry.model, id -> entry.attached.computeIfAbsent(id, i -> new BoneAttachedModelPartsImpl()));
                    }
                    final ModelAnimationKeyFrame next = animation.animation.next(time);
                    if (next == null) {
                        iterator.remove();
                        container = ModelPropertyContainer.blend(container, current.properties(), animation.transition().alpha(time));
                    } else {
                        final double duration = next.start() - current.start();
                        if (animation.animation.beforeStart(oldTime)) {
                            container = ModelPropertyContainer.blend(entry.container, current.properties(), animation.transition().alpha(time));
                        } else {
                            container = current.interpolatedProperties(time, duration, entry.container, next.properties());
                        }
                    }
                }
                entry.container = ModelPropertyContainer.copyFallback(container, entry.container);
            }
            renderModel(entry, matrices, vertexConsumers, world);
        }
    }

    private void renderModel(final Entry entry, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, @Nullable final BlockRenderView renderView) {
        for (final Map.Entry<Identifier, BoneAttachedModelParts> partsEntry : entry.attached.entrySet()) {
            final ModelPartRenderContext renderContext = ModelPartRenderContext.create(entry.model, partsEntry.getKey(), entry.container, properties, matrices, vertexConsumers, renderView);
            for (final Identifier part : partsEntry.getValue().parts()) {
                partsEntry.getValue().part(part).render(renderContext);
            }
        }
    }

    @Override
    public void addSceneAnimationCallback(final Identifier layer, final Runnable runnable) {
        if (animationMap.get(layer) == null) {
            runnable.run();
        } else {
            animationCallbackQueues.computeIfAbsent(layer, l -> new ArrayDeque<>()).add(runnable);
        }
    }

    public static class ModelBuilderImpl implements ModelBuilder {
        private final Map<Identifier, Optional<Identifier>> ids = new Object2ReferenceOpenHashMap<>();

        @Override
        public ModelBuilder addBone(final Identifier id, final Optional<Identifier> parentId) {
            ids.put(id, parentId);
            return this;
        }

        private void verify() {
            final Set<Identifier> verified = new ObjectOpenHashSet<>();
            final Map<Identifier, Optional<Identifier>> ids = new Object2ReferenceOpenHashMap<>(this.ids);
            for (final Iterator<Map.Entry<Identifier, Optional<Identifier>>> iterator = ids.entrySet().iterator(); iterator.hasNext(); ) {
                final Map.Entry<Identifier, Optional<Identifier>> entry = iterator.next();
                if (entry.getValue().isEmpty()) {
                    verified.add(entry.getKey());
                    iterator.remove();
                }
            }
            if (verified.isEmpty()) {
                throw new RuntimeException("No non-parented bones!");
            }
            while (!ids.isEmpty()) {
                boolean changes = false;
                for (final Iterator<Map.Entry<Identifier, Optional<Identifier>>> iterator = ids.entrySet().iterator(); iterator.hasNext(); ) {
                    final Map.Entry<Identifier, Optional<Identifier>> entry = iterator.next();
                    if (verified.contains(entry.getValue().get())) {
                        verified.add(entry.getKey());
                        iterator.remove();
                        changes = true;
                    }
                }
                if (!changes && !ids.isEmpty()) {
                    throw new RuntimeException("Bone with no valid parent detected!");
                }
            }
        }
    }

    private static final class Entry {
        private final Model model;
        private final Map<Identifier, BoneAttachedModelParts> attached = new Object2ReferenceOpenHashMap<>();
        private ModelPropertyContainer container = ModelPropertyContainer.builder().build();
        private final SortedMap<Identifier, ModelAnimationWithTransition> animationMap = new Object2ObjectAVLTreeMap<>(ModelAnimation.ANIMATION_LAYER.phaseComparator());

        private Entry(final Model model) {
            this.model = model;
            for (final Identifier bone : model.bones()) {
                attached.put(bone, new BoneAttachedModelPartsImpl());
            }
        }
    }

    private record ModelAnimationWithTransition(ModelAnimation animation, Transition transition) {
    }

    private record SceneAnimationWithTransition(SceneAnimation animation, Transition transition) {
    }
}
