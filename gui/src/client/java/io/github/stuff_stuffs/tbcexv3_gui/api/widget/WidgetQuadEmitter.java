package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Quadrilateral;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface WidgetQuadEmitter {
    default void pos(final Axis axis, final int index, final float p) {
        switch (axis) {
            case X -> x(index, p);
            case Y -> y(index, p);
        }
    }

    float x(int index);

    float y(int index);

    void x(int index, float x);

    void y(int index, float y);

    default void pos(final int index, final float x, final float y) {
        x(index, x);
        y(index, y);
    }

    default void quad(final Quadrilateral quadrilateral) {
        for (int i = 0; i < 4; i++) {
            pos(i, (float) quadrilateral.getVertexX(i), (float) quadrilateral.getVertexY(i));
        }
    }

    int light(int index);

    void light(int index, int light);

    default void light(final int l0, final int l1, final int l2, final int l3) {
        light(0, l0);
        light(1, l1);
        light(2, l2);
        light(3, l3);
    }

    float spriteU(int vertexIndex);

    float spriteV(int vertexIndex);

    void spriteU(int vertexIndex, float u);

    void spriteV(int vertexIndex, float v);

    default void sprite(final Sprite sprite) {
        spriteU(0, sprite.getMinU());
        spriteU(1, sprite.getMinU());
        spriteU(2, sprite.getMaxU());
        spriteU(3, sprite.getMaxU());

        spriteV(0, sprite.getMinV());
        spriteV(1, sprite.getMaxV());
        spriteV(2, sprite.getMaxV());
        spriteV(3, sprite.getMinV());
    }

    int color(int index);

    void color(int index, int color);

    default void color(final int c0, final int c1, final int c2, final int c3) {
        color(0, c0);
        color(1, c1);
        color(2, c2);
        color(3, c3);
    }

    Optional<Identifier> texture();

    void emit();
}
