package io.github.stuff_stuffs.tbcexv3core.api.battle.action;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.action.BattleActionTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface BattleActionType<T extends BattleAction> {
    Identifier NOOP_ID = TBCExV3Core.createId("default");
    Registry<BattleActionType<?>> REGISTRY = FabricRegistryBuilder.from(new DefaultedRegistry<>(NOOP_ID.toString(), RegistryKey.<BattleActionType<?>>ofRegistry(TBCExV3Core.createId("battle_actions")), Lifecycle.stable(), BattleActionType::getReference)).buildAndRegister();
    Codec<BattleActionType<?>> CODEC = REGISTRY.getCodec();
    BattleActionType<NoopBattleAction> NOOP_BATTLE_ACTION_TYPE = createAndRegister(NOOP_ID, NoopBattleAction.class, Codec.unit(NoopBattleAction.INSTANCE), Codec.unit(NoopBattleAction.INSTANCE));

    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleAction action);

    RegistryEntry.Reference<BattleActionType<?>> getReference();

    Class<T> getActionClass();

    default <T1 extends BattleAction> @Nullable BattleActionType<T1> checkedCast(final Class<T1> actionClass) {
        if (actionClass == getActionClass()) {
            return (BattleActionType<T1>) this;
        }
        return null;
    }

    static <T extends BattleAction> BattleActionType<T> createAndRegister(final Identifier id, final Class<T> actionClass, final Encoder<T> encoder, final Decoder<T> decoder) {
        final BattleActionTypeImpl<T> type = new BattleActionTypeImpl<>(encoder, decoder, actionClass);
        Registry.register(REGISTRY, id, type);
        return type;
    }

    static <T extends BattleAction> @Nullable BattleActionType<T> get(final Identifier id, final Class<T> actionClass) {
        return REGISTRY.getOrEmpty(id).map(action -> action.checkedCast(actionClass)).orElse(null);
    }
}
