package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Surface;

public final class TBCExGUI {
    public static final Color DEFAULT = new Color(0.25F, 0.25F, 0.25F, 0.25F);
    public static final Color DARK = new Color(0.35F, 0.35F, 0.35F, 0.65F);
    public static final Color LIGHT = new Color(0.4F, 0.4F, 0.4F, 0.65F);
    public static final Color SELECTED = new Color(0.425F, 0.425F, 0.425F, 0.90F);

    public static final Surface DEFAULT_SURFACE = Surface.flat(DEFAULT.argb());
    public static final Surface DARK_SURFACE = Surface.flat(DARK.argb());
    public static final Surface LIGHT_SURFACE = Surface.flat(LIGHT.argb());
    public static final Surface SELECTED_SURFACE = Surface.flat(SELECTED.argb());

    private TBCExGUI() {
    }
}
