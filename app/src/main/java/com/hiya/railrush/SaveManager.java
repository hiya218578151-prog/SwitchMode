package com.hiya.railrush;

import android.content.Context;
import android.content.SharedPreferences;

final class SaveManager {
    private static final String PREFS = "rail_rush_save_v2";
    private final SharedPreferences prefs;

    int coins = 250;
    int keys = 6;
    int bestScore = 0;
    int multiplier = 1;
    int missionsCompleted = 0;
    int boards = 3;
    int selectedCharacter = 0;
    int selectedOutfit = 0;
    int selectedBoard = 0;
    int magnetLevel = 1;
    int sneakersLevel = 1;
    int x2Level = 1;
    int jetpackLevel = 1;
    int pogoLevel = 1;
    int boardPowerLevel = 1;

    int seasonTokens = 0;
    int eventTokens = 0;
    int characterTokens = 0;
    int wordStreak = 0;
    String wordProgress = "";
    long wordDay = -1L;
    long seasonStart = 0L;
    int seasonClaimedTier = 0;

    long lastDailyClaimDay = -1L;
    long lastMysteryClaimDay = -1L;
    long lastWeeklyClaim = -1L;
    int loginStreak = 0;
    int mysteryBoxesOpened = 0;
    int superBoxes = 1;

    int weekBest = 0;
    int league = 1;
    int seasonChallengeScore = 0;
    int seasonChallengeRuns = 0;
    int mysteryHurdlesBest = 0;
    int marathonBest = 0;
    int showdownWins = 0;
    int lowGravityBest = 0;

    int collectionRewardTier = 0;
    boolean board2Unlocked = false;
    boolean board3Unlocked = false;
    boolean character2Unlocked = false;
    boolean character3Unlocked = false;
    boolean outfit1Unlocked = false;
    boolean outfit2Unlocked = false;

    boolean soundOn = true;
    boolean vibrationOn = true;

    long totalRuns = 0L;
    long totalDistance = 0L;
    long totalCoins = 0L;
    long totalPowerUps = 0L;
    long totalJumps = 0L;
    long totalRolls = 0L;

    SaveManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        coins = prefs.getInt("coins", 250);
        keys = prefs.getInt("keys", 6);
        bestScore = prefs.getInt("bestScore", 0);
        multiplier = prefs.getInt("multiplier", 1);
        missionsCompleted = prefs.getInt("missionsCompleted", 0);
        boards = prefs.getInt("boards", 3);
        selectedCharacter = prefs.getInt("selectedCharacter", 0);
        selectedOutfit = prefs.getInt("selectedOutfit", 0);
        selectedBoard = prefs.getInt("selectedBoard", 0);
        magnetLevel = prefs.getInt("magnetLevel", 1);
        sneakersLevel = prefs.getInt("sneakersLevel", 1);
        x2Level = prefs.getInt("x2Level", 1);
        jetpackLevel = prefs.getInt("jetpackLevel", 1);
        pogoLevel = prefs.getInt("pogoLevel", 1);
        boardPowerLevel = prefs.getInt("boardPowerLevel", 1);
        seasonTokens = prefs.getInt("seasonTokens", 0);
        eventTokens = prefs.getInt("eventTokens", 0);
        characterTokens = prefs.getInt("characterTokens", 0);
        wordStreak = prefs.getInt("wordStreak", 0);
        wordProgress = prefs.getString("wordProgress", "");
        wordDay = prefs.getLong("wordDay", -1L);
        seasonStart = prefs.getLong("seasonStart", 0L);
        seasonClaimedTier = prefs.getInt("seasonClaimedTier", 0);
        lastDailyClaimDay = prefs.getLong("lastDailyClaimDay", -1L);
        lastMysteryClaimDay = prefs.getLong("lastMysteryClaimDay", -1L);
        lastWeeklyClaim = prefs.getLong("lastWeeklyClaim", -1L);
        loginStreak = prefs.getInt("loginStreak", 0);
        mysteryBoxesOpened = prefs.getInt("mysteryBoxesOpened", 0);
        superBoxes = prefs.getInt("superBoxes", 1);
        weekBest = prefs.getInt("weekBest", 0);
        league = prefs.getInt("league", 1);
        seasonChallengeScore = prefs.getInt("seasonChallengeScore", 0);
        seasonChallengeRuns = prefs.getInt("seasonChallengeRuns", 0);
        mysteryHurdlesBest = prefs.getInt("mysteryHurdlesBest", 0);
        marathonBest = prefs.getInt("marathonBest", 0);
        showdownWins = prefs.getInt("showdownWins", 0);
        lowGravityBest = prefs.getInt("lowGravityBest", 0);
        collectionRewardTier = prefs.getInt("collectionRewardTier", 0);
        board2Unlocked = prefs.getBoolean("board2Unlocked", false);
        board3Unlocked = prefs.getBoolean("board3Unlocked", false);
        character2Unlocked = prefs.getBoolean("character2Unlocked", false);
        character3Unlocked = prefs.getBoolean("character3Unlocked", false);
        outfit1Unlocked = prefs.getBoolean("outfit1Unlocked", false);
        outfit2Unlocked = prefs.getBoolean("outfit2Unlocked", false);
        soundOn = prefs.getBoolean("soundOn", true);
        vibrationOn = prefs.getBoolean("vibrationOn", true);
        totalRuns = prefs.getLong("totalRuns", 0L);
        totalDistance = prefs.getLong("totalDistance", 0L);
        totalCoins = prefs.getLong("totalCoins", 0L);
        totalPowerUps = prefs.getLong("totalPowerUps", 0L);
        totalJumps = prefs.getLong("totalJumps", 0L);
        totalRolls = prefs.getLong("totalRolls", 0L);

        if (seasonStart == 0L) {
            seasonStart = System.currentTimeMillis();
            save();
        }
    }

    void save() {
        prefs.edit()
            .putInt("coins", coins)
            .putInt("keys", keys)
            .putInt("bestScore", bestScore)
            .putInt("multiplier", multiplier)
            .putInt("missionsCompleted", missionsCompleted)
            .putInt("boards", boards)
            .putInt("selectedCharacter", selectedCharacter)
            .putInt("selectedOutfit", selectedOutfit)
            .putInt("selectedBoard", selectedBoard)
            .putInt("magnetLevel", magnetLevel)
            .putInt("sneakersLevel", sneakersLevel)
            .putInt("x2Level", x2Level)
            .putInt("jetpackLevel", jetpackLevel)
            .putInt("pogoLevel", pogoLevel)
            .putInt("boardPowerLevel", boardPowerLevel)
            .putInt("seasonTokens", seasonTokens)
            .putInt("eventTokens", eventTokens)
            .putInt("characterTokens", characterTokens)
            .putInt("wordStreak", wordStreak)
            .putString("wordProgress", wordProgress)
            .putLong("wordDay", wordDay)
            .putLong("seasonStart", seasonStart)
            .putInt("seasonClaimedTier", seasonClaimedTier)
            .putLong("lastDailyClaimDay", lastDailyClaimDay)
            .putLong("lastMysteryClaimDay", lastMysteryClaimDay)
            .putLong("lastWeeklyClaim", lastWeeklyClaim)
            .putInt("loginStreak", loginStreak)
            .putInt("mysteryBoxesOpened", mysteryBoxesOpened)
            .putInt("superBoxes", superBoxes)
            .putInt("weekBest", weekBest)
            .putInt("league", league)
            .putInt("seasonChallengeScore", seasonChallengeScore)
            .putInt("seasonChallengeRuns", seasonChallengeRuns)
            .putInt("mysteryHurdlesBest", mysteryHurdlesBest)
            .putInt("marathonBest", marathonBest)
            .putInt("showdownWins", showdownWins)
            .putInt("lowGravityBest", lowGravityBest)
            .putInt("collectionRewardTier", collectionRewardTier)
            .putBoolean("board2Unlocked", board2Unlocked)
            .putBoolean("board3Unlocked", board3Unlocked)
            .putBoolean("character2Unlocked", character2Unlocked)
            .putBoolean("character3Unlocked", character3Unlocked)
            .putBoolean("outfit1Unlocked", outfit1Unlocked)
            .putBoolean("outfit2Unlocked", outfit2Unlocked)
            .putBoolean("soundOn", soundOn)
            .putBoolean("vibrationOn", vibrationOn)
            .putLong("totalRuns", totalRuns)
            .putLong("totalDistance", totalDistance)
            .putLong("totalCoins", totalCoins)
            .putLong("totalPowerUps", totalPowerUps)
            .putLong("totalJumps", totalJumps)
            .putLong("totalRolls", totalRolls)
            .apply();
    }

    boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        save();
        return true;
    }

    boolean spendKeys(int amount) {
        if (keys < amount) return false;
        keys -= amount;
        save();
        return true;
    }

    void addCoins(int amount) {
        coins += Math.max(0, amount);
        save();
    }

    long dayStart(long time) {
        return time / 86400000L;
    }

    int seasonTier() {
        long days = Math.max(0L, dayStart(System.currentTimeMillis()) - dayStart(seasonStart));
        int byTime = (int)Math.min(30L, days + 1L);
        int byTokens = Math.min(30, seasonTokens / 40);
        return Math.min(30, Math.max(1, Math.min(byTime, Math.max(1, byTokens))));
    }

    int[] loadMissionProgress() {
        int[] result = new int[] {0, 0, 0};
        String raw = prefs.getString("missionProgress", "0,0,0");
        String[] parts = raw.split(",");
        for (int i = 0; i < result.length && i < parts.length; i++) {
            try { result[i] = Math.max(0, Integer.parseInt(parts[i])); }
            catch (NumberFormatException ignored) {}
        }
        return result;
    }

    void saveMissionProgress(java.util.List<Mission> missions) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) b.append(',');
            b.append(i < missions.size() ? missions.get(i).progress : 0);
        }
        prefs.edit().putString("missionProgress", b.toString()).apply();
    }
}
