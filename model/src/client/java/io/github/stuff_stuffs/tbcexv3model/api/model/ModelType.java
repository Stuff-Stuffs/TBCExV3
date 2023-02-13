package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import io.wispforest.owo.util.OwoFreezer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class ModelType {
    private static final Map<Identifier, ModelType> CACHE = new Object2ReferenceOpenHashMap<>();
    private static boolean FROZEN = false;
    public static final ModelType PARTICIPANT = getOrCreate(TBCExV3Model.id("model_type/participant"));
    public static final ModelType ENVIRONMENT = getOrCreate(TBCExV3Model.id("model_type/environment"));
    public static final ModelType PARTICLE = getOrCreate(TBCExV3Model.id("model_type/particle"));
    private final Identifier id;
    private final Set<ModelType> parents;
    private final Set<Identifier> requiredAnimations = new ObjectOpenHashSet<>();

    private ModelType(final Identifier id, final Set<ModelType> parents) {
        this.id = id;
        this.parents = parents;
    }

    public void addRequiredAnimation(final Identifier id) {
        if (FROZEN) {
            throw new RuntimeException();
        }
        requiredAnimations.add(id);
    }

    public Set<ModelType> parents() {
        return Collections.unmodifiableSet(parents);
    }

    public Set<Identifier> requiredAnimations() {
        return Collections.unmodifiableSet(requiredAnimations);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModelType type)) {
            return false;
        }

        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static ModelType getOrCreate(final Identifier id) {
        return CACHE.computeIfAbsent(id, identifier -> new ModelType(identifier, Set.of()));
    }

    public static ModelType getOrCreate(final Identifier id, final Set<ModelType> parents) {
        ModelType type = CACHE.get(id);
        if (type == null) {
            type = new ModelType(id, Set.copyOf(parents));
            CACHE.put(id, type);
        }
        return type;
    }

    static {
        OwoFreezer.registerFreezeCallback(() -> FROZEN = true);
        PARTICIPANT.addRequiredAnimation(RequiredAnimations.Participant.IDLE);
        PARTICIPANT.addRequiredAnimation(RequiredAnimations.Participant.DAMAGE_TAKEN);
        PARTICIPANT.addRequiredAnimation(RequiredAnimations.Participant.DEATH);
        PARTICIPANT.addRequiredAnimation(RequiredAnimations.Participant.SPAWN);

        ENVIRONMENT.addRequiredAnimation(RequiredAnimations.Environment.PHASE_IN);
        ENVIRONMENT.addRequiredAnimation(RequiredAnimations.Environment.PHASE_OUT);

        PARTICLE.addRequiredAnimation(RequiredAnimations.Particle.PHASE_IN);
        PARTICLE.addRequiredAnimation(RequiredAnimations.Particle.PHASE_OUT);
    }
}
