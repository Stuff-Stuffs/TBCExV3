package io.github.stuff_stuffs.tbcexv3core.impl.battles.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemModel;
import io.github.stuff_stuffs.tbcexv3core.api.battles.item.BattleParticipantItemModelRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BattleParticipantItemModelRegistryImpl implements BattleParticipantItemModelRegistry {
    private static final Function<BattleParticipantItemType<?>, BattleParticipantItemModel> DEFAULT_MODEL_FACTORY = type -> (matrices, stack, state, context, world, light) -> {
        final List<ItemStack> stacks = stack.getItem().toItemStacks(stack);
        if (stacks.isEmpty()) {
            return;
        }
        final ItemStack itemStack = Util.getRandom(stacks, world.random);
        //FIXME
        //MinecraftClient.getInstance().getItemRenderer().renderItem(null, itemStack, ModelTransformation.Mode.NONE, false, matrices, context, world, light, OverlayTexture.DEFAULT_UV, 0);
    };
    private final Map<BattleParticipantItemType<?>, Function<Object, BattleParticipantItemModel>> modelMap = new Object2ReferenceOpenHashMap<>();

    @Override
    public <T extends BattleParticipantItem> void register(final BattleParticipantItemType<T> type, final Function<T, BattleParticipantItemModel> model) {
        if (modelMap.put(type, (Function<Object, BattleParticipantItemModel>) (Object) model) != null) {
            throw new RuntimeException("Duplicate model factories");
        }
    }

    @Override
    public BattleParticipantItemModel get(final BattleParticipantItem item) {
        final Function<Object, BattleParticipantItemModel> function = modelMap.get(item.type());
        if (function == null) {
            return DEFAULT_MODEL_FACTORY.apply(item.type());
        }
        return function.apply(item);
    }
}
