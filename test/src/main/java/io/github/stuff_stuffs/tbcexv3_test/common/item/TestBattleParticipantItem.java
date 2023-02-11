package io.github.stuff_stuffs.tbcexv3_test.common.item;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3_test.common.TBCExV3Test;
import io.github.stuff_stuffs.tbcexv3_test.common.action.BattleParticipantMeleeTestBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionBattleParticipantTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.CoreBattleActionTargetTypes;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemRarity;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestBattleParticipantItem implements BattleParticipantItem {
    public static final Codec<TestBattleParticipantItem> CODEC = Codec.LONG.xmap(TestBattleParticipantItem::new, item -> item.id);
    private final long id;

    public TestBattleParticipantItem(final long id) {
        this.id = id;
    }

    @Override
    public BattleParticipantItemType<?> type() {
        return TestBattleParticipantItemTypes.TEST_ITEM_TYPE;
    }

    @Override
    public BattleParticipantItemRarity rarity() {
        final Random random = Random.create(id);
        final BattleParticipantItemRarity.RarityClass[] values = BattleParticipantItemRarity.RarityClass.values();
        final BattleParticipantItemRarity.RarityClass rarityClass = values[random.nextInt(values.length)];
        return new BattleParticipantItemRarity(random.nextDouble(), rarityClass);
    }

    @Override
    public Text name(final BattleParticipantStateView stateView) {
        return Text.of(Long.toString(id, 16));
    }

    @Override
    public TooltipText description(final BattleParticipantStateView stateView) {
        return new TooltipText(List.of(Text.of("""

                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec et odio felis. Ut enim odio, facilisis id ligula quis, tempus faucibus dui. Vestibulum commodo eros eget tortor fringilla vehicula. Duis at nulla ac ante lacinia sodales ut at est. Vivamus porttitor egestas diam at dapibus. Integer faucibus nunc vitae tortor placerat, ut malesuada risus bibendum. Fusce dignissim arcu pellentesque, aliquet tellus mattis, convallis mi. Fusce rutrum, mi vitae fermentum iaculis, felis sem pharetra est, in laoreet felis tellus et tortor. Suspendisse semper metus ac dolor hendrerit bibendum. Nam accumsan dignissim lorem, sed fermentum libero malesuada ut. Cras accumsan aliquam massa, non venenatis massa congue vitae. Aenean ante risus, maximus in nisi rutrum, porttitor sodales urna.

                Cras sed felis eu tellus sagittis auctor. Nunc cursus vestibulum pretium. Duis nec fermentum mauris, pharetra maximus eros. Donec sagittis dui non nunc imperdiet molestie. Aliquam sagittis leo sit amet velit iaculis cursus. Donec tempus sodales lacus, tincidunt ultrices ipsum fermentum a. Etiam odio arcu, accumsan a nisl at, fermentum sollicitudin ante. Vestibulum justo erat, venenatis at tempus at, consectetur vitae tellus. Pellentesque lobortis tellus ac ornare vehicula. Donec consequat massa erat, eget auctor ex lacinia vel. Pellentesque eleifend, nibh in aliquet porttitor, velit mauris ullamcorper diam, rutrum consectetur diam libero sed lectus. Nunc sit amet fringilla purus. Duis sit amet urna in nibh facilisis eleifend aliquam dictum lectus. Nullam sem justo, venenatis quis pulvinar quis, venenatis id diam. Vivamus nec eros fringilla, iaculis diam non, blandit dolor.

                Nulla tempus porta urna, sit amet tincidunt lacus efficitur sit amet. Maecenas vitae enim ac diam posuere semper eu at nibh. Donec eleifend, dui nec blandit blandit, elit sem condimentum lacus, vitae tempor neque felis sit amet metus. Mauris in risus ut augue feugiat rutrum. Cras faucibus viverra risus at laoreet. Sed vitae volutpat erat, eget rhoncus ante. Mauris elit ligula, semper nec velit vitae, dictum efficitur lectus. Nullam at tempor lorem, non semper mi. Aenean tincidunt feugiat faucibus. Interdum et malesuada fames ac ante ipsum primis in faucibus. Ut sapien diam, scelerisque ac lacus sit amet, pulvinar vulputate justo. Donec sit amet cursus libero. Integer et lacus imperdiet, pretium ex nec, ullamcorper mauris. Praesent et dui fringilla, tempus velit sit amet, congue arcu. Sed cursus consectetur laoreet.

                Fusce nulla nunc, ultricies vel volutpat quis, fringilla quis felis. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Interdum et malesuada fames ac ante ipsum primis in faucibus. Pellentesque ante velit, posuere at enim et, accumsan semper ipsum. Pellentesque non porttitor ligula. Ut nisl orci, suscipit ut sodales eget, tincidunt et dolor. Sed iaculis ullamcorper lacus, in interdum lacus. Vestibulum dapibus dictum gravida. Cras iaculis ex eget orci placerat pretium. Maecenas mauris neque, accumsan ut ipsum vitae, accumsan elementum tortor. Sed ullamcorper diam non ex ultricies, vel convallis neque laoreet. Fusce et felis lectus. Maecenas id dui metus. Praesent ornare dui a erat sodales lobortis. Phasellus finibus finibus arcu, sit amet lobortis dui.

                Maecenas varius nec quam vel convallis. Praesent vitae congue mauris. Suspendisse pulvinar arcu a velit malesuada, nec consectetur tortor laoreet. Mauris ultricies rhoncus facilisis. Proin sit amet eros tellus. In id tincidunt mi. Cras sagittis venenatis enim, sit amet suscipit neque rhoncus luctus. Maecenas felis nibh, luctus vitae porttitor eget, congue non mi. Donec commodo sapien posuere magna rhoncus, eu placerat felis vulputate. Aliquam lobortis tincidunt mi, nec rutrum tortor accumsan vel. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Morbi interdum leo eu leo consequat cursus. Nullam augue erat, malesuada ut ornare vitae, ultrices sit amet lacus.\s"""
        )));
    }

    @Override
    public List<ItemStack> toItemStacks(final BattleParticipantItemStack stack) {
        return List.of(new ItemStack(Items.DAMAGED_ANVIL));
    }

    @Override
    public boolean matches(final BattleParticipantItem other) {
        if (other.type() == type() && other instanceof TestBattleParticipantItem item) {
            return id == item.id;
        }
        return false;
    }

    @Override
    public Optional<TagKey<BattleParticipantEquipmentSlot>> getAcceptableSlots(final BattleParticipantStateView stateView) {
        return Optional.empty();
    }

    @Override
    public List<BattleParticipantAction> actions(final BattleParticipantStateView stateView, final BattleParticipantItemStack stack, final Optional<BattleParticipantInventoryHandle> handle) {
        final BattleParticipantAction action = new BattleParticipantAction() {
            @Override
            public Text name(final BattleParticipantStateView state) {
                return Text.of("Test Melee");
            }

            @Override
            public TooltipText description(final BattleParticipantStateView state) {
                return new TooltipText(List.of(Text.of("Test description")));
            }

            private static BattleParticipantActionBuilder.RaycastIterator<BattleParticipantActionBattleParticipantTarget> getTargets(final BattleParticipantStateView state, final Consumer<BattleParticipantActionTarget> consumer) {
                final BattleParticipantTeam team = state.getTeam();
                return BattleParticipantActionBattleParticipantTarget.filter(state, i -> i.getBattleState().getTeamRelation(i.getTeam(), team) == BattleParticipantTeamRelation.ENEMIES, i -> Text.of(i.getHandle().getUuid().toString()), i -> new TooltipText(List.of(Text.of("Test Description"))), consumer);
            }

            @Override
            public BattleParticipantActionBuilder builder(final BattleParticipantStateView state, final Consumer<BattleAction> consumer) {
                return BattleParticipantActionBuilder.create(
                        stateView,
                        l -> !l.isEmpty(),
                        l -> new BattleParticipantMeleeTestBattleAction(state.getHandle(), l.get(0).handle()),
                        new ArrayList<>(),
                        (stateView, targets, targetConsumer) -> BattleParticipantActionBuilder.TargetProvider.single(
                                stateView,
                                targetConsumer,
                                CoreBattleActionTargetTypes.BATTLE_PARTICIPANT_TARGET_TYPE,
                                () -> getTargets(stateView, targetConsumer).iterator(),
                                () -> getTargets(stateView, targetConsumer).raycaster()
                        ),
                        (BiConsumer<ArrayList<BattleParticipantActionBattleParticipantTarget>, BattleParticipantActionTarget>) (targets, target) -> {
                            TBCExV3Test.MESSAGE_CONSUMER.accept(Text.of("Targeted " + ((BattleParticipantActionBattleParticipantTarget) target).handle()));
                            targets.add(
                                    (BattleParticipantActionBattleParticipantTarget) target
                            );
                        },
                        consumer
                );
            }

            @Override
            public Optional<Identifier> renderer(final BattleParticipantStateView state) {
                return Optional.empty();
            }
        };
        return List.of(action);
    }
}
