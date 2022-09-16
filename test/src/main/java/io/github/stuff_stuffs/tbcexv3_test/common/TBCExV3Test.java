package io.github.stuff_stuffs.tbcexv3_test.common;

import com.mojang.brigadier.CommandDispatcher;
import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

public class TBCExV3Test implements ModInitializer, PreLaunchEntrypoint {
    @Override
    public void onInitialize() {
        TestEntities.init();
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
                context.getSource().sendError(Text.translatable("tbcex.test.empty_battle_entities"));
                return 1;
            }
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
}
