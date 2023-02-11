package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;

public class SimpleParentComponent extends WrappingParentComponent<Component> {
    public SimpleParentComponent(final Sizing horizontalSizing, final Sizing verticalSizing, final Component child) {
        super(horizontalSizing, verticalSizing, child);
    }
}
