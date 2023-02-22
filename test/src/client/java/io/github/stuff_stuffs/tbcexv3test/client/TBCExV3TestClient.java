package io.github.stuff_stuffs.tbcexv3test.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimatorRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContextFactory;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleParticipantAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleParticipantActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.AnimationDataBattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.CoreBattleEntityComponents;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3model.api.animation.FindModelAnimationEvent;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationKeyFrame;
import io.github.stuff_stuffs.tbcexv3model.api.animation.SceneAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.model.BoneAttachedModelParts;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.CuboidModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyKey;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpolation;
import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import io.github.stuff_stuffs.tbcexv3test.common.TBCExV3Test;
import io.github.stuff_stuffs.tbcexv3test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3test.common.entity.TestEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TBCExV3TestClient implements ClientModInitializer {
    private static final Identifier WALK_ANIMATION_ID = TBCExV3Model.id("walk");

    @Override
    public void onInitializeClient() {
        FindModelAnimationEvent.EVENT.register((id, context) -> {
             if (context.modelType() == ModelType.PARTICIPANT && id.equals(WALK_ANIMATION_ID) && context.data() instanceof BattleParticipantAnimationContext animationContext) {
                final ModelAnimationKeyFrame start = ModelAnimationKeyFrame.simple(context.time(), ModelPropertyContainer.builder().build(), Interpolation.linear());
                final BattleStateView state = animationContext.parent().state();
                final BlockPos center = state.getParticipantByHandle(animationContext.handle()).getBounds().center();
                final ModelAnimationKeyFrame end = ModelAnimationKeyFrame.simple(context.time() + 400, ModelPropertyContainer.builder().put(ModelPropertyKey.POSITION, new Interpable.InterpableVec3d(Vec3d.ofBottomCenter(center))).build(), Interpolation.linear());
                return Optional.of(ModelAnimation.of(List.of(start, end)));
            }
            return Optional.empty();
        });
        FindModelAnimationEvent.EVENT.register((id, context) -> {
            if (id.equals(FindModelAnimationEvent.SETUP_ANIMATION_ID) && context.data() instanceof BattleParticipantAnimationContext data) {
                final BattleParticipantBounds bounds = data.parent().state().getParticipantByHandle(data.handle()).getBounds();
                final ModelAnimationKeyFrame keyFrame = ModelAnimationKeyFrame.simple(context.time(), ModelPropertyContainer.builder().put(ModelPropertyKey.POSITION, new Interpable.InterpableVec3d(Vec3d.ofBottomCenter(bounds.center()))).build(), Interpolation.linear(), (model, function) -> {
                    final BoneAttachedModelParts root = function.apply(TBCExV3Model.id("root"));
                    final Iterator<BattleParticipantBounds.Part> parts = data.parent().state().getParticipantByHandle(data.handle()).getBounds().parts();
                    while (parts.hasNext()) {
                        final BattleParticipantBounds.Part part = parts.next();
                        final ModelPart modelPart = new CuboidModelPart((float) part.box().getXLength(), (float) part.box().getYLength(), (float) part.box().getZLength(), 16, 16, 16, 16);
                        root.addPart(part.id(), ModelPart.offset(modelPart, (float) (-part.box().getXLength() / 2.0), 0.0F, (float) (-part.box().getZLength() / 2.0)));
                    }
                });
                return Optional.of(ModelAnimation.of(List.of(keyFrame)));
            }
            return Optional.empty();
        });
        ActionTraceAnimatorRegistry.INSTANCE.register(BattleParticipantActionTraces.BattleParticipantJoined.ANIMATION_DATA, (trace, contextFactory, view) -> {
            if (!(trace.value() instanceof BattleParticipantActionTraces.BattleParticipantJoined joined)) {
                return Optional.empty();
            }
            final ModelType type = ModelType.getOrCreate(contextFactory.state().getParticipantByHandle(joined.handle()).getEntityComponent(CoreBattleEntityComponents.ANIMATION_DATA_BATTLE_ENTITY_COMPONENT_TYPE).map(AnimationDataBattleEntityComponent::getModelTypeId).orElse(ModelType.PARTICIPANT.getId()));
            final Optional<ModelAnimation> animation = view.findModelAnimation(FindModelAnimationEvent.SETUP_ANIMATION_ID, type, contextFactory.forChild(joined.handle(), trace), 0);
            return Optional.of(scene -> {
                final Identifier id = BattleAnimationContextFactory.toModelId(joined.handle());
                scene.addModel(id, AnimationScene.modelBuilder().addBone(TBCExV3Model.id("root"), Optional.empty()), type);
                if (animation.isPresent()) {
                    scene.setModelAnimation(id, ModelAnimation.SETUP, animation.get(), view.createTransition(Interpolation.linear(), 0, 0));
                }
            });
        });
        ActionTraceAnimatorRegistry.INSTANCE.register(BattleParticipantActionTraces.BattleParticipantLeft.ANIMATION_DATA, (trace, contextFactory, view) -> {
            if (!(trace.value() instanceof BattleParticipantActionTraces.BattleParticipantJoined left)) {
                return Optional.empty();
            }
            final Identifier id = BattleAnimationContextFactory.toModelId(left.handle());
            final Model model = view.getModel(id);
            if (model == null) {
                return Optional.empty();
            }
            final Optional<ModelAnimation> animation = view.findModelAnimation(FindModelAnimationEvent.SETUP_ANIMATION_ID, model.type(), contextFactory.forChild(left.handle(), trace), 0);
            return Optional.of(scene -> {
                final SceneAnimation.ReserveBuilder builder = SceneAnimation.reservationBuilder();
                if (animation.isPresent()) {
                    builder.add(id, ModelAnimation.ACTION, animation.get());
                }
                builder.apply(() -> scene.removeModel(id), scene, SceneAnimation.ACTIVE, Interpolation.linear(), 0);
            });
        });
        ActionTraceAnimatorRegistry.INSTANCE.register(BattleParticipantActionTraces.BattleParticipantMove.ANIMATION_DATA, (trace, contextFactory, view) -> {
            if (!(trace.value() instanceof BattleParticipantActionTraces.BattleParticipantMove moved)) {
                return Optional.empty();
            }
            final Identifier modelId = BattleAnimationContextFactory.toModelId(moved.handle());
            final Model model = view.getModel(modelId);
            if (model == null) {
                return Optional.empty();
            }
            final Optional<ModelAnimation> animation = view.findModelAnimation(WALK_ANIMATION_ID, model.type(), contextFactory.forChild(moved.handle(), trace), 0);
            return animation.map(modelAnimation -> scene -> scene.setModelAnimation(modelId, ModelAnimation.ACTION, modelAnimation, scene.createTransition(Interpolation.linear(), 0, 10)));
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
    }
}
