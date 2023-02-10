package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.impl.scene.AnimationSceneImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionfc;

import java.util.Set;

public interface AnimationScene {
    Set<Identifier> models();

    Model model(Identifier id);

    void removeModel(Identifier id);

    void addModel(Identifier id);

    void render(MatrixStack matrices, VertexConsumerProvider vertexConsumer, Vec3d cameraPos, Quaternionfc cameraLook, double time);

    static AnimationScene create() {
        return new AnimationSceneImpl();
    }
}
