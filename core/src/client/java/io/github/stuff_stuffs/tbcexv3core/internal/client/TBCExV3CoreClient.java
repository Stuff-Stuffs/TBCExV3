package io.github.stuff_stuffs.tbcexv3core.internal.client;

import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemFilter;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemFilters;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemSort;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemSorts;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemRarity;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.internal.client.entity.TBCExClientPlayerExtensions;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.PlayerCurrentBattleReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.PlayerCurrentBattleRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TBCExV3CoreClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(TBCExV3Core.MOD_ID + "(Client)");
    private final static List<Consumer<WorldRenderContext>> DEFERRED_RENDERING = new ArrayList<>();

    public static void defer(final Consumer<WorldRenderContext> consumer) {
        DEFERRED_RENDERING.add(consumer);
    }

    @Override
    public void onInitializeClient() {
        TBCExV3Core.setClientLogger(() -> {
            if (MinecraftClient.getInstance().isOnThread()) {
                return LOGGER;
            } else {
                return null;
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.age % 20 == 0) {
                PlayerCurrentBattleRequestSender.send();
            }
            if (client.player != null && ((TBCExClientPlayerExtensions) client.player).tbcexcore$action$current() != null) {
                if (((TBCExPlayerEntity) client.player).tbcex$getCurrentBattle() == null) {
                    ((TBCExClientPlayerExtensions) client.player).tbcexcore$action$setCurrent(null, null);
                }
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            DEFERRED_RENDERING.forEach(e -> e.accept(context));
            DEFERRED_RENDERING.clear();
            ((ClientBattleWorld) context.world()).tbcex$render(context);
        });
        BattleUpdateReceiver.init();
        EntityBattlesUpdateReceiver.init();
        PlayerCurrentBattleReceiver.init();
        BattleParticipantItemFilters.instance().register(TBCExV3Core.createId("all"), new BattleParticipantItemFilter() {
            @Override
            public boolean accepted(final BattleParticipantItemStack stack, final BattleParticipantStateView view) {
                return true;
            }

            @Override
            public OrderedText name() {
                return Text.of("ALL").asOrderedText();
            }
        }, view -> true);
        BattleParticipantItemSorts.instance().register(TBCExV3Core.createId("count"), new BattleParticipantItemSort() {
            @Override
            public int compare(final BattleParticipantItemStack first, final BattleParticipantItemStack second, final BattleParticipantStateView view) {
                return Integer.compare(first.getCount(), second.getCount());
            }

            @Override
            public OrderedText name() {
                return Text.of("COUNT").asOrderedText();
            }
        }, view -> true);
        BattleParticipantItemSorts.instance().register(TBCExV3Core.createId("rarity"), new BattleParticipantItemSort() {
            @Override
            public int compare(final BattleParticipantItemStack first, final BattleParticipantItemStack second, final BattleParticipantStateView view) {
                return BattleParticipantItemRarity.COMPARATOR.compare(first.getItem().rarity(), second.getItem().rarity());
            }

            @Override
            public OrderedText name() {
                return Text.of("RARITY").asOrderedText();
            }
        }, view -> true);
    }
}
