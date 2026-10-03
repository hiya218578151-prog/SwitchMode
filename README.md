# Rail Rush

**Rail Rush** is an original Android keypad-first endless runner built from scratch from the supplied 17-page research specification.

The reference document describes a three-lane runner with lane switching, jumping, rolling, trains/obstacles, coins, powers, boards, missions, achievements, Word Hunt, Season Hunt, Daily Events, Mystery Boxes, Challenges, Collections, World Tour, Profile/Friends, Shop and progression systems. fileciteturn0file0L13-L18

## What is implemented

- Three-lane endless runner with procedural perspective rendering.
- Physical keypad + D-pad controls: 2/4/6/8, 5, 7, 0.
- Touch/swipe fallback for ordinary phones.
- Trains, low/high barriers, tunnels, gaps and grind obstacles.
- Coins, Keys, Season Tokens, Event Tokens and Character Tokens.
- Magnet, Super Sneakers, Score x2, Jetpack, Pogo, Super Mysterizer and Hourglass.
- Hoverboard inventory with one-hit protection and upgrades.
- Score Boosters and Headstarts.
- Missions with progress, rewards, skip option and multiplier progression.
- Base multiplier up to 30x; completed mission sets grant Super Boxes after the cap.
- Achievement tracking with four-rank framing.
- Daily Word Hunt with letters, streak and reward claim.
- 30-tier Season Hunt with daily-style gating and 21-day season reset.
- Rotating Daily Events and weekly reward.
- Mini / Mystery / Super Mystery Boxes and Jackpot reward path.
- Eight challenge modes, including four-level Mystery Hurdles and Low Gravity.
- Collections for characters, outfits and boards.
- Separate character / outfit systems.
- Rotating World Tour with a three-week world cycle.
- Local Profile / weekly Top Run / league / Friend Tag style identifier.
- News and featured event slots.
- Local persistent save system plus human-readable Save Code.
- Audio beeps and vibration feedback.
- Android 4.4.4 compatible minimum SDK target (API 19).

## Project structure

- `GameModels.java` — gameplay entities and states.
- `GameEngine.java` — gameplay, progression, challenges, economy and input.
- `GameRenderer.java` — all Canvas UI, world rendering and HUD.
- `SaveManager.java` — persistent local progression.
- `GameView.java` — frame loop and keypad/touch routing.
- `FEATURE_COVERAGE.md` — mapping from the research specification to implementation.
- `.github/workflows/android-build.yml` — debug APK CI build.

## Original implementation

The project does **not** copy proprietary characters, artwork, code or audio from the reference game. It uses original Rail Rush branding, characters, boards and procedural visuals while implementing the gameplay concepts described in the research.

## Android Studio

Open the repository as a Gradle Android project. The app module is `app`, uses Java 8 source compatibility, minimum API 19, and targets API 28.

The repository includes GitHub Actions for `assembleDebug`. This environment cannot directly execute the Android SDK build, so the final APK build should be confirmed by Android Studio or the repository's Actions runner.
