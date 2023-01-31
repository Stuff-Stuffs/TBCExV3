package io.github.stuff_stuffs.tbcexv3core.api.util;

import io.github.stuff_stuffs.tbcexv3core.internal.client.mixin.AccessorTextHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.*;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TBCExTextUtil {
    private TBCExTextUtil() {
    }

    public static int textEndIndex(final Text text, final int startIndex, final int maxWidth, final TextRenderer textRenderer) {
        final TextHandler.WidthRetriever widthRetriever = ((AccessorTextHandler) textRenderer.getTextHandler()).tbcex$getWidthRetriever();
        final WidthLimitingVisitor visitor = new WidthLimitingVisitor(widthRetriever, maxWidth);
        if (startIndex == 0) {
            TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor);
        } else {
            TextVisitFactory.visitFormatted(text, Style.EMPTY, new IndexWaitingVisitor(visitor, startIndex));
            return visitor.getLength() + startIndex;
        }
        return visitor.getLength();
    }

    public static StringVisitable subtext(final Text text, final int startInclusive, final int endExclusive) {
        if (startInclusive == endExclusive) {
            return StringVisitable.EMPTY;
        }
        final List<StyledString> strings = new ArrayList<>();
        text.visit((style, asString) -> {
            strings.add(new StyledString(asString, style));
            return Optional.empty();
        }, Style.EMPTY);
        int sum = 0;
        final TextCollector collector = new TextCollector();
        int index = 0;
        while (sum + strings.get(index).literal.length() < startInclusive) {
            sum += strings.get(index).literal.length();
            index++;
        }
        if (sum < startInclusive) {
            final StyledString string = strings.get(index);
            final String s = string.literal.substring(startInclusive - sum);
            collector.add(StringVisitable.styled(s, string.style));
            sum = startInclusive + s.length();
        }
        while (sum + strings.get(index).literal.length() < endExclusive) {
            final StyledString string = strings.get(index);
            collector.add(StringVisitable.styled(string.literal, string.style));
            sum += string.literal.length();
            index++;
        }
        if (sum < endExclusive - 1) {
            final StyledString string = strings.get(index);
            final String s = string.literal.substring(0, endExclusive - sum);
            collector.add(StringVisitable.styled(s, string.style));
        }
        return collector.getCombined();
    }

    public static Optional<List<OrderedText>> splitContentDependent(final Text text, final TextRenderer textRenderer, final ContentDependentTextDisplay display) {
        final int max = textEndIndex(text, 0, Integer.MAX_VALUE, textRenderer);
        int iterCount = 0;
        final int MAX_ITER_COUNT = 128;
        while (true) {
            final Optional<IntList> opt = display.maxWidths();
            if (opt.isEmpty()) {
                return Optional.empty();
            }
            final IntList list = opt.get();
            final IntList textIndices = new IntArrayList();
            while (textIndices.size() < list.size()) {
                final int maxWidth = list.getInt(textIndices.size());
                final int lastEnd;
                if (textIndices.isEmpty()) {
                    lastEnd = 0;
                } else {
                    lastEnd = textIndices.getInt(textIndices.size() - 1);
                }
                final int endIndex = textEndIndex(text, lastEnd, maxWidth, textRenderer);
                textIndices.add(endIndex);
                if (endIndex == max) {
                    final int size = textIndices.size();
                    final List<OrderedText> texts = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        final int last = i == 0 ? 0 : textIndices.getInt(i - 1);
                        texts.add(Language.getInstance().reorder(subtext(text, last, textIndices.getInt(i))));
                    }
                    return Optional.of(texts);
                }
            }
            if (iterCount == MAX_ITER_COUNT) {
                return Optional.empty();
            }
            iterCount++;
            display.incrementLineCount();
        }
    }

    public interface ContentDependentTextDisplay {
        Optional<IntList> maxWidths();

        void incrementLineCount();
    }

    private static class IndexWaitingVisitor implements CharacterVisitor {
        private final CharacterVisitor delegate;
        private final int start;

        private IndexWaitingVisitor(final CharacterVisitor delegate, final int start) {
            this.delegate = delegate;
            this.start = start;
        }

        @Override
        public boolean accept(final int index, final Style style, final int codePoint) {
            return index < start || delegate.accept(index - start, style, codePoint);
        }
    }

    private static class StyledString implements StringVisitable {
        final String literal;
        final Style style;

        public StyledString(final String literal, final Style style) {
            this.literal = literal;
            this.style = style;
        }

        @Override
        public <T> Optional<T> visit(final Visitor<T> visitor) {
            return visitor.accept(literal);
        }

        @Override
        public <T> Optional<T> visit(final StyledVisitor<T> styledVisitor, final Style style) {
            return styledVisitor.accept(this.style.withParent(style), literal);
        }
    }

    private static class WidthLimitingVisitor implements CharacterVisitor {
        private final TextHandler.WidthRetriever widthRetriever;
        private float widthLeft;
        private int length;

        public WidthLimitingVisitor(final TextHandler.WidthRetriever retriever, final float maxWidth) {
            widthRetriever = retriever;
            widthLeft = maxWidth;
        }

        @Override
        public boolean accept(final int i, final Style style, final int j) {
            widthLeft -= widthRetriever.getWidth(j, style);
            if (widthLeft >= 0.0F) {
                length = i + Character.charCount(j);
                return true;
            } else {
                return false;
            }
        }

        public int getLength() {
            return length;
        }

        public void resetLength() {
            length = 0;
        }
    }

    private static class LineWrappingCollector {
        final List<StyledString> parts;
        private String joined;

        public LineWrappingCollector(final List<StyledString> parts) {
            this.parts = parts;
            joined = parts.stream().map(part -> part.literal).collect(Collectors.joining());
        }

        public char charAt(final int index) {
            return joined.charAt(index);
        }

        public StringVisitable collectLine(final int lineLength, final int skippedLength, final Style style) {
            final TextCollector textCollector = new TextCollector();
            final ListIterator<StyledString> listIterator = parts.listIterator();
            int i = lineLength;
            boolean bl = false;

            while (listIterator.hasNext()) {
                final StyledString styledString = listIterator.next();
                final String string = styledString.literal;
                final int j = string.length();
                if (!bl) {
                    if (i > j) {
                        textCollector.add(styledString);
                        listIterator.remove();
                        i -= j;
                    } else {
                        final String string2 = string.substring(0, i);
                        if (!string2.isEmpty()) {
                            textCollector.add(StringVisitable.styled(string2, styledString.style));
                        }

                        i += skippedLength;
                        bl = true;
                    }
                }

                if (bl) {
                    if (i <= j) {
                        final String string2 = string.substring(i);
                        if (string2.isEmpty()) {
                            listIterator.remove();
                        } else {
                            listIterator.set(new StyledString(string2, style));
                        }
                        break;
                    }

                    listIterator.remove();
                    i -= j;
                }
            }

            joined = joined.substring(lineLength + skippedLength);
            return textCollector.getCombined();
        }

        @Nullable
        public StringVisitable collectRemainders() {
            final TextCollector textCollector = new TextCollector();
            parts.forEach(textCollector::add);
            parts.clear();
            return textCollector.getRawCombined();
        }
    }
}
