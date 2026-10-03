# Rail Rush — implementation notes

This repository is a from-scratch implementation inspired by the supplied research specification for a three-lane mobile endless runner. The reference describes lane switching, jumping, rolling, trains/obstacles, coins, power-ups, boards, missions, score multiplier progression, achievements, shop/economy, and changing worlds. See the supplied file for the source research.

## Original implementation choices

- Original title: Rail Rush.
- Original characters: Nova and Kai.
- Original boards: Pulse, Volt, Solar.
- Procedural Canvas rendering: no proprietary art assets are bundled.
- Small-screen portrait layout.
- Keypad-first input with touch/swipe fallback.
- Android minimum API 19, covering Android 4.4.4.
- Local SharedPreferences save system.
- Four procedural visual world themes.

## Keypad

- D-pad left/right or 4/6: change lanes.
- D-pad up or 2: jump.
- D-pad down or 8: roll.
- Center/Enter/5: start, confirm, or activate board while running.
- 7: activate board during a run.
- 0 / Back: pause or go home.
- Menu shortcuts: 1 characters, 3 shop, 4 missions, 6 achievements, 7 boards, 9 settings.

## Gameplay loop

Objects are spawned ahead on a perspective track and move toward the player. The run accelerates gradually. Obstacles require lane changes, jumping, or rolling. Coins increase score and the local run total. Power-ups temporarily modify the player. A board supplies one-hit protection for a limited time.

## Progression

Three active missions persist locally. Completing all three increases the base score multiplier up to 30x and grants a bonus. Shops upgrade the power-up durations, add boards, and unlock original characters/boards. Best score, currency, inventory, selections, settings, and mission progress are saved locally.

## Next expansion points

The code is intentionally separated into model, engine, renderer, and persistence layers so additional world packs, event calendars, collections, online profiles, audio assets, and richer obstacle patterns can be added without rewriting the input model.
