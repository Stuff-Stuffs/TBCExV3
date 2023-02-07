package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

//TODO off thread file loading
public class ServerBattleWorldDatabase {
    private static final Codec<List<Pair<UUID, BattleEntityComponent>>> DELAYED_COMPONENT_CODEC = Codec.list(Codec.pair(CodecUtil.UUID_CODEC, BattleEntityComponent.CODEC));
    private static final String BATTLE_DIRECTORY = "./battles";
    private static final String BATTLE_ACTIVE_ENTITIES_DIRECTORY = "./battle_entities";
    private static final String BATTLE_INACTIVE_ENTITIES_DIRECTORY = "./battle_entities_history";
    private static final String DELAYED_COMPONENT_DIRECTORY = "./delayed_components";
    private final Registry<Biome> biomeRegistry;
    private final Path rootFolder;
    private final Path battles;
    private final Path activeParticipants;
    private final Path inactiveParticipants;
    private final Path delayedComponents;


    public ServerBattleWorldDatabase(Registry<Biome> registry, final Path path) {
        biomeRegistry = registry;
        rootFolder = path;
        battles = rootFolder.resolve(BATTLE_DIRECTORY);
        activeParticipants = rootFolder.resolve(BATTLE_ACTIVE_ENTITIES_DIRECTORY);
        inactiveParticipants = rootFolder.resolve(BATTLE_INACTIVE_ENTITIES_DIRECTORY);
        delayedComponents = rootFolder.resolve(DELAYED_COMPONENT_DIRECTORY);
        createDirectory(rootFolder);
        createDirectory(battles);
        createDirectory(activeParticipants);
        createDirectory(inactiveParticipants);
        createDirectory(delayedComponents);
    }

    private static void createDirectory(final Path path) {
        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                throw new RuntimeException();
            }
        } else {
            try {
                Files.createDirectories(path);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void overwriteFile(final Path path, final byte[] data) {
        try {
            Files.write(path, data, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveBattle(final UUID uuid, final Battle battle) {
        final byte[] encoded = encodeBattle(battle);
        overwriteFile(file(battles, uuid), encoded);
    }

    public UUID[] getParticipatedBattles(final UUID uuid, final TriState active) {
        final List<UUID> uuids = new ArrayList<>();
        if (active == TriState.TRUE || active == TriState.DEFAULT) {
            try {
                final byte[] bytes = Files.readAllBytes(file(activeParticipants, uuid));
                final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                while (buffer.remaining() > 15) {
                    uuids.add(new UUID(buffer.getLong(), buffer.getLong()));
                }
            } catch (final IOException ignored) {
            }
        }

        if (active == TriState.FALSE || active == TriState.DEFAULT) {
            try {
                final byte[] bytes = Files.readAllBytes(file(inactiveParticipants, uuid));
                final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                while (buffer.remaining() > 15) {
                    uuids.add(new UUID(buffer.getLong(), buffer.getLong()));
                }
            } catch (final IOException ignored) {
            }
        }
        return uuids.toArray(new UUID[0]);
    }

    public DataResult<Battle.Factory> loadBattle(final UUID uuid) {
        try {
            final byte[] bytes = Files.readAllBytes(file(battles, uuid));
            return decodeBattle(bytes);
        } catch (final NoSuchFileException e) {
            return DataResult.error("Battle does not exist");
        } catch (final IOException e) {
            return DataResult.error("Error: " + e);
        }
    }

    public void onBattleJoinLeave(final UUID entityId, final UUID battleId, final boolean active) {
        if (active) {
            final Path inactivePath = file(inactiveParticipants, entityId);
            if (Files.isRegularFile(inactivePath)) {
                try (final FileChannel channel = FileChannel.open(inactivePath, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                    final long position = findUUID(channel, battleId);
                    if (position > -1) {
                        final long size = channel.size() - position + Long.BYTES * 2;
                        final ByteBuffer buffer = ByteBuffer.allocate((int) size).order(ByteOrder.LITTLE_ENDIAN);
                        final int read = channel.read(buffer, position + Long.BYTES * 2);
                        channel.truncate(position);
                        if (read > 0) {
                            buffer.flip();
                            channel.write(buffer);
                        }
                    }
                } catch (final NoSuchFileException ignored) {
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
            final Path activePath = file(activeParticipants, entityId);
            try (final FileChannel channel = FileChannel.open(activePath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                final long position = findUUID(channel, battleId);
                if (position < 0) {
                    final long realPos = -(position + 1);
                    final long size = channel.size() - realPos;
                    final ByteBuffer buffer = ByteBuffer.allocate((int) size).order(ByteOrder.LITTLE_ENDIAN);
                    final int read = channel.read(buffer, realPos);
                    channel.truncate(realPos);
                    final ByteBuffer inserted = ByteBuffer.allocate(Long.BYTES * 2).order(ByteOrder.LITTLE_ENDIAN);
                    channel.write(inserted);
                    if (read > 0) {
                        buffer.flip();
                        channel.write(buffer);
                    }
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            final Path activePath = file(inactiveParticipants, entityId);
            if (Files.isRegularFile(activePath)) {
                try (final FileChannel channel = FileChannel.open(activePath, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                    final long position = findUUID(channel, battleId);
                    if (position > -1) {
                        final long size = channel.size() - position + Long.BYTES * 2;
                        final ByteBuffer buffer = ByteBuffer.allocate((int) size).order(ByteOrder.LITTLE_ENDIAN);
                        final int read = channel.read(buffer, position + Long.BYTES * 2);
                        channel.truncate(position);
                        if (read > 0) {
                            buffer.flip();
                            channel.write(buffer);
                        }
                    }
                } catch (final NoSuchFileException ignored) {
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
            final Path inactivePath = file(activeParticipants, entityId);
            try (final FileChannel channel = FileChannel.open(inactivePath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                final long position = findUUID(channel, battleId);
                if (position < 0) {
                    final long realPos = -(position + 1);
                    final long size = channel.size() - realPos;
                    final ByteBuffer buffer = ByteBuffer.allocate((int) size).order(ByteOrder.LITTLE_ENDIAN);
                    final int read = channel.read(buffer, realPos);
                    channel.truncate(realPos);
                    final ByteBuffer inserted = ByteBuffer.allocate(Long.BYTES * 2).order(ByteOrder.LITTLE_ENDIAN);
                    channel.write(inserted);
                    if (read > 0) {
                        buffer.flip();
                        channel.write(buffer);
                    }
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static long findUUID(final SeekableByteChannel channel, final UUID uuid) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2).order(ByteOrder.LITTLE_ENDIAN);
        long start = 0;
        long size = channel.size();
        size = size - (size % (Long.BYTES * 2));
        long end = size / (Long.BYTES * 2) - 1;
        while (start <= end) {
            buffer.clear();
            final long mid = (start + end) >>> 1;
            channel.position(mid * Long.BYTES * 2);
            final int read = channel.read(buffer);
            if (read != Long.BYTES * 2) {
                throw new RuntimeException();
            } else {
                buffer.flip();
                final UUID single = new UUID(buffer.getLong(), buffer.getLong());
                final int i = uuid.compareTo(single);
                if (i == 0) {
                    return mid * Long.BYTES * 2;
                }
                if (i < 0) {
                    end = mid - 1;
                } else {
                    start = mid + 1;
                }
            }
        }
        return -(start * Long.BYTES * 2 + 1);
    }

    public UUID findUnusedBattleUuid(final Random random) {
        UUID uuid;
        do {
            uuid = new UUID(random.nextLong(), random.nextLong());
        } while (Files.isRegularFile(file(battles, uuid)));
        return uuid;
    }

    public ServerBattleWorldContainer.DelayedComponents getDelayedComponents(final UUID uuid) {
        final Path path = file(delayedComponents, uuid);
        try (final FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            final ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(intBuffer);
            intBuffer.flip();
            final int count = intBuffer.getInt();
            final Map<UUID, List<BattleEntityComponent>> components = new Object2ReferenceOpenHashMap<>();
            for (int i = 0; i < count; i++) {
                intBuffer.clear();
                channel.read(intBuffer);
                intBuffer.flip();
                final byte[] data = new byte[intBuffer.getInt()];
                final ByteBuffer sized = ByteBuffer.wrap(data);
                channel.read(sized);
                final List<Pair<UUID, BattleEntityComponent>> list = decodeDelayedComponents(data).getOrThrow(false, s -> {
                    throw new RuntimeException(s);
                });
                for (final Pair<UUID, BattleEntityComponent> pair : list) {
                    components.computeIfAbsent(pair.getFirst(), l -> new ArrayList<>()).add(pair.getSecond());
                }
            }
            return new ServerBattleWorldContainer.DelayedComponents(components);
        } catch (final NoSuchFileException e) {
            return new ServerBattleWorldContainer.DelayedComponents(Map.of());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveDelayedComponents(final UUID entityId, final @Nullable ServerBattleWorldContainer.DelayedComponents components) {
        final Path path = file(delayedComponents, entityId);
        if (components == null) {
            if (Files.isRegularFile(path)) {
                try {
                    Files.delete(path);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            final List<Pair<UUID, BattleEntityComponent>> componentList = new ArrayList<>(components.components.size() * 4);
            for (final Map.Entry<UUID, List<BattleEntityComponent>> entry : components.components.entrySet()) {
                for (final BattleEntityComponent component : entry.getValue()) {
                    componentList.add(Pair.of(entry.getKey(), component));
                }
            }
            final byte[] bytes = encodeDelayedComponents(componentList);
            try (final FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                if (channel.size() == 0) {
                    final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
                    buffer.putInt(1);
                    buffer.flip();
                    channel.write(buffer);
                } else {
                    final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
                    channel.read(buffer);
                    final int i = buffer.flip().getInt();
                    buffer.putInt(i, 0);
                    buffer.flip();
                    channel.position(0);
                    channel.write(buffer);
                }
                final ByteBuffer buffer = ByteBuffer.wrap(bytes);
                final ByteBuffer size = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
                size.putInt(bytes.length);
                size.flip();
                channel.position(channel.size());
                channel.write(size);
                channel.write(buffer);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private byte[] encodeBattle(final Battle battle) {
        final NbtCompound wrapper = new NbtCompound();
        wrapper.put("data", Battle.encoder(biomeRegistry).encodeStart(NbtOps.INSTANCE, battle).getOrThrow(false, s -> {
            throw new RuntimeException(s);
        }));
        final ByteArrayOutputStream stream = new ByteArrayOutputStream(65535);
        try {
            NbtIo.writeCompressed(wrapper, stream);
            return stream.toByteArray();
        } catch (final IOException e) {
            throw new RuntimeException("Error while saving battle", e);
        }
    }

    private DataResult<Battle.Factory> decodeBattle(final byte[] bytes) {
        try {
            final NbtCompound compound = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
            final NbtElement data = compound.get("data");
            return Battle.decoder(biomeRegistry).parse(NbtOps.INSTANCE, data);
        } catch (final IOException e) {
            throw new RuntimeException("Error while loading battle", e);
        }
    }

    private static DataResult<List<Pair<UUID, BattleEntityComponent>>> decodeDelayedComponents(final byte[] bytes) {
        try {
            final NbtCompound compound = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
            final NbtElement data = compound.get("data");
            return DELAYED_COMPONENT_CODEC.parse(NbtOps.INSTANCE, data);
        } catch (final IOException e) {
            throw new RuntimeException("Error while loading delayed component", e);
        }
    }

    private static byte[] encodeDelayedComponents(final List<Pair<UUID, BattleEntityComponent>> list) {
        final DataResult<NbtElement> encoded = DELAYED_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, list);
        final NbtCompound wrapper = new NbtCompound();
        wrapper.put("data", encoded.getOrThrow(false, s -> {
            throw new RuntimeException(s);
        }));
        final ByteArrayOutputStream stream = new ByteArrayOutputStream(65535);
        try {
            NbtIo.writeCompressed(wrapper, stream);
            return stream.toByteArray();
        } catch (final IOException e) {
            throw new RuntimeException("Error while saving battle", e);
        }
    }

    private static Path file(final Path parent, final UUID uuid) {
        return parent.resolve(uuid.toString() + ".tbcex");
    }
}
