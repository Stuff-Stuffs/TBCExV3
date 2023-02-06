package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.impl.battles.participant.action.BattleActionHudRegistryImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.mixin.MixinGameRenderer;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface BattleActionHudRegistry {
    BattleActionHudRegistry INSTANCE = new BattleActionHudRegistryImpl();

    void register(Identifier id, Function<MouseLocker, BiFunction<Sizing, Sizing, ParentComponent>> renderer);

    BiFunction<Sizing, Sizing, ParentComponent> get(Identifier id, MouseLocker locker);

    interface MouseLocker {
        void lockMouse();

        void unlockMouse();
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
