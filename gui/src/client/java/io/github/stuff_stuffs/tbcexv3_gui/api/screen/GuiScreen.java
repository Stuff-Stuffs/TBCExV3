package io.github.stuff_stuffs.tbcexv3_gui.api.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.widget.WidgetRenderContextImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GuiScreen<RootWidget extends Widget<Data>, Data> extends Screen {
    private final RootWidget root;
    private int lastMouseX = Integer.MIN_VALUE;
    private int lastMouseY = Integer.MIN_VALUE;
    private @Nullable Rectangle lastScreenBounds = null;
    private int tickCount;
    private Point2d mousePoint = new Point2d(-100, -100);

    public GuiScreen(final Text title, final RootWidget widget, final Data data) {
        super(title);
        root = widget;
        root.setup(WidgetContext.root(data, () -> {
            lastScreenBounds = null;
            checkSize(true);
        }));
    }

    public RootWidget getRoot() {
        return root;
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        mousePoint = localizeMouse(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        return root.handleEvent(new WidgetEvent.MouseScrollEvent(localizeMouse(mouseX, mouseY), amount / 16.0));
    }

    @Override
    public void tick() {
        super.tick();
        root.handleEvent(new WidgetEvent.TickEvent(tickCount++, Optional.of(mousePoint)));
    }

    @Override
    public void render(final MatrixStack matrices, final int mouseX, final int mouseY, final float delta) {
        final Rectangle screenBounds = checkSize(false);

        if (mouseX != lastMouseX && mouseY != lastMouseY) {
            if (lastMouseX == Integer.MIN_VALUE) {
                root.handleEvent(new WidgetEvent.MouseMoveEvent(localizeMouse(mouseX, mouseY), localizeMouse(mouseX, mouseY), time(delta)));
            } else {
                root.handleEvent(new WidgetEvent.MouseMoveEvent(localizeMouse(lastMouseX, lastMouseY), localizeMouse(mouseX, mouseY), time(delta)));
            }
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        matrices.push();
        setupBounds(matrices);
        final Window window = MinecraftClient.getInstance().getWindow();
        final Matrix4f prevProjection = RenderSystem.getProjectionMatrix();
        final Matrix4f matrix4f = Matrix4f.projectionMatrix(0.0F, window.getFramebufferWidth(), 0.0F, window.getFramebufferHeight(), 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(matrix4f);

        final WidgetRenderContextImpl renderContext = new WidgetRenderContextImpl(time(delta), screenBounds, matrices.peek().getPositionMatrix());
        root.draw(renderContext);

        renderContext.draw();

        matrices.pop();
        RenderSystem.setProjectionMatrix(prevProjection);
    }

    private float time(final float delta) {
        return (tickCount + delta) * 0.05F;
    }

    private void setupBounds(final MatrixStack matrices) {
        final int width = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        final int height = MinecraftClient.getInstance().getWindow().getFramebufferHeight();
        matrices.scale(width, height, 1);
        if (width > height) {
            matrices.scale(height / (float) width, 1, 1);
            matrices.translate((width / (double) height - 1) / 2d, 0, 0);
        } else if (width < height) {
            matrices.scale(1, width / (float) height, 1);
            matrices.translate(0, (height / (double) width - 1) / 2d, 0);
        }
    }

    private Rectangle findBounds() {
        final int width = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        final int height = MinecraftClient.getInstance().getWindow().getFramebufferHeight();
        final double minX;
        final double minY;
        final double maxX;
        final double maxY;
        if (width > height) {
            minY = 0;
            maxY = 1;

            minX = -(width / (double) height - 1) / 2d;
            maxX = 1 + (width / (double) height - 1) / 2d;
        } else if (width < height) {
            minX = 0;
            maxX = 1;

            minY = -(width / (double) height - 1) / 2d;
            maxY = 1 + (width / (double) height - 1) / 2d;
        } else {
            minX = 0;
            minY = 0;
            maxX = 1;
            maxY = 1;
        }
        return new Rectangle(new Point2d(minX, minY), new Point2d(maxX, maxY));
    }

    private Point2d localizeMouse(final double mouseX, final double mouseY) {
        final int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        final int height = MinecraftClient.getInstance().getWindow().getScaledHeight();
        double x = mouseX / (double) width;
        double y = mouseY / (double) height;
        if (width > height) {
            x = x * (width / (double) height);
            x = x - (width / (double) height - 1) / 2.0;
        } else if (height > width) {
            y = y * (height / (double) width);
            y = y - (height / (double) width - 1) / 2.0;
        }
        return new Point2d(x, y);
    }

    private Rectangle checkSize(final boolean force) {
        final Rectangle screenBounds = findBounds();

        if (force || !screenBounds.equals(lastScreenBounds)) {
            lastScreenBounds = screenBounds;
            root.resize(new RectangleRange(screenBounds));
        }
        return screenBounds;
    }

    @Override
    public void resize(final MinecraftClient client, final int width, final int height) {
        super.resize(client, width, height);
        final RectangleRange rect = new RectangleRange(checkSize(false));
        root.resize(rect);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        return root.handleEvent(new WidgetEvent.MousePressEvent(localizeMouse(mouseX, mouseY), button, time(MinecraftClient.getInstance().getTickDelta())));
    }
}
