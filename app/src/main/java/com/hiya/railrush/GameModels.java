package com.hiya.railrush;

enum ScreenState {
    HOME, RUNNING, PAUSED, GAME_OVER, SHOP, MISSIONS, CHARACTERS, BOARDS, ACHIEVEMENTS, SETTINGS
}

enum PowerType {
    NONE, MAGNET, SNEAKERS, X2, JETPACK
}

enum ObstacleType {
    TRAIN, LOW_BARRIER, HIGH_BARRIER
}

final class GameObject {
    enum Kind { OBSTACLE, COIN, POWER }
    final Kind kind;
    final ObstacleType obstacleType;
    final PowerType powerType;
    int lane;
    float z;
    float spin;
    boolean active = true;

    GameObject(Kind kind, int lane, float z, ObstacleType obstacleType, PowerType powerType) {
        this.kind = kind;
        this.lane = lane;
        this.z = z;
        this.obstacleType = obstacleType;
        this.powerType = powerType;
    }
}

final class Mission {
    final String title;
    final String metric;
    final int target;
    int progress;
    boolean rewarded;

    Mission(String title, String metric, int target) {
        this.title = title;
        this.metric = metric;
        this.target = target;
    }

    boolean done() {
        return progress >= target;
    }

    float fraction() {
        return Math.min(1f, target == 0 ? 1f : progress / (float) target);
    }
}

final class TrailParticle {
    float x;
    float y;
    float vx;
    float vy;
    float life;
    float maxLife;
    int color;

    TrailParticle(float x, float y, float vx, float vy, float life, int color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.color = color;
    }
}
