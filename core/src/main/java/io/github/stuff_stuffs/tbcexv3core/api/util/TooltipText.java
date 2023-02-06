package io.github.stuff_stuffs.tbcexv3core.api.util;

import net.minecraft.text.Text;

import java.util.List;

public record TooltipText(List<Text> texts) {
    public static final TooltipText EMPTY = new TooltipText(List.of());
}
