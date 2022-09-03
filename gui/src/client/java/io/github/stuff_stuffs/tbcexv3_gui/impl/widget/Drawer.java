package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import net.minecraft.client.render.RenderLayer;

interface Drawer {
    void submit(final RenderLayer layer, final byte[] data);
}
