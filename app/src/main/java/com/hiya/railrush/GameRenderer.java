package com.hiya.railrush;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.List;

final class GameRenderer {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Path path = new Path();

    private int w;
    private int h;
    private float sx;
    private float sy;

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
                drawOverlayMenu(c, e, "PAUSED", new String[]{"המשך", "התחל מחדש", "בית"});
                break;
            case GAME_OVER:
                drawRun(c, e);
                drawOverlayMenu(c, e, "RUN OVER", new String[]{"ריצה חדשה", "בית"});
                drawGameOverStats(c, e);
                break;
            case SHOP:
                drawPanel(c, e, "חנות", new String[]{
                        "שדרוג מגנט  •  LV " + e.save.magnetLevel,
                        "שדרוג סניקרס  •  LV " + e.save.sneakersLevel,
                        "שדרוג x2  •  LV " + e.save.x2Level,
                        "חבילת לוחות  •  +2"
                });
                break;
            case MISSIONS:
                drawMissions(c, e);
                break;
            case CHARACTERS:
                drawCharacters(c, e);
                break;
            case BOARDS:
                drawBoards(c, e);
                break;
            case ACHIEVEMENTS:
                drawAchievements(c, e);
                break;
            case SETTINGS:
                drawPanel(c, e, "הגדרות", new String[]{
                        "צלילים  •  " + (e.save.soundOn ? "פעיל" : "כבוי"),
                        "רטט  •  " + (e.save.vibrationOn ? "פעיל" : "כבוי")
                });
                break;
            case HOME:
            default:
                drawHome(c, e);
                break;
        }

        if (e.noticeTimer > 0f) drawNotice(c, e.notice);
    }

    private void drawBackdrop(Canvas c, GameEngine e) {
        int top;
        int bottom;
        if (e.state == ScreenState.RUNNING || e.state == ScreenState.PAUSED || e.state == ScreenState.GAME_OVER) {
            int theme = e.worldTheme;
            if (theme == 0) { top = 0xff08152F; bottom = 0xff182C4E; }
            else if (theme == 1) { top = 0xff260E31; bottom = 0xff4E1A4D; }
            else if (theme == 2) { top = 0xff08292A; bottom = 0xff145B56; }
            else { top = 0xff191A2E; bottom = 0xff36304B; }
        } else {
            top = 0xff050712;
            bottom = 0xff141A2B;
        }

        p.setShader(new LinearGradient(0, 0, 0, h,
                top, bottom, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        // Moon / sun glow.
        p.setColor(0x22FFFFFF);
        c.drawCircle(w * 0.79f, h * 0.16f, Math.min(w, h) * 0.085f, p);
        p.setColor(0x10B9F3FF);
        c.drawCircle(w * 0.79f, h * 0.16f, Math.min(w, h) * 0.14f, p);

        // Distant skyline.
        p.setColor(0x331B2035);
        float y = h * 0.42f;
        for (int i = 0; i < 13; i++) {
            float bw = w * (0.045f + (i % 3) * 0.012f);
            float bh = h * (0.08f + (i % 5) * 0.032f);
            float x = w * (i / 13f);
            c.drawRect(x, y - bh, x + bw, y, p);
        }
        p.setColor(0x447EE7FF);
        for (int i = 0; i < 18; i++) {
            float x = (i * 47f) % Math.max(1, w);
            float yy = h * (0.08f + ((i * 29) % 33) / 100f);
            c.drawRect(x, yy, x + 1.5f * sx, yy + 1.5f * sy, p);
        }
    }

    private void drawHome(Canvas c, GameEngine e) {
        // Brand header.
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setColor(0xffF4F8FF);
        p.setTextSize(37 * sx);
        c.drawText("RAIL", w * 0.5f, h * 0.14f, p);
        p.setColor(0xff66E7FF);
        c.drawText("RUSH", w * 0.5f, h * 0.205f, p);

        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(11 * sx);
        p.setColor(0x99DDEAFF);
        c.drawText("NEON KEYPAD RUNNER", w * 0.5f, h * 0.245f, p);

        // Stats ribbon.
        drawStat(c, w * 0.18f, h * 0.30f, "COINS", String.valueOf(e.save.coins), 0xffFFE36E);
        drawStat(c, w * 0.50f, h * 0.30f, "BEST", String.valueOf(e.save.bestScore), 0xff78D6FF);
        drawStat(c, w * 0.82f, h * 0.30f, "MULTI", e.save.multiplier + "x", 0xffFF7EC7);

        String[] menu = {"▶  התחל ריצה", "▣  חנות", "✓  משימות", "●  דמויות", "◆  לוחות", "★  הישגים", "⚙  הגדרות"};
        float top = h * 0.37f;
        float itemH = h * 0.065f;
        for (int i = 0; i < menu.length; i++) {
            drawMenuRow(c, menu[i], i == e.focus, top + i * itemH, itemH);
        }

        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(9 * sx);
        p.setColor(0x88DCE8F6);
        c.drawText("↑↓ בחירה   •   5 אישור   •   0 חזרה   •   1 דמות   •   7 לוח", w * 0.5f, h * 0.965f, p);
    }

    private void drawStat(Canvas c, float x, float y, String label, String value, int color) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(8 * sx);
        p.setColor(0x66FFFFFF);
        c.drawText(label, x, y, p);
        p.setTextSize(17 * sx);
        p.setColor(color);
        c.drawText(value, x, y + 20 * sy, p);
    }

    private void drawRun(Canvas c, GameEngine e) {
        float horizon = h * 0.365f;
        float floorY = h * 0.96f;
        float roadTopW = w * 0.22f;
        float roadBottomW = w * 0.94f;
        float center = w * 0.5f;

        // Rails bed.
        path.reset();
        path.moveTo(center - roadTopW * 0.5f, horizon);
        path.lineTo(center + roadTopW * 0.5f, horizon);
        path.lineTo(center + roadBottomW * 0.5f, floorY);
        path.lineTo(center - roadBottomW * 0.5f, floorY);
        path.close();
        p.setShader(new LinearGradient(0, horizon, 0, floorY,
                0xff20263A, 0xff080B12, Shader.TileMode.CLAMP));
        c.drawPath(path, p);
        p.setShader(null);

        // Track edges.
        stroke.setStrokeWidth(3 * sx);
        stroke.setColor(0xff63E7FF);
        drawPerspectiveLine(c, center - roadTopW * 0.5f, horizon, center - roadBottomW * 0.5f, floorY);
        drawPerspectiveLine(c, center + roadTopW * 0.5f, horizon, center + roadBottomW * 0.5f, floorY);

        stroke.setStrokeWidth(1.5f * sx);
        stroke.setColor(0x7793A6FF);
        for (int lane = 1; lane < 3; lane++) {
            float topX = laneX(lane, horizon, roadTopW, center);
            float bottomX = laneX(lane, floorY, roadBottomW, center);
            drawPerspectiveLine(c, topX, horizon, bottomX, floorY);
        }

        // Speed streaks.
        stroke.setStrokeWidth(1.2f * sx);
        stroke.setColor(0x226AEFFF);
        for (int i = 0; i < 10; i++) {
            float z = ((i * 0.107f) + (e.worldPulse * e.speed * 0.32f)) % 1f;
            float yy = horizon + (floorY - horizon) * (z * z);
            float half = roadTopW * 0.5f + (roadBottomW * 0.5f - roadTopW * 0.5f) * (z * z);
            c.drawLine(center - half, yy, center - half + 12 * sx, yy, stroke);
            c.drawLine(center + half, yy, center + half - 12 * sx, yy, stroke);
        }

        drawObjects(c, e, horizon, floorY, roadTopW, roadBottomW, center);
        drawPlayer(c, e, floorY, roadBottomW, center);
        drawParticles(c, e);

        // HUD.
        drawPill(c, 10 * sx, 12 * sy, 92 * sx, 31 * sy, "SCORE", String.valueOf(Math.round(e.score)), 0xff78D6FF);
        drawPill(c, w - 102 * sx, 12 * sy, 92 * sx, 31 * sy, "COINS", String.valueOf(e.runCoins), 0xffFFE36E);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(12 * sx);
        p.setColor(0xCCFFFFFF);
        c.drawText(Math.round(e.distance) + "m", w * 0.5f, 27 * sy, p);
        p.setTextSize(9 * sx);
        p.setColor(0x99E3EBF6);
        c.drawText(e.save.multiplier + "x", w * 0.5f, 39 * sy, p);

        if (e.activePower != PowerType.NONE && e.powerTimer > 0f) {
            drawPowerBar(c, e);
        }
        if (e.boardActive) {
            drawBoardShield(c, e);
        }

        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(8 * sx);
        p.setColor(0x66FFFFFF);
        c.drawText("←4      2↑      8↓      6→     7 BOARD", w * 0.5f, h * 0.987f, p);
    }

    private float laneX(int lane, float y, float roadW, float center) {
        float spread = roadW / 3f;
        return center + (lane - 1) * spread;
    }

    private void drawPerspectiveLine(Canvas c, float x1, float y1, float x2, float y2) {
        c.drawLine(x1, y1, x2, y2, stroke);
    }

    private void drawObjects(Canvas c, GameEngine e, float horizon, float floorY,
                             float roadTopW, float roadBottomW, float center) {
        for (GameObject o : e.objects) {
            if (!o.active || o.z > 1.7f) continue;
            float t = Math.max(0f, Math.min(1f, 1f - o.z / 1.45f));
            float yy = horizon + (floorY - horizon) * (t * t);
            float roadW = roadTopW + (roadBottomW - roadTopW) * (t * t);
            float x = laneX(o.lane, yy, roadW, center);
            float scale = 0.27f + t * 1.75f;

            if (o.kind == GameObject.Kind.COIN) {
                p.setColor(0x22000000);
                c.drawCircle(x + 2 * sx, yy + 5 * sy, 8 * scale * sx, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xffFFE36E);
                c.drawCircle(x, yy, 7 * scale * sx, p);
                p.setColor(0xAAFFFFFF);
                c.drawCircle(x - 2 * sx * scale, yy - 2 * sy * scale, 2 * scale * sx, p);
            } else if (o.kind == GameObject.Kind.POWER) {
                int color = powerColor(o.powerType);
                p.setColor(color);
                float s = 10 * scale * sx;
                path.reset();
                path.moveTo(x, yy - s);
                path.lineTo(x + s, yy);
                path.lineTo(x, yy + s);
                path.lineTo(x - s, yy);
                path.close();
                c.drawPath(path, p);
                p.setColor(0xCCFFFFFF);
                p.setTextSize(Math.max(7, 8 * scale * sx));
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText(powerLetter(o.powerType), x, yy + 3 * sy * scale, p);
            } else {
                drawObstacle(c, o, x, yy, scale);
            }
        }
    }

    private void drawObstacle(Canvas c, GameObject o, float x, float y, float s) {
        float width = 34 * s * sx;
        float height = 42 * s * sy;
        if (o.obstacleType == ObstacleType.TRAIN) {
            r.set(x - width, y - height, x + width, y + 4 * s * sy);
            p.setColor(0xff8C93A8);
            c.drawRoundRect(r, 4 * s * sx, 4 * s * sy, p);
            p.setColor(0xff30374B);
            c.drawRoundRect(x - width + 5 * s * sx, y - height + 7 * s * sy,
                    x + width - 5 * s * sx, y - height + 21 * s * sy,
                    2 * s * sx, 2 * s * sy, p);
            p.setColor(0xff66E7FF);
            c.drawCircle(x - width + 9 * s * sx, y - 3 * s * sy, 3 * s * sx, p);
            c.drawCircle(x + width - 9 * s * sx, y - 3 * s * sy, 3 * s * sx, p);
        } else if (o.obstacleType == ObstacleType.LOW_BARRIER) {
            r.set(x - width, y - 18 * s * sy, x + width, y + 2 * s * sy);
            p.setColor(0xffFF5677);
            c.drawRoundRect(r, 2 * s * sx, 2 * s * sy, p);
            p.setColor(0x99FFFFFF);
            c.drawRect(x - width, y - 15 * s * sy, x + width, y - 11 * s * sy, p);
        } else {
            r.set(x - width * 0.8f, y - height * 0.82f, x + width * 0.8f, y - 6 * s * sy);
            p.setColor(0xffA16BFF);
            c.drawRoundRect(r, 3 * s * sx, 3 * s * sy, p);
            p.setColor(0xffFFDF5B);
            c.drawRect(x - width * 0.63f, y - height * 0.71f,
                    x + width * 0.63f, y - height * 0.62f, p);
        }
    }

    private void drawPlayer(Canvas c, GameEngine e, float floorY, float roadBottomW, float center) {
        float x = laneX(e.playerLane, floorY, roadBottomW, center);
        float jump = e.jumpTimer > 0 ? (float) Math.sin((Math.min(1f, e.jumpTimer / 0.65f)) * Math.PI) : 0f;
        float baseY = floorY - 43 * sy - jump * 70 * sy;
        float bob = (float) Math.sin(e.worldPulse * 14f) * 1.4f * sy;
        baseY += bob;

        int body = e.save.selectedCharacter == 0 ? 0xff5BE7FF : 0xffff6FAD;
        int board = e.save.selectedBoard == 0 ? 0xffB9FF4A
                : (e.save.selectedBoard == 1 ? 0xff7C8CFF : 0xffffB74D);

        if (e.boardActive) {
            p.setColor(0x4433DFFF);
            c.drawOval(x - 28 * sx, baseY + 20 * sy, x + 28 * sx, baseY + 35 * sy, p);
            p.setColor(board);
            r.set(x - 25 * sx, baseY + 16 * sy, x + 25 * sx, baseY + 22 * sy);
            c.drawRoundRect(r, 5 * sx, 5 * sy, p);
        }

        if (e.jumpTimer <= 0f && e.rollTimer <= 0f) {
            p.setColor(body);
            r.set(x - 13 * sx, baseY, x + 13 * sx, baseY + 31 * sy);
            c.drawRoundRect(r, 8 * sx, 8 * sy, p);
            p.setColor(0xffF3D2B8);
            c.drawCircle(x, baseY - 10 * sy, 11 * sx, p);
            p.setColor(0xff1B2030);
            c.drawCircle(x - 4 * sx, baseY - 11 * sy, 1.5f * sx, p);
            c.drawCircle(x + 4 * sx, baseY - 11 * sy, 1.5f * sx, p);
            p.setColor(0xff1E2334);
            c.drawRect(x - 11 * sx, baseY + 28 * sy, x - 2 * sx, baseY + 35 * sy, p);
            c.drawRect(x + 2 * sx, baseY + 28 * sy, x + 11 * sx, baseY + 35 * sy, p);
        } else if (e.rollTimer > 0f) {
            p.setColor(body);
            c.drawOval(x - 25 * sx, baseY + 3 * sy, x + 25 * sx, baseY + 18 * sy, p);
            p.setColor(0xffF3D2B8);
            c.drawCircle(x + 16 * sx, baseY + 7 * sy, 8 * sx, p);
        } else {
            p.setColor(body);
            r.set(x - 10 * sx, baseY - 15 * sy, x + 10 * sx, baseY + 17 * sy);
            c.drawRoundRect(r, 8 * sx, 8 * sy, p);
            p.setColor(0xffF3D2B8);
            c.drawCircle(x, baseY - 25 * sy, 10 * sx, p);
        }

        // Power aura.
        if (e.activePower != PowerType.NONE && e.powerTimer > 0f) {
            int color = powerColor(e.activePower);
            stroke.setColor(color);
            stroke.setStrokeWidth(2.3f * sx);
            r.set(x - 31 * sx, baseY - 44 * sy, x + 31 * sx, baseY + 44 * sy);
            c.drawOval(r, stroke);
        }
    }

    private void drawParticles(Canvas c, GameEngine e) {
        for (TrailParticle q : e.particles) {
            p.setColor(q.color);
            float alpha = Math.max(0f, q.life / q.maxLife);
            p.setAlpha((int) (alpha * 220));
            c.drawCircle(q.x * w, q.y * h, 2.5f * alpha * sx + 0.5f, p);
            p.setAlpha(255);
        }
    }

    private void drawPowerBar(Canvas c, GameEngine e) {
        float left = w * 0.28f;
        float right = w * 0.72f;
        float y = 56 * sy;
        p.setColor(0x5520293D);
        r.set(left, y, right, y + 6 * sy);
        c.drawRoundRect(r, 4, 4, p);
        float max = e.activePower == PowerType.JETPACK ? 6.3f : 6.8f;
        p.setColor(powerColor(e.activePower));
        r.set(left, y, left + (right - left) * Math.min(1f, e.powerTimer / max), y + 6 * sy);
        c.drawRoundRect(r, 4, 4, p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(7.5f * sx);
        p.setColor(0xCCFFFFFF);
        c.drawText(powerLetter(e.activePower) + "  " + Math.ceil(e.powerTimer) + "s", w * 0.5f, y - 3 * sy, p);
    }

    private void drawBoardShield(Canvas c, GameEngine e) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2 * sx);
        p.setColor(0xCCB8FF4A);
        r.set(7 * sx, 50 * sy, w - 7 * sx, 75 * sy);
        c.drawRoundRect(r, 12 * sx, 12 * sy, p);
        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(8 * sx);
        p.setColor(0xCCB8FF4A);
        c.drawText("BOARD  " + Math.ceil(e.boardTimer) + "s", w * 0.5f, 66 * sy, p);
    }

    private void drawPill(Canvas c, float x, float y, float width, float height,
                          String label, String value, int color) {
        p.setColor(0x55202B42);
        r.set(x, y, x + width, y + height);
        c.drawRoundRect(r, 11 * sx, 11 * sy, p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(7 * sx);
        p.setColor(0x99DCE7F7);
        c.drawText(label, x + width / 2, y + 10 * sy, p);
        p.setTextSize(12 * sx);
        p.setColor(color);
        c.drawText(value, x + width / 2, y + 25 * sy, p);
    }

    private void drawMenuRow(Canvas c, String text, boolean selected, float top, float itemH) {
        float left = w * 0.12f;
        float right = w * 0.88f;
        if (selected) {
            p.setColor(0xCC233C5A);
            r.set(left, top, right, top + itemH - 5 * sy);
            c.drawRoundRect(r, 12 * sx, 12 * sy, p);
            stroke.setStrokeWidth(1.2f * sx);
            stroke.setColor(0xBB66E7FF);
            c.drawRoundRect(r, 12 * sx, 12 * sy, stroke);
        }
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", selected ? Typeface.BOLD : Typeface.NORMAL));
        p.setTextSize(14 * sx);
        p.setColor(selected ? 0xffF4FBFF : 0xB8E0E8F2);
        c.drawText(text, (left + right) * 0.5f, top + itemH * 0.64f, p);
    }

    private void drawPanel(Canvas c, GameEngine e, String title, String[] items) {
        drawPanelHeader(c, title, e);
        float top = h * 0.32f;
        float itemH = h * 0.095f;
        for (int i = 0; i < items.length; i++) {
            drawCard(c, items[i], i == e.focus, top + i * itemH, itemH, i);
        }
        drawBottomHint(c, "↑↓ בחירה   •   5 קנייה/החלפה   •   0 חזרה");
    }

    private void drawPanelHeader(Canvas c, String title, GameEngine e) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(28 * sx);
        p.setColor(0xffF4F8FF);
        c.drawText(title, w * 0.5f, h * 0.14f, p);
        p.setTextSize(10 * sx);
        p.setColor(0x99C9D8EA);
        c.drawText("COINS  " + e.save.coins + "    KEYS  " + e.save.keys, w * 0.5f, h * 0.19f, p);
    }

    private void drawCard(Canvas c, String text, boolean selected, float top, float height, int index) {
        float left = w * 0.08f;
        float right = w * 0.92f;
        p.setColor(selected ? 0xCC243B5B : 0x8820293D);
        r.set(left, top, right, top + height - 8 * sy);
        c.drawRoundRect(r, 14 * sx, 14 * sy, p);
        if (selected) {
            stroke.setStrokeWidth(1.2f * sx);
            stroke.setColor(0xCC66E7FF);
            c.drawRoundRect(r, 14 * sx, 14 * sy, stroke);
        }
        p.setTextAlign(Paint.Align.LEFT);
        p.setTypeface(Typeface.create("sans", selected ? Typeface.BOLD : Typeface.NORMAL));
        p.setTextSize(12 * sx);
        p.setColor(selected ? 0xffF4F9FF : 0xC8D9E5F2);
        c.drawText(text, left + 16 * sx, top + height * 0.56f, p);

        String badge = index == 0 ? "I" : index == 1 ? "II" : index == 2 ? "III" : "IV";
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(10 * sx);
        p.setColor(0x668AE8FF);
        c.drawText(badge, right - 20 * sx, top + height * 0.56f, p);
    }

    private void drawMissions(Canvas c, GameEngine e) {
        drawPanelHeader(c, "משימות", e);
        float top = h * 0.30f;
        for (int i = 0; i < e.missions.size(); i++) {
            Mission m = e.missions.get(i);
            float y = top + i * h * 0.18f;
            p.setColor(0x88202B42);
            r.set(w * 0.08f, y, w * 0.92f, y + h * 0.135f);
            c.drawRoundRect(r, 14 * sx, 14 * sy, p);

            p.setTextAlign(Paint.Align.LEFT);
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            p.setTextSize(13 * sx);
            p.setColor(0xffF1F8FF);
            c.drawText(m.title, w * 0.13f, y + 25 * sy, p);

            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(10 * sx);
            p.setColor(0xAFCBDAE9);
            c.drawText(m.progress + " / " + m.target, w * 0.13f, y + 44 * sy, p);

            p.setColor(0x443A4B64);
            r.set(w * 0.13f, y + 53 * sy, w * 0.87f, y + 59 * sy);
            c.drawRoundRect(r, 5, 5, p);
            p.setColor(m.done() ? 0xff6BFFB2 : 0xff66E7FF);
            r.set(w * 0.13f, y + 53 * sy,
                    w * 0.13f + w * 0.74f * m.fraction(), y + 59 * sy);
            c.drawRoundRect(r, 5, 5, p);
        }
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(10 * sx);
        p.setColor(0xAACBDAE9);
        c.drawText("כל 3 משימות → מכפיל +1 (עד 30x)", w * 0.5f, h * 0.90f, p);
        drawBottomHint(c, "5 / 0  •  אישור / חזרה");
    }

    private void drawCharacters(Canvas c, GameEngine e) {
        drawPanelHeader(c, "דמויות", e);
        drawCharacterCard(c, 0, "NOVA", "FREE", e.save.selectedCharacter == 0,
                e.save.selectedCharacter == 0 ? 0xff5BE7FF : 0xff8392A8);
        drawCharacterCard(c, 1, "KAI", e.save.character2Unlocked ? "UNLOCKED" : "500 COINS",
                e.save.selectedCharacter == 1, 0xffff6FAD);
        drawBottomHint(c, "↑↓ בחירה   •   5 בחירה   •   0 חזרה");
    }

    private void drawCharacterCard(Canvas c, int index, String name, String price,
                                   boolean selected, int bodyColor) {
        float top = h * 0.30f + index * h * 0.28f;
        p.setColor(selected ? 0xCC263F60 : 0x7720293D);
        r.set(w * 0.10f, top, w * 0.90f, top + h * 0.23f);
        c.drawRoundRect(r, 18 * sx, 18 * sy, p);

        p.setColor(bodyColor);
        c.drawCircle(w * 0.25f, top + h * 0.10f, 27 * sx, p);
        p.setColor(0xffF3D2B8);
        c.drawCircle(w * 0.25f, top + h * 0.065f, 17 * sx, p);

        p.setTextAlign(Paint.Align.LEFT);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(16 * sx);
        p.setColor(0xffF3F8FF);
        c.drawText(name, w * 0.43f, top + 40 * sy, p);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(9 * sx);
        p.setColor(0xAFCBDAE9);
        c.drawText(price, w * 0.43f, top + 60 * sy, p);

        if (selected) {
            p.setTextSize(9 * sx);
            p.setColor(0xff6BFFB2);
            c.drawText("SELECTED", w * 0.43f, top + 82 * sy, p);
        }
    }

    private void drawBoards(Canvas c, GameEngine e) {
        drawPanelHeader(c, "לוחות", e);
        drawBoardCard(c, 0, "PULSE", "FREE", e.save.selectedBoard == 0, 0xffB9FF4A);
        drawBoardCard(c, 1, "VOLT", e.save.board2Unlocked ? "UNLOCKED" : "700 COINS",
                e.save.selectedBoard == 1, 0xff7C8CFF);
        drawBoardCard(c, 2, "SOLAR", e.save.board3Unlocked ? "UNLOCKED" : "1200 COINS",
                e.save.selectedBoard == 2, 0xffffB74D);
        drawBottomHint(c, "↑↓ בחירה   •   5 בחירה   •   0 חזרה");
    }

    private void drawBoardCard(Canvas c, int index, String name, String price,
                               boolean selected, int color) {
        float top = h * 0.27f + index * h * 0.20f;
        p.setColor(selected ? 0xCC263F60 : 0x7720293D);
        r.set(w * 0.09f, top, w * 0.91f, top + h * 0.155f);
        c.drawRoundRect(r, 16 * sx, 16 * sy, p);

        p.setColor(color);
        r.set(w * 0.15f, top + 36 * sy, w * 0.36f, top + 46 * sy);
        c.drawRoundRect(r, 7 * sx, 7 * sy, p);

        p.setTextAlign(Paint.Align.LEFT);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(13 * sx);
        p.setColor(0xffF4F8FF);
        c.drawText(name, w * 0.43f, top + 35 * sy, p);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(9 * sx);
        p.setColor(0xAFCBDAE9);
        c.drawText(price, w * 0.43f, top + 54 * sy, p);
        p.setTextSize(8 * sx);
        p.setColor(0x88DDEBFA);
        c.drawText("BOARD SHIELD • 8s", w * 0.43f, top + 70 * sy, p);
    }

    private void drawAchievements(Canvas c, GameEngine e) {
        drawPanelHeader(c, "הישגים", e);
        String[] names = {"Starter", "Coin Hunter", "Long Rail", "Power Rider"};
        int[] progress = {
                Math.min(1, e.save.bestScore > 0 ? 1 : 0),
                Math.min(100, e.save.coins),
                Math.min(10000, e.save.bestScore),
                e.save.missionsCompleted
        };
        for (int i = 0; i < names.length; i++) {
            float y = h * 0.29f + i * h * 0.145f;
            boolean done = progress[i] >= (i == 0 ? 1 : i == 1 ? 100 : i == 2 ? 1000 : 6);
            p.setColor(done ? 0xCC264D48 : 0x8820293D);
            r.set(w * 0.09f, y, w * 0.91f, y + h * 0.105f);
            c.drawRoundRect(r, 13 * sx, 13 * sy, p);
            p.setTextAlign(Paint.Align.LEFT);
            p.setTypeface(Typeface.create("sans", Typeface.BOLD));
            p.setTextSize(12 * sx);
            p.setColor(done ? 0xff6BFFB2 : 0xffE5EEF8);
            c.drawText((done ? "★ " : "☆ ") + names[i], w * 0.14f, y + 25 * sy, p);
            p.setTypeface(Typeface.DEFAULT);
            p.setTextSize(9 * sx);
            p.setColor(0xAFCBDAE9);
            c.drawText(String.valueOf(progress[i]), w * 0.14f, y + 45 * sy, p);
        }
        drawBottomHint(c, "5 / 0  •  חזרה לבית");
    }

    private void drawOverlayMenu(Canvas c, GameEngine e, String title, String[] items) {
        p.setColor(0x8F00030A);
        c.drawRect(0, 0, w, h, p);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(30 * sx);
        p.setColor(0xffF5FAFF);
        c.drawText(title, w * 0.5f, h * 0.28f, p);

        float top = h * 0.38f;
        float itemH = h * 0.08f;
        for (int i = 0; i < items.length; i++) {
            drawMenuRow(c, items[i], i == e.focus, top + i * itemH, itemH);
        }
        drawBottomHint(c, "↑↓ בחירה   •   5 אישור   •   0 חזרה");
    }

    private void drawGameOverStats(Canvas c, GameEngine e) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(11 * sx);
        p.setColor(0xB8E4EDF6);
        c.drawText("SCORE " + Math.round(e.score) + "   •   DIST " + Math.round(e.distance) + "m",
                w * 0.5f, h * 0.35f, p);
    }

    private void drawNotice(Canvas c, String message) {
        p.setColor(0xDD111A2A);
        r.set(w * 0.18f, h * 0.08f, w * 0.82f, h * 0.135f);
        c.drawRoundRect(r, 14 * sx, 14 * sy, p);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(10 * sx);
        p.setColor(0xffEAF7FF);
        c.drawText(message, w * 0.5f, h * 0.112f, p);
    }

    private void drawBottomHint(Canvas c, String text) {
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(9 * sx);
        p.setColor(0x85DCE8F3);
        c.drawText(text, w * 0.5f, h * 0.965f, p);
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

    private String powerLetter(PowerType type) {
        switch (type) {
            case MAGNET: return "M";
            case SNEAKERS: return "S";
            case X2: return "X2";
            case JETPACK: return "J";
            default: return "?";
        }
    }
}
