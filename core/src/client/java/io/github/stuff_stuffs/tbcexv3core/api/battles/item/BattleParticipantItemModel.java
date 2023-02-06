package io.github.stuff_stuffs.tbcexv3core.api.battles.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;

public interface BattleParticipantItemModel {
    void render(MatrixStack matrices, BattleParticipantItemStack stack, BattleParticipantStateView state, VertexConsumerProvider vertexConsumers, World world, int light);
}
