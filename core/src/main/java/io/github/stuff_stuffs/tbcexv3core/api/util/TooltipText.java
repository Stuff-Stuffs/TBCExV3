package io.github.stuff_stuffs.tbcexv3core.api.util;

import net.minecraft.text.OrderedText;

import java.util.List;

public record TooltipText(List<OrderedText> texts) {
    public static final TooltipText EMPTY = new TooltipText(List.of());
}
