package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface WidgetRenderContext {
    float time();

    void pushTransform(Transform transform);

    void popTransform();

    WidgetRenderContext child();

    WidgetTextEmitter textEmitter();

    sealed interface Transform {
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
                emitter.x(i, emitter.x(i) - (float) x);
                emitter.y(i, emitter.y(i) - (float) y);
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
