package io.github.stuff_stuffs.tbcexv3util.api.util;

import io.github.stuff_stuffs.tbcexv3util.impl.util.PathfinderImpl;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface Pathfinder {
    static <T> PathTree<T> find(final BlockPos start, final NeighbourGetter getter, final PostProcessor<T> processor) {
        return PathfinderImpl.find(start, getter, processor);
    }

    interface NeighbourGetter {
        void neighbours(BlockPos pos, Node node, NodeAppender appender);

        void start(BlockPos pos, NodeAppender appender);

    }

    interface NodeAppender {
        void append(BlockPos pos, double cost);

        double getCost(BlockPos pos);
    }

    interface Node {
        BlockPos pos();

        double cost();

        @Nullable Node previous();
    }

    interface PathTree<T> {
        Set<BlockPos> endPositions();

        T getPath(BlockPos end);
    }

    interface PostProcessor<T> {
        boolean isValidEndPoint(Node node);

        T process(List<BlockPos> positions);
    }
}
