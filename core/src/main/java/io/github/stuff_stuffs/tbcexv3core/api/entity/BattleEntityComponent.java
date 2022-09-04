package io.github.stuff_stuffs.tbcexv3core.api.entity;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import net.minecraft.entity.Entity;

public interface BattleEntityComponent {
    void apply(Entity entity, BattleView view);
}
