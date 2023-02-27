package io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event;

import io.github.stuff_stuffs.tbcexv3core.api.util.EventGenerationUtil;
import io.github.stuff_stuffs.tbcexv3core.impl.event.EventMapImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.function.Function;

@ApiStatus.NonExtendable
public interface EventMap extends EventMapView {
    <ViewType, EventType> Event<ViewType, EventType> getEvent(EventKey<ViewType, EventType> key);

    interface Builder {
        boolean contains(EventKey<?, ?> key);

        default <ViewType, EventType> Builder unsortedViewLike(final EventKey<ViewType, EventType> key) {
            return unsorted(key, EventGenerationUtil.generateVoidConverter(key.getMutClass(), key.getViewClass()), EventGenerationUtil.generateVoidInvoker(key.getMutClass()));
        }

        default <ViewType, EventType> Builder unsortedBooleanAnd(final EventKey<ViewType, EventType> key) {
            return unsortedAnd(key, EventGenerationUtil.generateBooleanAndInvoker(key.getMutClass()));
        }

        default <ViewType, EventType> Builder unsortedBooleanOr(final EventKey<ViewType, EventType> key) {
            return unsortedOr(key, EventGenerationUtil.generateBooleanAndInvoker(key.getMutClass()));
        }

        default <ViewType, EventType> Builder unsortedAnd(final EventKey<ViewType, EventType> key, final InvokerFactory<EventType> invokerFactory) {
            return unsorted(key, EventGenerationUtil.generateBooleanConverter(key.getMutClass(), key.getViewClass(), true), invokerFactory);
        }

        default <ViewType, EventType> Builder unsortedOr(final EventKey<ViewType, EventType> key, final InvokerFactory<EventType> invokerFactory) {
            return unsorted(key, EventGenerationUtil.generateBooleanConverter(key.getMutClass(), key.getViewClass(), false), invokerFactory);
        }

        default <ViewType, EventType> Builder unsortedDoublePassthrough(final EventKey<ViewType, EventType> key, final int returnIndex) {
            return unsorted(key, EventGenerationUtil.generateDoubleConverter(key.getMutClass(), key.getViewClass(), i -> i, returnIndex), EventGenerationUtil.generateDoubleReuseInvoker(key.getMutClass(), returnIndex));
        }

        <ViewType, EventType> Builder unsorted(EventKey<ViewType, EventType> key, Function<ViewType, EventType> converter, InvokerFactory<EventType> invokerFactory);

        <ViewType, EventType> Builder sorted(EventKey<ViewType, EventType> key, Function<ViewType, EventType> converter, InvokerFactory<EventType> invokerFactory, Comparator<? super EventType> comparator);

        EventMap build();

        static Builder create() {
            return new EventMapImpl.BuilderImpl();
        }
    }
}
