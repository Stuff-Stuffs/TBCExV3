package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.model.BoneAttachedModelParts;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpolation;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ModelAnimationKeyFrame {
    double start();

    ModelPropertyContainer properties();

    ModelPropertyContainer interpolatedProperties(double time, double duration, ModelPropertyContainer old, ModelPropertyContainer nextKeyFrame);

    void addOrRemoveParts(Model model, Function<Identifier, BoneAttachedModelParts> partsGetter);

    static ModelAnimationKeyFrame simple(final double time, final ModelPropertyContainer properties, final Interpolation interpolation) {
        return simple(time, properties, interpolation, (model, function) -> {
        });
    }

    static ModelAnimationKeyFrame simple(final double time, final ModelPropertyContainer properties, final Interpolation interpolation, final BiConsumer<Model, Function<Identifier, BoneAttachedModelParts>> partModifier) {
        return new ModelAnimationKeyFrame() {
            @Override
            public double start() {
                return time;
            }

            @Override
            public ModelPropertyContainer properties() {
                return properties;
            }

            @Override
            public ModelPropertyContainer interpolatedProperties(final double time, final double duration, final ModelPropertyContainer old, final ModelPropertyContainer nextKeyFrame) {
                final double relative = (time - start()) / duration;
                return ModelPropertyContainer.blend(ModelPropertyContainer.fallback(properties, old), nextKeyFrame, interpolation.remapAlpha(relative));
            }

            @Override
            public void addOrRemoveParts(final Model model, final Function<Identifier, BoneAttachedModelParts> partsGetter) {
                partModifier.accept(model, partsGetter);
            }
        };
    }
}
