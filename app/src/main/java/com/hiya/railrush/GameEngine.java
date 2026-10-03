package com.hiya.railrush;

import android.content.Context;
import android.os.Vibrator;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

final class GameEngine {
    final SaveManager save;
    final List<GameObject> objects = new ArrayList<>();
    final List<TrailParticle> particles = new ArrayList<>();
    final List<Mission> missions = new ArrayList<>();
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
    float spawnTimer = 0.35f;
    float jumpTimer;
    float rollTimer;
    float laneCooldown;
    float boardTimer;
    boolean boardActive;

    PowerType activePower = PowerType.NONE;
    float powerTimer;
    float x2Timer;

    float noticeTimer;
    String notice = "";
    int totalJumps;
    int totalRolls;
    int totalPowerUps;

    int worldTheme = 0;
    float worldPulse;

    private final Vibrator vibrator;

    GameEngine(Context context) {
        this.context = context.getApplicationContext();
        save = new SaveManager(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        buildMissions();
    }

    void update(float dt) {
        worldPulse += dt;
        if (noticeTimer > 0f) noticeTimer -= dt;

        if (state == ScreenState.RUNNING) {
            updateRun(dt);
        }

        Iterator<TrailParticle> it = particles.iterator();
        while (it.hasNext()) {
            TrailParticle p = it.next();
            p.life -= dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if (p.life <= 0f) it.remove();
        }
    }

    private void updateRun(float dt) {
        speed = Math.min(0.74f, speed + dt * 0.008f);
        distance += speed * 82f * dt;
        float multiplier = save.multiplier * (x2Timer > 0f ? 2f : 1f);
        score += speed * 105f * multiplier * dt;

        laneCooldown -= dt;
        jumpTimer = Math.max(0f, jumpTimer - dt);
        rollTimer = Math.max(0f, rollTimer - dt);
        boardTimer = Math.max(0f, boardTimer - dt);
        powerTimer = Math.max(0f, powerTimer - dt);
        x2Timer = Math.max(0f, x2Timer - dt);

        if (boardActive && boardTimer <= 0f) {
            boardActive = false;
            notice("Board expired", 1.1f);
        }
        if (powerTimer <= 0f) {
            activePower = PowerType.NONE;
        }

        float laneSpeed = 9f * (activePower == PowerType.SNEAKERS ? 1.3f : 1f);
        laneVisual += (playerLane - laneVisual) * Math.min(1f, laneSpeed * dt);

        spawnTimer -= dt;
        if (spawnTimer <= 0f) {
            spawnChunk();
            spawnTimer = Math.max(0.42f, 0.84f - speed * 0.36f);
        }

        for (GameObject object : objects) {
            if (!object.active) continue;
            object.z -= speed * dt;
            object.spin += dt * 5f;
            if (object.z < 0.22f && object.z > 0.015f) {
                processObject(object);
            }
            if (object.z <= -0.04f) object.active = false;
        }

        cleanupObjects();

        if (distance > 0 && ((int) distance) % 500 == 0) {
            if (runKeys == 0 && distance > 50) {
                runKeys++;
                notice("Key +1", 1.0f);
            }
        }

        addMissionMetric("distance", (int) (speed * 82f * dt));
    }

    private void spawnChunk() {
        int lane = random.nextInt(3);
        int typeRoll = random.nextInt(10);
        ObstacleType obstacle = typeRoll < 4 ? ObstacleType.LOW_BARRIER
                : (typeRoll < 8 ? ObstacleType.TRAIN : ObstacleType.HIGH_BARRIER);

        objects.add(new GameObject(
                GameObject.Kind.OBSTACLE, lane, 1.08f, obstacle, PowerType.NONE));

        int coinLane = random.nextInt(3);
        for (int i = 0; i < 5; i++) {
            float z = 1.18f + i * 0.105f;
            objects.add(new GameObject(
                    GameObject.Kind.COIN, coinLane, z, null, PowerType.NONE));
        }

        if (random.nextFloat() < 0.22f) {
            PowerType power = randomPower();
            objects.add(new GameObject(
                    GameObject.Kind.POWER, (lane + 1 + random.nextInt(2)) % 3, 1.48f, null, power));
        }

        if (random.nextFloat() < 0.18f) {
            int secondLane = (lane + 1 + random.nextInt(2)) % 3;
            objects.add(new GameObject(
                    GameObject.Kind.OBSTACLE, secondLane, 1.52f,
                    random.nextBoolean() ? ObstacleType.TRAIN : ObstacleType.LOW_BARRIER, null));
        }
    }

    private PowerType randomPower() {
        int r = random.nextInt(100);
        if (r < 26) return PowerType.MAGNET;
        if (r < 52) return PowerType.SNEAKERS;
        if (r < 78) return PowerType.X2;
        return PowerType.JETPACK;
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
                addMissionMetric("coins", amount);
                spawnBurst(object, 0xffFFE36E);
                return;
            }
            if (object.z < 0.03f) object.active = false;
            return;
        }

        if (object.kind == GameObject.Kind.POWER) {
            if (Math.abs(object.lane - playerLane) <= 1 || object.z < 0.07f && activePower == PowerType.MAGNET) {
                object.active = false;
                activatePower(object.powerType);
                totalPowerUps++;
                addMissionMetric("power", 1);
                spawnBurst(object, powerColor(object.powerType));
                return;
            }
            if (object.z < 0.01f) object.active = false;
            return;
        }

        if (object.z <= 0.12f && object.lane == playerLane) {
            if (isSafeAgainst(object.obstacleType)) {
                spawnBurst(object, 0xff63F2FF);
                object.active = false;
            } else if (boardActive) {
                boardActive = false;
                boardTimer = 0f;
                object.active = false;
                spawnBurst(object, 0xffF6F0FF);
                notice("BOARD SAVE!", 1.4f);
                vibrate(30);
            } else {
                gameOver();
            }
        }
    }

    private boolean isSafeAgainst(ObstacleType type) {
        if (activePower == PowerType.JETPACK && powerTimer > 0f) return true;
        if (jumpTimer > 0f) return type != ObstacleType.LOW_BARRIER || jumpTimer > 0f;
        if (rollTimer > 0f) return type == ObstacleType.LOW_BARRIER;
        return false;
    }

    private void cleanupObjects() {
        Iterator<GameObject> it = objects.iterator();
        while (it.hasNext()) {
            if (!it.next().active) it.remove();
        }
    }

    private void activatePower(PowerType type) {
        activePower = type;
        int level;
        switch (type) {
            case MAGNET:
                level = save.magnetLevel;
                powerTimer = 6.0f + level * 0.8f;
                notice("MAGNET ONLINE", 1.0f);
                break;
            case SNEAKERS:
                level = save.sneakersLevel;
                powerTimer = 6.0f + level * 0.8f;
                notice("SUPER SNEAKERS", 1.0f);
                break;
            case X2:
                level = save.x2Level;
                x2Timer = 6.0f + level * 0.8f;
                powerTimer = x2Timer;
                notice("SCORE x2", 1.0f);
                break;
            case JETPACK:
                level = save.jetpackLevel;
                powerTimer = 4.5f + level * 0.6f;
                jumpTimer = powerTimer;
                notice("JETPACK!", 1.0f);
                break;
            default:
                break;
        }
        vibrate(15);
    }

    void moveLane(int direction) {
        if (state != ScreenState.RUNNING || laneCooldown > 0f) return;
        int next = Math.max(0, Math.min(2, playerLane + direction));
        if (next != playerLane) {
            playerLane = next;
            laneCooldown = 0.085f;
            spawnBurst(null, 0xff64E5FF);
        }
    }

    void jump() {
        if (state != ScreenState.RUNNING || rollTimer > 0f) return;
        float duration = activePower == PowerType.SNEAKERS ? 0.72f : 0.52f;
        jumpTimer = Math.max(jumpTimer, duration);
        totalJumps++;
        addMissionMetric("jumps", 1);
        spawnBurst(null, 0xff8EF8FF);
    }

    void roll() {
        if (state != ScreenState.RUNNING || jumpTimer > 0f) return;
        rollTimer = 0.44f;
        totalRolls++;
        addMissionMetric("rolls", 1);
        spawnBurst(null, 0xffFF89C9);
    }

    private void startRun() {
        state = ScreenState.RUNNING;
        focus = 0;
        objects.clear();
        particles.clear();
        score = 0f;
        distance = 0f;
        runCoins = 0;
        runKeys = 0;
        speed = 0.34f;
        spawnTimer = 0.25f;
        jumpTimer = rollTimer = laneCooldown = 0f;
        boardTimer = 0f;
        boardActive = false;
        activePower = PowerType.NONE;
        powerTimer = 0f;
        x2Timer = 0f;
        totalJumps = totalRolls = totalPowerUps = 0;
        playerLane = 1;
        laneVisual = 1f;
        worldTheme = (worldTheme + 1) % 4;
        notice("RUN START!", 1.0f);
    }

    private void gameOver() {
        state = ScreenState.GAME_OVER;
        int finalScore = Math.max(0, Math.round(score));
        if (finalScore > save.bestScore) save.bestScore = finalScore;
        save.addCoins(runCoins);
        if (runKeys > 0) {
            save.keys += runKeys;
        }
        save.save();
        focus = 0;
        vibrate(70);
        notice("RUN ENDED", 1.4f);
    }

    void activateBoard() {
        if (state != ScreenState.RUNNING) return;
        if (boardActive) return;
        if (save.boards <= 0) {
            notice("NO BOARDS", 1.1f);
            return;
        }
        save.boards--;
        save.save();
        boardActive = true;
        boardTimer = 8f;
        notice("BOARD READY", 1.0f);
        vibrate(20);
    }

    private void buildMissions() {
        missions.clear();
        int level = save.missionsCompleted / 3;
        switch (level % 4) {
            case 0:
                missions.add(new Mission("אסוף מטבעות", "coins", 40 + level * 4));
                missions.add(new Mission("רוץ מרחק", "distance", 900 + level * 75));
                missions.add(new Mission("קפיצות", "jumps", 8 + level));
                break;
            case 1:
                missions.add(new Mission("התחמק", "rolls", 9 + level));
                missions.add(new Mission("צבור כוח", "power", 4 + level));
                missions.add(new Mission("אסוף מטבעות", "coins", 55 + level * 4));
                break;
            case 2:
                missions.add(new Mission("רוץ רחוק", "distance", 1400 + level * 90));
                missions.add(new Mission("קפוץ מעל", "jumps", 12 + level));
                missions.add(new Mission("הפעל לוחות", "boards", 2));
                break;
            default:
                missions.add(new Mission("אסוף מטבעות", "coins", 70 + level * 5));
                missions.add(new Mission("התגלגל", "rolls", 14 + level));
                missions.add(new Mission("צבור מרחק", "distance", 1800 + level * 110));
                break;
        }
    }

    private void addMissionMetric(String metric, int amount) {
        if (amount <= 0) return;
        for (Mission m : missions) {
            if (m.metric.equals(metric) && !m.done()) {
                m.progress = Math.min(m.target, m.progress + amount);
                if (m.done() && !m.rewarded) {
                    m.rewarded = true;
                    save.coins += 25;
                    save.keys += 1;
                    save.save();
                    notice("MISSION +", 1.2f);
                }
            }
        }
        boolean all = true;
        for (Mission m : missions) all &= m.done();
        if (all) {
            save.missionsCompleted += 3;
            save.multiplier = Math.min(30, save.multiplier + 1);
            save.coins += 100;
            save.save();
            buildMissions();
            notice("MULTIPLIER +" + save.multiplier + "x", 1.6f);
        }
    }

    private void spawnBurst(GameObject source, int color) {
        for (int i = 0; i < 8; i++) {
            float x = (source == null ? 0.5f : 0.45f + source.lane * 0.05f);
            float y = 0.74f;
            float angle = (float) (random.nextDouble() * Math.PI * 2);
            float speed = 28f + random.nextFloat() * 45f;
            particles.add(new TrailParticle(
                    x, y, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    0.30f + random.nextFloat() * 0.35f, color));
        }
    }

    private int powerColor(PowerType type) {
        switch (type) {
            case MAGNET: return 0xffE96BFF;
            case SNEAKERS: return 0xff6BFFB2;
            case X2: return 0xffFFD34F;
            case JETPACK: return 0xff72C9FF;
            default: return 0xffffffff;
        }
    }

    void notice(String message, float seconds) {
        notice = message;
        noticeTimer = seconds;
    }

    private void vibrate(long ms) {
        if (save.vibrationOn && vibrator != null) {
            try {
                vibrator.vibrate(ms);
            } catch (Exception ignored) {
            }
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

        if (state != ScreenState.RUNNING) {
            if (keyCode == KeyEvent.KEYCODE_1) {
                state = ScreenState.CHARACTERS; focus = save.selectedCharacter; return true;
            }
            if (keyCode == KeyEvent.KEYCODE_3) {
                state = ScreenState.SHOP; focus = 0; return true;
            }
            if (keyCode == KeyEvent.KEYCODE_7) {
                state = ScreenState.BOARDS; focus = save.selectedBoard; return true;
            }
            if (keyCode == KeyEvent.KEYCODE_9) {
                state = ScreenState.SETTINGS; focus = 0; return true;
            }
            if (keyCode == KeyEvent.KEYCODE_6) {
                state = ScreenState.ACHIEVEMENTS; focus = 0; return true;
            }
            if (keyCode == KeyEvent.KEYCODE_4) {
                state = ScreenState.MISSIONS; focus = 0; return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_7) {
            activateBoard();
            return true;
        }

        return false;
    }

    private int menuCount() {
        switch (state) {
            case HOME: return 7;
            case SHOP: return 4;
            case CHARACTERS: return 2;
            case BOARDS: return 3;
            case SETTINGS: return 2;
            case PAUSED: return 3;
            case GAME_OVER: return 2;
            default: return 1;
        }
    }

    private void moveFocus(int direction) {
        int count = menuCount();
        if (count <= 1) return;
        focus = (focus + direction) % count;
        if (focus < 0) focus += count;
    }

    private void primary() {
        switch (state) {
            case HOME:
                switch (focus) {
                    case 0: startRun(); break;
                    case 1: state = ScreenState.SHOP; focus = 0; break;
                    case 2: state = ScreenState.MISSIONS; focus = 0; break;
                    case 3: state = ScreenState.CHARACTERS; focus = save.selectedCharacter; break;
                    case 4: state = ScreenState.BOARDS; focus = save.selectedBoard; break;
                    case 5: state = ScreenState.ACHIEVEMENTS; focus = 0; break;
                    case 6: state = ScreenState.SETTINGS; focus = 0; break;
                    default: break;
                }
                break;
            case RUNNING:
                activateBoard();
                break;
            case PAUSED:
                if (focus == 0) state = ScreenState.RUNNING;
                else if (focus == 1) startRun();
                else { state = ScreenState.HOME; focus = 0; }
                break;
            case GAME_OVER:
                if (focus == 0) startRun();
                else { state = ScreenState.HOME; focus = 0; }
                break;
            case SHOP:
                buyShopItem(focus);
                break;
            case CHARACTERS:
                selectCharacter(focus);
                break;
            case BOARDS:
                selectBoard(focus);
                break;
            case SETTINGS:
                if (focus == 0) save.soundOn = !save.soundOn;
                else save.vibrationOn = !save.vibrationOn;
                save.save();
                break;
            case MISSIONS:
            case ACHIEVEMENTS:
                state = ScreenState.HOME; focus = 0;
                break;
        }
    }

    private void buyShopItem(int which) {
        int price;
        switch (which) {
            case 0:
                price = 180 + save.magnetLevel * 90;
                if (save.magnetLevel >= 5) { notice("MAX", 0.8f); return; }
                if (save.spendCoins(price)) { save.magnetLevel++; notice("MAGNET LV " + save.magnetLevel, 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 1:
                price = 180 + save.sneakersLevel * 90;
                if (save.sneakersLevel >= 5) { notice("MAX", 0.8f); return; }
                if (save.spendCoins(price)) { save.sneakersLevel++; notice("SNEAKERS LV " + save.sneakersLevel, 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 2:
                price = 220 + save.x2Level * 100;
                if (save.x2Level >= 5) { notice("MAX", 0.8f); return; }
                if (save.spendCoins(price)) { save.x2Level++; notice("x2 LV " + save.x2Level, 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            case 3:
                price = 300;
                if (save.spendCoins(price)) { save.boards += 2; notice("BOARD +2", 1.0f); }
                else notice("NOT ENOUGH COINS", 1.0f);
                break;
            default:
                break;
        }
    }

    private void selectCharacter(int index) {
        if (index == 0) {
            save.selectedCharacter = 0;
            save.save();
            notice("NOVA SELECTED", 1.0f);
        } else if (index == 1) {
            if (!save.character2Unlocked) {
                if (save.spendCoins(500)) {
                    save.character2Unlocked = true;
                    save.selectedCharacter = 1;
                    save.save();
                    notice("KAI UNLOCKED", 1.0f);
                } else notice("500 COINS", 1.0f);
            } else {
                save.selectedCharacter = 1;
                save.save();
                notice("KAI SELECTED", 1.0f);
            }
        }
    }

    private void selectBoard(int index) {
        if (index == 0) {
            save.selectedBoard = 0;
            save.save();
            notice("PULSE BOARD", 1.0f);
        } else if (index == 1) {
            if (!save.board2Unlocked) {
                if (save.spendCoins(700)) {
                    save.board2Unlocked = true;
                    save.selectedBoard = 1;
                    save.save();
                    notice("VOLT UNLOCKED", 1.0f);
                } else notice("700 COINS", 1.0f);
            } else {
                save.selectedBoard = 1;
                save.save();
                notice("VOLT BOARD", 1.0f);
            }
        } else {
            if (!save.board3Unlocked) {
                if (save.spendCoins(1200)) {
                    save.board3Unlocked = true;
                    save.selectedBoard = 2;
                    save.save();
                    notice("SOLAR UNLOCKED", 1.0f);
                } else notice("1200 COINS", 1.0f);
            } else {
                save.selectedBoard = 2;
                save.save();
                notice("SOLAR BOARD", 1.0f);
            }
        }
    }

    void goBack() {
        switch (state) {
            case RUNNING:
                state = ScreenState.PAUSED;
                focus = 0;
                break;
            case PAUSED:
            case GAME_OVER:
            case SHOP:
            case MISSIONS:
            case CHARACTERS:
            case BOARDS:
            case ACHIEVEMENTS:
            case SETTINGS:
                state = ScreenState.HOME;
                focus = 0;
                break;
            default:
                break;
        }
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
            if (y > height * 0.78f) {
                if (x < width * 0.33f) moveLane(-1);
                else if (x > width * 0.66f) moveLane(1);
                else jump();
            } else if (y < height * 0.28f) {
                activateBoard();
            } else {
                roll();
            }
            return;
        }

        if (state == ScreenState.HOME) {
            int count = menuCount();
            float top = height * 0.36f;
            float itemH = height * 0.065f;
            int tapped = (int) ((y - top) / itemH);
            if (tapped >= 0 && tapped < count) {
                focus = tapped;
                primary();
            }
        }
    }

    void draw(android.graphics.Canvas canvas, int width, int height) {
        renderer.draw(canvas, width, height, this);
    }
}
