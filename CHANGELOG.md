# Changelog

## 1.2.1
- Fixed the title screen icon row not re-centering around icons added by other mods (e.g. ukulib's config button), which left a foreign icon sticking out of the row.
- The pause menu icon row now applies our buttons after other mods have added theirs, so the row stays correctly ordered and centered.

## 1.2.0
- Updated for Minecraft 26.2.
- Added custom icon buttons to the pause menu and title screen. Each button has its own icon, link and tooltip, and opens the link via the vanilla confirmation prompt.
- Icon buttons can be configured separately for the title screen and the pause menu, and vanilla pause icons (bug report, feedback, player reporting, …) can be hidden (the row reflows with no gap) and replaced with your own.
- Button icons can be a PNG file or a built-in sprite via `"icon": "sprite:..."`.
- Buttons can set an `index` to position them anywhere in the icon row (even before the vanilla icons), on both the title and pause screens.
- Requires exactly Minecraft 26.2.
- Menu text can now be placed in any of the four corners, with a separate text per corner.
- Added clickable links in menu text using `[label](https://...)`, opened via the vanilla confirmation prompt.
- Menu text and the hidden Realms button now use the Fabric Screen API instead of mixins, for better stability across game updates.
- **New config format:** all settings now live in a single `config/packbranding/config.json` (replacing `menu.properties` and `windowtitle.txt`).

> ⚠️ The config format changed. Delete your old `menu.properties` / `windowtitle.txt`; a new `config.json` with examples is created on first launch.
