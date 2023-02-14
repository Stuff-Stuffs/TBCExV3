package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.gui.DoublePressHandler;
import io.github.stuff_stuffs.tbcexv3core.api.gui.TBCExGUI;
import io.github.stuff_stuffs.tbcexv3core.api.gui.WrapperComponent;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import io.github.stuff_stuffs.tbcexv3core.impl.battles.participant.action.BattleActionHudRegistryImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.entity.TBCExClientPlayerExtensions;
import io.github.stuff_stuffs.tbcexv3core.internal.client.mixin.MixinGameRenderer;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface BattleActionHudRegistry {
    BattleActionHudRegistry INSTANCE = new BattleActionHudRegistryImpl();

    void register(Identifier id, BiFunction<MouseLocker, Consumer<Runnable>, BiFunction<Sizing, Sizing, ParentComponent>> renderer);

    BiFunction<Sizing, Sizing, ParentComponent> get(Identifier id, MouseLocker locker, Consumer<Runnable> cleanup);

    interface MouseLocker {
        void lockMouse();

        void unlockMouse();
    }

    interface TargetRenderer {
        void render(@Nullable BattleParticipantActionTarget selected, BattleParticipantActionBuilder<?> builder, BattleView state, Consumer<Runnable> cleanup);
    }

    private static @Nullable BattleView tryGetBattleView() {
        final BattleHandle handle = ((TBCExPlayerEntity) MinecraftClient.getInstance().player).tbcex$getCurrentBattle();
        if (handle == null) {
            return null;
        }
        final BattleParticipantHandle participantHandle = BattleParticipantHandle.of(MinecraftClient.getInstance().player.getUuid(), handle);
        final BattleWorld world = ((BattleWorld) MinecraftClient.getInstance().world);
        return world.tryGetBattleView(participantHandle.getParent());
    }

    private static @Nullable BattleParticipantStateView tryGetParticipantView() {
        final BattleHandle handle = ((TBCExPlayerEntity) MinecraftClient.getInstance().player).tbcex$getCurrentBattle();
        if (handle == null) {
            return null;
        }
        final BattleParticipantHandle participantHandle = BattleParticipantHandle.of(MinecraftClient.getInstance().player.getUuid(), handle);
        final BattleWorld world = ((BattleWorld) MinecraftClient.getInstance().world);
        final BattleView battleView = world.tryGetBattleView(participantHandle.getParent());
        if (battleView == null) {
            return null;
        }
        return battleView.getState().getParticipantByHandle(participantHandle);
    }

    static BiFunction<MouseLocker, Consumer<Runnable>, BiFunction<Sizing, Sizing, ParentComponent>> basic(final TargetRenderer targetRenderer) {
        return (locker, cleanup) -> (horizontal, vertical) -> {
            final FlowLayout layout = Containers.horizontalFlow(horizontal, vertical);
            final boolean[] freeCam = new boolean[]{false};
            layout.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    freeCam[0] = !freeCam[0];
                    if (freeCam[0]) {
                        locker.lockMouse();
                    } else {
                        locker.unlockMouse();
                    }
                    return true;
                }
                return false;
            });
            final BattleParticipantActionBuilder<?> builder = ((TBCExClientPlayerExtensions) MinecraftClient.getInstance().player).tbcexcore$action$current();
            if (builder == null) {
                throw new RuntimeException();
            }
            final Observable<Optional<Pair<BattleParticipantActionTarget, Double>>> targetObservable = Observable.of(Optional.empty());
            final Observable<Boolean> canBuild = Observable.of(false);
            final FlowLayout flowLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
            flowLayout.gap(8);
            flowLayout.surface(TBCExGUI.DEFAULT_SURFACE);
            targetObservable.observe(pair -> {
                flowLayout.clearChildren();
                if (pair.isPresent()) {
                    final BattleParticipantStateView stateView = tryGetParticipantView();
                    if (stateView == null) {
                        return;
                    }
                    final LabelComponent name = Components.label(pair.get().getFirst().name());
                    flowLayout.child(name);
                    final FlowLayout descriptionLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
                    descriptionLayout.surface(TBCExGUI.TOOLTIP_SURFACE);
                    descriptionLayout.margins(Insets.both(4, 4));
                    final TooltipText description = pair.get().getFirst().description();
                    for (final Text text : description.texts()) {
                        descriptionLayout.child(Components.label(text));
                    }
                    flowLayout.child(descriptionLayout);
                }
            });
            final DoublePressHandler<DoublePressHandler.MouseDownContext> handler = DoublePressHandler.mousePress(layout.mouseDown(), GLFW.GLFW_MOUSE_BUTTON_LEFT);
            handler.doublePress.subscribe(context -> {
                if (targetObservable.get().isPresent()) {
                    final Pair<BattleParticipantActionTarget, Double> pair = targetObservable.get().get();
                    final BattleParticipantActionBuilder.TargetIterator<?> iterator = builder.targets(pair.getFirst().type());
                    while (iterator.hasNext()) {
                        if (iterator.next().equals(pair.getFirst())) {
                            iterator.accept();
                            canBuild.set(builder.canBuild());
                            return true;
                        }
                    }
                }
                return false;
            });
            final GridLayout gridLayout = Containers.grid(Sizing.content(), Sizing.content(), 1, 1);
            gridLayout.surface(TBCExGUI.DEFAULT_SURFACE);
            final WrapperComponent<LabelComponent> buildLabel = new WrapperComponent<>(Sizing.fixed(32), Sizing.fixed(24), Components.label(Text.of("Do it!")));
            gridLayout.child(buildLabel, 0, 0);
            canBuild.observe(b -> {
                if (b) {
                    layout.child(gridLayout);
                } else {
                    layout.removeChild(gridLayout);
                }
            });
            gridLayout.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && canBuild.get()) {
                    builder.build();
                    MinecraftClient.getInstance().setScreen(null);
                    locker.lockMouse();
                    return true;
                }
                return false;
            });
            layout.child(flowLayout);
            layout.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    return false;
                }
                canBuild.set(builder.canBuild());
                final Iterator<? extends BattleParticipantActionTargetType<?>> types = builder.types();
                final BattleView battle = ((BattleWorld) MinecraftClient.getInstance().world).tryGetBattleView(((TBCExPlayerEntity) MinecraftClient.getInstance().player).tbcex$getCurrentBattle());
                if (battle == null) {
                    return false;
                }
                final Vec3d camera = battle.toLocal(MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
                final Vec3d end = camera.add(BattleActionHudRegistry.getMouseVector().multiply(24));
                double best = targetObservable.get().map(Pair::getSecond).orElse(Double.POSITIVE_INFINITY);
                BattleParticipantActionTarget target = null;
                boolean previous = false;
                while (types.hasNext()) {
                    final BattleParticipantActionTargetType<?> type = types.next();
                    final BattleParticipantActionBuilder.TargetRaycaster<?> raycaster = builder.raycastTargets(type);
                    final Optional<? extends Pair<? extends BattleParticipantActionTarget, Double>> query;
                    if ((query = raycaster.query(camera, end)).isPresent()) {
                        if (targetObservable.get().isPresent() && query.get().getFirst().equals(targetObservable.get().get().getFirst())) {
                            previous = true;
                        }
                        if (query.get().getSecond() < best) {
                            best = query.get().getSecond();
                            target = query.get().getFirst();
                        }
                    }
                }
                if (target != null) {
                    targetObservable.set(Optional.of(Pair.of(target, best)));
                } else if (!previous) {
                    targetObservable.set(Optional.empty());
                }
                return true;
            });
            final WrapperComponent<FlowLayout> wrapper = new WrapperComponent<>(Sizing.content(), Sizing.content(), layout);
            wrapper.preDraw.subscribe(handler::update);
            wrapper.preDraw.subscribe(delta -> targetRenderer.render(targetObservable.get().map(Pair::getFirst).orElse(null), builder, tryGetBattleView(), cleanup));
            return wrapper;
        };
    }

    static Vec3d getMouseVector() {
        final MinecraftClient client = MinecraftClient.getInstance();
        final double fov = Math.toRadians(((MixinGameRenderer) client.gameRenderer).invokeGetFov(client.gameRenderer.getCamera(), 0.5f, true));
        final Vector3f vec3f = new Vector3f((float) (client.getWindow().getFramebufferWidth() / 2d - client.mouse.getX()), (float) (client.getWindow().getFramebufferHeight() / 2d - client.mouse.getY()), (client.getWindow().getFramebufferHeight() / 2f) / ((float) Math.tan(fov / 2d)));
        final Quaternionf rotation = client.gameRenderer.getCamera().getRotation();
        vec3f.rotate(rotation);
        vec3f.normalize();
        return new Vec3d(vec3f);
    }
}
