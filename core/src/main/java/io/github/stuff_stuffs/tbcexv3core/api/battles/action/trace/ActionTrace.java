package io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace;

public sealed interface ActionTrace permits ActionTrace.BattleEnd, ActionTrace.BattleStart, BattleActionTraces.BattleAddEffect, BattleActionTraces.BattleRemoveEffect, BattleActionTraces.BattleSetBounds, BattleActionTraces.BattleTeamSetRelation, ParticipantActionTraces.BattleParticipantAddEffect, ParticipantActionTraces.BattleParticipantChangeBounds, ParticipantActionTraces.BattleParticipantEndMove, ParticipantActionTraces.BattleParticipantJoined, ParticipantActionTraces.BattleParticipantLeft, ParticipantActionTraces.BattleParticipantMove, ParticipantActionTraces.BattleParticipantRemoveEffect, ParticipantActionTraces.BattleParticipantSetTeam, ParticipantActionTraces.BattleParticipantStartMove, ParticipantActionTraces.Inventory.Equip, ParticipantActionTraces.Inventory.Give, ParticipantActionTraces.Inventory.Take, ParticipantActionTraces.Inventory.Unequip, ParticipantActionTraces.Stat.AddStatModifier, ParticipantActionTraces.Stat.RemoveStatModifier {
    final class BattleStart implements ActionTrace {
        public static final BattleStart INSTANCE = new BattleStart();

        private BattleStart() {
        }
    }

    final class BattleEnd implements ActionTrace {
        public static final BattleEnd INSTANCE = new BattleEnd();

        private BattleEnd() {
        }
    }

    interface Participant {

    }

}
