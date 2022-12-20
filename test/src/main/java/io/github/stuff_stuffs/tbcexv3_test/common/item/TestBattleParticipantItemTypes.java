package io.github.stuff_stuffs.tbcexv3_test.common.item;

import io.github.stuff_stuffs.tbcexv3_test.common.TBCExV3Test;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;
import net.minecraft.registry.Registry;

public final class TestBattleParticipantItemTypes {
    public static final BattleParticipantItemType<TestBattleParticipantItem> TEST_ITEM_TYPE = BattleParticipantItemType.create(TestBattleParticipantItem.CODEC, TestBattleParticipantItem.class);

    public static void init() {
        Registry.register(BattleParticipantItemType.REGISTRY, TBCExV3Test.id("test"), TEST_ITEM_TYPE);
    }

    private TestBattleParticipantItemTypes() {
    }
}
