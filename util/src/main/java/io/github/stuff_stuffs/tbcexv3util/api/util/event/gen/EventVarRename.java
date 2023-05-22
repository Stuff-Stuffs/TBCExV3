package io.github.stuff_stuffs.tbcexv3util.api.util.event.gen;

import java.lang.annotation.*;

@Repeatable(EventVarRenames.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface EventVarRename {
    String name();

    EventPhase phase();
}
