package io.github.stuff_stuffs.tbcexv3core.internal.client;

import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateReceiver;
import net.fabricmc.api.ClientModInitializer;

public class TBCExV3CoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BattleUpdateReceiver.init();
        EntityBattlesUpdateReceiver.init();
    }
}
