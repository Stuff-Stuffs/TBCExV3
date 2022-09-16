package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.BattleParticipantTeamImpl;
import net.minecraft.util.Identifier;

public interface BattleParticipantTeam {
    BattleHandle getOwner();

    Identifier getIdentifier();

    static BattleParticipantTeam of(final BattleHandle handle, final Identifier id) {
        return new BattleParticipantTeamImpl(handle, id);
    }

    static Codec<BattleParticipantTeam> codec() {
        return BattleParticipantTeamImpl.CODEC;
    }
}
