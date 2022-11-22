package io.github.stuff_stuffs.tbcexv3_gui.internal.client;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.CachingSprite;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.impl.client.texture.SpriteRegistryCallbackHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.concurrent.atomic.AtomicLong;

public class TBCExV3GuiClient implements ClientModInitializer {
    public static final String MOD_ID = "tbcexv3_gui";
    public static final Identifier FLAT_SPRITE = id("gui/flat");
    public static final Event<ResolutionChangedEvent> RESOLUTION_CHANGED_EVENT = EventFactory.createArrayBacked(ResolutionChangedEvent.class, events -> (width, height) -> {
        for (ResolutionChangedEvent event : events) {
            event.resolutionChanged(width, height);
        }
    });
    private static final AtomicLong DATA_VERSION = new AtomicLong(0);
    private static StencilFrameBuffer GUI_FRAME_BUFFER;
    public static final CachingSprite FLAT_SPRITE_CACHE = new CachingSprite(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, FLAT_SPRITE);

    public static StencilFrameBuffer getGuiFrameBuffer() {
        if (GUI_FRAME_BUFFER == null) {
            final Window window = MinecraftClient.getInstance().getWindow();
            GUI_FRAME_BUFFER = new StencilFrameBuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
        }
        return GUI_FRAME_BUFFER;
    }

    @Override
    public void onInitializeClient() {
        RESOLUTION_CHANGED_EVENT.register((width, height) -> getGuiFrameBuffer().resize(width, height, MinecraftClient.IS_SYSTEM_MAC));
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id("data_version_incr");
            }

            @Override
            public void reload(final ResourceManager manager) {
                DATA_VERSION.getAndIncrement();
            }
        });
        SpriteRegistryCallbackHolder.eventLocal(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> registry.register(FLAT_SPRITE));
    }

    public static long dataVersion() {
        return DATA_VERSION.getAcquire();
    }

    public interface ResolutionChangedEvent {
        void resolutionChanged(int width, int height);
    }

    public static Identifier id(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
