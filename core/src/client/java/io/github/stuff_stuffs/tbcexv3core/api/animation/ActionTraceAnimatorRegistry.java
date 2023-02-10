package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.impl.animation.ActionTraceAnimatorRegistryImpl;
import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Consumer;

public interface ActionTraceAnimatorRegistry {
    ActionTraceAnimatorRegistry INSTANCE = new ActionTraceAnimatorRegistryImpl();

    void register(Identifier watch, ActionTraceAnimator animator);

    Optional<Consumer<AnimationManager<BattleAnimationContext>>> animate(TracerView.Node<ActionTrace> trace);
}
