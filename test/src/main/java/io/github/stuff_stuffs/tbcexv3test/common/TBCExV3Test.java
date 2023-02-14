package io.github.stuff_stuffs.tbcexv3test.common;

import com.mojang.brigadier.CommandDispatcher;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleParticipantMoveBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantDefaultActionGatherEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionBlockPosTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.CoreBattleActionTargetTypes;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattlePlayerComponentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.InventoryBattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3test.common.action.TestBattleActions;
import io.github.stuff_stuffs.tbcexv3test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3test.common.entity.TestEntityComponent;
import io.github.stuff_stuffs.tbcexv3test.common.item.TestBattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3test.common.item.TestBattleParticipantItemTypes;
import io.github.stuff_stuffs.tbcexv3util.api.util.Pathfinder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TBCExV3Test implements ModInitializer, PreLaunchEntrypoint {
    public static final String MOD_ID = "tbcexv3_test";
    public static Consumer<Text> MESSAGE_CONSUMER = text -> {
    };

    @Override
    public void onInitialize() {
        TestEntities.init();
        TestBattleParticipantItemTypes.init();
        TestBattleActions.init();
        BattlePlayerComponentEvent.EVENT.register((entity, builder) -> {
            final InventoryBattleEntityComponent.Builder componentBuilder = InventoryBattleEntityComponent.builder();
            final Random random = entity.getRandom();
            for (int i = 0; i < 32; i++) {
                componentBuilder.addStack(BattleParticipantItemStack.of(new TestBattleParticipantItem(random.nextLong()), random.nextBetween(1, 128)));
            }
            builder.addComponent(new TestEntityComponent(20, 20, entity.interactionManager.isCreative()));
            builder.addComponent(componentBuilder.build());
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> testCommand(dispatcher));
        BattleParticipantDefaultActionGatherEvent.EVENT.register((state, actionConsumer) -> actionConsumer.accept(new BattleParticipantAction() {
            @Override
            public Text name(final BattleParticipantStateView state) {
                return Text.of("walk");
            }

            @Override
            public TooltipText description(final BattleParticipantStateView state) {
                return new TooltipText(List.of(Text.of("The name is about all you need to know")));
            }

            private static BattleParticipantActionBuilder.TargetRaycaster<BattleParticipantActionBlockPosTarget> getRaycastTargets(final BattleParticipantStateView state, final Consumer<BattleParticipantActionTarget> consumer, final Pathfinder.PathTree<List<BlockPos>> pathTree) {
                final BattleEnvironmentView environment = state.getBattleState().getEnvironment();
                return BattleParticipantActionBlockPosTarget.filterRaycast(pos -> pathTree.endPositions().contains(pos), (pos, start, end) -> {
                    final BlockHitResult raycast = environment.getBlockState(pos).getCollisionShape(environment.asBlockView(), pos).raycast(start, end, pos);
                    return raycast != null && raycast.getType() != HitResult.Type.MISS;
                }, p -> Text.of(p.toString()), p -> new TooltipText(List.of()), consumer);
            }

            private static BattleParticipantActionBuilder.TargetIterator<BattleParticipantActionBlockPosTarget> getIteratorTargets(final BattleParticipantStateView state, final Consumer<BattleParticipantActionTarget> consumer, final Pathfinder.PathTree<List<BlockPos>> pathTree) {
                return BattleParticipantActionBlockPosTarget.computed(pathTree.endPositions(), p -> Text.of(p.toString()), p -> new TooltipText(List.of()), consumer);
            }

            private static Pathfinder.PathTree<List<BlockPos>> gatherPaths(final BattleParticipantStateView state) {
                return Pathfinder.find(state.getBounds().center(), new Pathfinder.NeighbourGetter() {
                    @Override
                    public void neighbours(final BlockPos pos, final Pathfinder.Node node, final Pathfinder.NodeAppender appender) {
                        for (final BlockPos outward : BlockPos.iterateOutwards(pos, 1, 0, 1)) {
                            if (appender.getCost(outward) > node.cost() + 1.00001 && state.getBattleState().getEnvironment().checkForStanding(state.getBounds(), outward, true)) {
                                appender.append(outward, node.cost() + 1);
                            }
                        }
                    }

                    @Override
                    public void start(final BlockPos pos, final Pathfinder.NodeAppender appender) {
                        if (state.getBattleState().getEnvironment().checkForStanding(state.getBounds(), pos, true)) {
                            appender.append(pos, 0);
                        }
                    }
                }, new Pathfinder.PostProcessor<>() {
                    @Override
                    public boolean isValidEndPoint(final Pathfinder.Node node) {
                        return true;
                    }

                    @Override
                    public List<BlockPos> process(final List<BlockPos> positions) {
                        return positions;
                    }
                });
            }

            @Override
            public BattleParticipantActionBuilder<?> builder(final BattleParticipantStateView state, final Consumer<BattleAction> consumer) {
                final Pathfinder.PathTree<List<BlockPos>> pathTree = gatherPaths(state);
                return BattleParticipantActionBuilder.create(
                        state,
                        l -> !l.isEmpty(),
                        l -> new BattleParticipantMoveBattleAction(pathTree.getPath(l.get(0).pos()), state.getHandle()),
                        new ArrayList<>(),
                        (stateView, targets, targetConsumer) -> BattleParticipantActionBuilder.TargetProvider.single(
                                stateView,
                                targetConsumer,
                                CoreBattleActionTargetTypes.BLOCK_POS_TARGET_TYPE,
                                () -> getIteratorTargets(stateView, targetConsumer, pathTree),
                                () -> getRaycastTargets(stateView, targetConsumer, pathTree)
                        ),
                        (BiConsumer<ArrayList<BattleParticipantActionBlockPosTarget>, BattleParticipantActionTarget>) (targets, target) -> {
                            TBCExV3Test.MESSAGE_CONSUMER.accept(Text.of("Targeted " + ((BattleParticipantActionBlockPosTarget) target).pos()));
                            targets.add(
                                    (BattleParticipantActionBlockPosTarget) target
                            );
                        },
                        consumer,
                        pathTree,
                        (p, s) -> p
                );
            }

            @Override
            public Optional<Identifier> renderer(final BattleParticipantStateView state) {
                return Optional.of(TBCExV3Test.id("walk"));
            }
        }));
    }

    private static void testCommand(final CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tbcexCreateBattle").then(CommandManager.argument("targets", EntityArgumentType.entities()).executes(context -> {
            final Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "targets");
            final Set<BattleEntity> battleEntities = new ReferenceOpenHashSet<>();
            for (final Entity entity : entities) {
                if (entity instanceof BattleEntity battleEntity) {
                    battleEntities.add(battleEntity);
                } else {
                    context.getSource().sendError(Text.translatable("tbcex.test.not_battle_entity", entity.getUuid()));
                }
            }
            if (battleEntities.size() < 2) {
                context.getSource().sendError(Text.translatable("tbcex.test.not_enough_battle_entities"));
                return 1;
            }
            final Map<BattleEntity, Identifier> teamMap = new Object2ReferenceOpenHashMap<>();
            for (final BattleEntity entity : battleEntities) {
                if (entity instanceof PlayerEntity) {
                    teamMap.put(entity, TBCExV3Core.createId("player"));
                } else {
                    teamMap.put(entity, TBCExV3Core.createId("enemy"));
                }
            }
            final InitialTeamSetupBattleAction.Builder builder = InitialTeamSetupBattleAction.builder();
            builder.addTeam(TBCExV3Core.createId("player"));
            builder.addTeam(TBCExV3Core.createId("enemy"));
            builder.setRelation(TBCExV3Core.createId("player"), TBCExV3Core.createId("enemy"), BattleParticipantTeamRelation.ENEMIES);
            try {
                ((ServerBattleWorld) context.getSource().getWorld()).createBattle(teamMap, builder.build());
            } catch (final Throwable t) {
                t.printStackTrace();
                return 1;
            }
            return 0;
        })));
    }

    @Override
    public void onPreLaunch() {
        final Properties properties = System.getProperties();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && String.CASE_INSENSITIVE_ORDER.compare("true", (String) properties.getOrDefault("renderdoc", "false")) == 0) {
            try {
                System.loadLibrary("renderdoc");
            } catch (final Exception e) {
                LoggerFactory.getLogger(TBCExV3Test.class).error("Render doc not found, rendering debug will be disabled!");
            }
        }
    }

    public static Identifier id(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
