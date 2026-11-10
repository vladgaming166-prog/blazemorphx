# Blaze Morphx

Premium client-side Fabric mod for **Minecraft Java 1.21.11**.

Blaze Morphx replaces the vanilla title screen with a glass PvP-client UI, plays an animated MP4 menu background, and adds a full account manager, customization center and FPS optimization dashboard.

## Features

- Custom glass main menu with Blaze Morphx branding
- Looping muted MP4 background (default underwater video + import your own)
- Microsoft device-code login and offline/cracked profiles
- Font, background, crosshair and HUD editors
- Optimization presets that write real vanilla video settings
- Live FPS / memory monitors with before/after comparison
- Keybinds: Right Shift (settings), Right Ctrl (HUD editor), F7 (stopwatch)

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) `0.141.6+1.21.11` or newer for 1.21.11
3. Drop `blazemorphx-1.0.0.jar` from `build/libs` into `.minecraft/mods`

## Build

```bash
./gradlew build
```

The remapped JAR is written to `build/libs/blazemorphx-1.0.0.jar`.

## Video playback

Minecraft does not decode MP4 natively. Blaze Morphx uses:

1. JavaCV / FFmpeg natives bundled in the JAR
2. A system `ffmpeg` CLI fallback
3. A procedural bubble fallback if decoding fails

The default video is extracted to `.minecraft/config/blazemorphx/cache/default_background.mp4` on first launch. Playback pauses when the menu is hidden and decoder resources are released on shutdown.

## Accounts

Premium login uses official Microsoft device-code OAuth. The mod never asks for a Microsoft password. Tokens are stored encrypted in `config/blazemorphx/accounts.json`.

Account switching replaces the client `Session`. Fully reconnecting some Minecraft services (realms keys, some launcher-only state) still requires a game restart. Offline profiles only work on offline-mode servers.

## Settings location

`config/blazemorphx/`

- `config.json` — UI, HUD, crosshair, optimization flags
- `accounts.json` — encrypted account list
- `backgrounds/` — imported videos and images
- `fonts/` — imported TTF/OTF files
- `profiles/` — configuration profiles
