# PackBranding

A simple client-side mod for modpack creators to customize their pack's branding.

## Features

- **Custom Window Title** - Change the game window title with dynamic tokens
- **Menu Text** - Add custom text to main menu and pause menu (bottom right corner)
- **Hex Colors** - Full support for `#RRGGBB` hex colors in text
- **Color Codes** - Standard Minecraft color codes (`&c`, `&a`, `&l`, etc.)
- **Dynamic Tokens** - Use `{mcversion}`, `{packversion}`, `{username}`, `{modcount}`
- **Hide Realms** - Option to hide the Realms button from main menu
- **Custom Icon** - Replace the window icon with your own

## Configuration

All config files are in `.minecraft/config/packbranding/`

### windowtitle.txt
```
MyModpack {packversion} - Minecraft {mcversion}
```

### menu.properties
```properties
# Pack version - use {packversion} token in texts
packVersion=1.0

# Text displayed in main menu (bottom right, above copyright)
# Colors: #RRGGBB (hex), &c red, &a green, &e yellow, &f white
# Formatting: &l bold, &o italic, &n underline
# Tokens: {mcversion}, {packversion}, {username}, {modcount}
customTextMainMenu=#FF5555MyPack #FFFFFF{packversion} #AAAAAA| MC {mcversion}

# Text displayed in pause menu (bottom right)
customTextPauseMenu=#FF5555MyPack #FFFFFF{packversion}

# Hide Realms button (true/false)
hideRealmsButton=true

# Enable custom window icon (true/false)
# Icon must be at: config/packbranding/icon.png
enableCustomIcon=false
```

## Color Examples

| Code | Color |
|------|-------|
| `#FF5555` | Red |
| `#55FF55` | Green |
| `#5555FF` | Blue |
| `#FFAA00` | Orange |
| `#FFFFFF` | White |
| `#AAAAAA` | Gray |
| `&c` | Red |
| `&a` | Green |
| `&e` | Yellow |
| `&l` | Bold |
| `&o` | Italic |

## Requirements

- Minecraft 1.21.11+
- Fabric Loader 0.18.4+
- Fabric API
