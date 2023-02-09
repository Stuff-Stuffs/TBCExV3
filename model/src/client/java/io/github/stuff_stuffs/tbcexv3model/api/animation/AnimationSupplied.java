package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.util.Interval;

public record AnimationSupplied(AnimationResource resource, Interval interval, boolean soft) {
}
