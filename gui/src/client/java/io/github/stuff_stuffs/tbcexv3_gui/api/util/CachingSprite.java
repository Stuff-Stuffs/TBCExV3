package io.github.stuff_stuffs.tbcexv3_gui.api.util;

import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public final class CachingSprite {
    private final Identifier spriteAtlas;
    private final Identifier spriteId;
    private Sprite spriteCache;
    private long version = Long.MIN_VALUE;

    public CachingSprite(final Identifier spriteAtlas, final Identifier spriteId) {
        this.spriteAtlas = spriteAtlas;
        this.spriteId = spriteId;
    }

    public Sprite getSprite() {
        final long l = TBCExV3GuiClient.dataVersion();
        if (version != l) {
            spriteCache = MinecraftClient.getInstance().getSpriteAtlas(spriteAtlas).apply(spriteId);
            version = l;
        }
        return spriteCache;
    }
}
