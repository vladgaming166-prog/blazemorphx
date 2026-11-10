# Blaze Morphx

Lightweight client-side Fabric mod for **Minecraft Java 1.21.11**.

Vanilla Minecraft buttons, a customizable crosshair, real video-settings presets, a looping menu background, and a working multi-account manager.

## Download

1. Open the [Actions tab](https://github.com/vladgaming166-prog/blazemorphx/actions).
2. Open the latest green **Build** run.
3. Download **blazemorphx-1.1.0**.
4. Put `blazemorphx-1.1.0.jar` in `.minecraft/mods`.
5. Delete older `blazemorphx-1.0.x` jars.

You need Minecraft **1.21.11**, Fabric Loader **0.19.x or 0.20.x**, Fabric API `0.141.6+1.21.11`, and Java 21.

## Session switching

Blaze Morphx no longer overwrites the launcher session on startup. That was the invalid-session bug.

Switching a Microsoft account in the menu:

- Refreshes the access token off the render thread
- Replaces `Session`, `UserApiService`, `ProfileKeys`, `SocialInteractionsManager` and `AbuseReportContext`
- Restores the previous session if Minecraft rejects the new token

You do **not** need to restart Minecraft to switch between saved Microsoft accounts in the menu. If you are already on a multiplayer server, disconnect and rejoin so the server sees the new identity. Realms checkers created at launch may still need a restart.

Offline profiles never join online-mode servers. That is Minecraft's authentication, not a Blaze Morphx limitation.

## Features

- Vanilla-style main menu: Singleplayer, Multiplayer, Accounts, Customization, Optimization, Settings, Quit
- Looping menu background (bundled underwater frames, optional live MP4 via system `ffmpeg`)
- Video decoding stops when you leave the menu or enter a world
- Microsoft device-code login (no password prompt) and offline profiles
- Crosshair editor with live preview, presets, RGB, outline, T-shape, opacity
- Optimization presets that write real vanilla options, with Restore Previous Settings
- HUD modules that only render in gameplay
