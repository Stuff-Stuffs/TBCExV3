package io.github.stuff_stuffs.tbcexv3core.internal.common;

import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public interface AbstractServerBattleWorld extends ServerBattleWorld {
    boolean tryApplyDelayedComponents(UUID uuid, ServerWorld world);
}
