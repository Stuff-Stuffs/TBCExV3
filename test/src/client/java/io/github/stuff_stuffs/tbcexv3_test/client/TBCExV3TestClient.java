package io.github.stuff_stuffs.tbcexv3_test.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3_test.common.TBCExV3Test;
import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntity;
import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimator;
import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimatorRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ParticipantActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3model.api.animation.Animation;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.CuboidModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Skeleton;
import io.github.stuff_stuffs.tbcexv3model.api.scene.SceneRenderContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Consumer;

public class TBCExV3TestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ActionTraceAnimatorRegistry.INSTANCE.register(ParticipantActionTraces.BattleParticipantJoined.ANIMATION_DATA, trace -> {
            if (!(trace.value() instanceof ParticipantActionTraces.BattleParticipantJoined joined)) {
                return Optional.empty();
            }
            final Identifier id = TBCExV3Test.id(joined.handle().getUuid().toString().replace('-', '_'));
            return Optional.of(Animation.provideModel(id, (model, context) -> {
                final Identifier root = TBCExV3Test.id("root");
                final Skeleton skeleton = model.skeleton();
                skeleton.addBone(root, Optional.empty());
                final BlockPos center = context.context().state().getParticipantByHandle(joined.handle()).getBounds().center();
                skeleton.bone(root).setTransform(new Matrix4f().identity().translate(center.getX() + 0.5F, center.getY(), center.getZ() + 0.5F));
                model.get(root).addPart(TBCExV3Test.id("body"), new CuboidModelPart(0.5F, 1.0F, 0.5F, 0, 0, 256, 256));
            }));
        });

        ActionTraceAnimatorRegistry.INSTANCE.register(ParticipantActionTraces.BattleParticipantLeft.ANIMATION_DATA, trace -> {
            if (!(trace.value() instanceof ParticipantActionTraces.BattleParticipantJoined joined)) {
                return Optional.empty();
            }
            final Identifier id = TBCExV3Test.id(joined.handle().getUuid().toString().replace('-', '_'));
            return Optional.of(Animation.removeModel(id));
        });
        TBCExV3Test.MESSAGE_CONSUMER = t -> MinecraftClient.getInstance().player.sendMessage(t);
        WorldRenderEvents.AFTER_ENTITIES.register(new WorldRenderEvents.AfterEntities() {
            @Override
            public void afterEntities(final WorldRenderContext context) {
                final ClientPlayerEntity player = MinecraftClient.getInstance().player;
                final BattleHandle handle = ((TBCExPlayerEntity) player).tbcex$getCurrentBattle();
                if (handle != null) {
                    final BattleView view = ((BattleWorld) player.world).tryGetBattleView(handle);
                    if (view != null) {
                        final BattleParticipantTeam team = view.getState().getTeamByParticipant(BattleParticipantHandle.of(player.getUuid(), handle));
                        view.getState().getParticipantStream().forEach(new Consumer<BattleParticipantHandle>() {
                            @Override
                            public void accept(final BattleParticipantHandle handle) {
                                final BattleParticipantStateView other = view.getState().getParticipantByHandle(handle);
                                final BattleParticipantTeamRelation relation = view.getState().getTeamRelation(team, other.getTeam());
                                final Vec3d pos = context.camera().getPos();
                                final MatrixStack stack = context.matrixStack();
                                final BossBar.Color color = switch (relation) {
                                    case ALLIES -> BossBar.Color.GREEN;
                                    case NEUTRAL -> BossBar.Color.WHITE;
                                    case ENEMIES -> BossBar.Color.RED;
                                };
                                stack.push();
                                stack.translate(-pos.x, -pos.y, -pos.z);
                                final BlockPos center = view.toGlobal(other.getBounds().center());
                                final float r = relation == BattleParticipantTeamRelation.ENEMIES ? 1.0F : 0.0F;
                                final float g = relation == BattleParticipantTeamRelation.ALLIES ? 1.0F : 0.0F;
                                final VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());
                                other.getBounds().partStream().map(BattleParticipantBounds.Part::box).forEach(box -> WorldRenderer.drawBox(stack, consumer, view.toGlobal(box), r, g, 0.0F, 1));
                                stack.translate(center.getX(), center.getY(), center.getZ());
                                stack.translate(0, 2, 0);
                                stack.multiplyPositionMatrix(new Matrix4f().identity().billboardCylindrical(new Vector3f(), new Vector3f((float) (center.getX() - pos.x), (float) (center.getY() - pos.y), (float) (center.getZ() - pos.z)), new Vector3f(0, 1, 0)));
                                stack.translate(-(182 / 32F / 2F), 0, 0);
                                stack.scale(1 / 32f, 1 / 32f, 1 / 32f);
                                final double health = other.getHealth();
                                final double maxHealth = other.getStatMap().compute(CoreBattleParticipantStats.MAX_HEALTH, null);
                                final double percent = health / maxHealth;
                                final int width = (int) (percent * 183.0);
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                RenderSystem.setShaderTexture(0, new Identifier("textures/gui/bars.png"));
                                DrawableHelper.drawTexture(stack, 0, 0, 0, 0, color.ordinal() * 5 * 2 + 5, width, 5, 256, 256);
                                DrawableHelper.drawTexture(stack, 0, 0, 0, 0, 80 + (4 - 1) * 5 * 2 + 5, 182, 5, 256, 256);

                                stack.pop();
                            }
                        });
                    }
                }
            }
        });
        EntityRendererRegistry.register(TestEntities.TEST_ENTITY_TYPE, ctx -> new EntityRenderer<>(ctx) {
            @Override
            public Identifier getTexture(final TestEntity entity) {
                return new Identifier("nop", "nop");
            }
        });
    }
}
