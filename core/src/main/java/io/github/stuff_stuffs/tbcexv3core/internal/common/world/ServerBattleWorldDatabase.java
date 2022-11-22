package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import com.mojang.serialization.DataResult;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.CompoundByteIterable;
import jetbrains.exodus.FixedLengthByteIterable;
import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.env.*;
import jetbrains.exodus.util.ByteArraySizedInputStream;
import jetbrains.exodus.util.LightOutputStream;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

//TODO off thread file loading
public class ServerBattleWorldDatabase implements AutoCloseable {
    private static final StoreConfig STORE_CONFIG = StoreConfig.getStoreConfig(false, false);
    private static final String BATTLE_STORE = "battles";
    private static final String BATTLE_ACTIVE_ENTITIES_STORE = "battle_entities";
    private static final String BATTLE_INACTIVE_ENTITIES_STORE = "battle_entities";
    private static final String DELAYED_COMPONENT_STORE = "delayed_components";
    private static final UUIDBinding UUID_BINDING = new UUIDBinding();
    private final Environment backing;

    public ServerBattleWorldDatabase(final Path file) {
        backing = Environments.newInstance(file.toFile(), new EnvironmentConfig().setEnvCompactOnOpen(true));
    }

    public void saveBattle(final UUID uuid, final Battle battle) {
        final ByteIterable encoded = encodeBattle(battle);
        final ByteIterable key = UUID_BINDING.objectToEntry(uuid);
        final Transaction transaction = backing.beginTransaction();
        do {
            final Store store = backing.openStore(BATTLE_STORE, STORE_CONFIG, transaction);
            store.put(transaction, key, encoded);
        } while (!transaction.commit());
    }

    public UUID[] getParticipatedBattles(final UUID uuid, final TriState active) {
        final Transaction transaction = backing.beginReadonlyTransaction();
        final List<UUID> uuids = new ArrayList<>();
        if (active == TriState.TRUE || active == TriState.DEFAULT) {
            if (backing.storeExists(BATTLE_ACTIVE_ENTITIES_STORE, transaction)) {
                final Store store = backing.openStore(BATTLE_ACTIVE_ENTITIES_STORE, STORE_CONFIG, transaction);
                final ByteIterable iterable = store.get(transaction, UUID_BINDING.objectToEntry(uuid));
                if (iterable != null) {
                    final Iterator<UUID> iterator = asPastBattles(iterable);
                    while (iterator.hasNext()) {
                        final UUID next = iterator.next();
                        uuids.add(next);
                    }
                }
            }
        }
        if (active == TriState.FALSE || active == TriState.DEFAULT) {
            if (backing.storeExists(BATTLE_INACTIVE_ENTITIES_STORE, transaction)) {
                final Store store = backing.openStore(BATTLE_INACTIVE_ENTITIES_STORE, STORE_CONFIG, transaction);
                final ByteIterable iterable = store.get(transaction, UUID_BINDING.objectToEntry(uuid));
                if (iterable != null) {
                    final Iterator<UUID> iterator = asPastBattles(iterable);
                    while (iterator.hasNext()) {
                        final UUID next = iterator.next();
                        uuids.add(next);
                    }
                }
            }
        }
        transaction.abort();
        return uuids.toArray(new UUID[0]);
    }

    public DataResult<BiFunction<BattleHandle, BattleStateMode, Battle>> loadBattle(final UUID uuid) {
        final Transaction transaction = backing.beginReadonlyTransaction();
        if (!backing.storeExists(BATTLE_STORE, transaction)) {
            transaction.abort();
            return DataResult.error("Battle does not exist");
        }
        final Store store = backing.openStore(BATTLE_STORE, STORE_CONFIG, transaction);
        final ByteIterable iterable = store.get(transaction, UUID_BINDING.objectToEntry(uuid));
        if (iterable == null) {
            transaction.abort();
            return DataResult.error("Battle does not exist");
        }
        transaction.abort();
        return decodeBattle(iterable);
    }

    public void onBattleJoin(final UUID entityId, final UUID battleId, final boolean active) {
        final ByteIterable key = UUID_BINDING.objectToEntry(entityId);
        final ByteIterable data = UUID_BINDING.objectToEntry(battleId);
        if (active) {
            final Transaction transaction = backing.beginTransaction();
            do {
                final Store store = backing.openStore(BATTLE_ACTIVE_ENTITIES_STORE, STORE_CONFIG, transaction);
                final ByteIterable existing = store.get(transaction, key);
                if (existing == null) {
                    store.put(transaction, key, UUID_BINDING.objectToEntry(battleId));
                } else {
                    final ByteBuffer buffer = ByteBuffer.wrap(existing.getBytesUnsafe());
                    final int UUID_SIZE = Long.BYTES * 2;
                    final int i = binarySearch(buffer, battleId);
                    final FixedLengthByteIterable prefix = new FixedLengthByteIterable(existing, 0, Math.abs(i) * UUID_SIZE) {
                    };
                    final int offset;
                    if (i < 0) {
                        offset = Math.abs(i) * UUID_SIZE;
                    } else {
                        offset = i * UUID_SIZE + UUID_SIZE;
                    }
                    final FixedLengthByteIterable suffix = new FixedLengthByteIterable(existing, offset, existing.getLength() - offset) {
                    };
                    store.put(transaction, key, new CompoundByteIterable(new ByteIterable[]{prefix, data, suffix}));
                }
            } while (!transaction.commit());
        } else {
            final Transaction transaction = backing.beginTransaction();
            do {
                final Store activeStore = backing.openStore(BATTLE_ACTIVE_ENTITIES_STORE, STORE_CONFIG, transaction);
                final ByteIterable activeExisting = activeStore.get(transaction, key);
                if (activeExisting != null) {
                    final ByteBuffer buffer = ByteBuffer.wrap(activeExisting.getBytesUnsafe());
                    final int UUID_SIZE = Long.BYTES * 2;
                    final int i = binarySearch(buffer, battleId);
                    if (i >= 0) {
                        final FixedLengthByteIterable prefix = new FixedLengthByteIterable(activeExisting, 0, Math.abs(i) * UUID_SIZE) {
                        };
                        final int offset = i * UUID_SIZE + UUID_SIZE;
                        final FixedLengthByteIterable suffix = new FixedLengthByteIterable(activeExisting, offset, activeExisting.getLength() - offset) {
                        };
                        activeStore.put(transaction, key, new CompoundByteIterable(new ByteIterable[]{prefix, suffix}));
                    }
                }
                final Store inactiveStore = backing.openStore(BATTLE_INACTIVE_ENTITIES_STORE, STORE_CONFIG, transaction);
                final ByteIterable inactiveExisting = inactiveStore.get(transaction, key);
                if (inactiveExisting == null) {
                    inactiveStore.put(transaction, key, data);
                } else {
                    final ByteBuffer buffer = ByteBuffer.wrap(inactiveExisting.getBytesUnsafe());
                    final int i = binarySearch(buffer, battleId);
                    final int UUID_SIZE = Long.BYTES * 2;
                    if (i < 0) {
                        final FixedLengthByteIterable prefix = new FixedLengthByteIterable(inactiveExisting, 0, Math.abs(i) * UUID_SIZE) {
                        };
                        final FixedLengthByteIterable suffix = new FixedLengthByteIterable(inactiveExisting, Math.abs(i) * UUID_SIZE, inactiveExisting.getLength() - Math.abs(i) * UUID_SIZE) {
                        };
                        activeStore.put(transaction, key, new CompoundByteIterable(new ByteIterable[]{prefix, data, suffix}));
                    }
                }
            } while (!transaction.commit());
        }
    }

    private static int binarySearch(final ByteBuffer buffer, final UUID key) {
        final int UUID_SIZE = Long.BYTES * 2;
        int low = 0;
        int high = buffer.remaining() / UUID_SIZE - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final UUID midVal = new UUID(buffer.getLong(mid * UUID_SIZE), buffer.getLong(mid * UUID_SIZE + UUID_SIZE / 2));

            final int comp = midVal.compareTo(key);
            if (comp < 0) {
                low = mid + 1;
            } else if (comp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }

    @Override
    public void close() {
        backing.close();
    }

    public UUID findUnusedBattleUuid(final Random random) {
        final Transaction transaction = backing.beginReadonlyTransaction();
        if (backing.storeExists(BATTLE_STORE, transaction)) {
            final Store store = backing.openStore(BATTLE_STORE, STORE_CONFIG, transaction);
            UUID uuid;
            do {
                uuid = new UUID(random.nextLong(), random.nextLong());
            } while (store.get(transaction, UUID_BINDING.objectToEntry(uuid)) != null);
            transaction.abort();
            return uuid;
        } else {
            transaction.abort();
            return new UUID(random.nextLong(), random.nextLong());
        }
    }

    public ServerBattleWorldContainer.DelayedComponents getDelayedComponents(final UUID uuid) {
        //TODO don't forget about this!
        return new ServerBattleWorldContainer.DelayedComponents(Map.of());
    }

    public void saveDelayedComponents(final UUID entityId, final ServerBattleWorldContainer.DelayedComponents components) {
        //TODO don't forget about this!
    }

    private record DelayedComponentsPacked(Map<UUID, List<BattleEntityComponent>> map) {
    }

    private static final class UUIDBinding extends ComparableBinding {
        private final byte[] bytes = new byte[16];
        private final ByteBuffer buffer = ByteBuffer.wrap(bytes);

        @Override
        public UUID readObject(@NotNull final ByteArrayInputStream stream) {
            final int i = stream.read(bytes, 0, 16);
            if (i != 16) {
                throw new RuntimeException();
            }
            buffer.position(0);
            return new UUID(buffer.getLong(), buffer.getLong());
        }

        @Override
        public void writeObject(@NotNull final LightOutputStream output, @NotNull final Comparable object) {
            if (object instanceof UUID uuid) {
                buffer.position(0);
                buffer.putLong(uuid.getMostSignificantBits());
                buffer.putLong(uuid.getLeastSignificantBits());
                output.write(bytes);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private Iterator<UUID> asPastBattles(final ByteIterable iterable) {
        final int objectLength = Long.BYTES * 2;
        final byte[] unsafe = iterable.getBytesUnsafe();
        final int length = iterable.getLength();
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            public UUID next() {
                final UUID uuid = UUID_BINDING.readObject(new ByteArraySizedInputStream(unsafe, index, 16));
                index = index + objectLength;
                return uuid;
            }
        };
    }

    private static ByteIterable encodeBattle(final Battle battle) {
        final NbtCompound wrapper = new NbtCompound();
        wrapper.put("data", Battle.encoder().encodeStart(NbtOps.INSTANCE, battle).getOrThrow(false, s -> {
            throw new RuntimeException(s);
        }));
        final ByteArrayOutputStream stream = new ByteArrayOutputStream(65535);
        try {
            NbtIo.writeCompressed(wrapper, stream);
            return new ArrayByteIterable(stream.toByteArray());
        } catch (final IOException e) {
            throw new RuntimeException("Error while saving battle", e);
        }
    }

    private static DataResult<BiFunction<BattleHandle, BattleStateMode, Battle>> decodeBattle(final ByteIterable iterable) {
        try {
            final NbtCompound compound = NbtIo.readCompressed(new ByteArrayInputStream(iterable.getBytesUnsafe()));
            final NbtElement data = compound.get("data");
            return Battle.decoder().parse(NbtOps.INSTANCE, data);
        } catch (final IOException e) {
            throw new RuntimeException("Error while loading battle", e);
        }
    }
}
