package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface WidgetRenderContext {
    float time();

    Rectangle screenBounds();

    void pushTransform(Transform transform);

    void popTransform();

    WidgetRenderContext child();

    WidgetTextEmitter textEmitter();

    sealed interface Transform {
        static Transform scissor(final Rectangle scissors) {
            return new ScissorTransform(scissors);
        }

        static Transform translate(final double x, final double y) {
            return new Translate(x, y);
        }

        static Transform scale(final double x, final double y) {
            return new Scale(x, y);
        }

        static Transform rotate(final double angle) {
            return new Rotate(angle);
        }
    }

    non-sealed interface QuadTransform extends Transform {
        void transform(WidgetQuadEmitter emitter);
    }

    final class Translate implements QuadTransform {
        private final double x;
        private final double y;

        public Translate(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void transform(final WidgetQuadEmitter emitter) {
            for (int i = 0; i < 4; i++) {
                emitter.x(i, emitter.x(i) + (float) x);
                emitter.y(i, emitter.y(i) + (float) y);
            }
            emitter.emit();
        }
    }

    final class Scale implements QuadTransform {
        private final double x;
        private final double y;

        public Scale(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void transform(final WidgetQuadEmitter emitter) {
            for (int i = 0; i < 4; i++) {
                emitter.x(i, emitter.x(i) * (float) x);
                emitter.y(i, emitter.y(i) * (float) y);
            }
            emitter.emit();
        }
    }

    final class Rotate implements QuadTransform {
        private final float cos;
        private final float sin;

        public Rotate(final double angle) {
            cos = MathHelper.cos((float) angle);
            sin = MathHelper.sin((float) angle);
        }

        @Override
        public void transform(final WidgetQuadEmitter emitter) {
            for (int i = 0; i < 4; i++) {
                final float x = emitter.x(i);
                final float y = emitter.y(i);
                emitter.x(i, x * cos - y * sin);
                emitter.y(i, x * sin + y * cos);
            }
            emitter.emit();
        }
    }

    final class ScissorTransform implements Transform {
        public final Rectangle rectangle;

        public ScissorTransform(final Rectangle rectangle) {
            this.rectangle = rectangle;
        }
    }

    WidgetQuadEmitter emitter();
}
