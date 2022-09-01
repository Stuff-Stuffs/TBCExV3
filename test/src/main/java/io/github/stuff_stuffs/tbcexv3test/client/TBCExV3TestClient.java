package io.github.stuff_stuffs.tbcexv3test.client;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3gui.api.screen.GuiScreen;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.Axis;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.BasicWidgets;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.WidgetRenderUtils;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.WidgetUtils;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.container.AbstractListLikeContainerWidget;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.container.ExpandableListContainerWidget;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.container.ScrollingContainerWidget;
import io.github.stuff_stuffs.tbcexv3gui.internal.client.TBCExV3GuiRenderLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

public class TBCExV3TestClient implements ClientModInitializer, PreLaunchEntrypoint {
    public static final String MOD_ID = "tbcexv3_test";
    private static final KeyBinding OPEN_TEST_SCREEN = new KeyBinding(MOD_ID + ".open_test_screen", GLFW.GLFW_KEY_KP_SUBTRACT, "misc");

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_TEST_SCREEN);
        ClientTickEvents.START_WORLD_TICK.register(world -> {
            if (OPEN_TEST_SCREEN.wasPressed()) {
                final ScrollingContainerWidget.ScrollbarInfo<ScrollState> info = new ScrollingContainerWidget.ScrollbarInfo<ScrollState>() {
                    @Override
                    public double calculateScrollbarSize(final ScrollState data, final Rectangle bounds) {
                        return bounds.height() / 10;
                    }

                    @Override
                    public double getScrollAmount(final ScrollState data, final Rectangle bounds, final Rectangle innerBounds) {
                        return data.scrollAmount;
                    }

                    @Override
                    public double getScrollbarLength(final ScrollState data, final Rectangle bounds, final Rectangle innerBounds) {
                        return bounds.height() / 5;
                    }
                };
                final ScrollingContainerWidget<ScrollState> scroller = new ScrollingContainerWidget<>(Axis.Y, true, Sizer.max(), info, ScrollingContainerWidget.ScrollbarState.stateUpdater(Axis.Y), ScrollingContainerWidget.basicScrollbarRenderer(true, info, data -> 0xFF0000FF));
                final ExpandableListContainerWidget<Void> columns = new ExpandableListContainerWidget<>(WidgetRenderUtils.Renderer.empty(), (data, startA1, maxA1, remainingA1, rowIndex, maxIndex, axis) -> Math.min(remainingA1, 0.25), AbstractListLikeContainerWidget.Justification.LOWER, Axis.Y, true);
                final Random random = Random.create();
                for (int i = 0; i < 64; i++) {
                    final Widget<WidgetUtils.MutableButtonStateHolder> buttonWidget = BasicWidgets.button(Function.identity(), WidgetRenderUtils.basicPanelTerminal(0x7F000000 | random.nextInt(0xFFFFFF), TBCExV3GuiRenderLayers.getPosColorCull()), Sizer.max());
                    columns.add(buttonWidget, WidgetUtils.MutableButtonStateHolder::standalone);
                }
                scroller.setChild(columns, WidgetContext::stateless);
                MinecraftClient.getInstance().setScreen(new GuiScreen<>(Text.of("sadas"), scroller, new ScrollState()));
            }
        });
    }

    private static final class ScrollState implements ScrollingContainerWidget.ScrollbarState, ScrollingContainerWidget.InnerBoundsHolder {
        private Rectangle bounds;
        private Rectangle innerBounds;
        private double scrollAmount;

        @Override
        public Rectangle innerBounds() {
            return innerBounds;
        }

        @Override
        public void setInnerBounds(final Rectangle rectangle) {
            innerBounds = rectangle;
        }

        @Override
        public double getScrollAmount() {
            return scrollAmount;
        }

        @Override
        public void setScrollAmount(final double scrollAmount) {
            this.scrollAmount = scrollAmount;
        }

        @Override
        public Rectangle bounds() {
            return bounds;
        }

        @Override
        public void setBounds(final Rectangle bounds) {
            this.bounds = bounds;
        }
    }

    @Override
    public void onPreLaunch() {
        System.loadLibrary("renderdoc");
    }
}
