package io.github.stuff_stuffs.tbcexv3util.api.util.event.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface EventVarRenames {
    EventVarRename[] value();
}
