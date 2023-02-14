package io.github.stuff_stuffs.tbcexv3core.internal.client.entity;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface TBCExClientPlayerExtensions {
    @Nullable BattleParticipantActionBuilder<?> tbcexcore$action$current();

    @Nullable Text tbcexcore$action$title();

    void tbcexcore$action$setCurrent(@Nullable BattleParticipantActionBuilder<?> builder, @Nullable Text title);
}
