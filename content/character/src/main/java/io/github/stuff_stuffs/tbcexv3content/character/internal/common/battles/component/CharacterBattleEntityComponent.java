package io.github.stuff_stuffs.tbcexv3content.character.internal.common.battles.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterDataContainer;
import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterPlayer;
import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterStatContainer;
import io.github.stuff_stuffs.tbcexv3content.character.api.LevelContainer;
import io.github.stuff_stuffs.tbcexv3content.character.api.job.CharacterJobData;
import io.github.stuff_stuffs.tbcexv3content.character.api.race.CharacterRacialData;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStat;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatMap;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifier;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifierPhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3util.api.util.OperationChainDisplayBuilder;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.UUID;
import java.util.function.BinaryOperator;

public class CharacterBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<CharacterBattleEntityComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(component -> component.uuid),
                    CharacterRacialData.CODEC.fieldOf("racialData").forGetter(component -> component.racialData),
                    CharacterJobData.CODEC.fieldOf("jobData").forGetter(component -> component.jobData)
            ).apply(instance, CharacterBattleEntityComponent::new)
    );
    public static BinaryOperator<CharacterBattleEntityComponent> COMBINER = (first, second) -> {
        throw new UnsupportedOperationException("Tried combining character battle entity components!");
    };
    private final UUID uuid;
    private final CharacterRacialData racialData;
    private final CharacterJobData jobData;

    public CharacterBattleEntityComponent(UUID uuid, final CharacterRacialData data, final CharacterJobData jobData) {
        this.uuid = uuid;
        racialData = data;
        this.jobData = jobData;
    }

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {
        racialData.racialBattleEffect().ifPresent(effect -> state.addEffect(effect, tracer));
        jobData.jobBattleEffect(racialData).ifPresent(effect -> state.addEffect(effect, tracer));
        final CharacterStatContainer racialStats = racialData.stats();
        final CharacterStatContainer jobStats = jobData.stats(racialData);
        final BattleParticipantStatMap statMap = state.getStatMap();
        for (final BattleParticipantStat stat : BattleParticipantStat.REGISTRY) {
            final int racialLevel = racialStats.getLevel(stat);
            if (racialLevel != 0) {
                statMap.addModifier(stat, new BattleParticipantStatModifier() {
                    @Override
                    public BattleParticipantStatModifierPhase getPhase() {
                        return BattleParticipantStatModifierPhase.ADD;
                    }

                    @Override
                    public double modify(final double value, @Nullable final OperationChainDisplayBuilder displayBuilder) {
                        if (displayBuilder != null) {
                            displayBuilder.push(OperationChainDisplayBuilder.Operation.ADD, OptionalDouble.of(racialLevel), Text.of("Racial Bonus"));
                        }
                        return value + racialLevel;
                    }
                }, tracer);
            }
            final int jobLevel = jobStats.getLevel(stat);
            if (jobLevel != 0) {
                statMap.addModifier(stat, new BattleParticipantStatModifier() {
                    @Override
                    public BattleParticipantStatModifierPhase getPhase() {
                        return BattleParticipantStatModifierPhase.ADD;
                    }

                    @Override
                    public double modify(final double value, @Nullable final OperationChainDisplayBuilder displayBuilder) {
                        if (displayBuilder != null) {
                            displayBuilder.push(OperationChainDisplayBuilder.Operation.ADD, OptionalDouble.of(jobLevel), Text.of("Class Bonus"));
                        }
                        return value + jobLevel;
                    }
                }, tracer);
            }
        }
    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {
        Entity entity = world.getEntity(uuid);
        if(entity==null) {
            ((ServerBattleWorld) world).pushDelayedPlayerComponent(uuid, view.getState().getHandle(), this);
        } else {
            if(entity instanceof CharacterPlayer player) {
                final CharacterDataContainer container = player.tbcex$getCharacterData();
                final LevelContainer levelContainer = container.racialData().levelContainer();
            }
        }
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CharacterBattleEntityComponents.CHARACTER_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
