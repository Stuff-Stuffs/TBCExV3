package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.ClientBattleImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

//TODO timeout
public class ClientBattleWorldContainer implements AutoCloseable {
    private final Map<BattleHandle, ClientBattleImpl> battles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> entityBattles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> activeEntityBattles = new Object2ReferenceOpenHashMap<>();
    private final Set<BattleHandle> battleUpdateRequestsToSend = new ObjectOpenHashSet<>();
    private final Set<UUID> entityBattleRequestsToSend = new ObjectOpenHashSet<>();
    private final Map<BattleHandle, AnimationState> animationState = new Object2ReferenceOpenHashMap<>();

    public ClientBattleWorldContainer() {
    }

    public @Nullable AnimationScene<BattleAnimationContext> getScene(final BattleHandle handle) {
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
    }

    public void update(final BattleUpdate update) {
        if (battles.containsKey(update.handle())) {
            battles.get(update.handle()).update(update);
        } else if (update.offset() == 0 && update.initialData().isPresent()) {
            final BattleUpdate.InitialData data = update.initialData().get();
            final BattleHandle handle = update.handle();
            final ClientBattleImpl battle = new ClientBattleImpl(handle, BattleStateMode.CLIENT, data.initialEnvironment(), data.origin(), (animation, state) -> animationState.computeIfAbsent(handle, i -> new AnimationState()).add(animation, state));
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
        for (final AnimationState value : animationState.values()) {
            try {
                value.scene.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        animationState.clear();
    }

    private static final class AnimationState {
        private final AnimationScene<BattleAnimationContext> scene = AnimationScene.create();

        public void add(final BiConsumer<AnimationScene<BattleAnimationContext>, BattleStateView> consumer, final BattleStateView state) {
            consumer.accept(scene, state);
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
                scene.update(time, new BattleAnimationContext() {
                    @Override
                    public BattleStateView state() {
                        return battle.getState();
                    }

                    @Override
                    public BlockPos toLocal(final BlockPos global) {
                        return battle.toLocal(global);
                    }

                    @Override
                    public BlockPos toGlobal(final BlockPos local) {
                        return battle.toGlobal(local);
                    }

                    @Override
                    public Vec3d toLocal(final Vec3d global) {
                        return battle.toLocal(global);
                    }

                    @Override
                    public Vec3d toGlobal(final Vec3d local) {
                        return battle.toGlobal(local);
                    }
                });
                scene.render(matrices, context.consumers(), pos, context.camera().getRotation(), time);
                matrices.pop();
            }
        }
    }
}
