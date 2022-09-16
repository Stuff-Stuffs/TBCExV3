package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import net.minecraft.util.Identifier;

public record BattleParticipantTeamImpl(BattleHandle owner, Identifier id) implements BattleParticipantTeam {
    public static final Codec<BattleParticipantTeam> CODEC = RecordCodecBuilder.create(instance -> instance.group(BattleHandle.codec().fieldOf("owner").forGetter(BattleParticipantTeam::getOwner), Identifier.CODEC.fieldOf("id").forGetter(BattleParticipantTeam::getIdentifier)).apply(instance, BattleParticipantTeamImpl::new));

    @Override
    public BattleHandle getOwner() {
        return owner;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
