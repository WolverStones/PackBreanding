# PackBranding

A lightweight client-side Fabric mod designed for modpack creators to easily customize their pack's branding in Minecraft.

## Features

- **Custom Window Title** - Replace the default "Minecraft" window title
- **Menu Branding** - Display custom text in main menu and pause menu
- **Hex Color Support** - Use any color with `#RRGGBB` format
- **Minecraft Color Codes** - Standard `&c`, `&a`, `&l` formatting codes
- **Dynamic Tokens** - Insert version, username, and more automatically
- **Hide Realms Button** - Clean up the main menu
- **Custom Window Icon** - Replace the default Minecraft icon

## Installation

1. Install [Fabric Loader](https://fabricmc.net/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `packbranding-x.x.x.jar` into your `mods` folder
4. Launch the game to generate config files

## Configuration

Config files are located in `.minecraft/config/packbranding/`

### Window Title (`windowtitle.txt`)

Simply write the title you want:

```
MyAwesomePack - Minecraft {mcversion}
```

**Available tokens:**
- `{mcversion}` - Current Minecraft version (e.g., `1.21.11`)
- `{packversion}` - Your pack version from menu.properties
- `{username}` - Player's username
- `{modcount}` - Number of loaded mods

### Menu Settings (`menu.properties`)

```properties
# PackBranding Config

# Pack version - use {packversion} token in texts
packVersion=1.0.0

# Text displayed in main menu (bottom right, above copyright)
# Leave empty to disable
customTextMainMenu=#FF5555MyPack #FFFFFFv{packversion} #AAAAAA| #55FF55MC {mcversion}

# Text displayed in pause menu (bottom right)
customTextPauseMenu=#FF5555MyPack #FFFFFFv{packversion}

# Hide Realms button from main menu
hideRealmsButton=true

# Enable custom window icon
# Place your icons at: config/packbranding/icon/icon_16x16.png and icon_32x32.png
# Or a single icon at: config/packbranding/icon.png
enableCustomIcon=false
```

## Color Formatting

### Hex Colors
Use `#RRGGBB` format directly in text:
```
#FF0000Red #00FF00Green #0000FFBlue
```

### Minecraft Color Codes
Use `&` followed by a code:

| Code | Color | Code | Format |
|------|-------|------|--------|
| `&0` | Black | `&l` | **Bold** |
| `&1` | Dark Blue | `&o` | *Italic* |
| `&2` | Dark Green | `&n` | Underline |
| `&3` | Dark Aqua | `&m` | ~~Strikethrough~~ |
| `&4` | Dark Red | `&r` | Reset |
| `&5` | Dark Purple | | |
| `&6` | Gold | | |
| `&7` | Gray | | |
| `&8` | Dark Gray | | |
| `&9` | Blue | | |
| `&a` | Green | | |
| `&b` | Aqua | | |
| `&c` | Red | | |
| `&d` | Light Purple | | |
| `&e` | Yellow | | |
| `&f` | White | | |

## Examples

### Simple pack branding:
```properties
customTextMainMenu=#55FF55CoolPack #FFFFFF1.0
```
Result: <span style="color:#55FF55">CoolPack</span> <span style="color:#FFFFFF">1.0</span>

### Detailed info:
```properties
customTextMainMenu=#FF5555MyPack #FFFFFFv{packversion} #AAAAAA| #AAFFAAMinecraft {mcversion} #AAAAAA| #FFAA00{modcount} mods
```

### Using MC color codes:
```properties
customTextMainMenu=&cMyPack &f{packversion} &7| &aMC {mcversion}
```

## Custom Icon

1. Set `enableCustomIcon=true` in menu.properties
2. Place either:
   - `config/packbranding/icon/icon_16x16.png` and `config/packbranding/icon/icon_32x32.png`
   - or a single `config/packbranding/icon.png`
3. Use PNG format with transparency; for single icon, 256x256 is a good default

## Requirements

- Minecraft 1.21.11+
- Fabric Loader 0.18.4+
- Fabric API

## License

MIT License - Feel free to include in any modpack!
