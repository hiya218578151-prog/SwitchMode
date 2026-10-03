package com.hiya.railrush;

import android.content.Context;
import android.content.SharedPreferences;

final class SaveManager {
    private static final String PREFS = "rail_rush_save";
    private final SharedPreferences prefs;

    int coins;
    int keys;
    int bestScore;
    int multiplier;
    int missionsCompleted;
    int boards;
    int selectedCharacter;
    int selectedBoard;
    int magnetLevel;
    int sneakersLevel;
    int x2Level;
    int jetpackLevel;
    boolean soundOn;
    boolean vibrationOn;
    boolean board2Unlocked;
    boolean board3Unlocked;
    boolean character2Unlocked;

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
        selectedBoard = prefs.getInt("selectedBoard", 0);
        magnetLevel = prefs.getInt("magnetLevel", 1);
        sneakersLevel = prefs.getInt("sneakersLevel", 1);
        x2Level = prefs.getInt("x2Level", 1);
        jetpackLevel = prefs.getInt("jetpackLevel", 1);
        soundOn = prefs.getBoolean("soundOn", true);
        vibrationOn = prefs.getBoolean("vibrationOn", true);
        board2Unlocked = prefs.getBoolean("board2Unlocked", false);
        board3Unlocked = prefs.getBoolean("board3Unlocked", false);
        character2Unlocked = prefs.getBoolean("character2Unlocked", false);
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
                .putInt("selectedBoard", selectedBoard)
                .putInt("magnetLevel", magnetLevel)
                .putInt("sneakersLevel", sneakersLevel)
                .putInt("x2Level", x2Level)
                .putInt("jetpackLevel", jetpackLevel)
                .putBoolean("soundOn", soundOn)
                .putBoolean("vibrationOn", vibrationOn)
                .putBoolean("board2Unlocked", board2Unlocked)
                .putBoolean("board3Unlocked", board3Unlocked)
                .putBoolean("character2Unlocked", character2Unlocked)
                .apply();
    }

    boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        save();
        return true;
    }

    void addCoins(int amount) {
        coins += Math.max(0, amount);
        save();
    }

    boolean spendKeys(int amount) {
        if (keys < amount) return false;
        keys -= amount;
        save();
        return true;
    }

    int[] loadMissionProgress() {
        int[] result = new int[3];
        String raw = prefs.getString("missionProgress", "0,0,0");
        String[] parts = raw.split(",");
        for (int i = 0; i < result.length && i < parts.length; i++) {
            try {
                result[i] = Math.max(0, Integer.parseInt(parts[i]));
            } catch (NumberFormatException ignored) {
                result[i] = 0;
            }
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
