package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.bounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BattleParticipantBoundsImpl implements BattleParticipantBounds {
    private static final Codec<Box> BOX_CODEC = Codec.DOUBLE.listOf().comapFlatMap(BattleParticipantBoundsImpl::of, BattleParticipantBoundsImpl::of);
    public static final Codec<BattleParticipantBoundsImpl> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.unboundedMap(Identifier.CODEC, BOX_CODEC).fieldOf("parts").forGetter(BattleParticipantBoundsImpl::encodable), BlockPos.CODEC.fieldOf("center").forGetter(bounds -> bounds.center)).apply(instance, BattleParticipantBoundsImpl::new));
    public static final Codec<BattleParticipantBounds> CASTED_CODEC = CodecUtil.castedCodec(CODEC, BattleParticipantBoundsImpl.class, BattleParticipantBounds.class);
    private final Map<Identifier, PartImpl> parts;
    private final BlockPos center;

    private BattleParticipantBoundsImpl(final Map<Identifier, Box> parts, final BlockPos center) {
        this(decodable(parts), center, null);
    }

    private BattleParticipantBoundsImpl(final Map<Identifier, PartImpl> parts, final BlockPos center, final Void hack) {
        this.parts = parts;
        this.center = center;
    }

    @Override
    public Iterator<Part> parts() {
        return parts.values().stream().map(part -> (Part) part).iterator();
    }

    @Override
    public Optional<Part> byId(final Identifier id) {
        return Optional.ofNullable(parts.get(id));
    }

    @Override
    public BlockPos center() {
        return center;
    }

    private Map<Identifier, Box> encodable() {
        final Map<Identifier, Box> map = new Object2ReferenceOpenHashMap<>();
        for (final PartImpl part : parts.values()) {
            map.put(part.id(), part.box());
        }
        return map;
    }

    public record PartImpl(Identifier id, Box box) implements Part {
    }

    public static final class BuilderImpl implements Builder {
        private final Map<Identifier, PartImpl> parts;
        private final BlockPos center;

        public BuilderImpl(final BlockPos center) {
            parts = new Object2ReferenceOpenHashMap<>();
            this.center = center;
        }

        @Override
        public Builder add(final Identifier id, final Box box) {
            if (parts.put(id, new PartImpl(id, box)) != null) {
                throw new IllegalArgumentException("Tried to put duplicate parts in BattleParticipantBounds!");
            }
            return this;
        }

        @Override
        public boolean contains(final Identifier id) {
            return parts.containsKey(id);
        }

        @Override
        public BlockPos center() {
            return center;
        }

        @Override
        public BattleParticipantBounds build() {
            return new BattleParticipantBoundsImpl(new Object2ReferenceOpenHashMap<>(parts), center, null);
        }
    }

    private static Map<Identifier, PartImpl> decodable(final Map<Identifier, Box> map) {
        final Map<Identifier, PartImpl> parts = new Object2ReferenceOpenHashMap<>();
        for (final Map.Entry<Identifier, Box> entry : map.entrySet()) {
            parts.put(entry.getKey(), new PartImpl(entry.getKey(), entry.getValue()));
        }
        return parts;
    }

    private static DataResult<Box> of(final List<Double> doubles) {
        if (doubles.size() == 6) {
            return DataResult.success(new Box(doubles.get(0), doubles.get(1), doubles.get(2), doubles.get(3), doubles.get(4), doubles.get(5)));
        }
        return DataResult.error("Incorrect number of arguments!");
    }

    private static List<Double> of(final Box box) {
        return List.of(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }
}
