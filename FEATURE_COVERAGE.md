# Rail Rush — Coverage of the supplied 17-page research specification

The supplied document is treated as the product/gameplay reference. This project is an original from-scratch implementation: original code, title, characters, boards, and procedural visuals are used rather than copying proprietary assets.

## Coverage map

| Research topic | Implementation |
|---|---|
| 3-lane endless runner | Procedural perspective track + endless object spawning |
| Lane switch / jump / roll | D-pad, arrows and 2/4/6/8 keypad |
| Trains, barriers, tunnels, gaps, grind | Multiple obstacle types + collision rules |
| Score + multiplier | Distance score, x2 power, base multiplier up to 30x |
| Coins / Keys / event resources | Run currency, Keys, Season/Event/Character Tokens |
| Power-Ups | Magnet, Sneakers, x2, Jetpack, Pogo, Super Mysterizer, Hourglass |
| Hoverboards | Inventory, selection, board powers and one-hit save |
| Score Booster / Headstart | Inventory, shop purchase, automatic run consumption |
| Missions | 3 active missions, progress, rewards, skip for Coins, multiplier growth |
| Achievements | 8 long-term achievements with Bronze/Silver/Gold/Diamond framing |
| Word Hunt | Daily word, collectible letters, streak, rewards |
| Season Hunt | 30-tier seasonal track, token progression, 24-hour-style day gating, 21-day reset |
| Daily Events | Monday/Tuesday/Character Bonus/Friday/Weekend rotations |
| Mystery Boxes | Mini, paid, Super Box, random resources and Jackpot |
| Challenges | Mystery Hurdles, Season Challenge, Marathon, Tag Time Attack, No Floor, Lava, Showdown, Low Gravity |
| Hurdles difficulty | 1–4 keypad-selectable difficulty |
| Collections | Character/Outfit/Board collection points and rewards, gated at 7x |
| Character / Outfit separation | Separate character and outfit screens and saved selections |
| World Tour | Rotating world pool with 3-week automatic rotation and preview |
| Collaborations | Featured guest-theme event slot without copied third-party artwork |
| Profile / Top Run / Friends | Local profile, weekly score, league, Friend Tag-style identifier |
| Main-screen modules | Daily Gift, News, Events, Missions, Shop, Collections, Worlds and more |
| Save / transfer concept | Local persistent save plus human-readable Save Code |
| Shop economy | Power-up upgrades, boards, stock, boosters and unlocks |
| Key revive | 1 → 2 → 4 → 8… Key rescue flow after collision |
| Keypad-first UX | Every screen supports physical-key navigation; touch/swipe remains fallback |
| Audio / haptics | Lightweight ToneGenerator feedback and vibration |
| Small-screen optimization | Portrait Canvas UI, scrollable menu selection and high-contrast focus states |

## Notes on things intentionally not copied

The research document names the original game's characters, branded collaborations and proprietary visual identity. Those are used only as reference concepts. Rail Rush uses original characters (Nova, Kai, Zia), original boards (Pulse, Volt, Solar), and procedural artwork so the repository contains a new implementation rather than a direct asset/code copy.
