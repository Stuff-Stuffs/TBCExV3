package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OneHotComponent<I> extends BaseParentComponent {
    private I index;
    protected final Map<I, Component> componentsByIndex;
    private boolean cleared = false;

    protected OneHotComponent(final Sizing horizontalSizing, final Sizing verticalSizing, final Map<I, Component> componentsByIndex, final I index) {
        super(horizontalSizing, verticalSizing);
        this.componentsByIndex = new Object2ReferenceOpenHashMap<>(componentsByIndex);
        this.index = index;
    }

    protected OneHotComponent(final Sizing horizontalSizing, final Sizing verticalSizing, final I index) {
        super(horizontalSizing, verticalSizing);
        componentsByIndex = new Object2ReferenceOpenHashMap<>();
        this.index = index;
    }

    protected Component child() {
        return componentsByIndex.get(index);
    }

    @Override
    protected int determineHorizontalContentSize(final Sizing sizing) {
        final Component child = child();
        return (child != null ? child.fullSize().width() : 0) + padding.get().horizontal();
    }

    @Override
    protected int determineVerticalContentSize(final Sizing sizing) {
        final Component child = child();
        return (child != null ? child.fullSize().height() : 0) + padding.get().vertical();
    }

    public void setIndex(final I index) {
        if (cleared || !index.equals(this.index)) {
            final Component child = child();
            if (child != null) {
                child.dismount(DismountReason.REMOVED);
            }
            this.index = index;
            updateLayout();
            cleared = false;
        }
    }

    protected int childMountX(final Component child) {
        return x + child.margins().get().left() + padding.get().left();
    }

    protected int childMountY(final Component child) {
        return y + child.margins().get().top() + padding.get().top();
    }

    @Override
    public void layout(final Size space) {
        this.space = space;
        final Component component = componentsByIndex.get(index);
        component.dismount(DismountReason.LAYOUT_INFLATION);
        component.inflate(calculateChildSpace(space));
        component.mount(this, childMountX(component), childMountY(component));
    }

    @Override
    public List<Component> children() {
        final Component child = child();
        if (child != null) {
            return List.of(child);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public ParentComponent removeChild(final Component child) {
        final Iterator<Map.Entry<I, Component>> iterator = componentsByIndex.entrySet().iterator();
        boolean updated = false;
        while (iterator.hasNext()) {
            final Map.Entry<I, Component> entry = iterator.next();
            final Component component = entry.getValue();
            if (component.equals(child)) {
                if (entry.getKey().equals(index)) {
                    updated = true;
                    component.dismount(DismountReason.REMOVED);
                }
                iterator.remove();
            }
        }
        if (updated) {
            updateLayout();
        }
        cleared = true;
        return this;
    }

    protected void clear() {
        final Iterator<Map.Entry<I, Component>> iterator = componentsByIndex.entrySet().iterator();
        boolean updated = false;
        while (iterator.hasNext()) {
            final Map.Entry<I, Component> entry = iterator.next();
            final Component component = entry.getValue();
            if (entry.getKey().equals(index)) {
                updated = true;
                component.dismount(DismountReason.REMOVED);
            }
            iterator.remove();
        }
        if (updated) {
            updateLayout();
        }
        componentsByIndex.clear();
    }

    public static final class StringIndexed extends OneHotComponent<String> {
        public StringIndexed(final Sizing horizontalSizing, final Sizing verticalSizing, final Map<String, Component> componentsByIndex, final String index) {
            super(horizontalSizing, verticalSizing, componentsByIndex, index);
        }

        public StringIndexed(final Sizing horizontalSizing, final Sizing verticalSizing) {
            super(horizontalSizing, verticalSizing, "");
        }

        @Override
        public void parseProperties(final UIModel model, final Element element, final Map<String, Element> children) {
            super.parseProperties(model, element, children);

            try {
                final Element childElement = children.get("children");
                if (childElement != null) {
                    final Map<String, Element> newChildren = UIParsing.childElements(childElement);
                    for (final Map.Entry<String, Element> entry : newChildren.entrySet()) {
                        componentsByIndex.put(entry.getKey(), model.parseComponent(Component.class, entry.getValue()));
                    }
                }
                clear();
                setIndex(element.getAttribute("index"));
            } catch (final UIModelParsingException exception) {
                throw new UIModelParsingException("Could not initialize container child", exception);
            }
        }

        public static StringIndexed parse(final Element element) {
            UIParsing.expectAttributes(element, "index");
            UIParsing.expectChildren(element, UIParsing.childElements(element), "children");
            return new StringIndexed(Sizing.content(), Sizing.content());
        }
    }
}
