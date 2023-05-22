package io.github.stuff_stuffs.tbcexv3util.api.util.event.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface SimpleEventInfo {
    EventType type();

    String defaultValue() default "";

    String combiner() default "";

    String compareBy() default "";

    String comparator() default "";
}
