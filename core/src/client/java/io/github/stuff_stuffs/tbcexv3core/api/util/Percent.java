package io.github.stuff_stuffs.tbcexv3core.api.util;

import io.wispforest.owo.ui.core.Animatable;

public record Percent(double percent) implements Animatable<Percent> {
    public Percent {
        if (percent < 0 || percent > 1) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Percent interpolate(final Percent next, final float delta) {
        return new Percent(percent * (1 - delta) + next.percent * delta);
    }
}
