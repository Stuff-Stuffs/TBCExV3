package io.github.stuff_stuffs.tbcexv3test.client;

import io.github.stuff_stuffs.tbcexv3gui.api.Point2d;
import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.screen.GuiScreen;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.BasicWidgets;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.SingleAnimationWidget;
import io.github.stuff_stuffs.tbcexv3gui.internal.client.TBCExV3GuiRenderLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
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
                        final Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(parent.time()*4);
                        final Quaternion revQuaternion = Vec3f.NEGATIVE_Z.getDegreesQuaternion(parent.time()*8);
                        return Optional.of(parent.pushScissor(new Rectangle(new Point2d(0, 0), new Point2d(1, 1))).pushMatrix(new Matrix4f(quaternion)).pushScissor(new Rectangle(new Point2d(0, 0), new Point2d(1, 1))).pushMatrix(new Matrix4f(revQuaternion)));
                    }

                    @Override
                    public Optional<WidgetEvent> animateEvent(final Void data, final WidgetEvent event) {
                        return Optional.of(event);
                    }
                });
                animationWidget.setChild(BasicWidgets.basicPanel(0xFF00FFFF, TBCExV3GuiRenderLayers.getPosColorCull(), (min, max) -> new Rectangle(new Point2d(0.0, 0.0), new Point2d(0.5, 0.5))), Function.identity());
                MinecraftClient.getInstance().setScreen(new GuiScreen<>(Text.of("sadas"), animationWidget, null));
            }
        });
    }

    @Override
    public void onPreLaunch() {
        System.loadLibrary("renderdoc");
    }
}
