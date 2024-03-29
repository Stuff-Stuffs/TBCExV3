package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimatorRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContextFactory;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleParticipantAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleSceneAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.impl.ClientBattleImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

//TODO timeout
public class ClientBattleWorldContainer implements AutoCloseable {
    private final Map<BattleHandle, ClientBattleImpl> battles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> entityBattles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> activeEntityBattles = new Object2ReferenceOpenHashMap<>();
    private final Set<BattleHandle> battleUpdateRequestsToSend = new ObjectOpenHashSet<>();
    private final Set<UUID> entityBattleRequestsToSend = new ObjectOpenHashSet<>();
    private final Map<BattleHandle, AnimationState> animationState = new Object2ReferenceOpenHashMap<>();
    private final World world;

    public ClientBattleWorldContainer(final World world) {
        this.world = world;
    }

    public @Nullable AnimationScene<BattleSceneAnimationContext, BattleParticipantAnimationContext> getScene(final BattleHandle handle) {
        final AnimationState state = animationState.get(handle);
        if (state != null) {
            return state.scene;
        }
        return null;
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
        for (ClientBattleImpl value : battles.values()) {
            value.tick();
        }
    }

    public void update(final BattleUpdate update) {
        if (battles.containsKey(update.handle())) {
            battles.get(update.handle()).update(update);
        } else if (update.offset() == 0 && update.initialData().isPresent()) {
            final BattleUpdate.InitialData data = update.initialData().get();
            final BattleHandle handle = update.handle();
            final ClientBattleImpl battle = new ClientBattleImpl(handle, BattleStateMode.CLIENT, data.initialEnvironment(), data.origin(), (trace) -> animationState.computeIfAbsent(handle, i -> new AnimationState(handle, this, world)).add(trace));
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

    @Override
    public void close() {
        animationState.clear();
    }

    private static final class AnimationState {
        private final AnimationScene<BattleSceneAnimationContext, BattleParticipantAnimationContext> scene;
        private final BattleHandle handle;
        private final ClientBattleWorldContainer container;
        private final World world;

        private AnimationState(final BattleHandle handle, final ClientBattleWorldContainer container, final World world) {
            this.handle = handle;
            this.container = container;
            this.world = world;
            scene = AnimationScene.create(world.getTime());
        }

        public void add(final TracerView.Node<ActionTrace> trace) {
            final BattleView battle = container.getBattle(handle);
            if (battle == null) {
                return;
            }
            ActionTraceAnimatorRegistry.INSTANCE.animate(trace, BattleAnimationContextFactory.create(battle), scene).ifPresent(animation -> animation.accept(scene));
        }

        public void render(final WorldRenderContext context, final BattleView battle) {
            if (battle.getState().getPhase() == BattleStatePhase.FIGHT) {
                final double time = context.tickDelta() + context.world().getTime();
                final MatrixStack matrices = context.matrixStack();
                matrices.push();
                final Vec3d pos = context.camera().getPos();
                matrices.translate(-pos.x, -pos.y, -pos.z);
                final Vec3d v = battle.toGlobal(Vec3d.ZERO);
                matrices.translate(v.x, v.y, v.z);
                scene.render(matrices, context.consumers(), world, time);
                matrices.pop();
            }
        }
    }
}
