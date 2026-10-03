package com.hiya.railrush;

enum ScreenState {
    HOME, RUNNING, PAUSED, GAME_OVER,
    SHOP, MISSIONS, ACHIEVEMENTS, WORD_HUNT, SEASON_HUNT, EVENTS,
    MYSTERY_BOX, CHALLENGES, COLLECTIONS, CHARACTERS, OUTFITS, BOARDS,
    WORLD_TOUR, PROFILE, FRIENDS, NEWS, BOOSTS, DAILY_REWARDS, SETTINGS
}

enum PowerType {
    NONE, MAGNET, SNEAKERS, X2, JETPACK, POGO, MYSTERIZER, HOURGLASS
}

enum ObstacleType {
    TRAIN, LOW_BARRIER, HIGH_BARRIER, TUNNEL, GAP, GRIND
}

final class GameObject {
    enum Kind { OBSTACLE, COIN, POWER, LETTER, TOKEN }
    final Kind kind;
    final ObstacleType obstacleType;
    final PowerType powerType;
    final String payload;
    int lane;
    float z;
    float spin;
    boolean active = true;

    GameObject(Kind kind, int lane, float z, ObstacleType obstacleType,
               PowerType powerType, String payload) {
        this.kind = kind;
        this.lane = lane;
        this.z = z;
        this.obstacleType = obstacleType;
        this.powerType = powerType;
        this.payload = payload;
    }

    static GameObject obstacle(int lane, float z, ObstacleType type) {
        return new GameObject(Kind.OBSTACLE, lane, z, type, PowerType.NONE, "");
    }

    static GameObject coin(int lane, float z) {
        return new GameObject(Kind.COIN, lane, z, null, PowerType.NONE, "");
    }

    static GameObject power(int lane, float z, PowerType type) {
        return new GameObject(Kind.POWER, lane, z, null, type, "");
    }

    static GameObject letter(int lane, float z, String letter) {
        return new GameObject(Kind.LETTER, lane, z, null, PowerType.NONE, letter);
    }

    static GameObject token(int lane, float z) {
        return new GameObject(Kind.TOKEN, lane, z, null, PowerType.NONE, "");
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

    boolean done() { return progress >= target; }

    float fraction() {
        return Math.min(1f, target <= 0 ? 1f : progress / (float) target);
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
