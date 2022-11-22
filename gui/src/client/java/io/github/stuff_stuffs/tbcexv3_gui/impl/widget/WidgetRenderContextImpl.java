package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetQuadEmitter;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetTextEmitter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WidgetRenderContextImpl implements AbstractWidgetRenderContext {
    private final float z;
    private final WidgetTextEmitter textEmitter;
    private final AbstractWidgetRenderContext parent;
    private final ObjectArrayList<Transform> transforms;
    private final IntArrayList scissorStates;
    private int children = 0;

    public WidgetRenderContextImpl(final float z, final AbstractWidgetRenderContext parent) {
        this.z = z;
        textEmitter = new WidgetTextEmitterImpl(this::draw, z + 0.1F);
        this.parent = parent;
        transforms = new ObjectArrayList<>();
        scissorStates = new IntArrayList();
    }

    @Override
    public float time() {
        return parent.time();
    }

    @Override
    public WidgetQuadEmitter emitter() {
        return new WidgetQuadEmitterImpl(z, this::draw);
    }

    @Override
    public void pushTransform(final Transform transform) {
        transforms.push(transform);
        if (transform instanceof ScissorTransform scissor) {
            scissorStates.push(parent.createScissorState(scissorStates.isEmpty() ? -1 : scissorStates.peekInt(0), transformScissors(scissor)));
        }
    }

    private ScissorTransform transformScissors(final ScissorTransform transform) {
        final Rectangle rect = transform.rectangle;
        final EmittedQuad emittedQuad = new EmittedQuad(z, new float[]{(float) rect.lower().x(), (float) rect.lower().x(), (float) rect.upper().x(), (float) rect.upper().x()}, new float[]{(float) rect.lower().y(), (float) rect.upper().y(), (float) rect.upper().y(), (float) rect.lower().y()}, new float[4], new float[4], new int[4], new int[4], false);
        transformScissors(emittedQuad);
        final Point2d lower = new Point2d(emittedQuad.x(0), emittedQuad.y(0));
        final Point2d upper = new Point2d(emittedQuad.x(2), emittedQuad.y(2));
        return new ScissorTransform(new Rectangle(lower, upper));
    }

    @Override
    public void popTransform() {
        if (transforms.pop() instanceof ScissorTransform) {
            scissorStates.popInt();
        }
    }

    @Override
    public WidgetRenderContext child() {
        return new WidgetRenderContextImpl(z + children++ + 1, this);
    }

    @Override
    public WidgetTextEmitter textEmitter() {
        return textEmitter;
    }

    @Override
    public void transformScissors(final EmittedQuad quad) {
        for (final Transform transform : transforms) {
            if (transform instanceof QuadTransform quadTransform) {
                quadTransform.transform(quad);
            }
        }
        parent.transformScissors(quad);
    }

    @Override
    public int createScissorState(final int parent, final ScissorTransform transform) {
        return parent == -1 && !scissorStates.isEmpty() ? this.parent.createScissorState(scissorStates.peekInt(0), transform) : this.parent.createScissorState(parent, transform);
    }

    @Override
    public void draw(final EmittedQuad quad) {
        if (quad.scissorState() == -1 && !scissorStates.isEmpty()) {
            quad.scissorState(scissorStates.peekInt(0));
        }
        for (final Transform transform : transforms) {
            if (transform instanceof QuadTransform quadTransform) {
                quadTransform.transform(quad);
                if (!quad.clear()) {
                    return;
                }
            }
        }
        parent.draw(quad);
    }
}
