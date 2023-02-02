package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;

import java.util.Collection;
import java.util.Comparator;

public class SortedFlowLayout<T extends Component> extends FlowLayout {
    private final Comparator<T> comparator;
    private final Class<T> componentClazz;

    public SortedFlowLayout(final Sizing horizontalSizing, final Sizing verticalSizing, final Algorithm algorithm, final Comparator<T> comparator, final Class<T> clazz) {
        super(horizontalSizing, verticalSizing, algorithm);
        this.comparator = comparator;
        componentClazz = clazz;
    }

    @Override
    public FlowLayout child(final Component child) {
        if (componentClazz.isInstance(child)) {
            return super.child(child);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public FlowLayout children(final Collection<Component> children) {
        for (final Component child : children) {
            if (!componentClazz.isInstance(child)) {
                throw new IllegalArgumentException();
            }
        }
        return super.children(children);
    }

    @Override
    public FlowLayout child(final int index, final Component child) {
        if (componentClazz.isInstance(child)) {
            return super.child(index, child);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public FlowLayout children(final int index, final Collection<Component> children) {
        for (final Component child : children) {
            if (!componentClazz.isInstance(child)) {
                throw new IllegalArgumentException();
            }
        }
        return super.children(index, children);
    }

    public void sort() {
        children.sort((i, j) -> comparator.compare((T) i, (T) j));
        updateLayout();
    }
}
