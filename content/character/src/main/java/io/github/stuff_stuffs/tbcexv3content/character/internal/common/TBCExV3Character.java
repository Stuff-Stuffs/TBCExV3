package io.github.stuff_stuffs.tbcexv3content.character.internal.common;

import io.github.stuff_stuffs.tbcexv3content.character.internal.common.battles.component.CharacterBattleEntityComponents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TBCExV3Character implements ModInitializer {
    public static final String MOD_ID = "tbcexv3character";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CharacterBattleEntityComponents.init();
    }

    public static Identifier id(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
