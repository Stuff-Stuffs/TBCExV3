package io.github.stuff_stuffs.tbcexv3_test.common;

import com.mojang.brigadier.CommandDispatcher;
import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3_test.common.item.TestBattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3_test.common.item.TestBattleParticipantItemTypes;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattlePlayerComponentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.InventoryBattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
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
import net.minecraft.util.math.random.Random;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TBCExV3Test implements ModInitializer, PreLaunchEntrypoint {
    public static final String MOD_ID = "tbcexv3_test";

    @Override
    public void onInitialize() {
        TestEntities.init();
        TestBattleParticipantItemTypes.init();
        BattlePlayerComponentEvent.EVENT.register((entity, builder) -> {
            final InventoryBattleEntityComponent.Builder componentBuilder = InventoryBattleEntityComponent.builder();
            final Random random = entity.getRandom();
            for (int i = 0; i < 32; i++) {
                componentBuilder.addStack(BattleParticipantItemStack.of(new TestBattleParticipantItem(random.nextLong()), random.nextBetween(1, 128)));
            }

            builder.addComponent(componentBuilder.build());
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> testCommand(dispatcher));
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
            ((ServerBattleWorld) context.getSource().getWorld()).createBattle(teamMap, builder.build());
            return 0;
        })));
    }

    @Override
    public void onPreLaunch() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
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
