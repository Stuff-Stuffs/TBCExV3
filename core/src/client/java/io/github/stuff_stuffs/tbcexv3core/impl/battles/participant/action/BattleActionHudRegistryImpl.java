package io.github.stuff_stuffs.tbcexv3core.impl.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleActionHudRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionBattleParticipantTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.CoreBattleActionTargetTypes;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.internal.client.TBCExV3CoreClient;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class BattleActionHudRegistryImpl implements BattleActionHudRegistry {
    private final Map<Identifier, BiFunction<MouseLocker, Consumer<Runnable>, BiFunction<Sizing, Sizing, ParentComponent>>> rendererFactories = new Object2ReferenceOpenHashMap<>();
    public static final Identifier DEFAULT_RENDERER_ID = TBCExV3Core.createId("default_action_renderer");
    public static final BiFunction<MouseLocker, Consumer<Runnable>, BiFunction<Sizing, Sizing, ParentComponent>> DEFAULT_FACTORY = BattleActionHudRegistry.basic((selected, builder, battle, cleanup) -> {
        final Iterator<? extends BattleParticipantActionTargetType<?>> types = builder.types();
        while (types.hasNext()) {
            final BattleParticipantActionTargetType<?> type = types.next();
            final BattleParticipantActionBuilder.TargetIterator<?> iterator = builder.targets(type);
            while (iterator.hasNext()) {
                final BattleParticipantActionTarget next = iterator.next();
                if (type == CoreBattleActionTargetTypes.BATTLE_PARTICIPANT_TARGET_TYPE) {
                    final BattleParticipantHandle handle = ((BattleParticipantActionBattleParticipantTarget) next).handle();
                    final BattleParticipantBounds bounds = battle.getState().getParticipantByHandle(handle).getBounds();
                    final BattleParticipantBounds moved = BattleParticipantBounds.move(battle.toGlobal(bounds.center()), bounds);
                    TBCExV3CoreClient.defer(context -> {
                        final VertexConsumerProvider consumers = context.consumers();
                        final MatrixStack matrices = context.matrixStack();
                        matrices.push();
                        final Camera c = context.camera();
                        matrices.translate(-c.getPos().x, -c.getPos().y, -c.getPos().z);
                        moved.parts().forEachRemaining(part -> WorldRenderer.drawBox(matrices, consumers.getBuffer(RenderLayer.getLines()), part.box(), 0, 1, 0, 0.75F));
                        matrices.pop();
                    });
                }
            }
        }
    });

    @Override
    public void register(final Identifier id, final BiFunction<MouseLocker, Consumer<Runnable>, BiFunction<Sizing, Sizing, ParentComponent>> renderer) {
        if (rendererFactories.put(id, renderer) != null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public BiFunction<Sizing, Sizing, ParentComponent> get(final Identifier id, final MouseLocker locker, Consumer<Runnable> cleanup) {
        final BiFunction<MouseLocker, Consumer<Runnable>, BiFunction<Sizing, Sizing, ParentComponent>> factory = rendererFactories.get(id);
        return Objects.requireNonNullElse(factory, DEFAULT_FACTORY).apply(locker, cleanup);
    }
}
