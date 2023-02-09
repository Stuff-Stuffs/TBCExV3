package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3core.impl.animation.ActionTraceAnimatorRegistryImpl;
import io.github.stuff_stuffs.tbcexv3model.api.animation.Animation;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface ActionTraceAnimatorRegistry {
    ActionTraceAnimatorRegistry INSTANCE = new ActionTraceAnimatorRegistryImpl();

    void register(Identifier watch, ActionTraceAnimator animator);

    Optional<Animation<BattleAnimationContext>> animate(TracerView.Node<ActionTrace> trace);
}
