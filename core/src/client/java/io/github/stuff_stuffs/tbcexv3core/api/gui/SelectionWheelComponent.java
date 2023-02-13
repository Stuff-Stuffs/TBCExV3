package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExDrawer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExTextUtil;
import io.github.stuff_stuffs.tbcexv3core.api.util.Trapezoid;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class SelectionWheelComponent extends BaseComponent {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final List<Section> sections;
    private final double startRadPercent;
    private final double hoverRadSize;

    public SelectionWheelComponent(final double percent, final double hoverRadSize, final Sizing horizontalSizing, final Sizing verticalSizing) {
        sizing(horizontalSizing, verticalSizing);
        sections = new ObjectArrayList<>();
        startRadPercent = percent;
        this.hoverRadSize = hoverRadSize;
        mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                for (final Section section : sections) {
                    if (section.testMouse(mouseX, mouseY)) {
                        section.clickAction.run();
                        UISounds.playButtonSound();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    public Section addChild() {
        final Section newSection = new Section();
        sections.add(newSection);
        final int sectionCount = sections.size();
        final double anglePer = Math.PI * 2 / sectionCount;
        for (int i = 0; i < sectionCount; i++) {
            sections.get(i).update(anglePer * i, anglePer * (i + 1));
        }
        return newSection;
    }

    public int getChildCount() {
        return sections.size();
    }

    public Section getChild(final int i) {
        return sections.get(i);
    }

    @Override
    protected void applySizing() {
        super.applySizing();
        for (final Section section : sections) {
            section.updateBounds();
        }
    }

    @Override
    public void draw(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks, final float delta) {
        sections.forEach(section -> section.draw(matrices, mouseX, mouseY, partialTicks, delta));
    }

    @Override
    public void updateX(final int x) {
        super.updateX(x);
        for (final Section section : sections) {
            section.updateBounds();
        }
    }

    @Override
    public void updateY(final int y) {
        super.updateY(y);
        for (final Section section : sections) {
            section.updateBounds();
        }
    }

    public final class Section {
        private double startAngle = 0;
        private double endAngle = 1;
        private final Quaternionf rotation = new Quaternionf();

        private Trapezoid bounds;
        private Trapezoid hoverBounds;
        private Text text = Text.empty();
        private List<OrderedText> wrappedText = List.of();
        private List<OrderedText> wrappedHoverText = List.of();
        private Runnable clickAction = () -> {
        };
        private boolean hovered = false;
        private Color innerColor = Color.BLACK;
        private Color outerColor = Color.BLACK;
        private double xOff = 0;
        private double yOff = 0;

        public Section setClickAction(final Runnable clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        public Section setInnerColor(final Color innerColor) {
            this.innerColor = innerColor;
            return this;
        }

        public Section setOuterColor(final Color outerColor) {
            this.outerColor = outerColor;
            return this;
        }

        public Section setWrappedText(final Text text) {
            this.text = text;
            updateText();
            return this;
        }

        public void draw(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks, final float delta) {
            updateMouse(mouseX, mouseY);
            final Trapezoid area = hovered ? hoverBounds : bounds;
            TBCExDrawer.drawTrapezoid(matrices, area, innerColor, outerColor, x() + xOff, y() + yOff);
            final List<OrderedText> texts = hovered ? wrappedHoverText : wrappedText;
            final int lineCount = texts.size();
            if (lineCount == 0) {
                return;
            }
            double cX = 0;
            double cY = 0;
            for (int i = 0; i < 4; i++) {
                cX += area.x(i) + xOff + x();
                cY += area.y(i) + yOff + y();
            }
            cX /= 4;
            cY /= 4;
            final Vector3d s0 = new Vector3d();
            final Vector3d s1 = new Vector3d();
            s0.set(area.x(0), area.y(0), 0);
            s1.set(area.x(1), area.y(1), 0);
            rotation.transform(s0);
            rotation.transform(s1);
            final double width = horizontalSizing().get().value / 2.0;
            final double height = verticalSizing().get().value / 2.0;
            final double maxRad = Math.min(width, height);
            final double trapHeight = maxRad / hoverRadSize - maxRad * startRadPercent;
            final double dH = trapHeight / (lineCount * 2);
            final double angle = (startAngle + endAngle) * 0.5;
            final boolean b = Math.cos(angle) >= 0;
            double mult = b?-1:1;
            for (int i = 0; i < lineCount; i++) {
                matrices.push();
                matrices.translate(cX, cY, 0);
                matrices.multiply(rotation);
                final OrderedText text = texts.get(i);
                matrices.translate(-textRenderer.getWidth(text) / 2.0, dH * (i - mult * lineCount / 2.0) - (b?textRenderer.fontHeight*2:0), 0);
                textRenderer.draw(matrices, text, 0, 0, -1);
                matrices.pop();
            }
        }

        private void update(final double startAngle, final double endAngle) {
            this.startAngle = startAngle;
            this.endAngle = endAngle;
            final double angle = (startAngle + endAngle) * 0.5;
            rotation.setAngleAxis(Math.cos(angle) >= 0 ? angle : Math.PI + angle, 0, 0, -1);
            updateBounds();
        }


        private void updateBounds() {
            final double width = horizontalSizing().get().value / 2.0;
            final double height = verticalSizing().get().value / 2.0;
            final double maxRad = Math.min(width, height);
            xOff = width;
            yOff = height;
            bounds = new Trapezoid(maxRad * startRadPercent, maxRad / hoverRadSize, startAngle, endAngle);
            hoverBounds = new Trapezoid(maxRad * startRadPercent, maxRad, startAngle, endAngle);
            updateText();
        }

        private void updateText() {
            wrappedText = wrap(text, bounds);
            wrappedHoverText = wrap(text, hoverBounds);
        }

        private List<OrderedText> wrap(final Text text, final Trapezoid area) {
            final double angle = (startAngle + endAngle) * 0.5;
            final boolean b = Math.cos(angle) >= 0;
            final int maxLines = MathHelper.floor((area.getOuterRadius() - area.getInnerRadius()) / textRenderer.fontHeight);
            final Optional<List<OrderedText>> texts = TBCExTextUtil.splitContentDependent(text, textRenderer, new TBCExTextUtil.ContentDependentTextDisplay() {
                private int lineCount = 1;

                @Override
                public Optional<IntList> maxWidths() {
                    if (lineCount > maxLines) {
                        return Optional.empty();
                    }
                    return Optional.of(area.splitWidths(lineCount, b));
                }

                @Override
                public void incrementLineCount() {
                    lineCount++;
                }
            });
            return texts.orElseGet(() -> List.of(Text.of("Error").asOrderedText()));
        }

        public boolean updateMouse(final double x, final double y) {
            final boolean b = testMouse(x, y);
            if (b & !hovered) {
                UISounds.playInteractionSound();
            }
            hovered = b;
            return hovered;
        }

        public boolean testMouse(final double x, final double y) {
            final double xOff = x() + this.xOff;
            final double yOff = y() + this.yOff;
            return (hovered && hoverBounds.isIn(x - xOff, y - yOff)) || (!hovered && bounds.isIn(x - xOff, y - yOff));
        }
    }
}
