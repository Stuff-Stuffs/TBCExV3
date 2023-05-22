package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Comparator;

public final class BattleParticipantItemRarity {
    public static final Comparator<BattleParticipantItemRarity> COMPARATOR = Comparator.<BattleParticipantItemRarity, RarityClass>comparing(i -> i.rarityClass).thenComparingDouble(i -> i.progress);
    public static final Codec<BattleParticipantItemRarity> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("level").forGetter(BattleParticipantItemRarity::getLevel), Codec.DOUBLE.fieldOf("progress").forGetter(rarity -> rarity.progress), RarityClass.CODEC.fieldOf("class").forGetter(rarity -> rarity.rarityClass)).apply(instance, (level, progress1, rarityClass1) -> new BattleParticipantItemRarity(level, progress1, rarityClass1)));
    private static final RarityClass[] RARITY_CLASSES = RarityClass.values();
    private final int level;
    private final double progress;
    private final RarityClass rarityClass;

    public BattleParticipantItemRarity(final int level, final double progress, final RarityClass rarityClass) {
        this.level = level;
        this.progress = progress;
        this.rarityClass = rarityClass;
        if (!Double.isFinite(progress)) {
            throw new TBCExException("Non-finite rarity progress");
        }
        if (rarityClass != RarityClass.JUNK) {
            if (progress < 0) {
                throw new TBCExException("Less than zero rarity progress");
            }
        }
        if (rarityClass != RarityClass.LEGENDARY) {
            if (progress > 1) {
                throw new TBCExException("Rarity progress greater than one");
            }
        }
    }

    public int getLevel() {
        return 0;
    }

    public RarityClass getRarityClass() {
        return rarityClass;
    }

    public double getProgress() {
        return progress;
    }

    public Text getAsText() {
        return Text.literal(rarityClass.name() + '(' + level + ')').setStyle(Style.EMPTY.withColor(rarityClass.color | 0xFF000000));
    }

    public enum RarityClass {
        JUNK(0, 0x696A6A),//grey
        COMMON(100.0, 0xFFFFFF),//white
        UNCOMMON(10_000.0, 0xd18f9c),//pink
        RARE(1_000_000.0, 0x00cc66),//green
        EPIC(100_000_000.0, 0xE80000),//red
        LEGENDARY(10_000_000_000.0, 0xFBF236);//gold
        public static final Codec<RarityClass> CODEC = Codec.STRING.comapFlatMap(RarityClass::fromDynamic, Enum::name);
        private final double start;
        private final int color;

        RarityClass(final double start, final int color) {
            this.start = start;
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        private static DataResult<RarityClass> fromDynamic(final String s) {
            return switch (s) {
                case "JUNK" -> DataResult.success(JUNK);
                case "COMMON" -> DataResult.success(COMMON);
                case "UNCOMMON" -> DataResult.success(UNCOMMON);
                case "RARE" -> DataResult.success(RARE);
                case "EPIC" -> DataResult.success(EPIC);
                case "LEGENDARY" -> DataResult.success(LEGENDARY);
                default -> DataResult.error(() -> "No RarityClass with name: " + s);
            };
        }
    }

    public static BattleParticipantItemRarity getRarity(final int level, final double rarity) {
        if (rarity < 0) {
            throw new TBCExException("Negative rarity");
        }
        RarityClass greatestLessThan = null;
        int idx = -1;
        for (int i = 1; i < RARITY_CLASSES.length; i++) {
            if (RARITY_CLASSES[i].start > rarity) {
                idx = i;
                greatestLessThan = RARITY_CLASSES[i];
                break;
            }
        }
        if (greatestLessThan != null) {
            final RarityClass lessThan = RARITY_CLASSES[idx - 1];
            final double diff = greatestLessThan.start - lessThan.start;
            return new BattleParticipantItemRarity(level, (rarity - lessThan.start) / diff, lessThan);
        } else {
            return new BattleParticipantItemRarity(level, (rarity - RarityClass.LEGENDARY.start) / 1000.0, RarityClass.LEGENDARY);
        }
    }
}
