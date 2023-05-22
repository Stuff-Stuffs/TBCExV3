package io.github.stuff_stuffs.tbcexv3util.api.util.event;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Function;

public record EventKey<View, Mut>(String id, Class<Mut> mutClass, Class<View> viewClass,
                                  InvokerFactory<Mut> invokerFactory,
                                  Function<View, Mut> converter,
                                  @Nullable Comparator<Mut> comparator) {
}
