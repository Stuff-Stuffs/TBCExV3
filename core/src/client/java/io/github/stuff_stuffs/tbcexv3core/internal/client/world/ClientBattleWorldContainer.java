package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.impl.ClientBattleImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.TBCExV3CoreClient;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import io.github.stuff_stuffs.tbcexv3model.api.animation.Animation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.scene.SceneRenderContext;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

//TODO timeout
public class ClientBattleWorldContainer {
    private final Map<BattleHandle, ClientBattleImpl> battles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> entityBattles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> activeEntityBattles = new Object2ReferenceOpenHashMap<>();
    private final Set<BattleHandle> battleUpdateRequestsToSend = new ObjectOpenHashSet<>();
    private final Set<UUID> entityBattleRequestsToSend = new ObjectOpenHashSet<>();
    private final Map<BattleHandle, AnimationState> animationState = new Object2ReferenceOpenHashMap<>();

    public ClientBattleWorldContainer() {
    }

    public @Nullable BattleView getBattle(final BattleHandle handle) {
        battleUpdateRequestsToSend.add(handle);
        return battles.get(handle);
    }

    public void tick() {
        if (!battleUpdateRequestsToSend.isEmpty()) {
            final List<BattleUpdateRequest> updateRequests = new ArrayList<>(battleUpdateRequestsToSend.size());
            for (final BattleHandle handle : battleUpdateRequestsToSend) {
                if (battles.containsKey(handle)) {
                    updateRequests.add(battles.get(handle).createUpdateRequest());
                } else {
                    updateRequests.add(new BattleUpdateRequest(handle, -1));
                }
            }
            BattleUpdateRequestSender.send(updateRequests);
            battleUpdateRequestsToSend.clear();
        }
        if (!entityBattleRequestsToSend.isEmpty()) {
            EntityBattlesUpdateRequestSender.send(entityBattleRequestsToSend);
            entityBattleRequestsToSend.clear();
        }
    }

    public void update(final BattleUpdate update) {
        if (battles.containsKey(update.handle())) {
            battles.get(update.handle()).update(update);
        } else if (update.offset() == 0 && update.initialData().isPresent()) {
            final BattleUpdate.InitialData data = update.initialData().get();
            final BattleHandle handle = update.handle();
            final ClientBattleImpl battle = new ClientBattleImpl(handle, BattleStateMode.CLIENT, data.initialEnvironment(), data.origin(), animation -> animationState.computeIfAbsent(handle, i -> new AnimationState()).add(animation));
            battle.update(update);
            battles.put(handle, battle);
        }
    }

    public void update(final UUID entityId, final List<BattleParticipantHandle> battles, final List<BattleParticipantHandle> inactiveBattles) {
        activeEntityBattles.put(entityId, battles);
        entityBattles.put(entityId, inactiveBattles);
    }

    public List<BattleParticipantHandle> getEntityBattles(final UUID entityId, final TriState active) {
        entityBattleRequestsToSend.add(entityId);
        if (active == TriState.TRUE) {
            return activeEntityBattles.getOrDefault(entityId, List.of());
        } else if (active == TriState.FALSE) {
            return entityBattles.getOrDefault(entityId, List.of());
        } else {
            return Stream.concat(activeEntityBattles.getOrDefault(entityId, List.of()).stream(), entityBattles.getOrDefault(entityId, List.of()).stream()).toList();
        }
    }

    public void render(final WorldRenderContext context) {
        for (final Map.Entry<BattleHandle, AnimationState> entry : animationState.entrySet()) {
            entry.getValue().render(context, battles.get(entry.getKey()));
        }
    }

    private static final class AnimationState {
        private final AnimationManager<BattleAnimationContext> manager = AnimationManager.create();
        private double lastTime = Double.NEGATIVE_INFINITY;

        public void add(final Animation<BattleAnimationContext> animation) {
            final OptionalDouble submission = manager.submit(animation, lastTime);
            if (submission.isPresent()) {
                lastTime = submission.getAsDouble();
            } else {
                TBCExV3CoreClient.LOGGER.error("Error while trying to schedule animation");
            }
        }

        public void render(final WorldRenderContext context, final BattleView battle) {
            final double time = context.tickDelta() + context.world().getTime();
            final MatrixStack matrices = context.matrixStack();
            matrices.push();
            final Vec3d pos = battle.toLocal(context.camera().getPos());
            matrices.translate(-pos.x, -pos.y, -pos.z);
            final SceneRenderContext<BattleAnimationContext> sceneRenderContext = SceneRenderContext.create(matrices, context.consumers(), pos, context.camera().getRotation(), manager.scene(), battle::getState);
            manager.update(time);
            manager.forEach(animation -> animation.render(sceneRenderContext, time));
            manager.scene().render(matrices, context.consumers(), pos, context.camera().getRotation());
            matrices.pop();
        }
    }
}
