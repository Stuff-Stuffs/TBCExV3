package io.github.stuff_stuffs.tbcexv3util.api.util.event.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PassthroughEventInfo {
    String passthrough();

    EventType type();

    String combiner();

    String compareBy() default "";

    String comparator() default "";
}
