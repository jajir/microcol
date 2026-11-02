package org.akropia.model.campaign.po;

import org.akropia.model.Calendar;
import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Model;
import org.akropia.model.Player;

import com.google.common.base.Preconditions;

/**
 * Overview of the human player in a saved game (R-0382). It is derived for
 * display only; loading never reads game values from it. Identities are stable
 * keys, not translated labels.
 */
public class SaveSummaryPo {

    private String playerName;
    private String nation;
    private String difficulty;
    private int year;
    private String season;
    private int gold;
    private int rebelSupportPercent;
    private int peopleCount;
    private int colonyCount;
    private int shipCount;
    private String campaignName;
    private String missionName;

    /**
     * Describe the human player of the given state with the existing owners of
     * each value: base-score people, national rebel support and owned colonies
     * and ships.
     *
     * @param model        required game state being saved
     * @param campaignName optional campaign key
     * @param missionName  optional mission key
     * @return summary of the same state
     */
    public static SaveSummaryPo describe(final Model model,
            final String campaignName, final String missionName) {
        Preconditions.checkNotNull(model);
        final Player player = model.getPlayers().stream()
                .filter(Player::isHuman).findFirst().orElseThrow(
                        () -> new IllegalStateException("No human player"));
        final Calendar calendar = model.getCalendar();
        final SaveSummaryPo out = new SaveSummaryPo();
        out.playerName = player.getDisplayName();
        out.nation = EuropeanNation.findByPlayerName(player.getName())
                .map(EuropeanNation::name).orElse(player.getName());
        out.difficulty = model.getDifficulty().name();
        out.year = calendar.getCurrentYear();
        out.season = calendar.getCurrentSeason().map(Enum::name).orElse(null);
        out.gold = player.getGold();
        out.rebelSupportPercent = player.getNationalRebelSupport()
                .getDisplayPercent();
        if (player.isColonialNation()) {
            // Each living person once, wherever it is; no ships, wagons or
            // artillery.
            final var score = player.getBaseScore();
            out.peopleCount = score.getServants() + score.getColonists()
                    + score.getExperts();
        }
        out.colonyCount = player.getColonies().size();
        out.shipCount = (int) player.getAllUnits().stream()
                .filter(unit -> !unit.isCrownSeized())
                .filter(unit -> unit.getType().isShip()).count();
        out.campaignName = campaignName;
        out.missionName = missionName;
        return out;
    }

    /**
     * Check the required overview values: player, nation, difficulty, year,
     * gold, support and counts; season, campaign and mission are optional but
     * valid when present.
     *
     * @throws IllegalArgumentException when a value is missing or invalid
     */
    void validate() {
        Preconditions.checkArgument(playerName != null,
                "Player name is required");
        Preconditions.checkArgument(
                playerName.equals(Player.normalizeDisplayName(playerName)),
                "Player name is not normalized");
        Preconditions.checkArgument(nation != null && !nation.isBlank(),
                "Nation is required");
        Preconditions.checkArgument(difficulty != null,
                "Difficulty is required");
        Difficulty.valueOf(difficulty);
        Preconditions.checkArgument(year > 0, "Invalid year");
        if (season != null) {
            Calendar.Season.valueOf(season);
        }
        Preconditions.checkArgument(gold >= 0, "Negative gold");
        Preconditions.checkArgument(
                rebelSupportPercent >= 0 && rebelSupportPercent <= 100,
                "Support percentage outside 0..100");
        Preconditions.checkArgument(
                peopleCount >= 0 && colonyCount >= 0 && shipCount >= 0,
                "Negative count");
        Preconditions.checkArgument(
                campaignName == null || !campaignName.isBlank(),
                "Blank campaign");
        Preconditions.checkArgument(
                missionName == null || !missionName.isBlank(), "Blank mission");
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getNation() {
        return nation;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getYear() {
        return year;
    }

    public String getSeason() {
        return season;
    }

    public int getGold() {
        return gold;
    }

    public int getRebelSupportPercent() {
        return rebelSupportPercent;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public int getColonyCount() {
        return colonyCount;
    }

    public int getShipCount() {
        return shipCount;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public String getMissionName() {
        return missionName;
    }
}
