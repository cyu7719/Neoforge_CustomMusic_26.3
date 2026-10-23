# Custom Music Mod for NeoForge 1.21.1

This mod adds a **Jukebox Album** block that can store and play multiple music discs with advanced playback features.

## Features

### Jukebox Album Block
- **Double chest GUI** (54 slots per page)
- **Only accepts music discs** (vanilla and custom)
- **Pagination system** - Start with 1 page, upgrade with diamonds to add more pages
- **Music playback controls**:
  - Play/Pause button
  - Next/Previous song buttons (Spotify-like behavior)
  - Click on any disc to play it directly
  - Auto-play next song when current song finishes
- **Visual feedback**:
  - Green highlight on currently playing disc
  - Red highlight on paused disc
  - Page counter (e.g., "1/3")
- **Item retention** - Items stay in the block when broken
- **Music stops** when block is broken

### Crafting Recipes
1. **Jukebox Album**: 9 Jukeboxes in a 3x3 grid
2. **Upgrade (Add Page)**: Place Jukebox Album in center, surround with 8 Diamonds

### Custom Music Disc
- Includes a custom music disc item with custom texture and sound support

## Setup Instructions

### 1. Build the Mod
```bash
gradlew build
```

### 2. Add Your Custom Textures

Place your textures in the following locations:

#### Block Texture
- **Path**: `src/main/resources/assets/custommusic/textures/block/jukebox_album.png`
- **Size**: 16x16 pixels (or higher resolution multiples like 32x32, 64x64)
- **Format**: PNG

#### Custom Music Disc Texture
- **Path**: `src/main/resources/assets/custommusic/textures/item/custom_music_disc.png`
- **Size**: 16x16 pixels (or higher resolution multiples)
- **Format**: PNG

#### GUI Texture (Optional - uses default if not provided)
- **Path**: `src/main/resources/assets/custommusic/textures/gui/jukebox_album.png`
- **Size**: 256x256 pixels
- **Format**: PNG
- **Note**: If you want a custom GUI background, create this texture. Otherwise, it will use the default container texture.

### 3. Add Your Custom Music

Place your music file in:
- **Path**: `src/main/resources/assets/custommusic/sounds/music/custom_music_disc.ogg`
- **Format**: OGG Vorbis (required by Minecraft)
- **Note**: You can convert MP3/WAV to OGG using tools like Audacity or online converters

### 4. Replace Vanilla Music Disc Textures (Optional)

To replace vanilla music disc textures, create a resource pack or add textures to:
- `src/main/resources/assets/minecraft/textures/item/music_disc_*.png`

Replace `*` with the disc name (e.g., `music_disc_13.png`, `music_disc_cat.png`, etc.)

## How to Use

1. **Craft a Jukebox Album** using 9 Jukeboxes
2. **Place the block** in your world
3. **Right-click** to open the GUI
4. **Insert music discs** into any of the 54 slots
5. **Click the Play button** (▶) to start playing from the first disc on the current page
6. **Use controls**:
   - **Play/Pause**: Toggle music playback
   - **<< (Previous)**: Restart current song or go to previous song
   - **>> (Next)**: Skip to next song
   - **< (Previous Page)**: Go to previous page
   - **> (Next Page)**: Go to next page
   - **Click on a disc**: Play that specific disc
7. **Upgrade**: Surround with 8 diamonds in crafting table to add another page

## File Structure

```
CustomMusic/
├── src/main/
│   ├── java/com/custommusic/
│   │   ├── CustomMusicMod.java
│   │   ├── block/JukeboxAlbumBlock.java
│   │   ├── blockentity/JukeboxAlbumBlockEntity.java
│   │   ├── client/
│   │   │   ├── ClientSetup.java
│   │   │   └── screen/JukeboxAlbumScreen.java
│   │   ├── init/
│   │   │   ├── ModBlocks.java
│   │   │   ├── ModBlockEntities.java
│   │   │   ├── ModItems.java
│   │   │   ├── ModMenuTypes.java
│   │   │   └── ModSounds.java
│   │   ├── item/CustomMusicDiscItem.java
│   │   ├── menu/JukeboxAlbumMenu.java
│   │   └── network/
│   │       ├── ButtonClickPayload.java
│   │       ├── MusicStatePayload.java
│   │       └── NetworkHandler.java
│   └── resources/
│       ├── assets/custommusic/
│       │   ├── blockstates/jukebox_album.json
│       │   ├── lang/en_us.json
│       │   ├── models/
│       │   │   ├── block/jukebox_album.json
│       │   │   └── item/
│       │   │       ├── jukebox_album.json
│       │   │       └── custom_music_disc.json
│       │   ├── sounds.json
│       │   ├── textures/
│       │   │   ├── block/
│       │   │   │   └── [ADD YOUR TEXTURE HERE] jukebox_album.png
│       │   │   ├── item/
│       │   │   │   └── [ADD YOUR TEXTURE HERE] custom_music_disc.png
│       │   │   └── gui/
│       │   │       └── [OPTIONAL] jukebox_album.png
│       │   └── sounds/music/
│       │       └── [ADD YOUR SOUND HERE] custom_music_disc.ogg
│       ├── data/custommusic/recipe/
│       │   ├── jukebox_album.json
│       │   └── jukebox_album_upgrade.json
│       └── META-INF/neoforge.mods.toml
├── build.gradle
├── gradle.properties
└── settings.gradle
```

## Technical Details

- **Mod ID**: custommusic
- **Minecraft Version**: 1.21.1
- **NeoForge Version**: 21.1.72
- **Java Version**: 21

## Notes

- The upgrade recipe currently uses a simplified approach. Each time you craft with diamonds, it adds one page.
- Music playback uses Minecraft's native sound system
- The GUI shows 54 slots (6 rows × 9 columns) per page
- Items are stored in NBT data and persist when the block is broken

## License

MIT License
