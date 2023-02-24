package io.github.stuff_stuffs.tbcexv3util.api.util;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.OptionalDouble;

@ApiStatus.NonExtendable
public interface OperationChainDisplayBuilder {
    void push(Operation operation, OptionalDouble arg, Text source);

    interface Operation {
        Operation ADD = create(id("add"), false);
        Operation SUBTRACT = create(id("subtract"), false);
        Operation MULTIPLY = create(id("multiply"), false);
        Operation DIVIDE = create(id("divide"), false);

        private static Identifier id(final String path) {
            return new Identifier("tbcexv3util", path);
        }

        boolean unary();

        Identifier id();

        static Operation create(final Identifier id, final boolean unary) {
            return new Operation() {
                @Override
                public boolean unary() {
                    return unary;
                }

                @Override
                public Identifier id() {
                    return id;
                }

                @Override
                public String toString() {
                    return id.toString();
                }
            };
        }
    }
}
