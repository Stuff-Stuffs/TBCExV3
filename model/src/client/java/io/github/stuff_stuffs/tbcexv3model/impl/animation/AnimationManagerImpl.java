package io.github.stuff_stuffs.tbcexv3model.impl.animation;

import io.github.stuff_stuffs.tbcexv3model.api.animation.*;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interval;
import io.github.stuff_stuffs.tbcexv3model.impl.model.skeleton.SkeletonImpl;
import io.github.stuff_stuffs.tbcexv3model.impl.scene.AnimationSceneImpl;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleHeapPriorityQueue;
import it.unimi.dsi.fastutil.doubles.DoublePriorityQueue;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnimationManagerImpl<T> implements AnimationManager<T> {
    private final AnimationSceneImpl scene = new AnimationSceneImpl();
    private final List<ScheduledAnimation<T>> scheduledAnimations = new ArrayList<>();
    private State state = new State();
    private double lastTime = Double.NEGATIVE_INFINITY;

    private static <T> @Nullable List<AnimationDependency> dependencySolve(final State working, final Animation<T> animation, final double t) {
        final List<AnimationDependency> dependencies = animation.dependencies(t, working.supplied);
        final List<AnimationDependency> acceptedDependencies = new ArrayList<>();
        for (final AnimationDependency dependency : dependencies) {
            if (dependency.resource() instanceof AnimationResource.ModelResource modelResource) {
                final ResourceTracker tracker = working.modelTrackers.computeIfAbsent(modelResource.id(), i -> new ResourceTracker());
                if (dependency.required() && !tracker.modelLiveWhile(dependency.interval(), t)) {
                    return null;
                }
                if (!tracker.modelFreeWhile(dependency.interval(), t, dependency.exclusive())) {
                    return null;
                }
                acceptedDependencies.add(dependency);
            } else if (dependency.resource() instanceof AnimationResource.BoneResource boneResource) {
                final ResourceTracker modelTracker = working.modelTrackers.computeIfAbsent(boneResource.resource().id(), i -> new ResourceTracker());
                if (!modelTracker.boneLiveWhile(dependency.interval(), boneResource.id(), t) || !modelTracker.boneFreeWhile(dependency.interval(), boneResource.id(), t, dependency.exclusive())) {
                    if (dependency.required()) {
                        return null;
                    } else {
                        continue;
                    }
                }
            }
            acceptedDependencies.add(dependency);
        }
        for (final AnimationDependency dependency : acceptedDependencies) {
            working.events.add(dependency.interval().start() + t);
            working.events.add(dependency.interval().end() + t);
            final Interval offset = new Interval(dependency.interval().start() + t, dependency.interval().end() + t);
            final ResourceTracker.UsedInterval usedInterval = new ResourceTracker.UsedInterval(offset, dependency.exclusive());
            if (dependency.resource() instanceof AnimationResource.ModelResource modelResource) {
                final ResourceTracker tracker = working.modelTrackers.computeIfAbsent(modelResource.id(), i -> new ResourceTracker());
                tracker.usages.add(usedInterval);
            } else if (dependency.resource() instanceof AnimationResource.BoneResource boneResource) {
                final ResourceTracker tracker = working.modelTrackers.computeIfAbsent(boneResource.resource().id(), i -> new ResourceTracker());
                tracker.boneUsage.computeIfAbsent(boneResource.id(), i -> new ArrayList<>()).add(usedInterval);
            }
        }
        return acceptedDependencies;
    }

    private OptionalDouble suppliedSolve(final Animation<T> animation, final double min) {
        double t = Math.nextAfter(min, -1.0);
        outer:
        while (true) {
            final DoubleSortedSet tailSet = state.events.tailSet(t);
            if (tailSet.isEmpty()) {
                return OptionalDouble.empty();
            }
            t = tailSet.firstDouble();
            final State working = state.copy();
            final List<AnimationSupplied> suppliedResources = animation.supplied(t);
            for (final AnimationSupplied supplied : suppliedResources) {
                final Interval offset = new Interval(t + supplied.interval().start(), t + supplied.interval().end());
                if (supplied.resource() instanceof AnimationResource.ModelResource modelResource) {
                    final ResourceTracker tracker = working.modelTrackers.computeIfAbsent(modelResource.id(), i -> new ResourceTracker());
                    if (tracker.modelFreeWhile(supplied.interval(), t, !supplied.soft())) {
                        continue outer;
                    }
                    tracker.liveness.add(offset);
                } else if (supplied.resource() instanceof AnimationResource.BoneResource boneResource) {
                    final ResourceTracker tracker = working.modelTrackers.computeIfAbsent(boneResource.resource().id(), i -> new ResourceTracker());
                    if (tracker.boneFreeWhile(supplied.interval(), boneResource.id(), t, !supplied.soft())) {
                        continue outer;
                    }
                    tracker.boneLiveness.computeIfAbsent(boneResource.id(), i -> new ArrayList<>()).add(offset);
                }
                state.supplied.add(new AnimationSupplied(supplied.resource(), offset, supplied.soft()));
            }
            final List<AnimationDependency> dependencies = dependencySolve(working, animation, t);
            if (dependencies != null) {
                state = working;
                scheduledAnimations.add(animation.schedule(t, scene, dependencies));
                return OptionalDouble.of(t);
            }
        }
    }

    @Override
    public void update(final double time) {
        for (final Map.Entry<Identifier, ResourceTracker> entry : state.modelTrackers.entrySet()) {
            final ResourceTracker tracker = entry.getValue();
            final boolean liveLast = tracker.live(lastTime);
            final boolean liveCurrent = tracker.live(time);
            if (liveLast ^ liveCurrent) {
                if (liveCurrent) {
                    scene.addModel(entry.getKey());
                } else {
                    scene.removeModel(entry.getKey());
                }
            }
        }
        for (final Map.Entry<Identifier, ResourceTracker> entry : state.modelTrackers.entrySet()) {
            final ResourceTracker tracker = entry.getValue();
            for (final Map.Entry<Identifier, List<Interval>> boneL : tracker.boneLiveness.entrySet()) {
                final Identifier boneId = boneL.getKey();
                final boolean boneLiveLast = tracker.liveBone(boneId, lastTime);
                final boolean boneLiveCurrent = tracker.liveBone(boneId, time);
                if (boneLiveLast ^ boneLiveCurrent) {
                    if (boneLiveCurrent) {
                        ((SkeletonImpl) scene.model(entry.getKey()).skeleton()).addBone(boneId, Optional.empty());
                    } else {
                        ((SkeletonImpl) scene.model(entry.getKey()).skeleton()).removeBone(boneId);
                    }
                }
            }
        }
        lastTime = time;
    }

    @Override
    public OptionalDouble submit(final Animation<T> animation, final double after) {
        return suppliedSolve(animation, after);
    }

    @Override
    public AnimationScene scene() {
        return scene;
    }

    private static final class ResourceTracker {
        private final List<Interval> liveness = new ArrayList<>();
        private final List<UsedInterval> usages = new ArrayList<>();
        private final Map<Identifier, List<Interval>> boneLiveness = new Object2ReferenceOpenHashMap<>();
        private final Map<Identifier, List<UsedInterval>> boneUsage = new Object2ReferenceOpenHashMap<>();

        private record UsedInterval(Interval interval, boolean exclusive) {
        }

        public boolean live(final double time) {
            for (final Interval interval : liveness) {
                if (interval.start() <= time && time < interval.end()) {
                    return true;
                }
            }
            return false;
        }

        public boolean liveBone(final Identifier boneId, final double time) {
            final List<Interval> intervals = boneLiveness.get(boneId);
            if (intervals == null || intervals.isEmpty()) {
                return false;
            }
            for (final Interval interval : intervals) {
                if (interval.start() <= time && time < interval.end()) {
                    return true;
                }
            }
            return false;
        }

        public ResourceTracker copy() {
            final ResourceTracker copy = new ResourceTracker();
            copy.liveness.addAll(liveness);
            copy.usages.addAll(usages);
            for (final Map.Entry<Identifier, List<Interval>> entry : boneLiveness.entrySet()) {
                copy.boneLiveness.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            for (final Map.Entry<Identifier, List<UsedInterval>> entry : boneUsage.entrySet()) {
                copy.boneUsage.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return copy;
        }

        public boolean modelLiveWhile(final Interval interval, final double t) {
            return liveWhile(interval, t, liveness);
        }

        public boolean boneLiveWhile(final Interval interval, final Identifier boneId, final double t) {
            if (!liveWhile(interval, t, liveness)) {
                return false;
            }
            final List<Interval> intervals = boneLiveness.get(boneId);
            if (intervals == null) {
                return false;
            }
            return liveWhile(interval, t, intervals);
        }

        private static boolean liveWhile(final Interval interval, final double t, final List<Interval> intervals) {
            final DoublePriorityQueue liveTickets = new DoubleHeapPriorityQueue();
            final DoublePriorityQueue deadTickets = new DoubleHeapPriorityQueue();
            for (final Interval live : intervals) {
                if (live.end() < interval.start() + t || interval.end() + t < live.start()) {
                    continue;
                }
                liveTickets.enqueue(live.start());
                deadTickets.enqueue(live.end());
            }
            if (liveTickets.isEmpty()) {
                return false;
            }
            int refCount = 1;
            double time = liveTickets.dequeueDouble();
            if (interval.start() + t < time) {
                return false;
            }
            while (!liveTickets.isEmpty() || !deadTickets.isEmpty() && time < interval.end() + t) {
                if (liveTickets.isEmpty()) {
                    time = deadTickets.dequeueDouble();
                    refCount--;
                } else if (deadTickets.isEmpty()) {
                    time = liveTickets.dequeueDouble();
                    refCount++;
                } else {
                    if (liveTickets.firstDouble() <= deadTickets.firstDouble()) {
                        time = liveTickets.dequeueDouble();
                        refCount++;
                    } else {
                        time = deadTickets.dequeueDouble();
                        refCount--;
                        if (refCount == 0 && time < interval.end() + t) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public boolean modelFreeWhile(final Interval interval, final double t, final boolean exclusive) {
            if (!freeWhile(interval, exclusive, t, usages)) {
                return false;
            }
            if (exclusive) {
                for (final List<UsedInterval> value : boneUsage.values()) {
                    if (!freeWhile(interval, exclusive, t, value)) {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean boneFreeWhile(final Interval interval, final Identifier boneId, final double t, final boolean exclusive) {
            if (!freeWhile(interval, false, t, usages)) {
                return false;
            }
            final List<UsedInterval> intervals = boneUsage.get(boneId);
            if (intervals == null) {
                return true;
            }
            return freeWhile(interval, exclusive, t, intervals);
        }

        private static boolean freeWhile(final Interval interval, final boolean exclusive, final double t, final List<UsedInterval> usages) {
            for (final UsedInterval used : usages) {
                final Interval usage = used.interval();
                if (usage.end() <= interval.end()) {
                    if (interval.start() + t <= usage.end() && (exclusive || used.exclusive())) {
                        return false;
                    }
                } else {
                    if (usage.start() + t < interval.end() && (exclusive || used.exclusive())) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static final class State {
        private final DoubleSortedSet events;
        private final Map<Identifier, ResourceTracker> modelTrackers;
        private final List<AnimationSupplied> supplied = new ArrayList<>();

        private State() {
            events = new DoubleAVLTreeSet();
            modelTrackers = new Object2ReferenceOpenHashMap<>();
        }

        public State copy() {
            final State copy = new State();
            copy.events.addAll(events);
            for (final Map.Entry<Identifier, ResourceTracker> entry : modelTrackers.entrySet()) {
                copy.modelTrackers.put(entry.getKey(), entry.getValue().copy());
            }
            copy.supplied.addAll(supplied);
            return copy;
        }
    }
}
