package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetQuadEmitter;

import java.util.Arrays;
import java.util.function.Consumer;

public class WidgetQuadEmitterImpl implements WidgetQuadEmitter {
    private final float z;
    private final float[] xs;
    private final float[] ys;
    private final float[] us;
    private final float[] vs;
    private final int[] lights;
    private final int[] colors;
    private final Consumer<EmittedQuad> consumer;

    public WidgetQuadEmitterImpl(final float z, final Consumer<EmittedQuad> consumer) {
        this.z = z;
        this.consumer = consumer;
        xs = new float[4];
        ys = new float[4];
        us = new float[4];
        vs = new float[4];
        lights = new int[4];
        colors = new int[4];
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
    public boolean text() {
        return false;
    }

    @Override
    public void emit() {
        consumer.accept(new EmittedQuad(z, Arrays.copyOf(xs, xs.length), Arrays.copyOf(ys, ys.length), Arrays.copyOf(us, us.length), Arrays.copyOf(vs, vs.length), Arrays.copyOf(lights, lights.length), Arrays.copyOf(colors, colors.length), false));
        Arrays.fill(xs, 0);
        Arrays.fill(ys, 0);
        Arrays.fill(us, 0);
        Arrays.fill(vs, 0);
        Arrays.fill(lights, 0);
        Arrays.fill(colors, 0);
    }
}
