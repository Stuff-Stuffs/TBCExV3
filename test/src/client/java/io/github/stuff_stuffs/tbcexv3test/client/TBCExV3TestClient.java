package io.github.stuff_stuffs.tbcexv3test.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimatorRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleParticipantActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleActionHudRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionBlockPosTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.internal.client.TBCExV3CoreClient;
import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationFactory;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.api.model.RequiredAnimations;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.CuboidModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationSceneView;
import io.github.stuff_stuffs.tbcexv3test.common.TBCExV3Test;
import io.github.stuff_stuffs.tbcexv3test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3test.common.entity.TestEntity;
import io.github.stuff_stuffs.tbcexv3util.api.util.Pathfinder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class TBCExV3TestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ActionTraceAnimatorRegistry.INSTANCE.register(BattleParticipantActionTraces.BattleParticipantJoined.ANIMATION_DATA, trace -> {
            if (!(trace.value() instanceof BattleParticipantActionTraces.BattleParticipantJoined joined)) {
                return Optional.empty();
            }
            final Identifier id = BattleAnimationContext.toModelId(joined.handle());
            return Optional.of((scene, state) -> scene.manager().enqueueAnimation((animationScene, time, startTime) -> {
                final ModelAnimationFactory<BattleAnimationContext> factory = context -> (model, animationContext, t, st) -> true;
                animationScene.addModel(id, ModelType.PARTICIPANT, Map.of(
                        RequiredAnimations.Participant.IDLE, factory,
                        RequiredAnimations.Participant.DAMAGE_TAKEN, factory,
                        RequiredAnimations.Participant.DEATH, factory,
                        RequiredAnimations.Participant.SPAWN, factory
                ));
                final BattleParticipantStateView participant = state.getParticipantByHandle(joined.handle());
                final Model model = animationScene.getModel(id);
                model.skeleton().addBone(TBCExV3Test.id("root"), Optional.empty());
                final BlockPos center = participant.getBounds().center();
                model.skeleton().bone(TBCExV3Test.id("root")).setTransform(new Matrix4f().translation(center.getX() + 0.5F, center.getY(), center.getZ() + 0.5F));
                final Iterator<BattleParticipantBounds.Part> parts = participant.getBounds().parts();
                while (parts.hasNext()) {
                    final BattleParticipantBounds.Part part = parts.next();
                    final CuboidModelPart modelPart = new CuboidModelPart((float) part.box().getXLength(), (float) part.box().getYLength(), (float) part.box().getZLength(), 0, 0, 256, 256);
                    model.get(TBCExV3Test.id("root")).addPart(part.id(), ModelPart.offset(modelPart, (float) part.box().minX - (participant.getBounds().center().getX() + 0.5F), (float) part.box().minY - participant.getBounds().center().getY(), (float) part.box().minZ - (participant.getBounds().center().getZ() + 0.5F)));
                }
                return true;
            }));
        });
        ActionTraceAnimatorRegistry.INSTANCE.register(BattleParticipantActionTraces.BattleParticipantLeft.ANIMATION_DATA, trace -> {
            if (!(trace.value() instanceof BattleParticipantActionTraces.BattleParticipantJoined left)) {
                return Optional.empty();
            }
            final Identifier id = BattleAnimationContext.toModelId(left.handle());
            return Optional.of((manager, state) -> manager.removeModel(id));
        });
        ActionTraceAnimatorRegistry.INSTANCE.register(BattleParticipantActionTraces.BattleParticipantMove.ANIMATION_DATA, trace -> {
            if (!(trace.value() instanceof BattleParticipantActionTraces.BattleParticipantMove moved)) {
                return Optional.empty();
            }
            final Identifier id = BattleAnimationContext.toModelId(moved.handle());
            return Optional.of((manager, state) -> manager.manager().enqueueAnimation((scene, time, startTime) -> {
                final double length = 10;
                final double delta = MathHelper.clamp((time - startTime) / length, 0, 1);
                final double x = MathHelper.lerp(delta, moved.start().getX() + 0.5, moved.end().getX() + 0.5);
                final double y = MathHelper.lerp(delta, moved.start().getY(), moved.end().getY());
                final double z = MathHelper.lerp(delta, moved.start().getZ() + 0.5, moved.end().getZ() + 0.5);
                scene.getModel(id).skeleton().bone(TBCExV3Test.id("root")).setTransform(new Matrix4f().translation((float) x, (float) y, (float) z));
                return time > startTime + length;
            }));
        });
        TBCExV3Test.MESSAGE_CONSUMER = t -> MinecraftClient.getInstance().player.sendMessage(t);
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
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
                            stack.translate(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
                            stack.translate(0, 2, 0);
                            stack.multiplyPositionMatrix(new Matrix4f().identity().billboardCylindrical(new Vector3f(), new Vector3f((float) (center.getX() + 0.5 - pos.x), (float) (center.getY() - pos.y), (float) (center.getZ() + 0.5 - pos.z)), new Vector3f(0, 1, 0)));
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
                            RenderSystem.disableBlend();
                            stack.pop();
                        }
                    });
                }
            }
        });
        EntityRendererRegistry.register(TestEntities.TEST_ENTITY_TYPE, ctx -> new EntityRenderer<>(ctx) {
            @Override
            public Identifier getTexture(final TestEntity entity) {
                return new Identifier("nop", "nop");
            }
        });
        BattleActionHudRegistry.INSTANCE.register(TBCExV3Test.id("walk"), BattleActionHudRegistry.basic(new BattleActionHudRegistry.TargetRenderer() {
            private @Nullable AnimationSceneView.BufferToken token = null;

            private void renderPositions(final BattleStateView state, final Pathfinder.PathTree<?> tree, final Consumer<Runnable> cleanup) {
                final AnimationScene<BattleAnimationContext> scene = ((ClientBattleWorld) MinecraftClient.getInstance().world).tbcex$getScene(state.getHandle());
                if (scene == null) {
                    return;
                }
                final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                final MatrixStack stack = new MatrixStack();
                final Direction[] directions = Direction.values();
                for (final BlockPos position : tree.endPositions()) {
                    stack.push();
                    stack.translate(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
                    final Matrix4f matrix = stack.peek().getPositionMatrix();
                    final float r = 1 / 4.0F;
                    buffer.vertex(matrix, -r, 0.001F, -r).color(0x7F00FF00).next();
                    buffer.vertex(matrix, -r, 0.001F, r).color(0x7F00FF00).next();
                    buffer.vertex(matrix, r, 0.001F, r).color(0x7F00FF00).next();
                    buffer.vertex(matrix, r, 0.001F, -r).color(0x7F00FF00).next();
                    stack.pop();
                }
                token = scene.upload(buffer.end());
                cleanup.accept(() -> {
                    if (token != null && token.isValid()) {
                        token.destroy();
                        token = null;
                    }
                });
            }

            @Override
            public void render(@Nullable final BattleParticipantActionTarget selected, final BattleParticipantActionBuilder<?> builder, final BattleView state, final Consumer<Runnable> cleanup) {
                if (builder.renderData() instanceof Pathfinder.PathTree<?> p) {
                    if (token == null) {
                        renderPositions(state.getState(), p, cleanup);
                    }
                    if (token != null && token.isValid()) {
                        TBCExV3CoreClient.defer(context -> {
                            if (token == null || !token.isValid()) {
                                return;
                            }
                            final VertexBuffer buffer = token.getBuffer();
                            buffer.bind();
                            final MatrixStack stack = context.matrixStack();
                            stack.push();
                            final Vec3d pos = state.toLocal(context.camera().getPos());
                            stack.translate(-pos.x, -pos.y, -pos.z);
                            RenderSystem.enableBlend();
                            RenderSystem.enableDepthTest();
                            RenderSystem.defaultBlendFunc();
                            buffer.draw(stack.peek().getPositionMatrix(), context.projectionMatrix(), GameRenderer.getRenderTypeLinesProgram());
                            stack.pop();
                        });
                    }
                    if (selected instanceof BattleParticipantActionBlockPosTarget target) {
                        TBCExV3CoreClient.defer(context -> {
                            final Pathfinder.PathTree<List<BlockPos>> pathTree = (Pathfinder.PathTree<List<BlockPos>>) p;
                            final List<BlockPos> path = pathTree.getPath(target.pos());
                            if (path == null) {
                                return;
                            }
                            final VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.LINES);
                            final MatrixStack matrices = context.matrixStack();
                            matrices.push();
                            final Vec3d pos = state.toLocal(context.camera().getPos());
                            matrices.translate(-pos.x, -pos.y, -pos.z);
                            final Matrix4f matrix = matrices.peek().getPositionMatrix();
                            for (int i = 0; i < path.size() - 1; i++) {
                                final Vec3d start = Vec3d.ofCenter(path.get(i));
                                final Vec3d end = Vec3d.ofCenter(path.get(i + 1));
                                final float dX = (float) (end.x - start.x);
                                final float dY = (float) (end.y - start.y);
                                final float dZ = (float) (end.z - start.z);
                                final float invSq = MathHelper.fastInverseSqrt(dX * dX + dY * dY + dZ * dZ);
                                buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z).color(0xFF00FF00).normal(dX * invSq, dY * invSq, dZ * invSq).next();
                                buffer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z).color(0xFF00FF00).normal(dX * invSq, dY * invSq, dZ * invSq).next();
                            }
                            matrices.pop();
                        });

                    }
                }
            }
        }));
    }
}
