# PackBranding

A lightweight client-side Fabric mod designed for modpack creators to easily customize their pack's branding in Minecraft.

## Features

- **Custom Window Title** - Replace the default "Minecraft" window title
- **Menu Text** - Custom text in any of the four corners of the main menu and pause menu
- **Clickable Links** - `[label](https://…)` links in menu text, opened with a confirmation prompt
- **Icon Buttons** - Custom clickable icon buttons in the pause menu and title screen
- **Hex Color Support** - Use any color with `#RRGGBB` format
- **Minecraft Color Codes** - Standard `&c`, `&a`, `&l` formatting codes
- **Dynamic Tokens** - Insert version, username, and more automatically
- **Hide Realms Button** - Clean up the main menu
- **Custom Window Icon** - Replace the default Minecraft icon

## Installation

1. Install [Fabric Loader](https://fabricmc.net/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `packbranding-x.x.x.jar` into your `mods` folder
4. Launch the game to generate `config.json`

## Configuration

All settings live in a single file: `.minecraft/config/packbranding/config.json`.
A working example is generated on first launch.

```json
{
  "packVersion": "1.0",
  "windowTitle": {
    "enabled": true,
    "title": "Minecraft {mcversion}"
  },
  "icon": {
    "enabled": false
  },
  "hideRealms": true,
  "mainMenu": {
    "enabled": true,
    "aboveCopyright": true,
    "topLeft": "&6&lMyPack",
    "topRight": "&7v{packversion}",
    "bottomLeft": "[Website](https://example.com)",
    "bottomRight": "&8| &fMC {mcversion}"
  },
  "pauseMenu": {
    "enabled": true,
    "topLeft": "&6&lMyPack",
    "topRight": "&7v{packversion}",
    "bottomLeft": "&7Playing as &f{username}",
    "bottomRight": "&8| &fMC {mcversion}"
  },
  "menuButtons": {
    "enabled": true,
    "title": [
      { "icon": "example.png", "url": "https://example.com", "tooltip": "&9Visit our website" }
    ],
    "pause": [
      { "icon": "example.png", "url": "https://example.com", "tooltip": "&9Visit our website" }
    ],
    "hidePauseIcons": []
  }
}
```

Each menu has four independent corners (`topLeft`, `topRight`, `bottomLeft`, `bottomRight`); leave one empty to render nothing there. On the title screen, `aboveCopyright` keeps the bottom corners above the vanilla copyright/version line.

### Tokens

- `{mcversion}` - Current Minecraft version
- `{packversion}` - Your pack version from `config.json`
- `{username}` - Player's username
- `{modcount}` - Number of loaded mods
- `{modversion:modid}` - Version of a specific mod *(window title only)*

### Color Formatting

Hex colors with `#RRGGBB`, or Minecraft color codes with `&`:

| Code | Color | Code | Format |
|------|-------|------|--------|
| `&0` | Black | `&l` | **Bold** |
| `&1` | Dark Blue | `&o` | *Italic* |
| `&2` | Dark Green | `&n` | Underline |
| `&3` | Dark Aqua | `&m` | ~~Strikethrough~~ |
| `&4` | Dark Red | `&r` | Reset |
| `&5`–`&9`, `&a`–`&f` | Other colors | | |

### Links

Make part of any text a clickable link:

```
[Website](https://example.com)
```

Clicking it opens the URL after the vanilla "Open this link?" confirmation.

## Icon Buttons

1. Drop icon PNGs (16×16 recommended) into `config/packbranding/buttons/`.
2. List them under `menuButtons` in `config.json`, separately for `title` and `pause` (each entry has `icon`, `url`, `tooltip`).
3. Restart. Clicking opens the link after a confirmation. Buttons with a missing icon or invalid link are skipped.

Vanilla pause icons can be hidden via `menuButtons.hidePauseIcons` (a list of keys: `menu.reportBugs`, `menu.sendFeedback`, `menu.playerReporting`, `menu.online`), so you can replace them with your own.

An `icon` can also reference a built-in sprite instead of a PNG by using the `sprite:` prefix, e.g. `"icon": "sprite:icon/language"` (vanilla) or `"icon": "sprite:modid:path"` (another mod).

A button can set `"index"` to place it at an exact spot in the icon row (`0` = first, before the vanilla icons); omit it to append at the end. Works on both the title screen and the pause menu, and the row is re-centered automatically.

## Custom Icon

1. Set `"icon": { "enabled": true }` in `config.json`.
2. Place either:
   - `config/packbranding/icon/icon_16x16.png` and `config/packbranding/icon/icon_32x32.png`
   - or a single `config/packbranding/icon.png`
3. Use PNG format with transparency; for a single icon, 256x256 is a good default.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3+
- Fabric API

## License

Apache License 2.0 - Feel free to include in any modpack!
