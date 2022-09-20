package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import com.mojang.serialization.DataResult;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;
import org.mapdb.serializer.SerializerArray;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

//TODO off thread file loading
public class ServerBattleWorldDatabase implements AutoCloseable {
    private static final BattleOrPartialSerializer PARTIAL_BATTLE_SERIALIZER = new BattleOrPartialSerializer();
    private static final Serializer<PastBattle[]> UUID_ARRAY_SERIALIZER = new SerializerArray<>(new PastBattleSerializer());
    private static final PackedSerializer DELAYED_COMPONENT_SERIALIZER = new PackedSerializer();
    private final DB db;
    private final HTreeMap<UUID, BattleOrPartial> battles;
    private final HTreeMap<UUID, PastBattle[]> battleEntities;
    private final HTreeMap<UUID, DelayedComponentsPacked> delayedComponents;

    public ServerBattleWorldDatabase(final Path file) {
        db = DBMaker.fileDB(file.toFile()).fileMmapEnableIfSupported().make();
        battles = db.hashMap("battles", Serializer.UUID, PARTIAL_BATTLE_SERIALIZER).createOrOpen();
        battleEntities = db.hashMap("battle_entities", Serializer.UUID, UUID_ARRAY_SERIALIZER).createOrOpen();
        delayedComponents = db.hashMap("delayed_components", Serializer.UUID, DELAYED_COMPONENT_SERIALIZER).createOrOpen();
    }

    public void saveBattle(final UUID uuid, final Battle battle) {
        battles.put(uuid, new NonPartialBattle(battle));
    }

    public UUID[] getParticipatedBattles(final UUID uuid, final TriState active) {
        if (active == TriState.TRUE) {
            return Arrays.stream(battleEntities.getOrDefault(uuid, new PastBattle[0])).filter(PastBattle::active).map(PastBattle::id).toArray(UUID[]::new);
        } else if (active == TriState.FALSE) {
            return Arrays.stream(battleEntities.getOrDefault(uuid, new PastBattle[0])).filter(Predicate.not(PastBattle::active)).map(PastBattle::id).toArray(UUID[]::new);
        }
        return Arrays.stream(battleEntities.getOrDefault(uuid, new PastBattle[0])).map(PastBattle::id).toArray(UUID[]::new);
    }

    public DataResult<BiFunction<BattleHandle, BattleStateMode, Battle>> loadBattle(final UUID uuid) {
        final BattleOrPartial partial = battles.get(uuid);
        if (partial == null) {
            return DataResult.error("Battle does not exist");
        }
        return DataResult.success(((PartialBattle) partial).partial);
    }

    public void onBattleJoin(final UUID entityId, final UUID battleId, final boolean active) {
        battleEntities.merge(entityId, new PastBattle[]{new PastBattle(battleId, active)}, (currentIds, newId) -> {
            final PastBattle key = newId[0];
            final int i = Arrays.binarySearch(currentIds, key, PastBattle.COMPARATOR);
            final PastBattle[] copy;
            if (i < 0) {
                copy = Arrays.copyOf(currentIds, currentIds.length + 1);
                final int index = (-i) - 1;
                if (index == 0) {
                    System.arraycopy(copy, 0, copy, 1, copy.length - 1);
                    copy[0] = key;
                } else if (index == copy.length - 1) {
                    copy[index] = key;
                } else {
                    System.arraycopy(copy, index, copy, index + 1, copy.length - index - 1);
                    copy[index] = key;
                }
            } else {
                copy = Arrays.copyOf(currentIds, currentIds.length);
                copy[i] = key;
            }
            return copy;
        });
    }

    @Override
    public void close() {
        db.close();
    }

    public UUID findUnusedBattleUuid(final Random random) {
        UUID uuid;
        do {
            uuid = new UUID(random.nextLong(), random.nextLong());
        } while (battles.containsKey(uuid));
        return uuid;
    }

    public ServerBattleWorldContainer.DelayedComponents getDelayedComponents(final UUID uuid) {
        final DelayedComponentsPacked packed = delayedComponents.get(uuid);
        if (packed == null) {
            return new ServerBattleWorldContainer.DelayedComponents(Map.of());
        }
        return new ServerBattleWorldContainer.DelayedComponents(packed.map);
    }

    public void saveDelayedComponents(final UUID entityId, final ServerBattleWorldContainer.DelayedComponents components) {
        if (components == null) {
            delayedComponents.remove(entityId);
        } else {
            final DelayedComponentsPacked packed = new DelayedComponentsPacked(Map.copyOf(components.components));
            delayedComponents.put(entityId, packed);
        }
    }

    private sealed interface BattleOrPartial {

    }

    private record NonPartialBattle(Battle battle) implements BattleOrPartial {
    }

    private record PartialBattle(BiFunction<BattleHandle, BattleStateMode, Battle> partial) implements BattleOrPartial {
    }

    private static final class BattleOrPartialSerializer implements Serializer<BattleOrPartial> {
        @Override
        public void serialize(@NotNull final DataOutput2 out, @NotNull final BattleOrPartial value) throws IOException {
            final NbtCompound wrapper = new NbtCompound();
            if (!(value instanceof NonPartialBattle nonPartial)) {
                throw new TBCExException("Tried to save a partial battle!");
            }
            wrapper.put("data", Battle.encoder().encodeStart(NbtOps.INSTANCE, nonPartial.battle).getOrThrow(false, s -> {
                throw new TBCExException(s);
            }));
            NbtIo.write(wrapper, out);
        }

        @Override
        public BattleOrPartial deserialize(@NotNull final DataInput2 input, final int available) throws IOException {
            final NbtCompound wrapper = NbtIo.read(input);
            final NbtElement element = wrapper.get("data");
            final DataResult<BattleOrPartial> result = Battle.decoder().parse(NbtOps.INSTANCE, element).map(PartialBattle::new);
            return result.getOrThrow(false, s -> {
                throw new TBCExException(s);
            });
        }

        @Override
        public boolean isTrusted() {
            return true;
        }
    }

    private record DelayedComponentsPacked(Map<UUID, List<BattleEntityComponent>> map) {
    }

    private static final class PackedSerializer implements Serializer<DelayedComponentsPacked> {

        @Override
        public void serialize(@NotNull final DataOutput2 out, @NotNull final ServerBattleWorldDatabase.DelayedComponentsPacked value) throws IOException {
            final Map<java.util.UUID, List<BattleEntityComponent>> map = value.map;
            out.writeInt(map.size());
            for (final Map.Entry<java.util.UUID, List<BattleEntityComponent>> entry : map.entrySet()) {
                final java.util.UUID key = entry.getKey();
                out.writeLong(key.getLeastSignificantBits());
                out.writeLong(key.getMostSignificantBits());
                final List<BattleEntityComponent> components = entry.getValue();
                out.writeInt(components.size());
                for (final BattleEntityComponent component : components) {
                    encodeComponent(out, component, component.getType());
                }
            }
        }

        @Override
        public DelayedComponentsPacked deserialize(@NotNull final DataInput2 input, final int available) throws IOException {
            final int size = input.readInt();
            final Map<java.util.UUID, List<BattleEntityComponent>> componentMap = new Object2ReferenceOpenHashMap<>();
            for (int i = 0; i < size; i++) {
                final long least = input.readLong();
                final long most = input.readLong();
                final java.util.UUID uuid = new java.util.UUID(most, least);
                final int count = input.readInt();
                final List<BattleEntityComponent> components = new ArrayList<>(count);
                for (int j = 0; j < count; j++) {
                    components.add(decodeComponent(input));
                }
                componentMap.put(uuid, components);
            }
            return new DelayedComponentsPacked(componentMap);
        }
    }

    private static <T extends BattleEntityComponent> void encodeComponent(final DataOutput2 out, final BattleEntityComponent component, final BattleEntityComponentType<T> type) throws IOException {
        final DataResult<NbtElement> encode = type.encode(NbtOps.INSTANCE, component);
        final NbtCompound wrapper = new NbtCompound();
        wrapper.put("data", encode.getOrThrow(false, s -> {
            throw new TBCExException(s);
        }));
        NbtIo.write(wrapper, out);
    }

    private static BattleEntityComponent decodeComponent(final DataInput2 in) throws IOException {
        final NbtCompound wrapper = NbtIo.read(in);
        final NbtElement encoded = wrapper.get("data");
        final DataResult<BattleEntityComponent> parsed = BattleEntityComponent.CODEC.parse(NbtOps.INSTANCE, encoded);
        return parsed.getOrThrow(false, s -> {
            throw new TBCExException(s);
        });
    }

    private static final class PastBattleSerializer implements Serializer<PastBattle> {
        @Override
        public void serialize(@NotNull final DataOutput2 out, @NotNull final ServerBattleWorldDatabase.PastBattle value) throws IOException {
            out.writeLong(value.id().getMostSignificantBits());
            out.writeLong(value.id().getLeastSignificantBits());
            out.writeBoolean(value.active());
        }

        @Override
        public PastBattle deserialize(@NotNull final DataInput2 input, final int available) throws IOException {
            return new PastBattle(new UUID(input.readLong(), input.readLong()), input.readBoolean());
        }

        @Override
        public boolean equals(final PastBattle first, final PastBattle second) {
            return Objects.equals(first, second);
        }

        @Override
        public int hashCode(@NotNull final ServerBattleWorldDatabase.PastBattle o, final int seed) {
            final long a = o.id().getLeastSignificantBits() ^ o.id().getMostSignificantBits();
            return ((int) (a >> 32)) ^ (int) a ^ (o.active ? ((int) (a * 65535)) : 0);
        }
    }

    private record PastBattle(UUID id, boolean active) {
        private static final Comparator<PastBattle> COMPARATOR = Comparator.comparingLong((PastBattle o) -> o.id().getMostSignificantBits()).thenComparingLong(o -> o.id().getLeastSignificantBits());
    }
}
