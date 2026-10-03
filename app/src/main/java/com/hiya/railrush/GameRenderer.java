package com.hiya.railrush;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

final class GameRenderer {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Path path = new Path();

    private int w, h;
    private float sx, sy;

    GameRenderer() {
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
    }

    void draw(Canvas c, int width, int height, GameEngine e) {
        w = width;
        h = height;
        sx = width / 360f;
        sy = height / 640f;

        drawBackdrop(c, e);

        switch (e.state) {
            case RUNNING:
                drawRun(c, e);
                break;
            case PAUSED:
                drawRun(c, e);
                drawOverlay(c, e, "PAUSED", e.menuLabels());
                break;
            case GAME_OVER:
                drawRun(c, e);
                drawGameOver(c, e);
                break;
            case HOME:
                drawHome(c, e);
                break;
            case MISSIONS:
                drawMissions(c, e);
                break;
            case ACHIEVEMENTS:
                drawAchievements(c, e);
                break;
            case WORD_HUNT:
                drawWordHunt(c, e);
                break;
            case SEASON_HUNT:
                drawSeason(c, e);
                break;
            case EVENTS:
                drawEvents(c, e);
                break;
            case MYSTERY_BOX:
                drawMystery(c, e);
                break;
            case CHALLENGES:
                drawChallenges(c, e);
                break;
            case COLLECTIONS:
                drawCollections(c, e);
                break;
            case CHARACTERS:
                drawCharacters(c, e);
                break;
            case OUTFITS:
                drawOutfits(c, e);
                break;
            case BOARDS:
                drawBoards(c, e);
                break;
            case WORLD_TOUR:
                drawWorldTour(c, e);
                break;
            case PROFILE:
                drawProfile(c, e);
                break;
            case FRIENDS:
                drawFriends(c, e);
                break;
            case NEWS:
                drawNews(c, e);
                break;
            case SHOP:
            case SETTINGS:
            case DAILY_REWARDS:
            case BOOSTS:
                drawGenericList(c, e);
                break;
            default:
                drawHome(c, e);
                break;
        }

        if (e.noticeTimer > 0f) drawNotice(c, e.notice);
    }

    private void drawBackdrop(Canvas c, GameEngine e) {
        int top;
        int bottom;
        int theme = e.worldIndex % 5;

        if (e.state == ScreenState.RUNNING || e.state == ScreenState.PAUSED || e.state == ScreenState.GAME_OVER) {
            if (theme == 0) { top = 0xff061228; bottom = 0xff1A3152; }
            else if (theme == 1) { top = 0xff210B2E; bottom = 0xff55234E; }
            else if (theme == 2) { top = 0xff062628; bottom = 0xff17635B; }
            else if (theme == 3) { top = 0xff1B1230; bottom = 0xff4A315C; }
            else { top = 0xff0E1C38; bottom = 0xff263B66; }
        } else {
            top = 0xff050712;
            bottom = 0xff161D2F;
        }

        p.setShader(new LinearGradient(0, 0, 0, h, top, bottom, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        p.setColor(0x18FFFFFF);
        c.drawCircle(w * 0.79f, h * 0.14f, Math.min(w, h) * 0.085f, p);
        p.setColor(0x0E7BD9FF);
        c.drawCircle(w * 0.79f, h * 0.14f, Math.min(w, h) * 0.16f, p);

        p.setColor(0x33191F36);
        float skylineY = h * 0.39f;
        for (int i = 0; i < 14; i++) {
            float bw = w * (0.04f + (i % 3) * 0.013f);
            float bh = h * (0.08f + (i % 5) * 0.03f);
            float x = w * (i / 14f);
            c.drawRect(x, skylineY - bh, x + bw, skylineY, p);
        }

        p.setColor(0x445EDFFF);
        for (int i = 0; i < 19; i++) {
            float x = (i * 47f + e.worldPulse * 8f) % Math.max(1, w);
            float y = h * (0.06f + ((i * 23) % 31) / 100f);
            c.drawRect(x, y, x + 1.3f * sx, y + 1.3f * sy, p);
        }
    }

    private void drawHome(Canvas c, GameEngine e) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(35 * sx);
        p.setColor(0xffF7FAFF);
        c.drawText("RAIL", w * 0.5f, h * 0.105f, p);
        p.setColor(0xff5EE8FF);
        c.drawText("RUSH", w * 0.5f, h * 0.16f, p);

        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(9 * sx);
        p.setColor(0xA6DDEBFF);
        c.drawText("NEON KEYPAD RUNNER", w * 0.5f, h * 0.195f, p);
        c.drawText(e.currentWorld() + "  •  " + e.dailyEventName(), w * 0.5f, h * 0.22f, p);

        drawStatValue(c, w * 0.17f, h * 0.27f, "COINS", String.valueOf(e.save.coins), 0xffFFE36E);
        drawStatValue(c, w * 0.50f, h * 0.27f, "BEST", String.valueOf(e.save.bestScore), 0xff79D8FF);
        drawStatValue(c, w * 0.83f, h * 0.27f, "MULTI", e.save.multiplier + "x", 0xffff7cc8);

        drawMenu(c, e, e.menuLabels(), h * 0.315f, 0.072f);
        drawHint(c, "↑↓ SELECT  5 OK  0 BACK  2/4/6/8 SHORTCUTS");
    }

    private void drawMenu(Canvas c, GameEngine e, String[] items, float top, float rowFactor) {
        int visible = Math.max(5, Math.min(7, (int)((h * 0.61f) / (h * rowFactor))));
        int start = 0;
        if (e.focus >= visible) start = e.focus - visible + 1;

        float row = h * rowFactor;
        for (int i = start; i < items.length && i < start + visible; i++) {
            float y = top + (i - start) * row;
            drawMenuRow(c, items[i], i == e.focus, i + 1, y, row - 5 * sy);
        }

        if (items.length > visible) {
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(7 * sx);
            p.setColor(0x79D8E7F3);
            c.drawText((start + 1) + "-" + Math.min(items.length, start + visible)
                    + " / " + items.length, w * 0.91f, top - 7 * sy, p);
        }
    }

    private void drawMenuRow(Canvas c, String text, boolean selected, int number, float top, float height) {
        float left = w * 0.065f;
        float right = w * 0.935f;

        p.setColor(selected ? 0xD62A4668 : 0x80202B40);
        r.set(left, top, right, top + height);
        c.drawRoundRect(r, 12 * sx, 12 * sy, p);

        if (selected) {
            stroke.setStrokeWidth(1.3f * sx);
            stroke.setColor(0xCF63E6FF);
            c.drawRoundRect(r, 12 * sx, 12 * sy, stroke);
        }

        p.setTextAlign(Paint.Align.LEFT);
        p.setTypeface(Typeface.create("sans", selected ? Typeface.BOLD : Typeface.NORMAL));
        p.setTextSize(12.2f * sx);
        p.setColor(selected ? 0xffF6FBFF : 0xC7DFE9F5);
        c.drawText(text, left + 13 * sx, top + height * 0.64f, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(8 * sx);
        p.setColor(selected ? 0xff65E6FF : 0x708FB1C5);
        c.drawText(String.valueOf(number), right - 17 * sx, top + height * 0.63f, p);
    }

    private void drawStatValue(Canvas c, float x, float y, String label, String value, int color) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(7.5f * sx);
        p.setColor(0x79C5D7E7);
        c.drawText(label, x, y, p);
        p.setTextSize(16 * sx);
        p.setColor(color);
        c.drawText(value, x, y + 19 * sy, p);
    }

    private void drawHeader(Canvas c, String title, GameEngine e) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(25 * sx);
        p.setColor(0xffF4F9FF);
        c.drawText(title, w * 0.5f, h * 0.105f, p);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8 * sx);
        p.setColor(0xA2D3DFED);
        c.drawText("COINS " + e.save.coins + "   •   KEYS " + e.save.keys
                + "   •   MULTI " + e.save.multiplier + "x", w * 0.5f, h * 0.15f, p);
    }

    private void drawGenericList(Canvas c, GameEngine e) {
        String title;
        if (e.state == ScreenState.SHOP) title = "SHOP";
        else if (e.state == ScreenState.SETTINGS) title = "SETTINGS";
        else if (e.state == ScreenState.DAILY_REWARDS) title = "DAILY REWARD";
        else title = "BOOSTS";

        drawHeader(c, title, e);

        if (e.state == ScreenState.DAILY_REWARDS) {
            drawDailyCard(c, e);
            return;
        }

        drawMenu(c, e, e.menuLabels(), h * 0.205f, 0.085f);
        drawHint(c, "↑↓ SELECT  5 ACTIVATE  0 BACK");
    }

    private void drawDailyCard(Canvas c, GameEngine e) {
        boolean claimed = e.save.lastDailyClaimDay == currentDay();
        p.setColor(0xA3233550);
        r.set(w * 0.09f, h * 0.30f, w * 0.91f, h * 0.70f);
        c.drawRoundRect(r, 22 * sx, 22 * sy, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(20 * sx);
        p.setColor(0xffF6FBFF);
        c.drawText("LOGIN STREAK " + e.save.loginStreak, w * 0.5f, h * 0.39f, p);

        p.setTextSize(13 * sx);
        p.setColor(0xffFFE36E);
        c.drawText("DAY " + (((e.save.loginStreak) % 7) + 1), w * 0.5f, h * 0.46f, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(11 * sx);
        p.setColor(0xC8DDE9F3);
        c.drawText("Coins + keys + super box rotation", w * 0.5f, h * 0.52f, p);

        p.setTextSize(10 * sx);
        p.setColor(claimed ? 0xff9CB0C0 : 0xff6BFFB2);
        c.drawText(claimed ? "CLAIMED TODAY" : "PRESS 5 TO CLAIM", w * 0.5f, h * 0.61f, p);
        drawHint(c, "5 CLAIM  •  0 BACK");
    }

    private void drawMissions(Canvas c, GameEngine e) {
        drawHeader(c, "MISSIONS", e);
        for (int i = 0; i < e.missions.size(); i++) {
            Mission m = e.missions.get(i);
            float y = h * 0.21f + i * h * 0.19f;
            p.setColor(i == e.focus ? 0xB02D4969 : (m.done() ? 0xA72C5C53 : 0x8A202E45));
            r.set(w * 0.07f, y, w * 0.93f, y + h * 0.15f);
            c.drawRoundRect(r, 16 * sx, 16 * sy, p);

            p.setTextAlign(Paint.Align.LEFT);
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            p.setTextSize(12 * sx);
            p.setColor(0xffF3F8FF);
            c.drawText(m.title, w * 0.12f, y + 27 * sy, p);

            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(9 * sx);
            p.setColor(0xAFC7D8E8);
            c.drawText(m.progress + " / " + m.target, w * 0.12f, y + 46 * sy, p);

            p.setColor(0x553C4C64);
            r.set(w * 0.12f, y + 60 * sy, w * 0.88f, y + 67 * sy);
            c.drawRoundRect(r, 6, 6, p);
            p.setColor(m.done() ? 0xff6BFFB2 : 0xff66E7FF);
            r.set(w * 0.12f, y + 60 * sy,
                    w * 0.12f + w * 0.76f * m.fraction(), y + 67 * sy);
            c.drawRoundRect(r, 6, 6, p);
        }

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8.8f * sx);
        p.setColor(0x98DDEAF3);
        c.drawText("3 MISSIONS → MULTIPLIER +1 → MAX 30X → SUPER BOXES", w * 0.5f, h * 0.84f, p);
        drawHint(c, "↑↓ SELECT MISSION  5 SKIP • 200 COINS  0 BACK");
    }

    private void drawAchievements(Canvas c, GameEngine e) {
        drawHeader(c, "ACHIEVEMENTS", e);
        for (int i = 0; i < 8; i++) {
            float y = h * 0.205f + i * h * 0.085f;
            boolean done = achievementDone(e, i);
            p.setColor(done ? 0xB02D594E : 0x7E202D42);
            r.set(w * 0.06f, y, w * 0.94f, y + h * 0.065f);
            c.drawRoundRect(r, 10 * sx, 10 * sy, p);

            p.setTextAlign(Paint.Align.LEFT);
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            p.setTextSize(9.5f * sx);
            p.setColor(done ? 0xff6BFFB2 : 0xDBE7EFF7);
            c.drawText((done ? "★ " : "☆ ") + e.achievementText(i), w * 0.10f, y + h * 0.043f, p);

            p.setTextAlign(Paint.Align.RIGHT);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(8 * sx);
            p.setColor(0xA9CAD8E7);
            c.drawText(String.valueOf(e.achievementProgress(i)), w * 0.90f, y + h * 0.043f, p);
        }
        drawHint(c, "4 LEVELS  •  BRONZE  SILVER  GOLD  DIAMOND  •  0 BACK");
    }

    private boolean achievementDone(GameEngine e, int i) {
        switch (i) {
            case 0: return e.save.bestScore >= 1000;
            case 1: return e.save.bestScore >= 10000;
            case 2: return e.save.totalCoins >= 1000;
            case 3: return e.save.totalDistance >= 10000;
            case 4: return e.save.totalPowerUps >= 25;
            case 5: return e.save.multiplier >= 10;
            case 6: return e.save.multiplier >= 30;
            default: return e.save.totalRuns >= 50;
        }
    }

    private void drawWordHunt(Canvas c, GameEngine e) {
        drawHeader(c, "WORD HUNT", e);
        p.setColor(0xA62D405A);
        r.set(w * 0.08f, h * 0.24f, w * 0.92f, h * 0.64f);
        c.drawRoundRect(r, 22 * sx, 22 * sy, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(29 * sx);
        p.setColor(0xff63F2FF);
        c.drawText(e.wordStatus(), w * 0.5f, h * 0.42f, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(11 * sx);
        p.setColor(0xC8DCEAF5);
        c.drawText("Collect green letters during the run.", w * 0.5f, h * 0.50f, p);
        c.drawText("Streak " + e.save.wordStreak + "  •  Complete word for rewards.", w * 0.5f, h * 0.55f, p);

        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(10 * sx);
        p.setColor(e.wordComplete() ? 0xff6BFFB2 : 0xffffdb66);
        c.drawText(e.wordComplete() ? "PRESS 5 TO CLAIM" : "WORD IN PROGRESS", w * 0.5f, h * 0.71f, p);
        drawHint(c, "5 CLAIM  •  0 BACK");
    }

    private void drawSeason(Canvas c, GameEngine e) {
        drawHeader(c, "SEASON HUNT", e);
        int tier = e.save.seasonTier();
        int claimed = e.save.seasonClaimedTier;

        p.setColor(0xA4253550);
        r.set(w * 0.08f, h * 0.205f, w * 0.92f, h * 0.56f);
        c.drawRoundRect(r, 22 * sx, 22 * sy, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(31 * sx);
        p.setColor(0xffF7FAFF);
        c.drawText("TIER " + tier + " / 30", w * 0.5f, h * 0.34f, p);

        p.setTextSize(10 * sx);
        p.setColor(0xff7AD8FF);
        c.drawText("TOKENS " + e.save.seasonTokens + "   •   CLAIMED " + claimed, w * 0.5f, h * 0.41f, p);

        p.setColor(0x553B4B63);
        r.set(w * 0.14f, h * 0.47f, w * 0.86f, h * 0.49f);
        c.drawRoundRect(r, 8, 8, p);
        p.setColor(0xff69E6FF);
        r.set(w * 0.14f, h * 0.47f,
                w * 0.14f + w * 0.72f * Math.min(1f, e.seasonProgressPercent() / 100f), h * 0.49f);
        c.drawRoundRect(r, 8, 8, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(9 * sx);
        p.setColor(0xAAD6E5F1);
        c.drawText("30 seasonal reward tiers • tokens rotate with the season.", w * 0.5f, h * 0.62f, p);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setColor(tier > claimed ? 0xff6BFFB2 : 0xff91A5B7);
        c.drawText(tier > claimed ? "PRESS 5 TO CLAIM TIER" : "NO NEW TIER", w * 0.5f, h * 0.70f, p);
        drawHint(c, "5 CLAIM  •  0 BACK");
    }

    private void drawEvents(Canvas c, GameEngine e) {
        drawHeader(c, "DAILY EVENTS", e);
        String[] items = e.menuLabels();
        drawCardList(c, items, h * 0.205f, 0.10f, e.focus);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(9 * sx);
        p.setColor(0xB3D8E6F2);
        c.drawText("EVENT TOKENS " + e.save.eventTokens, w * 0.5f, h * 0.72f, p);
        c.drawText("Featured worlds, bonuses, jackpots and weekend word events.", w * 0.5f, h * 0.77f, p);
        drawHint(c, "↑↓ SELECT  5 ACTIVATE  0 BACK");
    }

    private void drawMystery(Canvas c, GameEngine e) {
        drawHeader(c, "MYSTERY BOXES", e);
        String[] labels = e.menuLabels();
        for (int i = 0; i < labels.length; i++) {
            float cx = w * (0.20f + i * 0.30f);
            float cy = h * 0.39f;
            p.setColor(i == e.focus ? 0xff8B6CFF : 0xff48556F);
            c.drawRoundRect(cx - 38 * sx, cy - 42 * sy, cx + 38 * sx, cy + 42 * sy, 13 * sx, 13 * sy, p);
            p.setColor(0x55FFFFFF);
            c.drawCircle(cx - 10 * sx, cy - 12 * sy, 5 * sx, p);
            c.drawCircle(cx + 10 * sx, cy + 5 * sy, 3 * sx, p);

            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            p.setTextSize(8 * sx);
            p.setColor(0xffF2F7FF);
            c.drawText(i == 0 ? "MINI" : i == 1 ? "BOX" : "SUPER", cx, cy + 62 * sy, p);
        }
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(9 * sx);
        p.setColor(0xB7D5E3EF);
        c.drawText("Coins • Keys • Tokens • Character Tokens • Boards", w * 0.5f, h * 0.66f, p);
        c.drawText("Opened: " + e.save.mysteryBoxesOpened + "   •   Super boxes: " + e.save.superBoxes,
                w * 0.5f, h * 0.71f, p);
        drawHint(c, "←→ SELECT BOX  5 OPEN  0 BACK");
    }

    private void drawChallenges(Canvas c, GameEngine e) {
        drawHeader(c, "CHALLENGES", e);
        String[] items = e.menuLabels();
        drawCardList(c, items, h * 0.205f, 0.087f, e.focus);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8.5f * sx);
        p.setColor(0xAFCFE0ED);
        c.drawText("Hurdles = 4 difficulty concept  •  Season = up to 5 runs  •  Tag = time attack",
                w * 0.5f, h * 0.90f, p);
        drawHint(c, "↑↓ SELECT  5 PLAY  0 BACK");
    }

    private void drawCollections(Canvas c, GameEngine e) {
        drawHeader(c, "COLLECTIONS", e);
        int points = e.collectionPointsForUi();
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(26 * sx);
        p.setColor(e.save.multiplier >= 7 ? 0xff6BFFB2 : 0xff92A5B6);
        c.drawText(e.save.multiplier >= 7 ? "COLLECTION POINTS " + points : "COLLECTIONS • UNLOCK AT 7X", w * 0.5f, h * 0.27f, p);

        String[] rows = {
                "CHARACTERS     " + unlockedCharacters(e),
                "OUTFITS        " + unlockedOutfits(e),
                "BOARDS         " + unlockedBoards(e),
                "REWARD TIER    " + e.save.collectionRewardTier
        };
        for (int i = 0; i < rows.length; i++) {
            float y = h * 0.34f + i * h * 0.11f;
            p.setColor(0x8E222E45);
            r.set(w * 0.10f, y, w * 0.90f, y + h * 0.08f);
            c.drawRoundRect(r, 12 * sx, 12 * sy, p);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(10.5f * sx);
            p.setColor(0xDCE6EFF7);
            c.drawText(rows[i], w * 0.5f, y + h * 0.051f, p);
        }

        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(10 * sx);
        p.setColor(0xffFFD86C);
        c.drawText("Press 5 for the next unlocked collection reward.", w * 0.5f, h * 0.83f, p);
        drawHint(c, "5 CLAIM REWARD  •  0 BACK");
    }

    private int unlockedCharacters(GameEngine e) {
        return 1 + (e.save.character2Unlocked ? 1 : 0) + (e.save.character3Unlocked ? 1 : 0);
    }

    private int unlockedOutfits(GameEngine e) {
        return 1 + (e.save.outfit1Unlocked ? 1 : 0) + (e.save.outfit2Unlocked ? 1 : 0);
    }

    private int unlockedBoards(GameEngine e) {
        return 1 + (e.save.board2Unlocked ? 1 : 0) + (e.save.board3Unlocked ? 1 : 0);
    }

    private void drawCharacters(Canvas c, GameEngine e) {
        drawHeader(c, "CHARACTERS", e);
        drawCharacter(c, 0, "NOVA", "FREE", e.save.selectedCharacter == 0, 0xff5BE7FF);
        drawCharacter(c, 1, "KAI", "500 COINS", e.save.selectedCharacter == 1, 0xffff6FAD);
        drawCharacter(c, 2, "ZIA", "60 TOKENS", e.save.selectedCharacter == 2, 0xffFFD45B);
        drawHint(c, "↑↓ SELECT  5 USE / UNLOCK  0 BACK");
    }

    private void drawCharacter(Canvas c, int i, String name, String cost, boolean selected, int color) {
        float top = h * 0.20f + i * h * 0.22f;
        p.setColor(selected ? 0xB82C4664 : 0x7E202D42);
        r.set(w * 0.08f, top, w * 0.92f, top + h * 0.17f);
        c.drawRoundRect(r, 15 * sx, 15 * sy, p);

        p.setColor(color);
        c.drawCircle(w * 0.21f, top + h * 0.085f, 28 * sx, p);
        p.setColor(0xffF1D0B5);
        c.drawCircle(w * 0.21f, top + h * 0.04f, 16 * sx, p);

        p.setTextAlign(Paint.Align.LEFT);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(14 * sx);
        p.setColor(0xffF5FAFF);
        c.drawText(name, w * 0.38f, top + 36 * sy, p);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8.8f * sx);
        p.setColor(0xB7D8E5F0);
        c.drawText(cost, w * 0.38f, top + 55 * sy, p);
        if (selected) {
            p.setColor(0xff6BFFB2);
            c.drawText("SELECTED", w * 0.38f, top + 73 * sy, p);
        }
    }

    private void drawOutfits(Canvas c, GameEngine e) {
        drawHeader(c, "OUTFITS", e);
        String[] names = {"BASE", "NOVA NIGHT", "KAI STORM"};
        String[] cost = {"FREE", "650 COINS", "850 COINS"};
        for (int i = 0; i < 3; i++) {
            float y = h * 0.205f + i * h * 0.19f;
            drawMenuRow(c, names[i] + " • " + cost[i], e.focus == i, i + 1, y, h * 0.14f);
        }
        drawHint(c, "↑↓ SELECT  5 USE / UNLOCK  0 BACK");
    }

    private void drawBoards(Canvas c, GameEngine e) {
        drawHeader(c, "HOVERBOARDS", e);
        drawBoard(c, 0, "PULSE", "FREE", e.save.selectedBoard == 0, 0xffB9FF4A);
        drawBoard(c, 1, "VOLT", "700 COINS", e.save.selectedBoard == 1, 0xff7C8CFF);
        drawBoard(c, 2, "SOLAR", "1200 COINS", e.save.selectedBoard == 2, 0xffffB74D);
        drawHint(c, "↑↓ SELECT  5 USE / UNLOCK  7 ACTIVATE IN RUN  0 BACK");
    }

    private void drawBoard(Canvas c, int i, String name, String cost, boolean selected, int color) {
        float y = h * 0.20f + i * h * 0.20f;
        p.setColor(selected ? 0xB02D4664 : 0x7A202D42);
        r.set(w * 0.08f, y, w * 0.92f, y + h * 0.15f);
        c.drawRoundRect(r, 15 * sx, 15 * sy, p);

        p.setColor(color);
        r.set(w * 0.12f, y + h * 0.055f, w * 0.36f, y + h * 0.075f);
        c.drawRoundRect(r, 10 * sx, 10 * sy, p);

        p.setTextAlign(Paint.Align.LEFT);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(13 * sx);
        p.setColor(0xffF2F8FF);
        c.drawText(name, w * 0.43f, y + 34 * sy, p);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8.8f * sx);
        p.setColor(0xBDD1E2EE);
        c.drawText(cost + "  •  SHIELD", w * 0.43f, y + 53 * sy, p);
        if (selected) {
            p.setColor(0xff6BFFB2);
            c.drawText("EQUIPPED", w * 0.43f, y + 72 * sy, p);
        }
    }

    private void drawWorldTour(Canvas c, GameEngine e) {
        drawHeader(c, "WORLD TOUR", e);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(25 * sx);
        p.setColor(0xff62E6FF);
        c.drawText(e.currentWorld(), w * 0.5f, h * 0.28f, p);

        p.setTextSize(9.5f * sx);
        p.setColor(0xB5D5E2EE);
        c.drawText("WORLD ROTATION • VISUAL THEME • MUSIC • TRACK RULES", w * 0.5f, h * 0.34f, p);
        c.drawText("Press 5 to preview the next world.", w * 0.5f, h * 0.39f, p);

        String[] worlds = e.worldList();
        int start = Math.max(0, e.worldIndex - 2);
        int end = Math.min(worlds.length, start + 5);
        for (int i = start; i < end; i++) {
            float y = h * 0.47f + (i - start) * h * 0.075f;
            boolean selected = i == e.worldIndex;
            p.setColor(selected ? 0xB52B4C66 : 0x76212D42);
            r.set(w * 0.13f, y, w * 0.87f, y + h * 0.055f);
            c.drawRoundRect(r, 10 * sx, 10 * sy, p);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(9 * sx);
            p.setColor(selected ? 0xffF4FBFF : 0xBFD0E0EB);
            c.drawText(worlds[i], w * 0.5f, y + h * 0.037f, p);
        }
        drawHint(c, "5 NEXT WORLD  •  0 BACK");
    }

    private void drawProfile(Canvas c, GameEngine e) {
        drawHeader(c, "PROFILE / TOP RUN", e);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(22 * sx);
        p.setColor(0xffF7FAFF);
        c.drawText("RR" + e.friendCode(), w * 0.5f, h * 0.25f, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(10 * sx);
        p.setColor(0xB3D8E7F2);
        c.drawText("LEAGUE " + e.leagueName() + "  •  WEEK BEST " + e.save.weekBest, w * 0.5f, h * 0.31f, p);
        c.drawText("TOP RUN is a weekly leaderboard concept.", w * 0.5f, h * 0.36f, p);

        String[] stats = {
                "RUNS " + e.save.totalRuns,
                "DISTANCE " + e.save.totalDistance + "m",
                "COINS " + e.save.totalCoins,
                "POWERS " + e.save.totalPowerUps,
                "ACHIEVEMENTS " + e.achievementCount()
        };
        for (int i = 0; i < stats.length; i++) {
            float y = h * 0.43f + i * h * 0.065f;
            p.setColor(0x84222E45);
            r.set(w * 0.12f, y, w * 0.88f, y + h * 0.05f);
            c.drawRoundRect(r, 9 * sx, 9 * sy, p);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(9.5f * sx);
            p.setColor(0xD6E0EAF4);
            c.drawText(stats[i], w * 0.5f, y + h * 0.033f, p);
        }
        drawHint(c, "5 BACK TO HOME  •  0 BACK");
    }

    private void drawFriends(Canvas c, GameEngine e) {
        drawHeader(c, "FRIENDS", e);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(19 * sx);
        p.setColor(0xff62E6FF);
        c.drawText("FRIEND TAG  RR" + e.friendCode(), w * 0.5f, h * 0.26f, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(9 * sx);
        p.setColor(0xB5D8E5F1);
        c.drawText("Designed for Friend Tags / QR expansion.", w * 0.5f, h * 0.31f, p);

        drawCardList(c, new String[]{"FRIEND 01 • ACTIVE", "FRIEND 02 • 12,840", "FRIEND 03 • 9,210"},
                h * 0.39f, 0.12f, e.focus);
        drawHint(c, "5 OPEN FRIEND  •  0 BACK");
    }

    private void drawNews(Canvas c, GameEngine e) {
        drawHeader(c, "NEWS", e);
        for (int i = 0; i < 7; i++) {
            float y = h * 0.20f + i * h * 0.083f;
            p.setColor(i == e.focus ? 0xB12C4766 : 0x7E202D42);
            r.set(w * 0.07f, y, w * 0.93f, y + h * 0.061f);
            c.drawRoundRect(r, 10 * sx, 10 * sy, p);

            p.setTextAlign(Paint.Align.LEFT);
            p.setTypeface(Typeface.create("sans", i == e.focus ? Typeface.BOLD : Typeface.NORMAL));
            p.setTextSize(9.3f * sx);
            p.setColor(0xDCE9F1F8);
            c.drawText(e.newsTitle(i), w * 0.10f, y + h * 0.039f, p);
        }
        drawHint(c, "↑↓ SELECT  5 OPEN  0 BACK");
    }

    private void drawCardList(Canvas c, String[] items, float top, float rowFactor, int selected) {
        float row = h * rowFactor;
        for (int i = 0; i < items.length; i++) {
            float y = top + i * row;
            p.setColor(i == selected ? 0xB02B4868 : 0x7A202C41);
            r.set(w * 0.08f, y, w * 0.92f, y + row - 7 * sy);
            c.drawRoundRect(r, 13 * sx, 13 * sy, p);
            if (i == selected) {
                stroke.setStrokeWidth(1.1f * sx);
                stroke.setColor(0xBE64E5FF);
                c.drawRoundRect(r, 13 * sx, 13 * sy, stroke);
            }

            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.create("sans", i == selected ? Typeface.BOLD : Typeface.NORMAL));
            p.setTextSize(10 * sx);
            p.setColor(i == selected ? 0xffF4FAFF : 0xC7D8E6F1);
            c.drawText(items[i], w * 0.5f, y + row * 0.53f, p);
        }
    }

    private void drawOverlay(Canvas c, GameEngine e, String title, String[] items) {
        p.setColor(0x9C00050B);
        c.drawRect(0, 0, w, h, p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(30 * sx);
        p.setColor(0xffF4FAFF);
        c.drawText(title, w * 0.5f, h * 0.28f, p);
        drawMenu(c, e, items, h * 0.38f, 0.09f);
        drawHint(c, "↑↓ SELECT  5 OK  0 BACK");
    }

    private void drawGameOver(Canvas c, GameEngine e) {
        p.setColor(0xC300050A);
        c.drawRect(0, 0, w, h, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(31 * sx);
        p.setColor(0xffF5FAFF);
        c.drawText("RUN OVER", w * 0.5f, h * 0.29f, p);

        p.setTextSize(13 * sx);
        p.setColor(0xff6CE8FF);
        c.drawText("SCORE " + Math.round(e.score), w * 0.5f, h * 0.38f, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(10 * sx);
        p.setColor(0xC6D9E6F1);
        c.drawText("DIST " + Math.round(e.distance) + "m   •   COINS " + e.runCoins
                + "   •   BEST " + e.save.bestScore, w * 0.5f, h * 0.44f, p);
        c.drawText(e.challengeTitle(e.challengeMode), w * 0.5f, h * 0.49f, p);

        drawMenu(c, e, e.menuLabels(), h * 0.55f, 0.10f);
    }

    private void drawRun(Canvas c, GameEngine e) {
        float horizon = h * 0.355f;
        float floorY = h * 0.96f;
        float roadTop = w * 0.22f;
        float roadBottom = w * 0.96f;
        float center = w * 0.5f;

        path.reset();
        path.moveTo(center - roadTop * 0.5f, horizon);
        path.lineTo(center + roadTop * 0.5f, horizon);
        path.lineTo(center + roadBottom * 0.5f, floorY);
        path.lineTo(center - roadBottom * 0.5f, floorY);
        path.close();

        p.setShader(new LinearGradient(0, horizon, 0, floorY,
                0xff262E43, 0xff080B12, Shader.TileMode.CLAMP));
        c.drawPath(path, p);
        p.setShader(null);

        stroke.setStrokeWidth(3 * sx);
        stroke.setColor(0xff5FE6FF);
        c.drawLine(center - roadTop * 0.5f, horizon, center - roadBottom * 0.5f, floorY, stroke);
        c.drawLine(center + roadTop * 0.5f, horizon, center + roadBottom * 0.5f, floorY, stroke);

        stroke.setStrokeWidth(1.1f * sx);
        stroke.setColor(0x7796A8FF);
        for (int lane = 1; lane < 3; lane++) {
            float tx = laneX(lane, roadTop, center);
            float bx = laneX(lane, roadBottom, center);
            c.drawLine(tx, horizon, bx, floorY, stroke);
        }

        for (int i = 0; i < 11; i++) {
            float z = ((i * 0.109f) + e.worldPulse * e.speed * 0.30f) % 1f;
            float yy = horizon + (floorY - horizon) * z * z;
            float half = roadTop * 0.5f + (roadBottom * 0.5f - roadTop * 0.5f) * z * z;
            stroke.setStrokeWidth(1.0f * sx);
            stroke.setColor(0x2C78D9FF);
            c.drawLine(center - half, yy, center - half + 13 * sx, yy, stroke);
            c.drawLine(center + half, yy, center + half - 13 * sx, yy, stroke);
        }

        drawObjects(c, e, horizon, floorY, roadTop, roadBottom, center);
        drawPlayer(c, e, floorY, roadBottom, center);
        drawParticles(c, e);
        drawRunHud(c, e);

        if (e.challengeMode != GameEngine.MODE_NORMAL) {
            drawChallengeRibbon(c, e);
        }

        if (e.challengeMode == GameEngine.MODE_HURDLES) {
            p.setColor(0x25EEF7FF);
            c.drawRect(0, h * 0.20f, w, h * 0.63f, p);
        }
    }

    private float laneX(int lane, float roadW, float center) {
        return center + (lane - 1) * roadW / 3f;
    }

    private void drawObjects(Canvas c, GameEngine e, float horizon, float floorY,
                             float roadTop, float roadBottom, float center) {
        for (GameObject o : e.objects) {
            if (!o.active || o.z > 1.70f) continue;
            float t = Math.max(0f, Math.min(1f, 1f - o.z / 1.50f));
            float depth = t * t;
            float yy = horizon + (floorY - horizon) * depth;
            float road = roadTop + (roadBottom - roadTop) * depth;
            float x = laneX(o.lane, road, center);
            float scale = 0.25f + t * 1.75f;

            if (o.kind == GameObject.Kind.COIN) {
                p.setColor(0x2F000000);
                c.drawCircle(x + 2 * sx, yy + 4 * sy, 8 * scale * sx, p);
                p.setColor(0xffFFE36E);
                c.drawCircle(x, yy, 7 * scale * sx, p);
                p.setColor(0xCCFFFFFF);
                c.drawCircle(x - 2 * sx * scale, yy - 2 * sy * scale, 2 * scale * sx, p);
            } else if (o.kind == GameObject.Kind.POWER) {
                int color = powerColor(o.powerType);
                p.setColor(color);
                float s = 11 * scale * sx;
                path.reset();
                path.moveTo(x, yy - s);
                path.lineTo(x + s, yy);
                path.lineTo(x, yy + s);
                path.lineTo(x - s, yy);
                path.close();
                c.drawPath(path, p);

                p.setTextAlign(Paint.Align.CENTER);
                p.setTypeface(Typeface.create("sans", Typeface.BOLD));
                p.setTextSize(Math.max(7, 8 * scale * sx));
                p.setColor(0xF2FFFFFF);
                c.drawText(powerLetter(o.powerType), x, yy + 3 * sy * scale, p);
            } else if (o.kind == GameObject.Kind.LETTER) {
                p.setColor(0xff65FF80);
                c.drawCircle(x, yy, 11 * scale * sx, p);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTypeface(Typeface.create("sans", Typeface.BOLD));
                p.setTextSize(Math.max(8, 12 * scale * sx));
                p.setColor(0xff08210C);
                c.drawText(o.payload, x, yy + 4 * scale * sy, p);
            } else if (o.kind == GameObject.Kind.TOKEN) {
                p.setColor(0xff7AD9FF);
                path.reset();
                path.moveTo(x, yy - 10 * scale * sy);
                path.lineTo(x + 8 * scale * sx, yy);
                path.lineTo(x, yy + 10 * scale * sy);
                path.lineTo(x - 8 * scale * sx, yy);
                path.close();
                c.drawPath(path, p);
            } else {
                drawObstacle(c, o, x, yy, scale);
            }
        }
    }

    private void drawObstacle(Canvas c, GameObject o, float x, float y, float s) {
        float ww = 35 * s * sx;
        float hh = 44 * s * sy;

        if (o.obstacleType == ObstacleType.TRAIN) {
            p.setColor(0xff858EA6);
            r.set(x - ww, y - hh, x + ww, y + 4 * s * sy);
            c.drawRoundRect(r, 5 * s * sx, 5 * s * sy, p);
            p.setColor(0xff2E3448);
            c.drawRoundRect(x - ww + 5 * s * sx, y - hh + 7 * s * sy,
                    x + ww - 5 * s * sx, y - hh + 22 * s * sy,
                    3 * s * sx, 3 * s * sy, p);
            p.setColor(0xff63E6FF);
            c.drawCircle(x - ww + 9 * s * sx, y - 2 * s * sy, 3 * s * sx, p);
            c.drawCircle(x + ww - 9 * s * sx, y - 2 * s * sy, 3 * s * sx, p);
        } else if (o.obstacleType == ObstacleType.LOW_BARRIER) {
            p.setColor(0xffff5877);
            r.set(x - ww, y - 17 * s * sy, x + ww, y + 2 * s * sy);
            c.drawRoundRect(r, 3 * s * sx, 3 * s * sy, p);
            p.setColor(0xBFFFFFFF);
            c.drawRect(x - ww, y - 14 * s * sy, x + ww, y - 10 * s * sy, p);
        } else if (o.obstacleType == ObstacleType.HIGH_BARRIER) {
            p.setColor(0xff9D70FF);
            r.set(x - ww * .82f, y - hh * .82f, x + ww * .82f, y - 5 * s * sy);
            c.drawRoundRect(r, 4 * s * sx, 4 * s * sy, p);
            p.setColor(0xffFFE05F);
            c.drawRect(x - ww * .60f, y - hh * .70f,
                    x + ww * .60f, y - hh * .62f, p);
        } else if (o.obstacleType == ObstacleType.TUNNEL) {
            p.setColor(0xff596A85);
            r.set(x - ww * .95f, y - hh * 1.05f, x + ww * .95f, y + 3 * s * sy);
            c.drawRoundRect(r, 6 * s * sx, 6 * s * sy, p);
            p.setColor(0xff0A0E17);
            r.set(x - ww * .68f, y - hh * .82f, x + ww * .68f, y - 4 * s * sy);
            c.drawRoundRect(r, 5 * s * sx, 5 * s * sy, p);
        } else if (o.obstacleType == ObstacleType.GAP) {
            p.setColor(0xff03050A);
            path.reset();
            path.moveTo(x - ww, y);
            path.lineTo(x - 6 * s * sx, y);
            path.lineTo(x, y + 22 * s * sy);
            path.lineTo(x + 6 * s * sx, y);
            path.lineTo(x + ww, y);
            path.lineTo(x + ww, y + 5 * s * sy);
            path.lineTo(x - ww, y + 5 * s * sy);
            path.close();
            c.drawPath(path, p);
        } else {
            stroke.setStrokeWidth(4 * s * sx);
            stroke.setColor(0xffFFCD5D);
            c.drawLine(x - ww, y, x + ww, y, stroke);
        }
    }

    private void drawPlayer(Canvas c, GameEngine e, float floorY, float roadBottom, float center) {
        float x = center + (e.playerLane - 1) * roadBottom / 3f;
        float air = 0f;
        if (e.jumpTimer > 0f) {
            float phase = Math.min(1f, e.jumpTimer / (e.challengeMode == GameEngine.MODE_LOW_GRAVITY ? 0.95f : 0.70f));
            air = (float)Math.sin(phase * Math.PI) * 75 * sy;
        }
        float baseY = floorY - 45 * sy - air;

        int body;
        if (e.save.selectedCharacter == 0) body = 0xff57E4FF;
        else if (e.save.selectedCharacter == 1) body = 0xffff72B7;
        else body = 0xffFFD55E;

        if (e.save.selectedOutfit == 1) body = 0xff9B76FF;
        if (e.save.selectedOutfit == 2) body = 0xffFF8C43;

        int board = e.save.selectedBoard == 0 ? 0xffB9FF4A
                : e.save.selectedBoard == 1 ? 0xff7F8BFF : 0xffffB752;

        if (e.boardActive) {
            p.setColor(0x4A8B7BFF);
            c.drawOval(x - 33 * sx, baseY + 22 * sy, x + 33 * sx, baseY + 37 * sy, p);
            p.setColor(board);
            r.set(x - 26 * sx, baseY + 17 * sy, x + 26 * sx, baseY + 23 * sy);
            c.drawRoundRect(r, 6 * sx, 6 * sy, p);
        }

        if (e.rollTimer > 0f) {
            p.setColor(body);
            c.drawOval(x - 27 * sx, baseY + 1 * sy, x + 27 * sx, baseY + 19 * sy, p);
            p.setColor(0xffF4D2B7);
            c.drawCircle(x + 17 * sx, baseY + 9 * sy, 8 * sx, p);
        } else {
            p.setColor(body);
            r.set(x - 13 * sx, baseY, x + 13 * sx, baseY + 31 * sy);
            c.drawRoundRect(r, 8 * sx, 8 * sy, p);
            p.setColor(0xffF2D2B8);
            c.drawCircle(x, baseY - 10 * sy, 11 * sx, p);
            p.setColor(0xff1A2030);
            c.drawCircle(x - 4 * sx, baseY - 11 * sy, 1.5f * sx, p);
            c.drawCircle(x + 4 * sx, baseY - 11 * sy, 1.5f * sx, p);
            p.setColor(0xff1F2535);
            c.drawRect(x - 11 * sx, baseY + 28 * sy, x - 2 * sx, baseY + 35 * sy, p);
            c.drawRect(x + 2 * sx, baseY + 28 * sy, x + 11 * sx, baseY + 35 * sy, p);
        }

        if (e.activePower != PowerType.NONE && e.powerTimer > 0f) {
            stroke.setStrokeWidth(2.2f * sx);
            stroke.setColor(powerColor(e.activePower));
            r.set(x - 31 * sx, baseY - 43 * sy, x + 31 * sx, baseY + 43 * sy);
            c.drawOval(r, stroke);
        }
    }

    private void drawParticles(Canvas c, GameEngine e) {
        for (TrailParticle q : e.particles) {
            p.setColor(q.color);
            float alpha = Math.max(0f, q.life / q.maxLife);
            p.setAlpha((int)(alpha * 220));
            c.drawCircle(q.x * w, q.y * h, 2.2f * alpha * sx + .6f, p);
            p.setAlpha(255);
        }
    }

    private void drawRunHud(Canvas c, GameEngine e) {
        drawPill(c, 8 * sx, 10 * sy, 98 * sx, 32 * sy, "SCORE", String.valueOf(Math.round(e.score)), 0xff78D6FF);
        drawPill(c, w - 106 * sx, 10 * sy, 98 * sx, 32 * sy, "COINS", String.valueOf(e.runCoins), 0xffFFE36E);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(12 * sx);
        p.setColor(0xE9F8FBFF);
        c.drawText(Math.round(e.distance) + "m", w * .5f, 26 * sy, p);
        p.setTextSize(8 * sx);
        p.setColor(0xA8D9E8F3);
        c.drawText(e.save.multiplier + "x", w * .5f, 39 * sy, p);

        if (e.activePower != PowerType.NONE && e.powerTimer > 0f) {
            drawPowerBar(c, e);
        }
        if (e.boardActive) {
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            p.setTextSize(8 * sx);
            p.setColor(0xffB8FF4A);
            c.drawText("BOARD " + Math.ceil(e.boardTimer) + "s", w * .5f, 64 * sy, p);
        }

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(8 * sx);
        p.setColor(0x6CCFDFED);
        c.drawText("4/← LANE   2/↑ JUMP   8/↓ ROLL   7 BOARD   0 PAUSE",
                w * .5f, h * .986f, p);
        if (e.challengeMode == GameEngine.MODE_ATTACK) {
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            p.setTextSize(10 * sx);
            p.setColor(0xffffd56e);
            c.drawText("TIME " + Math.ceil(e.attackTimer) + "s", w * .5f, 78 * sy, p);
        }
        if (e.scoreBoosterTimer > 0f) {
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(7.5f * sx);
            p.setColor(0xff6BFFB2);
            c.drawText("BOOST +5  " + Math.ceil(e.scoreBoosterTimer) + "s", w * .5f, 91 * sy, p);
        }
    }

    private void drawChallengeRibbon(Canvas c, GameEngine e) {
        p.setColor(0xB7253550);
        r.set(w * .20f, h * .08f, w * .80f, h * .125f);
        c.drawRoundRect(r, 12 * sx, 12 * sy, p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(8.5f * sx);
        p.setColor(0xffEAF7FF);
        c.drawText(e.challengeTitle(e.challengeMode), w * .5f, h * .107f, p);
    }

    private void drawPowerBar(Canvas c, GameEngine e) {
        float left = w * .27f;
        float right = w * .73f;
        float y = 49 * sy;
        float max = e.activePower == PowerType.JETPACK || e.activePower == PowerType.POGO ? 8f : 10f;

        p.setColor(0x50313D55);
        r.set(left, y, right, y + 6 * sy);
        c.drawRoundRect(r, 5, 5, p);

        p.setColor(powerColor(e.activePower));
        r.set(left, y, left + (right - left) * Math.min(1f, e.powerTimer / max), y + 6 * sy);
        c.drawRoundRect(r, 5, 5, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(7 * sx);
        p.setColor(0xE9F5FBFF);
        c.drawText(powerLetter(e.activePower) + " " + Math.ceil(e.powerTimer) + "s", w * .5f, y - 3 * sy, p);
    }

    private void drawPill(Canvas c, float x, float y, float width, float height,
                          String label, String value, int color) {
        p.setColor(0x55202B42);
        r.set(x, y, x + width, y + height);
        c.drawRoundRect(r, 11 * sx, 11 * sy, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(7 * sx);
        p.setColor(0x86D7E6F1);
        c.drawText(label, x + width/2, y + 10 * sy, p);

        p.setTextSize(12 * sx);
        p.setColor(color);
        c.drawText(value, x + width/2, y + 25 * sy, p);
    }

    private void drawNotice(Canvas c, String msg) {
        p.setColor(0xE0101828);
        r.set(w * .16f, h * .075f, w * .84f, h * .135f);
        c.drawRoundRect(r, 15 * sx, 15 * sy, p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(9.5f * sx);
        p.setColor(0xF2F3FAFF);
        c.drawText(msg, w * .5f, h * .112f, p);
    }

    private void drawHint(Canvas c, String text) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(7.7f * sx);
        p.setColor(0x7ED8E5EF);
        c.drawText(text, w * .5f, h * .965f, p);
    }

    private long currentDay() {
        return System.currentTimeMillis() / 86400000L;
    }

    private int powerColor(PowerType type) {
        switch (type) {
            case MAGNET: return 0xffE96BFF;
            case SNEAKERS: return 0xff6BFFB2;
            case X2: return 0xffffd34f;
            case JETPACK: return 0xff72C9FF;
            case POGO: return 0xffff8bd7;
            case MYSTERIZER: return 0xff9D7BFF;
            default: return 0xffffffff;
        }
    }

    private String powerLetter(PowerType type) {
        switch (type) {
            case MAGNET: return "M";
            case SNEAKERS: return "S";
            case X2: return "X2";
            case JETPACK: return "J";
            case POGO: return "P";
            case MYSTERIZER: return "S?";
            default: return "?";
        }
    }
}
