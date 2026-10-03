package com.hiya.railrush;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

final class GameEngine {
    static final int MODE_NORMAL = 0;
    static final int MODE_HURDLES = 1;
    static final int MODE_SEASON = 2;
    static final int MODE_MARATHON = 3;
    static final int MODE_ATTACK = 4;
    static final int MODE_NO_FLOOR = 5;
    static final int MODE_LAVA = 6;
    static final int MODE_SHOWDOWN = 7;
    static final int MODE_LOW_GRAVITY = 8;

    final SaveManager save;
    final List<GameObject> objects = new ArrayList<GameObject>();
    final List<TrailParticle> particles = new ArrayList<TrailParticle>();
    final List<Mission> missions = new ArrayList<Mission>();
    final Random random = new Random(20261004L);
    final Context context;
    final GameRenderer renderer = new GameRenderer();

    ScreenState state = ScreenState.HOME;
    int focus = 0;

    int playerLane = 1;
    float laneVisual = 1f;
    float score;
    float distance;
    int runCoins;
    int runKeys;

    float speed = 0.34f;
    float spawnTimer = 0.3f;
    float jumpTimer;
    float rollTimer;
    float laneCooldown;
    float boardTimer;
    boolean boardActive;

    PowerType activePower = PowerType.NONE;
    float powerTimer;
    float x2Timer;
    float headstartTimer;
    float attackTimer;
    float scoreBoosterTimer;
    int runRevives;

    float noticeTimer;
    String notice = "";
    float worldPulse;

    int totalJumps;
    int totalRolls;
    int totalPowerUps;
    int totalDodges;

    int worldIndex;
    int challengeMode = MODE_NORMAL;
    int mysteryDifficulty = 1;
    boolean pausedBySystem;

    private final Vibrator vibrator;
    private final ToneGenerator tone;

    private static final String[] WORDS = {
            "NEON", "RAIL", "RUSH", "JUMP", "COIN", "POWER", "BOARD",
            "NOVA", "SPEED", "TRACK", "URBAN", "NIGHT", "WAVE", "FLAME"
    };

    private static final String[] WORLDS = {
            "Neon City", "Rio Rush", "Tokyo Lights", "Metro Rome", "Ocean Rails",
            "Haunted Hood", "Alpine Run", "Desert Mirage", "Cyber London",
            "Shenzhen Pulse", "Skyline Mumbai", "Paris Night", "Cosmic Crossroads",
            "Istanbul Glow", "Hollywood Grid", "Winter Wonderland", "Aloha Hawaii",
            "Copenhagen Pop", "Buenos Aires Beat", "Subway Classic",
            "Moon Station", "Underwater", "Ancient East", "Future Arcade"
    };

    GameEngine(Context context) {
        this.context = context.getApplicationContext();
        save = new SaveManager(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 42);
        buildMissions();
        restoreMissionProgress();
        worldIndex = currentWorldIndex();
        ensureDailyData();
        ensureSeasonData();
        ensureWeeklyData();
        refreshWeekLeague();
    }

    void update(float dt) {
        worldPulse += dt;
        if (noticeTimer > 0f) noticeTimer -= dt;

        if (state == ScreenState.RUNNING) updateRun(dt);

        Iterator<TrailParticle> it = particles.iterator();
        while (it.hasNext()) {
            TrailParticle q = it.next();
            q.life -= dt;
            q.x += q.vx * dt;
            q.y += q.vy * dt;
            if (q.life <= 0f) it.remove();
        }
    }

    private void updateRun(float dt) {
        speed = Math.min(0.82f, speed + dt * (challengeMode == MODE_ATTACK ? 0.012f : 0.008f));
        if (challengeMode == MODE_ATTACK) {
            attackTimer -= dt;
            if (attackTimer <= 0f) { gameOver(); return; }
        }
        distance += speed * 82f * dt;

        if (headstartTimer > 0f) {
            headstartTimer = Math.max(0f, headstartTimer - dt);
        }

        jumpTimer = Math.max(0f, jumpTimer - dt);
        rollTimer = Math.max(0f, rollTimer - dt);
        laneCooldown = Math.max(0f, laneCooldown - dt);
        boardTimer = Math.max(0f, boardTimer - dt);
        powerTimer = Math.max(0f, powerTimer - dt);
        x2Timer = Math.max(0f, x2Timer - dt);
        scoreBoosterTimer = Math.max(0f, scoreBoosterTimer - dt);

        if (boardActive && boardTimer <= 0f) boardActive = false;
        if (powerTimer <= 0f) activePower = PowerType.NONE;

        float multiplier = save.multiplier * (x2Timer > 0f ? 2f : 1f);
        if (scoreBoosterTimer > 0f) multiplier += 5f;
        float challengeBonus = challengeMode == MODE_HURDLES ? 1.35f
                : challengeMode == MODE_SHOWDOWN ? 1.2f
                : challengeMode == MODE_MARATHON ? 1.15f
                : 1f;
        score += speed * 105f * multiplier * challengeBonus * dt;

        float laneSpeed = 10f * (activePower == PowerType.SNEAKERS ? 1.32f : 1f);
        laneVisual += (playerLane - laneVisual) * Math.min(1f, laneSpeed * dt);

        spawnTimer -= dt;
        if (spawnTimer <= 0f) {
            spawnChunk();
            float cadence = challengeMode == MODE_ATTACK ? 0.56f : 0.84f;
            if (challengeMode == MODE_HURDLES) cadence = Math.max(0.42f, 0.70f - mysteryDifficulty * 0.07f);
            spawnTimer = Math.max(0.31f - mysteryDifficulty * 0.02f, cadence - speed * 0.38f);
        }

        for (GameObject object : objects) {
            if (!object.active) continue;
            object.z -= speed * dt;
            object.spin += dt * 5f;

            if (object.z < 0.22f && object.z > -0.02f) {
                processObject(object);
            }
            if (object.z <= -0.08f) {
                if (object.kind == GameObject.Kind.OBSTACLE && object.lane != playerLane) {
                    totalDodges++;
                    addMissionMetric("dodges", 1);
                }
                object.active = false;
            }
        }
        cleanupObjects();

        if (challengeMode == MODE_NO_FLOOR && jumpTimer <= 0f && distance > 25f) {
            if (random.nextFloat() < dt * 0.18f) {
                gameOver();
                return;
            }
        }

        if (challengeMode == MODE_LAVA && jumpTimer <= 0f && distance > 30f) {
            if (random.nextFloat() < dt * 0.32f) {
                gameOver();
                return;
            }
        }

        // Word Hunt letters appear only on the current daily word.
        if (distance > 25 && random.nextFloat() < dt * 0.045f && !wordComplete()) {
            String word = todayWord();
            int index = Math.min(word.length() - 1, save.wordProgress.length());
            String letter = String.valueOf(word.charAt(index));
            objects.add(GameObject.letter(random.nextInt(3), 1.25f, letter));
        }

        // Season/event tokens are a separate collection layer.
        if (random.nextFloat() < dt * 0.04f) {
            objects.add(GameObject.token(random.nextInt(3), 1.34f));
        }

        int distanceTick = (int)Math.max(1, speed * 82f * dt);
        addMissionMetric("distance", distanceTick);
        save.totalDistance += distanceTick;
    }

    private void spawnChunk() {
        int lane = random.nextInt(3);
        int roll = random.nextInt(100);
        ObstacleType obstacle;

        if (challengeMode == MODE_NO_FLOOR) {
            obstacle = ObstacleType.GAP;
        } else if (challengeMode == MODE_HURDLES) {
            obstacle = roll < 52 ? ObstacleType.LOW_BARRIER
                    : (roll < 82 ? ObstacleType.HIGH_BARRIER : ObstacleType.TUNNEL);
        } else {
            obstacle = roll < 28 ? ObstacleType.LOW_BARRIER
                    : (roll < 60 ? ObstacleType.TRAIN
                    : (roll < 79 ? ObstacleType.HIGH_BARRIER
                    : (roll < 91 ? ObstacleType.TUNNEL : ObstacleType.GRIND)));
        }

        objects.add(GameObject.obstacle(lane, 1.08f, obstacle));

        if (challengeMode != MODE_HURDLES && challengeMode != MODE_NO_FLOOR) {
            int coinLane = random.nextInt(3);
            for (int i = 0; i < 5; i++) {
                objects.add(GameObject.coin(coinLane, 1.18f + i * 0.105f));
            }
        }

        if (random.nextFloat() < 0.22f && challengeMode != MODE_HURDLES) {
            objects.add(GameObject.power((lane + 1 + random.nextInt(2)) % 3, 1.46f, randomPower()));
        }

        if (random.nextFloat() < 0.16f) {
            int secondLane = (lane + 1 + random.nextInt(2)) % 3;
            objects.add(GameObject.obstacle(secondLane, 1.52f,
                    random.nextBoolean() ? ObstacleType.TRAIN : ObstacleType.LOW_BARRIER));
        }
    }

    private PowerType randomPower() {
        int r = random.nextInt(100);
        if (r < 23) return PowerType.MAGNET;
        if (r < 46) return PowerType.SNEAKERS;
        if (r < 68) return PowerType.X2;
        if (r < 82) return PowerType.JETPACK;
        if (r < 93) return PowerType.POGO;
        return PowerType.MYSTERIZER;
    }

    private void processObject(GameObject object) {
        if (!object.active) return;

        if (object.kind == GameObject.Kind.COIN) {
            boolean magnet = activePower == PowerType.MAGNET && powerTimer > 0f;
            if (object.lane == playerLane || magnet) {
                object.active = false;
                int amount = x2Timer > 0f ? 2 : 1;
                runCoins += amount;
                score += 14f * amount * save.multiplier;
                save.totalCoins += amount;
                addMissionMetric("coins", amount);
                spawnBurst(object, 0xffFFE36E);
                beep(92);
            }
            return;
        }

        if (object.kind == GameObject.Kind.TOKEN) {
            if (object.lane == playerLane || activePower == PowerType.MAGNET) {
                object.active = false;
                save.seasonTokens += 1;
                save.eventTokens += eventBonusTokens();
                save.save();
                spawnBurst(object, 0xff7CDBFF);
                notice("SEASON TOKEN +" + 1, 0.8f);
            }
            return;
        }

        if (object.kind == GameObject.Kind.LETTER) {
            if (object.lane == playerLane || activePower == PowerType.MAGNET) {
                object.active = false;
                collectWordLetter(object.payload);
            }
            return;
        }

        if (object.kind == GameObject.Kind.POWER) {
            if (object.lane == playerLane || (object.z < 0.08f && activePower == PowerType.MAGNET)) {
                object.active = false;
                activatePower(object.powerType);
                save.totalPowerUps++;
                addMissionMetric("power", 1);
                totalPowerUps++;
                spawnBurst(object, powerColor(object.powerType));
                beep(75);
            }
            return;
        }

        if (object.kind == GameObject.Kind.OBSTACLE && object.z <= 0.10f && object.lane == playerLane) {
            if (isSafeAgainst(object.obstacleType)) {
                object.active = false;
                totalDodges++;
                addMissionMetric("dodges", 1);
                spawnBurst(object, 0xff63F2FF);
                return;
            }

            if (boardActive) {
                boardActive = false;
                boardTimer = 0f;
                object.active = false;
                addMissionMetric("boards", 1);
                spawnBurst(object, 0xffF6F0FF);
                notice("BOARD SAVE!", 1.1f);
                vibrate(38);
                beep(62);
            } else {
                gameOver();
            }
        }
    }

    private boolean isSafeAgainst(ObstacleType type) {
        if (activePower == PowerType.JETPACK || activePower == PowerType.POGO) return true;
        if (jumpTimer > 0f) return type != ObstacleType.TUNNEL || challengeMode == MODE_LOW_GRAVITY;
        if (rollTimer > 0f) return type == ObstacleType.LOW_BARRIER || type == ObstacleType.TUNNEL;
        return type == ObstacleType.GRIND && jumpTimer <= 0f;
    }

    private void cleanupObjects() {
        Iterator<GameObject> it = objects.iterator();
        while (it.hasNext()) if (!it.next().active) it.remove();
    }

    private void activatePower(PowerType type) {
        activePower = type;
        int level;

        switch (type) {
            case MAGNET:
                level = save.magnetLevel;
                powerTimer = 6.0f + level * 0.8f;
                notice("MAGNET ONLINE", 0.85f);
                break;
            case SNEAKERS:
                level = save.sneakersLevel;
                powerTimer = 6.0f + level * 0.8f;
                notice("SUPER SNEAKERS", 0.85f);
                break;
            case X2:
                level = save.x2Level;
                x2Timer = 6.0f + level * 0.8f;
                powerTimer = x2Timer;
                notice("SCORE x2", 0.85f);
                break;
            case JETPACK:
                level = save.jetpackLevel;
                powerTimer = 4.5f + level * 0.6f;
                jumpTimer = powerTimer;
                notice("JETPACK!", 0.85f);
                break;
            case POGO:
                level = save.pogoLevel;
                powerTimer = 5.0f + level * 0.7f;
                jumpTimer = powerTimer;
                notice("POGO AIR", 0.85f);
                break;
            case HOURGLASS:
                attackTimer += 4.0f;
                notice("HOURGLASS +4s", 0.8f);
                break;
            case MYSTERIZER:
                powerTimer = 5.5f;
                int roll = random.nextInt(4);
                if (roll == 0) save.coins += 60;
                else if (roll == 1) boardActive = true;
                else if (roll == 2) x2Timer = 5.5f;
                else jumpTimer = 2.2f;
                save.save();
                notice("SUPER MYSTERY!", 0.95f);
                break;
            default:
                break;
        }

        addMissionMetric("power", 1);
        vibrate(15);
    }

    void moveLane(int direction) {
        if (state != ScreenState.RUNNING || laneCooldown > 0f) return;
        int next = Math.max(0, Math.min(2, playerLane + direction));
        if (next != playerLane) {
            playerLane = next;
            laneCooldown = 0.07f;
            spawnBurst(null, 0xff64E5FF);
        }
    }

    void jump() {
        if (state != ScreenState.RUNNING || rollTimer > 0f) return;
        float duration;
        if (challengeMode == MODE_LOW_GRAVITY) duration = 0.95f;
        else if (activePower == PowerType.SNEAKERS) duration = 0.72f;
        else duration = 0.52f;
        jumpTimer = Math.max(jumpTimer, duration);
        totalJumps++;
        save.totalJumps++;
        addMissionMetric("jumps", 1);
        spawnBurst(null, 0xff8EF8FF);
    }

    void roll() {
        if (state != ScreenState.RUNNING || jumpTimer > 0f) return;
        rollTimer = 0.44f;
        totalRolls++;
        save.totalRolls++;
        addMissionMetric("rolls", 1);
        spawnBurst(null, 0xffff89c9);
    }

    private void startRun(int mode) {
        state = ScreenState.RUNNING;
        focus = 0;
        objects.clear();
        particles.clear();
        score = 0f;
        distance = 0f;
        runCoins = 0;
        runKeys = 0;
        speed = 0.34f;
        spawnTimer = mode == MODE_ATTACK ? 0.18f : 0.25f;
        jumpTimer = rollTimer = laneCooldown = 0f;
        boardTimer = 0f;
        boardActive = false;
        activePower = PowerType.NONE;
        powerTimer = x2Timer = 0f;
        headstartTimer = 0f;
        attackTimer = mode == MODE_ATTACK ? 24f : 0f;
        scoreBoosterTimer = 0f;
        runRevives = 0;
        totalJumps = totalRolls = totalPowerUps = totalDodges = 0;
        playerLane = 1;
        laneVisual = 1f;
        worldIndex = currentWorldIndex();
        challengeMode = mode;

        save.totalRuns++;
        if (save.headstarts > 0 && mode != MODE_HURDLES) {
            save.headstarts--;
            headstartTimer = 2.5f;
        }
        if (save.scoreBoosters > 0 && mode != MODE_HURDLES) {
            save.scoreBoosters--;
            scoreBoosterTimer = 10f;
            save.save();
        }
        notice(mode == MODE_NORMAL ? "RUN START" : challengeTitle(mode), 1.0f);
        beep(70);
    }

    private void gameOver() {
        state = ScreenState.GAME_OVER;
        int finalScore = Math.max(0, Math.round(score));
        if (finalScore > save.bestScore) save.bestScore = finalScore;

        int challengeScore = Math.max(0, Math.round(score));
        if (challengeMode == MODE_HURDLES && challengeScore > save.mysteryHurdlesBest) save.mysteryHurdlesBest = challengeScore;
        if (challengeMode == MODE_MARATHON && challengeScore > save.marathonBest) save.marathonBest = challengeScore;
        if (challengeMode == MODE_LOW_GRAVITY && challengeScore > save.lowGravityBest) save.lowGravityBest = challengeScore;
        if (challengeMode == MODE_SHOWDOWN && challengeScore > save.weekBest) save.weekBest = challengeScore;
        if (challengeMode == MODE_SEASON) {
            save.seasonChallengeScore += challengeScore;
            save.seasonChallengeRuns = Math.min(5, save.seasonChallengeRuns + 1);
        }

        if (finalScore > save.weekBest) save.weekBest = finalScore;
        refreshLeagueFromScore();
        save.addCoins(runCoins);
        save.keys += runKeys;
        saveMissionProgress();
        save.save();

        state = ScreenState.GAME_OVER;
        focus = 0;
        notice("RUN OVER", 1.2f);
        vibrate(70);
        beep(45);
    }

    void activateBoard() {
        if (state != ScreenState.RUNNING) return;
        if (boardActive) return;
        if (save.boards <= 0) {
            notice("NO BOARDS", 1.0f);
            return;
        }
        save.boards--;
        save.save();
        boardActive = true;
        boardTimer = 8f + save.boardPowerLevel * 0.5f;
        notice("BOARD READY", 0.85f);
        vibrate(20);
    }

    private void buildMissions() {
        missions.clear();
        int level = save.missionsCompleted / 3;
        int cycle = level % 5;

        if (cycle == 0) {
            missions.add(new Mission("Collect Coins", "coins", 40 + level * 4));
            missions.add(new Mission("Run Distance", "distance", 900 + level * 70));
            missions.add(new Mission("Make Jumps", "jumps", 8 + level));
        } else if (cycle == 1) {
            missions.add(new Mission("Roll Obstacles", "rolls", 9 + level));
            missions.add(new Mission("Use Powers", "power", 4 + level));
            missions.add(new Mission("Dodge Cleanly", "dodges", 12 + level * 2));
        } else if (cycle == 2) {
            missions.add(new Mission("Run Far", "distance", 1400 + level * 90));
            missions.add(new Mission("Make Jumps", "jumps", 12 + level));
            missions.add(new Mission("Activate Boards", "boards", 2));
        } else if (cycle == 3) {
            missions.add(new Mission("Collect Coins", "coins", 70 + level * 5));
            missions.add(new Mission("Power Chain", "power", 8 + level));
            missions.add(new Mission("Roll Cleanly", "rolls", 14 + level));
        } else {
            missions.add(new Mission("Endless Distance", "distance", 1800 + level * 110));
            missions.add(new Mission("Perfect Dodges", "dodges", 25 + level * 2));
            missions.add(new Mission("Jump Master", "jumps", 18 + level));
        }
    }

    private void restoreMissionProgress() {
        int[] savedProgress = save.loadMissionProgress();
        for (int i = 0; i < missions.size() && i < savedProgress.length; i++) {
            missions.get(i).progress = Math.min(missions.get(i).target, savedProgress[i]);
        }
    }

    private void saveMissionProgress() {
        save.saveMissionProgress(missions);
    }

    private void addMissionMetric(String metric, int amount) {
        if (amount <= 0) return;

        boolean changed = false;
        for (Mission m : missions) {
            if (m.metric.equals(metric) && !m.done()) {
                m.progress = Math.min(m.target, m.progress + amount);
                changed = true;
                if (m.done() && !m.rewarded) {
                    m.rewarded = true;
                    save.coins += 25;
                    save.keys += 1;
                    notice("MISSION REWARD", 1.0f);
                }
            }
        }

        if (!changed) return;

        boolean all = true;
        for (Mission m : missions) all &= m.done();

        if (all) {
            save.missionsCompleted += 3;
            save.multiplier = Math.min(30, save.multiplier + 1);
            save.coins += 100;
            save.superBoxes += 1;
            saveMissionProgress();
            save.save();
            buildMissions();
            saveMissionProgress();
            notice("MULTIPLIER " + save.multiplier + "x!", 1.35f);
            beep(90);
        }
    }

    void openMysteryBox(int type) {
        if (type == 0) {
            int day = (int)dayIndex();
            if (save.lastMysteryClaimDay == day) {
                notice("DAILY BOX USED", 1.0f);
                return;
            }
            save.lastMysteryClaimDay = day;
            save.mysteryBoxesOpened++;
        } else if (type == 1) {
            if (!save.spendCoins(350)) {
                notice("350 COINS", 1.0f);
                return;
            }
            save.mysteryBoxesOpened++;
        } else {
            if (save.superBoxes <= 0) {
                notice("NO SUPER BOX", 1.0f);
                return;
            }
            save.superBoxes--;
            save.mysteryBoxesOpened++;
        }

        int reward = random.nextInt(type == 2 ? 6 : 4);
        if (reward == 0) {
            int coins = type == 2 ? 1400 : 260;
            save.coins += coins;
            notice("+" + coins + " COINS", 1.2f);
        } else if (reward == 1) {
            save.keys += type == 2 ? 4 : 1;
            notice("KEYS +", 1.0f);
        } else if (reward == 2) {
            save.seasonTokens += type == 2 ? 160 : 45;
            notice("SEASON TOKENS +", 1.0f);
        } else if (reward == 3) {
            save.characterTokens += type == 2 ? 60 : 15;
            notice("CHAR TOKEN +", 1.0f);
        } else if (reward == 4) {
            save.coins += 100000;
            notice("JACKPOT +100000", 1.3f);
        } else {
            save.boards += 3;
            notice("BOARDS +3", 1.0f);
        }
        save.save();
        beep(80);
    }

    void claimDailyReward() {
        long today = dayIndex();
        if (save.lastDailyClaimDay == today) {
            notice("ALREADY CLAIMED", 1.0f);
            return;
        }

        if (save.lastDailyClaimDay == today - 1) save.loginStreak++;
        else save.loginStreak = 1;

        save.lastDailyClaimDay = today;
        int day = ((save.loginStreak - 1) % 7) + 1;
        save.coins += 50 + day * 15;
        if (day == 3 || day == 6) save.keys++;
        if (day == 7) save.superBoxes++;
        save.save();
        notice("DAILY REWARD +" + (50 + day * 15), 1.15f);
        beep(92);
    }

    void claimSeasonTier() {
        int tier = save.seasonTier();
        if (tier <= save.seasonClaimedTier) {
            notice("TIER LOCKED", 1.0f);
            return;
        }

        save.seasonClaimedTier = tier;
        if (tier % 5 == 0) save.superBoxes++;
        else save.coins += 120 + tier * 10;
        save.save();
        notice("SEASON TIER " + tier + " CLAIMED", 1.15f);
    }

    void openEventReward() {
        int event = dailyEventIndex();
        int need = event == 0 ? 8 : 6;
        if (save.eventTokens < need) {
            notice("NEED " + need + " EVENT TOKENS", 1.0f);
            return;
        }
        save.eventTokens -= need;
        save.coins += 150;
        save.keys += event == 1 ? 1 : 0;
        save.save();
        notice("EVENT REWARD +", 1.0f);
    }

    void buyShopItem(int which) {
        int price;
        switch (which) {
            case 0:
                price = 180 + save.magnetLevel * 90;
                if (save.magnetLevel >= 5) { notice("MAX LEVEL", .8f); return; }
                if (save.spendCoins(price)) { save.magnetLevel++; notice("MAGNET LV " + save.magnetLevel, 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 1:
                price = 180 + save.sneakersLevel * 90;
                if (save.sneakersLevel >= 5) { notice("MAX LEVEL", .8f); return; }
                if (save.spendCoins(price)) { save.sneakersLevel++; notice("SNEAKERS LV " + save.sneakersLevel, 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 2:
                price = 220 + save.x2Level * 100;
                if (save.x2Level >= 5) { notice("MAX LEVEL", .8f); return; }
                if (save.spendCoins(price)) { save.x2Level++; notice("X2 LV " + save.x2Level, 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 3:
                price = 240 + save.jetpackLevel * 110;
                if (save.jetpackLevel >= 5) { notice("MAX LEVEL", .8f); return; }
                if (save.spendCoins(price)) { save.jetpackLevel++; notice("JETPACK LV " + save.jetpackLevel, 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 4:
                price = 300;
                if (save.spendCoins(price)) { save.boards += 2; notice("BOARD +2", 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 5:
                if (save.spendCoins(500)) { save.pogoLevel = Math.min(5, save.pogoLevel + 1); notice("POGO LV " + save.pogoLevel, 1.0f); }
                else notice("500 COINS", 1.0f);
                break;
            case 6:
                if (save.spendCoins(450)) { save.boardPowerLevel = Math.min(5, save.boardPowerLevel + 1); notice("BOARD POWER LV " + save.boardPowerLevel, 1.0f); }
                else notice("450 COINS", 1.0f);
                break;
            default:
                break;
        }
    }

    void selectCharacter(int index) {
        if (index == 0) {
            save.selectedCharacter = 0;
            save.save();
            notice("NOVA SELECTED", 0.9f);
        } else if (index == 1) {
            if (!save.character2Unlocked) {
                if (save.spendCoins(500)) save.character2Unlocked = true;
                else { notice("500 COINS", 1.0f); return; }
            }
            save.selectedCharacter = 1;
            save.save();
            notice("KAI SELECTED", 0.9f);
        } else {
            if (!save.character3Unlocked) {
                if (save.characterTokens < 60) { notice("60 CHAR TOKENS", 1.0f); return; }
                save.characterTokens -= 60;
                save.character3Unlocked = true;
            }
            save.selectedCharacter = 2;
            save.save();
            notice("ZIA SELECTED", 0.9f);
        }
    }

    void selectOutfit(int index) {
        if (index == 0) {
            save.selectedOutfit = 0;
            save.save();
            notice("BASE OUTFIT", 0.8f);
        } else if (index == 1) {
            if (!save.outfit1Unlocked) {
                if (!save.spendCoins(650)) { notice("650 COINS", 1.0f); return; }
                save.outfit1Unlocked = true;
            }
            save.selectedOutfit = 1;
            save.save();
            notice("NOVA NIGHT", 0.9f);
        } else {
            if (!save.outfit2Unlocked) {
                if (!save.spendCoins(850)) { notice("850 COINS", 1.0f); return; }
                save.outfit2Unlocked = true;
            }
            save.selectedOutfit = 2;
            save.save();
            notice("KAI STORM", 0.9f);
        }
    }

    void selectBoard(int index) {
        if (index == 0) {
            save.selectedBoard = 0;
            save.save();
            notice("PULSE BOARD", 0.9f);
        } else if (index == 1) {
            if (!save.board2Unlocked) {
                if (!save.spendCoins(700)) { notice("700 COINS", 1.0f); return; }
                save.board2Unlocked = true;
            }
            save.selectedBoard = 1;
            save.save();
            notice("VOLT BOARD", 0.9f);
        } else {
            if (!save.board3Unlocked) {
                if (!save.spendCoins(1200)) { notice("1200 COINS", 1.0f); return; }
                save.board3Unlocked = true;
            }
            save.selectedBoard = 2;
            save.save();
            notice("SOLAR BOARD", 0.9f);
        }
    }

    void selectChallenge(int index) {
        if (index == 0) {
            startRun(MODE_HURDLES);
        } else if (index == 1) {
            startRun(MODE_SEASON);
        } else if (index == 2) {
            startRun(MODE_MARATHON);
        } else if (index == 3) {
            startRun(MODE_ATTACK);
        } else if (index == 4) {
            startRun(MODE_NO_FLOOR);
        } else if (index == 5) {
            startRun(MODE_LAVA);
        } else if (index == 6) {
            startRun(MODE_SHOWDOWN);
        } else {
            startRun(MODE_LOW_GRAVITY);
        }
    }

    String[] menuLabels() {
        switch (state) {
            case HOME:
                return new String[]{
                        "START RUN", "DAILY GIFT", "SHOP", "MISSIONS", "SEASON HUNT",
                        "EVENTS", "WORD HUNT", "CHALLENGES", "MYSTERY BOX",
                        "BOOSTS", "COLLECTIONS", "CHARACTERS", "OUTFITS", "BOARDS",
                        "WORLD TOUR", "PROFILE / TOP RUN", "FRIENDS", "NEWS", "SETTINGS"
                };
            case SHOP:
                return new String[]{"Magnet Upgrade • LV " + save.magnetLevel, "Sneakers Upgrade • LV " + save.sneakersLevel, "Score x2 Upgrade • LV " + save.x2Level,
                        "Jetpack Upgrade", "Board Pack +2", "Pogo Upgrade", "Board Power"};
            case CHALLENGES:
                return new String[]{"Mystery Hurdles • " + mysteryDifficulty + "/4", "Season Challenge", "Marathon",
                        "Tag Time Attack", "No Floor", "Lava is Floor", "Showdown", "Low Gravity"};
            case CHARACTERS:
                return new String[]{"NOVA  •  FREE", "KAI  •  500 COINS", "ZIA  •  60 TOKENS"};
            case OUTFITS:
                return new String[]{"BASE", "NOVA NIGHT  •  650", "KAI STORM  •  850"};
            case BOARDS:
                return new String[]{"PULSE  •  FREE", "VOLT  •  700", "SOLAR  •  1200"};
            case BOOSTS:
                return new String[]{"Score Booster • " + save.scoreBoosters, "Headstart • " + save.headstarts, "Board Stock • " + save.boards};
            case MYSTERY_BOX:
                return new String[]{"MINI BOX  •  DAILY", "MYSTERY BOX  •  350", "SUPER BOX"};
            case SETTINGS:
                return new String[]{"SOUND  •  " + (save.soundOn ? "ON" : "OFF"),
                        "VIBRATION  •  " + (save.vibrationOn ? "ON" : "OFF"),
                        "SAVE CODE", "CONTROL GUIDE"};
            case FRIENDS:
                return new String[]{"MY FRIEND TAG", "FRIEND 01", "FRIEND 02", "FRIEND 03"};
            case EVENTS:
                return new String[]{dailyEventName(), "EVENT REWARD", "WEEK BONUS", "FEATURED COLLAB", "BACK"};
            case PROFILE:
                return new String[]{"TOP RUN", "STATS", "COLLECTION SCORE", "LEAGUE"};
            case PAUSED:
                return new String[]{"RESUME", "RESTART", "HOME"};
            case GAME_OVER:
                return new String[]{"REVIVE  •  KEY", "RUN AGAIN", "HOME"};
            default:
                return new String[]{"BACK"};
        }
    }

    int menuCount() {
        return menuLabels().length;
    }

    void moveFocus(int direction) {
        int count = menuCount();
        if (count <= 1) return;
        focus = (focus + direction) % count;
        if (focus < 0) focus += count;
    }

    private void primary() {
        switch (state) {
            case HOME:
                activateHome(focus);
                break;
            case RUNNING:
                activateBoard();
                break;
            case PAUSED:
                if (focus == 0) state = ScreenState.RUNNING;
                else if (focus == 1) startRun(MODE_NORMAL);
                else { state = ScreenState.HOME; focus = 0; }
                break;
            case GAME_OVER:
                if (focus == 0) reviveRun();
                else if (focus == 1) startRun(challengeMode);
                else { state = ScreenState.HOME; focus = 0; }
                break;
            case SHOP:
                buyShopItem(focus);
                break;
            case BOOSTS:
                buyBoost(focus);
                break;
            case CHARACTERS:
                selectCharacter(focus);
                break;
            case OUTFITS:
                selectOutfit(focus);
                break;
            case BOARDS:
                selectBoard(focus);
                break;
            case CHALLENGES:
                selectChallenge(focus);
                break;
            case MYSTERY_BOX:
                openMysteryBox(focus);
                break;
            case DAILY_REWARDS:
                claimDailyReward();
                break;
            case SEASON_HUNT:
                claimSeasonTier();
                break;
            case EVENTS:
                if (focus == 1) openEventReward();
                else if (focus == 2) claimWeeklyBonus();
                else if (focus == 3) openFeaturedCollab();
                else goBack();
                break;
            case WORD_HUNT:
                if (wordComplete() && save.wordClaimedDay != dayIndex()) {
                    save.coins += 250;
                    save.superBoxes++;
                    save.wordStreak++;
                    save.wordClaimedDay = dayIndex();
                    save.save();
                    notice("WORD COMPLETE!", 1.2f);
                } else if (save.wordClaimedDay == dayIndex()) {
                    notice("CLAIMED TODAY", 1.0f);
                } else {
                    notice(wordStatus(), 1.0f);
                }
                break;
            case MISSIONS:
                if (focus < 3) skipMission(focus); else { state = ScreenState.HOME; focus = 0; }
                break;
            case ACHIEVEMENTS:
                state = ScreenState.HOME; focus = 0;
                break;
            case COLLECTIONS:
                collectCollectionReward();
                break;
            case WORLD_TOUR:
                worldIndex = (worldIndex + 1) % WORLDS.length;
                notice("WORLD: " + WORLDS[worldIndex], 1.0f);
                break;
            case PROFILE:
                state = ScreenState.HOME; focus = 0;
                break;
            case FRIENDS:
                if (focus == 0) notice("TAG: RR" + friendCode(), 1.0f);
                else notice("FRIEND " + String.format("%02d", focus) + " • ONLINE", 0.9f);
                break;
            case NEWS:
                state = ScreenState.HOME; focus = 0;
                break;
            case SETTINGS:
                if (focus == 0) save.soundOn = !save.soundOn;
                else if (focus == 1) save.vibrationOn = !save.vibrationOn;
                else if (focus == 2) notice("SAVE: RR-" + friendCode() + "-" + save.bestScore, 1.6f);
                else notice("D-PAD / 2 4 6 8 / 5 / 7 / 0", 1.4f);
                save.save();
                break;
            default:
                state = ScreenState.HOME; focus = 0;
                break;
        }
    }

    private void activateHome(int index) {
        switch (index) {
            case 0: startRun(MODE_NORMAL); break;
            case 1: state = ScreenState.DAILY_REWARDS; focus = 0; break;
            case 2: state = ScreenState.SHOP; focus = 0; break;
            case 3: state = ScreenState.MISSIONS; focus = 0; break;
            case 4: state = ScreenState.SEASON_HUNT; focus = 0; break;
            case 5: state = ScreenState.EVENTS; focus = 0; break;
            case 6: state = ScreenState.WORD_HUNT; focus = 0; break;
            case 7: state = ScreenState.CHALLENGES; focus = 0; break;
            case 8: state = ScreenState.MYSTERY_BOX; focus = 0; break;
            case 9: state = ScreenState.BOOSTS; focus = 0; break;
            case 10: state = ScreenState.COLLECTIONS; focus = 0; break;
            case 11: state = ScreenState.CHARACTERS; focus = save.selectedCharacter; break;
            case 12: state = ScreenState.OUTFITS; focus = save.selectedOutfit; break;
            case 13: state = ScreenState.BOARDS; focus = save.selectedBoard; break;
            case 14: state = ScreenState.WORLD_TOUR; focus = 0; break;
            case 15: state = ScreenState.PROFILE; focus = 0; break;
            case 16: state = ScreenState.FRIENDS; focus = 0; break;
            case 17: state = ScreenState.NEWS; focus = 0; break;
            case 18: state = ScreenState.SETTINGS; focus = 0; break;
            default: break;
        }
    }

    private void collectCollectionReward() {
        int points = collectionPoints();
        int tier = points / 3;
        if (tier <= save.collectionRewardTier) {
            notice("COLLECTION TIER " + Math.min(6, tier), 1.0f);
            return;
        }
        save.collectionRewardTier = tier;
        save.coins += tier * 100;
        save.keys += tier;
        save.save();
        notice("COLLECTION REWARD!", 1.0f);
    }

    private int collectionPoints() {
        if (save.multiplier < 7) return 0;
        int points = 0;
        points += 3; // base character
        if (save.character2Unlocked) points += 3;
        if (save.character3Unlocked) points += 3;
        if (save.outfit1Unlocked) points += 2;
        if (save.outfit2Unlocked) points += 2;
        points += 3; // base board
        if (save.board2Unlocked) points += 3;
        if (save.board3Unlocked) points += 3;
        return points;
    }

    private void skipMission(int index) {
        if (index < 0 || index >= missions.size() || missions.get(index).done()) { notice("MISSION DONE", .8f); return; }
        if (!save.spendCoins(200)) { notice("200 COINS", 1.0f); return; }
        missions.get(index).progress = missions.get(index).target;
        missions.get(index).rewarded = true;
        saveMissionProgress();
        save.save();
        notice("MISSION SKIPPED", 1.0f);
    }

    private void buyBoost(int which) {
        if (which == 0) {
            if (save.spendCoins(260)) { save.scoreBoosters++; save.save(); notice("SCORE BOOST +1", 1.0f); }
            else notice("260 COINS", 1.0f);
        } else if (which == 1) {
            if (save.spendCoins(220)) { save.headstarts++; save.save(); notice("HEADSTART +1", 1.0f); }
            else notice("220 COINS", 1.0f);
        } else {
            if (save.spendCoins(300)) { save.boards += 2; save.save(); notice("BOARD STOCK +2", 1.0f); }
            else notice("300 COINS", 1.0f);
        }
    }

    private void reviveRun() {
        int cost = 1 << Math.min(4, runRevives);
        if (!save.spendKeys(cost)) { notice("NEED " + cost + " KEYS", 1.0f); return; }
        runRevives++;
        for (GameObject o : objects) if (o.kind == GameObject.Kind.OBSTACLE && o.z < 0.30f) o.active = false;
        boardActive = true;
        boardTimer = 5.5f;
        state = ScreenState.RUNNING;
        notice("REVIVED • KEYS -" + cost, 1.1f);
        vibrate(45);
    }

    private void claimWeeklyBonus() {
        long week = dayIndex() / 7L;
        if (save.lastWeeklyClaim == week) {
            notice("WEEK BONUS CLAIMED", 1.0f);
            return;
        }
        if (save.weekBest == 0) {
            notice("RUN FIRST FOR BONUS", 1.0f);
            return;
        }
        int reward = 100 + save.league * 35;
        save.lastWeeklyClaim = week;
        save.coins += reward;
        save.save();
        notice("WEEK BONUS +" + reward, 1.0f);
    }

    private void openFeaturedCollab() {
        String[] collabs = {"GUEST UNIVERSE", "TIME TRAVEL", "ARENA STARS", "GALACTIC CREWMATE"};
        notice("FEATURED: " + collabs[dailyEventIndex() % collabs.length], 1.1f);
    }

    private void ensureSeasonData() {
        long seasonDays = Math.max(0L, dayIndex() - save.dayStart(save.seasonStart));
        if (seasonDays >= 21L) {
            save.seasonStart = System.currentTimeMillis();
            save.seasonTokens = 0;
            save.seasonClaimedTier = 0;
            save.save();
        }
    }

    private void ensureDailyData() {
        long today = dayIndex();
        if (save.wordDay != today) {
            save.wordDay = today;
            save.wordProgress = "";
            save.eventTokens = 0;
            save.save();
        }
    }

    private void collectWordLetter(String letter) {
        String word = todayWord();
        int index = save.wordProgress.length();
        if (index < word.length() && String.valueOf(word.charAt(index)).equals(letter)) {
            save.wordProgress += letter;
            save.save();
            notice("LETTER " + letter, 0.75f);
            beep(88);
        }
    }

    String todayWord() {
        long index = Math.abs(dayIndex()) % WORDS.length;
        return WORDS[(int)index];
    }

    boolean wordComplete() {
        return save.wordProgress.equals(todayWord()) && save.wordClaimedDay != dayIndex();
    }

    String wordStatus() {
        String word = todayWord();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (i < save.wordProgress.length()) b.append(save.wordProgress.charAt(i));
            else b.append('_');
            if (i + 1 < word.length()) b.append(' ');
        }
        return b.toString();
    }

    int dailyEventIndex() {
        int day = (int)(dayIndex() % 7);
        return day;
    }

    int eventBonusTokens() {
        return dailyEventIndex() == 0 ? 2 : 1;
    }

    String dailyEventName() {
        switch (dailyEventIndex()) {
            case 0: return "MONDAY MYSTERY";
            case 1: return "TERRIFIC TUESDAY";
            case 2:
            case 3: return "CHARACTER BONUS";
            case 4: return "FABULOUS FRIDAY";
            case 5:
            case 6: return "WORDY WEEKEND";
            default: return "DAILY EVENT";
        }
    }

    String challengeTitle(int mode) {
        switch (mode) {
            case MODE_HURDLES: return "MYSTERY HURDLES";
            case MODE_SEASON: return "SEASON CHALLENGE";
            case MODE_MARATHON: return "CHALLENGE MARATHON";
            case MODE_ATTACK: return "TAG TIME ATTACK";
            case MODE_NO_FLOOR: return "NO FLOOR";
            case MODE_LAVA: return "LAVA IS FLOOR";
            case MODE_SHOWDOWN: return "SHOWDOWN";
            case MODE_LOW_GRAVITY: return "LOW GRAVITY";
            default: return "NORMAL RUN";
        }
    }

    String challengeInfo(int mode) {
        switch (mode) {
            case MODE_HURDLES: return "4 DIFFICULTIES • NO COINS";
            case MODE_SEASON: return "UP TO 5 RUNS • CUMULATIVE";
            case MODE_MARATHON: return "DISTANCE + TIME";
            case MODE_ATTACK: return "FAST TIMER • HOURGLASS";
            case MODE_NO_FLOOR: return "MISSING GROUND";
            case MODE_LAVA: return "GROUND = DANGER";
            case MODE_SHOWDOWN: return "RIVAL SCORE";
            case MODE_LOW_GRAVITY: return "LONG AIR TIME";
            default: return "";
        }
    }

    String currentWorld() {
        return WORLDS[worldIndex % WORLDS.length];
    }

    int currentWorldIndex() {
        long threeWeek = dayIndex() / 21L;
        return (int)(threeWeek % WORLDS.length);
    }

    String[] worldList() {
        return WORLDS.clone();
    }

    int collectionPointsForUi() {
        return collectionPoints();
    }

    int seasonProgressPercent() {
        return Math.min(100, save.seasonTokens * 100 / 40);
    }

    int friendCode() {
        int n = Math.abs((save.bestScore * 31 + save.coins * 17 + save.missionsCompleted * 11) % 1000000);
        return 100000 + n % 900000;
    }

    int achievementCount() {
        int done = 0;
        if (save.bestScore >= 1000) done++;
        if (save.bestScore >= 10000) done++;
        if (save.totalCoins >= 1000) done++;
        if (save.totalDistance >= 10000) done++;
        if (save.totalPowerUps >= 25) done++;
        if (save.multiplier >= 10) done++;
        if (save.multiplier >= 30) done++;
        if (save.totalRuns >= 50) done++;
        return done;
    }

    String achievementText(int index) {
        switch (index) {
            case 0: return "BRONZE • BEST SCORE 1K";
            case 1: return "SILVER • BEST SCORE 10K";
            case 2: return "GOLD • 1K COINS";
            case 3: return "DIAMOND • 10K METERS";
            case 4: return "POWER RIDER • 25 POWERS";
            case 5: return "MULTIPLIER • 10X";
            case 6: return "MASTER • 30X";
            default: return "ENDLESS • 50 RUNS";
        }
    }

    int achievementProgress(int index) {
        switch (index) {
            case 0: return save.bestScore >= 1000 ? 1 : save.bestScore;
            case 1: return Math.min(10000, save.bestScore);
            case 2: return (int)Math.min(1000L, save.totalCoins);
            case 3: return (int)Math.min(10000L, save.totalDistance);
            case 4: return (int)Math.min(25L, save.totalPowerUps);
            case 5: return Math.min(10, save.multiplier);
            case 6: return Math.min(30, save.multiplier);
            default: return (int)Math.min(50L, save.totalRuns);
        }
    }

    private void ensureWeeklyData() {
        long week = dayIndex() / 7L;
        if (save.weekStartDay != week) {
            save.weekStartDay = week;
            save.weekBest = 0;
            save.lastWeeklyClaim = -1L;
            save.save();
        }
    }

    private void refreshWeekLeague() {
        int rank = Math.max(1, save.weekBest / 1200);
        save.league = Math.max(1, Math.min(8, 1 + rank));
    }

    private void refreshLeagueFromScore() {
        save.league = Math.max(1, Math.min(8, 1 + save.weekBest / 2500));
    }

    String leagueName() {
        String[] names = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "ELITE", "LEGEND"};
        return names[Math.max(0, Math.min(names.length - 1, save.league - 1))];
    }

    String statsText() {
        return "RUNS " + save.totalRuns
                + "  •  METERS " + save.totalDistance
                + "  •  COINS " + save.totalCoins
                + "  •  POWERS " + save.totalPowerUps;
    }

    String newsTitle(int index) {
        switch (index) {
            case 0: return "WORLD TOUR • " + currentWorld();
            case 1: return "SEASON • TIER " + save.seasonTier() + "/30";
            case 2: return dailyEventName() + " IS LIVE";
            case 3: return "NEW COLLECTION MILESTONE";
            case 4: return "KEYPAD MODE • FULL SUPPORT";
            case 5: return "WEEKLY TOP RUN • " + save.weekBest;
            default: return "RAIL RUSH • VERSION 1.0";
        }
    }

    void goBack() {
        if (state != ScreenState.RUNNING) {
            saveMissionProgress();
            save.save();
        }
        switch (state) {
            case RUNNING:
                state = ScreenState.PAUSED;
                focus = 0;
                break;
            case PAUSED:
            case GAME_OVER:
            case SHOP:
            case MISSIONS:
            case ACHIEVEMENTS:
            case WORD_HUNT:
            case SEASON_HUNT:
            case EVENTS:
            case MYSTERY_BOX:
            case CHALLENGES:
            case COLLECTIONS:
            case CHARACTERS:
            case OUTFITS:
            case BOARDS:
            case WORLD_TOUR:
            case PROFILE:
            case FRIENDS:
            case NEWS:
            case BOOSTS:
            case DAILY_REWARDS:
            case SETTINGS:
                state = ScreenState.HOME;
                focus = 0;
                break;
            default:
                break;
        }
    }

    boolean handleKey(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_4) {
            if (state == ScreenState.RUNNING) moveLane(-1); else moveFocus(-1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_6) {
            if (state == ScreenState.RUNNING) moveLane(1); else moveFocus(1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_2) {
            if (state == ScreenState.RUNNING) jump(); else moveFocus(-1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_8) {
            if (state == ScreenState.RUNNING) roll(); else moveFocus(1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_5) {
            primary();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_0) {
            goBack();
            return true;
        }

        // Physical keypad shortcuts: direct access without scrolling.
        if (state != ScreenState.RUNNING) {
            if (keyCode == KeyEvent.KEYCODE_1) { state = ScreenState.CHARACTERS; focus = save.selectedCharacter; return true; }
            if (keyCode == KeyEvent.KEYCODE_3) { state = ScreenState.SHOP; focus = 0; return true; }
            if (keyCode == KeyEvent.KEYCODE_7) { state = ScreenState.BOARDS; focus = save.selectedBoard; return true; }
            if (keyCode == KeyEvent.KEYCODE_9) { state = ScreenState.SETTINGS; focus = 0; return true; }
        } else if (keyCode == KeyEvent.KEYCODE_7) {
            activateBoard();
            return true;
        }

        return false;
    }

    void handleSwipe(int direction) {
        if (state == ScreenState.RUNNING) {
            if (direction == -1) moveLane(-1);
            else if (direction == 1) moveLane(1);
            else if (direction == 2) jump();
            else if (direction == 3) roll();
        } else {
            if (direction == 2) moveFocus(-1);
            else if (direction == 3) moveFocus(1);
        }
    }

    void handleTap(float x, float y, int width, int height) {
        if (state == ScreenState.RUNNING) {
            if (y > height * 0.80f) {
                if (x < width * 0.33f) moveLane(-1);
                else if (x > width * 0.66f) moveLane(1);
                else jump();
            } else if (y < height * 0.20f) {
                activateBoard();
            } else {
                roll();
            }
            return;
        }

        String[] labels = menuLabels();
        float top = height * 0.31f;
        float row = Math.max(34f, height * 0.075f);
        int tapped = (int)((y - top) / row);
        if (tapped >= 0 && tapped < labels.length) {
            focus = tapped;
            primary();
        }
    }

    private long dayIndex() {
        return System.currentTimeMillis() / 86400000L;
    }

    private String eventNameForHint() {
        return dailyEventName();
    }

    private void resetHuntStreakIfNeeded() {
        // Daily streak is deliberately stable across app restarts.
    }

    private void spawnBurst(GameObject source, int color) {
        float x = 0.5f;
        if (source != null) x = 0.37f + source.lane * 0.13f;
        float y = 0.74f;
        for (int i = 0; i < 8; i++) {
            float angle = (float)(random.nextDouble() * Math.PI * 2.0);
            float sp = 28f + random.nextFloat() * 45f;
            particles.add(new TrailParticle(x, y,
                    (float)Math.cos(angle) * sp,
                    (float)Math.sin(angle) * sp,
                    0.25f + random.nextFloat() * 0.35f, color));
        }
    }

    private int powerColor(PowerType type) {
        switch (type) {
            case MAGNET: return 0xffE96BFF;
            case SNEAKERS: return 0xff6BFFB2;
            case X2: return 0xffFFD34F;
            case JETPACK: return 0xff72C9FF;
            case POGO: return 0xffff8bd7;
            case MYSTERIZER: return 0xff9d7bff;
            default: return 0xffffffff;
        }
    }

    void notice(String message, float seconds) {
        notice = message;
        noticeTimer = seconds;
    }

    private void vibrate(long ms) {
        if (!save.vibrationOn || vibrator == null) return;
        try { vibrator.vibrate(ms); } catch (Exception ignored) {}
    }

    private void beep(int toneType) {
        if (!save.soundOn || tone == null) return;
        try { tone.startTone(toneType, 45); } catch (Exception ignored) {}
    }

    void draw(android.graphics.Canvas canvas, int width, int height) {
        renderer.draw(canvas, width, height, this);
    }
}
