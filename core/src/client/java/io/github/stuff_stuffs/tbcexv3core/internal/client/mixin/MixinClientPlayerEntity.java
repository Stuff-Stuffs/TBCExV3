package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.internal.client.entity.TBCExClientPlayerExtensions;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity implements TBCExClientPlayerExtensions {
    @Unique
    private @Nullable BattleParticipantActionBuilder<?> currentBuilder = null;
    @Unique
    private @Nullable Text currentTitle;

    @Override
    public @Nullable BattleParticipantActionBuilder<?> tbcexcore$action$current() {
        return currentBuilder;
    }

    @Override
    public @Nullable Text tbcexcore$action$title() {
        return currentTitle;
    }

    @Override
    public void tbcexcore$action$setCurrent(@Nullable final BattleParticipantActionBuilder<?> builder, @Nullable final Text title) {
        if (builder == null ^ title == null) {
            throw new RuntimeException("Either both or neigther must be null!");
        }
        currentBuilder = builder;
        currentTitle = title;
    }
}
