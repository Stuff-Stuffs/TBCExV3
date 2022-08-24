package io.github.stuff_stuffs.tbcexv3gui.internal.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.Window;

public class TBCExV3GuiClient implements ClientModInitializer {
    public static final Event<ResolutionChangedEvent> RESOLUTION_CHANGED_EVENT = EventFactory.createArrayBacked(ResolutionChangedEvent.class, events -> (width, height) -> {
        for (ResolutionChangedEvent event : events) {
            event.resolutionChanged(width, height);
        }
    });
    private static StencilFrameBuffer GUI_FRAME_BUFFER;

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
    }

    public interface ResolutionChangedEvent {
        void resolutionChanged(int width, int height);
    }
}
