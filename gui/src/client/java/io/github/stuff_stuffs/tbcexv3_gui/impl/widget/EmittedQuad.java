package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetQuadEmitter;
import net.minecraft.util.Identifier;

import java.util.Optional;

class EmittedQuad implements WidgetQuadEmitter {
    private final float z;
    private final float[] xs;
    private final float[] ys;
    private final float[] us;
    private final float[] vs;
    private final int[] lights;
    private final int[] colors;
    private final Optional<Identifier> texture;
    private int scissorState = -1;
    private boolean emitted = false;

    EmittedQuad(final float z, final float[] xs, final float[] ys, final float[] us, final float[] vs, final int[] lights, final int[] colors, final boolean text, final Optional<Identifier> texture) {
        this.z = z;
        this.xs = xs;
        this.ys = ys;
        this.us = us;
        this.vs = vs;
        this.lights = lights;
        this.colors = colors;
        this.texture = texture;
    }

    public int scissorState() {
        return scissorState;
    }

    public void scissorState(final int scissorState) {
        this.scissorState = scissorState;
    }

    public float z() {
        return z;
    }

    @Override
    public float x(final int index) {
        return xs[index];
    }

    @Override
    public float y(final int index) {
        return ys[index];
    }

    @Override
    public void x(final int index, final float x) {
        xs[index] = x;
    }

    @Override
    public void y(final int index, final float y) {
        ys[index] = y;
    }

    @Override
    public int light(final int index) {
        return lights[index];
    }

    @Override
    public void light(final int index, final int light) {
        lights[index] = light;
    }

    @Override
    public float spriteU(final int vertexIndex) {
        return us[vertexIndex];
    }

    @Override
    public float spriteV(final int vertexIndex) {
        return vs[vertexIndex];
    }

    @Override
    public void spriteU(final int vertexIndex, final float u) {
        us[vertexIndex] = u;
    }

    @Override
    public void spriteV(final int vertexIndex, final float v) {
        vs[vertexIndex] = v;
    }

    @Override
    public int color(final int index) {
        return colors[index];
    }

    @Override
    public void color(final int index, final int color) {
        colors[index] = color;
    }

    @Override
    public Optional<Identifier> texture() {
        return texture;
    }

    @Override
    public void emit() {
        emitted = true;
    }

    public boolean clear() {
        final boolean b = emitted;
        emitted = false;
        return b;
    }
}
