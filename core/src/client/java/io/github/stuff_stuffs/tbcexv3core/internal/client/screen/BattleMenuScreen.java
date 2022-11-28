package io.github.stuff_stuffs.tbcexv3core.internal.client.screen;

import io.github.stuff_stuffs.tbcexv3_gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3_gui.api.screen.GuiScreen;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.*;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.ModifiableWidget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.OneHotContainerWidget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.menuparts.InventoryWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Function;

public class BattleMenuScreen extends GuiScreen<BackgroundWidget<BattleMenuScreen.RootData>, BattleMenuScreen.RootData> {
    public BattleMenuScreen(final BattleParticipantHandle handle) {
        super(Text.of("BattleScreen"), create(handle), new RootData(handle));
    }

    private static BackgroundWidget<RootData> create(final BattleParticipantHandle handle) {
        final BackgroundWidget<RootData> background = new BackgroundWidget<>(data -> 0.0, WidgetRenderUtils.basicPanelTerminal(0xD70F0F0F));
        final OneHotContainerWidget<RootData> oneHot = new OneHotContainerWidget<>();
        final OneHotContainerWidget.Handle mainHandle = oneHot.addChild(createMain(handle), Function.identity());
        final OneHotContainerWidget.Handle inventoryHandle = oneHot.addChild(wrapWithControl(InventoryWidget.createInventory(handle), state -> state.parent), context -> WidgetContext.dependent(context, InventoryWidget.InventoryState::new));
        final OneHotContainerWidget.Handle actionsHandle = oneHot.addChild(createActions(handle), Function.identity());
        final OneHotContainerWidget.Handle selfStatsHandle = oneHot.addChild(createSelfStats(handle), Function.identity());
        final OneHotContainerWidget.Handle otherStatsHandle = oneHot.addChild(createOtherStats(handle), Function.identity());
        oneHot.activeScreenGetter(data -> switch (data.menuState) {
            case MAIN -> mainHandle;
            case ACTIONS -> actionsHandle;
            case INVENTORY -> inventoryHandle;
            case SELF_STATS -> selfStatsHandle;
            case OTHER_STATS -> otherStatsHandle;
        });
        background.setChild(oneHot, Function.identity());
        return background;
    }

    private static Widget<RootData> createMain(final BattleParticipantHandle handle) {
        final SelectionWheelWidget<RootData> selectionWheel = new SelectionWheelWidget<>(SelectionWheelWidget.RadiusSizer.max(0.25, 0.85), StateUpdater.none());
        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>(
                        SelectionWheelWidget.SectionStateUpdater.basic(state -> state.rootData.menuState = MenuState.INVENTORY, (b, state) -> state.hovered = b, state -> state.hovered),
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("INVENTORY").asOrderedText()))
                ),
                context -> WidgetContext.dependent(context, SectionState::new)
        );

        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>(
                        SelectionWheelWidget.SectionStateUpdater.basic(state -> state.rootData.menuState = MenuState.ACTIONS, (b, state) -> state.hovered = b, state -> state.hovered),
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("ACTIONS").asOrderedText()))
                ),
                context -> WidgetContext.dependent(context, SectionState::new)
        );

        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>(
                        SelectionWheelWidget.SectionStateUpdater.basic(state -> state.rootData.menuState = MenuState.SELF_STATS, (b, state) -> state.hovered = b, state -> state.hovered),
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("SELF STATS").asOrderedText()))
                ),
                context -> WidgetContext.dependent(context, SectionState::new)
        );

        selectionWheel.add(
                new SelectionWheelWidget.BasicSection<>(
                        SelectionWheelWidget.SectionStateUpdater.basic(state -> state.rootData.menuState = MenuState.OTHER_STATS, (b, state) -> state.hovered = b, state -> state.hovered),
                        SelectionWheelWidget.SectionRenderer.flat(state -> state.hovered, state -> state.hovered ? 0xFF7F7F7F : 0x7F7F7F7F, state -> Optional.of(Text.of("OTHER STATS").asOrderedText()))
                ),
                context -> WidgetContext.dependent(context, SectionState::new)
        );

        return selectionWheel;
    }

    private static Widget<RootData> createActions(final BattleParticipantHandle handle) {
        return new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(-1), Sizer.max());
    }

    private static Widget<RootData> createOtherStats(final BattleParticipantHandle handle) {
        return new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(-1), Sizer.max());
    }

    private static <T> Widget<T> wrapWithControl(final Widget<T> widget, final Function<? super T, RootData> dataFunction) {
        final ModifiableWidget<T> w = new ModifiableWidget<>(ModifiableWidget.Animation.none(), (event, data) -> {
            if (event instanceof WidgetEvent.KeyPressEvent keyPress && keyPress.keyCode() == GLFW.GLFW_KEY_ESCAPE) {
                final RootData rootData = dataFunction.apply(data);
                rootData.menuState = MenuState.MAIN;
                return true;
            }
            return false;
        }, ModifiableWidget.WidgetEventPhase.POST_CHILD);
        w.setChild(widget, WidgetContext::passthrough);
        return w;
    }

    private static Widget<RootData> createSelfStats(final BattleParticipantHandle handle) {
        return new TerminalWidget<>(StateUpdater.none(), WidgetRenderUtils.basicPanelTerminal(-1), Sizer.max());
    }

    public static final class RootData {
        public final BattleParticipantHandle handle;
        public MenuState menuState = MenuState.MAIN;

        public RootData(final BattleParticipantHandle handle) {
            this.handle = handle;
        }
    }

    public enum MenuState {
        MAIN,
        INVENTORY,
        ACTIONS,
        SELF_STATS,
        OTHER_STATS
    }

    public static final class SectionState {
        public boolean hovered = false;
        public final RootData rootData;

        public SectionState(final RootData data) {
            rootData = data;
        }
    }
}
