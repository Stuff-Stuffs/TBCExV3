package io.github.stuff_stuffs.tbcexv3test.client;

import io.github.stuff_stuffs.tbcexv3gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3gui.api.screen.GuiScreen;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.*;
import io.github.stuff_stuffs.tbcexv3gui.internal.client.TBCExV3GuiRenderLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Function;

public class TBCExV3TestClient implements ClientModInitializer, PreLaunchEntrypoint {
    public static final String MOD_ID = "tbcexv3_test";
    private static final KeyBinding OPEN_TEST_SCREEN = new KeyBinding(MOD_ID + ".open_test_screen", GLFW.GLFW_KEY_KP_SUBTRACT, "misc");

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_TEST_SCREEN);
        ClientTickEvents.START_WORLD_TICK.register(world -> {
            if (OPEN_TEST_SCREEN.wasPressed()) {
                final SingleAnimationWidget<Void> animationWidget = new SingleAnimationWidget<>(new SingleAnimationWidget.Animation<>() {
                    @Override
                    public Optional<WidgetRenderContext> animate(final Void data, final WidgetRenderContext parent) {
                        return Optional.of(parent);
                    }

                    @Override
                    public Optional<WidgetEvent> animateEvent(final Void data, final WidgetEvent event) {
                        return Optional.of(event);
                    }
                }, WidgetUtils.builder().build(), SingleAnimationWidget.WidgetEventPhase.PRE_CHILD);
                final Widget<WidgetUtils.MutableButtonStateHolder> button = BasicWidgets.button(Function.identity(), WidgetRenderUtils.basicPanelTerminal(data -> switch (data.state()) {
                    case DEFAULT -> 0xFFFFFFFF;
                    case HOVER -> 0xFF7F7FFF;
                    case PRESSED -> 0xFF0707FF;
                }, TBCExV3GuiRenderLayers.getPosColorCull()), Sizer.max());
                animationWidget.setChild(button, WidgetUtils.MutableButtonStateHolder::standalone);

                MinecraftClient.getInstance().setScreen(new GuiScreen<>(Text.of("sadas"), animationWidget, null));
            }
        });
    }

    @Override
    public void onPreLaunch() {
        System.loadLibrary("renderdoc");
    }
}
