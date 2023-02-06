package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.MountingHelper;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

public class IndexedFlowLayout<T> extends BaseParentComponent {
    protected final Map<T, Component> children;
    protected Comparator<T> comparator;
    protected final List<Component> childrenView = new ArrayList<>();
    protected final Algorithm<? super T> algorithm;

    protected Size contentSize = Size.zero();
    protected int gap = 0;

    public IndexedFlowLayout(final Sizing horizontalSizing, final Sizing verticalSizing, final Comparator<T> comparator, final Algorithm<? super T> algorithm) {
        super(horizontalSizing, verticalSizing);
        children = new Object2ReferenceOpenHashMap<>();
        this.comparator = comparator;
        this.algorithm = algorithm;
    }

    public void setComparator(final Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    protected int determineHorizontalContentSize(final Sizing sizing) {
        return contentSize.width() + padding.get().horizontal();
    }

    @Override
    protected int determineVerticalContentSize(final Sizing sizing) {
        return contentSize.height() + padding.get().vertical();
    }

    @Override
    public void layout(final Size space) {
        sort();
        algorithm.layout(this);
    }

    public void updateSort() {
        sort();
        algorithm.layout(this);
    }

    protected void sort() {
        final Map.Entry<T, Component>[] entries = children.entrySet().toArray(Map.Entry[]::new);
        Arrays.sort(entries, Map.Entry.comparingByKey(comparator));
        childrenView.clear();
        for (final Map.Entry<T, Component> entry : entries) {
            childrenView.add(entry.getValue());
        }
    }

    public IndexedFlowLayout<T> child(final T index, final Component child) {
        final Component old = children.put(index, child);
        if (old != null) {
            old.dismount(DismountReason.REMOVED);
        }
        updateLayout();
        return this;
    }

    @Override
    public IndexedFlowLayout<T> removeChild(final Component child) {
        if (childrenView.remove(child)) {
            children.values().remove(child);
            child.dismount(DismountReason.REMOVED);
            updateLayout();
        }

        return this;
    }

    public IndexedFlowLayout<T> clearChildren() {
        for (final var child : childrenView) {
            child.dismount(DismountReason.REMOVED);
        }
        children.clear();
        childrenView.clear();
        updateLayout();

        return this;
    }

    @Override
    public List<Component> children() {
        return childrenView;
    }

    public IndexedFlowLayout<T> gap(final int gap) {
        this.gap = gap;
        updateLayout();
        return this;
    }

    public int gap() {
        return gap;
    }

    @Override
    public void draw(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks, final float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        drawChildren(matrices, mouseX, mouseY, partialTicks, delta, childrenView);
    }

    @FunctionalInterface
    public interface Algorithm<T> {
        void layout(IndexedFlowLayout<? extends T> container);

        Algorithm<Object> HORIZONTAL = container -> {
            var layoutWidth = new MutableInt(0);
            var layoutHeight = new MutableInt(0);

            final var layout = new ArrayList<Component>();
            final var padding = container.padding.get();
            final var childSpace = container.calculateChildSpace(container.space);

            var mountState = MountingHelper.mountEarly(container::mountChild, container.childrenView, childSpace, child -> {
                layout.add(child);

                child.inflate(childSpace);
                child.mount(container,
                        container.x + padding.left() + child.margins().get().left() + layoutWidth.intValue(),
                        container.y + padding.top() + child.margins().get().top());

                final var childSize = child.fullSize();
                layoutWidth.add(childSize.width() + container.gap);
                if (childSize.height() > layoutHeight.intValue()) {
                    layoutHeight.setValue(childSize.height());
                }
            });

            layoutWidth.subtract(container.gap);

            container.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
            container.applySizing();

            if (container.verticalAlignment() != VerticalAlignment.TOP) {
                for (var component : layout) {
                    component.updateY(component.y() + container.verticalAlignment().align(component.fullSize().height(), container.height - padding.vertical()));
                }
            }

            if (container.horizontalAlignment() != HorizontalAlignment.LEFT) {
                for (var component : layout) {
                    if (container.horizontalAlignment() == HorizontalAlignment.CENTER) {
                        component.updateX(component.x() + (container.width - padding.horizontal() - layoutWidth.intValue()) / 2);
                    } else {
                        component.updateX(component.x() + (container.width - padding.horizontal() - layoutWidth.intValue()));
                    }
                }
            }

            mountState.mountLate();
        };

        Algorithm<Object> VERTICAL = container -> {
            var layoutHeight = new MutableInt(0);
            var layoutWidth = new MutableInt(0);

            final var layout = new ArrayList<Component>();
            final var padding = container.padding.get();
            final var childSpace = container.calculateChildSpace(container.space);

            var mountState = MountingHelper.mountEarly(container::mountChild, container.childrenView, childSpace, child -> {
                layout.add(child);

                child.inflate(childSpace);
                child.mount(container,
                        container.x + padding.left() + child.margins().get().left(),
                        container.y + padding.top() + child.margins().get().top() + layoutHeight.intValue());

                final var childSize = child.fullSize();
                layoutHeight.add(childSize.height() + container.gap);
                if (childSize.width() > layoutWidth.intValue()) {
                    layoutWidth.setValue(childSize.width());
                }
            });

            layoutHeight.subtract(container.gap);

            container.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
            container.applySizing();

            if (container.horizontalAlignment() != HorizontalAlignment.LEFT) {
                for (var component : layout) {
                    component.updateX(component.x() + container.horizontalAlignment().align(component.fullSize().width(), container.width - padding.horizontal()));
                }
            }

            if (container.verticalAlignment() != VerticalAlignment.TOP) {
                for (var component : layout) {
                    if (container.verticalAlignment() == VerticalAlignment.CENTER) {
                        component.updateY(component.y() + (container.height - padding.vertical() - layoutHeight.intValue()) / 2);
                    } else {
                        component.updateY(component.y() + (container.height - padding.vertical() - layoutHeight.intValue()));
                    }
                }
            }

            mountState.mountLate();
        };
    }
}
