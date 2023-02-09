package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.util.Interval;

public record AnimationDependency(AnimationResource resource, Interval interval, boolean exclusive, boolean required) {
}
