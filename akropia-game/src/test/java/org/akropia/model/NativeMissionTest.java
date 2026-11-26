package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.akropia.model.NativeRelationsScene.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.akropia.model.game.GameModel;
import org.akropia.model.store.ModelPo;

/** One-shot diplomacy, including cancellation and stale confirmations. */
class NativeMissionTest {
    private static Model scene() {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0),
                        unit -> unit.setMissionary(true))
                .unit(DUTCH, UnitType.COLONIST, at(1, -1),
                        unit -> unit.setMissionary(true))
                .toModel();
    }

    private static NativeMissionView preview(final Model model, final int id) {
        return model.getNativeMissionPreview(model.getUnitById(id),
                settlement(model)).orElseThrow();
    }

    private static void send(final Model model, final int id) {
        model.establishNativeMission(model.getUnitById(id), settlement(model),
                preview(model, id));
    }

    @Test
    void missionaryIsConsumedAndChangesNationalFavourExactlyOnce() {
        final Model model = scene();
        final Unit missionary = model.getUnitById(200);
        final NativeMissionView shown = preview(model, 200);
        model.establishNativeMission(missionary, settlement(model), shown);
        assertTrue(model.tryGetUnitById(200).isEmpty());
        assertFalse(model.getPlayerByName(DUTCH).getAllUnits()
                .contains(missionary));
        assertTrue(settlement(model).getMissionaryId().isEmpty());
        assertEquals(6, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
        assertEquals(4, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.FRENCH));
        assertThrows(IllegalStateException.class, () -> model
                .establishNativeMission(missionary, settlement(model), shown));
        assertEquals(6, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
    }

    @Test
    void previewAndCancellationHaveNoEffectAndAnotherMissionaryCanFollow() {
        final Model model = scene();
        preview(model, 200);
        preview(model, 200);
        assertTrue(model.tryGetUnitById(200).isPresent());
        assertEquals(5, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
        send(model, 200);
        send(model, 201);
        assertEquals(7, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
        assertTrue(model.tryGetUnitById(201).isEmpty());
    }

    @Test
    void changedFavourRejectsStaleConfirmationWithoutConsumingVisitor() {
        final Model model = scene();
        final NativeMissionView stale = preview(model, 201);
        send(model, 200);
        assertThrows(IllegalStateException.class,
                () -> model.establishNativeMission(model.getUnitById(201),
                        settlement(model), stale));
        assertTrue(model.tryGetUnitById(201).isPresent());
        assertEquals(6, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
    }

    @Test
    void distantUnordainedAndWrongTurnVisitorsAreRejected() {
        final Model model = scene();
        final NativeMissionView shown = preview(model, 200);
        model.getUnitById(200).placeToLocation(at(3, 0));
        assertThrows(IllegalStateException.class,
                () -> model.establishNativeMission(model.getUnitById(200),
                        settlement(model), shown));
        final Model unordained = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED).toModel();
        assertTrue(unordained.getNativeMissionPreview(
                unordained.getUnitById(200), settlement(unordained)).isEmpty());
        final Model wrongTurn = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0),
                        unit -> unit.setMissionary(true))
                .currentPlayer(FRANCE).toModel();
        assertTrue(wrongTurn.getNativeMissionPreview(wrongTurn.getUnitById(200),
                settlement(wrongTurn)).isEmpty());
    }

    @Test
    void expertInfluenceAppliesOnceAndSurvivesReloadWithoutResidentMission() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.JESUIT_MISSIONARY, at(1, 0), unit -> {
                    unit.setWorkUpdates(0);
                    unit.setMissionary(true);
                }).toModel();
        assertEquals(2, preview(model, 200).getInfluence());
        send(model, 200);
        final Model restored = Model.make(model.save(),
                GameModel.gameOverEvaluators());
        assertTrue(restored.tryGetUnitById(200).isEmpty());
        assertEquals(7, restored.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
        assertEquals(3, restored.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.FRENCH));
        assertTrue(settlement(restored).getMissionaryId().isEmpty());
    }

    @Test
    void hostileTribeAcceptsMissionaryAndCanBeWonFromAnotherFriend() {
        final ModelPo saved = scene().save();
        saved.getNativeFriendships().stream()
                .filter(row -> row.getNation() == NativeNation.AZTEC)
                .forEach(row -> {
                    row.getFavour().put(EuropeanNation.DUTCH, 3);
                    row.getFavour().put(EuropeanNation.FRENCH, 8);
                    row.setFriend(EuropeanNation.FRENCH);
                });
        final Model model = Model.make(saved, GameModel.gameOverEvaluators());
        assertEquals(Optional.of(EuropeanNation.FRENCH),
                model.getNativeFriend(NativeNation.AZTEC));
        send(model, 200);
        assertEquals(4, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
        assertTrue(model.getNativeFriend(NativeNation.AZTEC).isEmpty());
    }
}
