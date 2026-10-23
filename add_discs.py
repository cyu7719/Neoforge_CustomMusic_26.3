import json
import os
import re
from pathlib import Path

ROOT = Path(__file__).parent

NEW_SONGS = [
    # name, seconds, ticks (if different from seconds*20), optional description, optional comparator
    ("dancing_in_the_starlit_night", 184, 3682, "Dancing in the Starlit Night - MAJORS"),
]

# Comparator numbers for the new songs (1-15). When None, the default below is used.
# You can override per song by adding a 5th tuple element, e.g.
# ("sawadika", 184, 3673, "SaWaDiKa - LISA", 15)
DEFAULT_COMPARATOR = 15

CREDIT = "Coba"


def title_case(name):
    return " ".join(w.capitalize() for w in name.split("_"))


def _sort_key(block):
    m = re.search(r'MUSIC_DISC_([A-Z0-9_]+)', block)
    if m:
        return m.group(1)
    m = re.search(r'([A-Z0-9_]+)_KEY', block)
    if m:
        return m.group(1)
    return ''


def _ident_key(block):
    """Return the full identifier used to detect duplicates."""
    m = re.search(r'(MUSIC_DISC_[A-Z0-9_]+)', block)
    if m:
        return m.group(1)
    m = re.search(r'([A-Z0-9_]+_KEY)', block)
    if m:
        return m.group(1)
    return ''


def _already_registered(text, block):
    key = _ident_key(block)
    if not key:
        return False
    # Match the identifier only when it is not part of a larger identifier.
    return re.search(r'(?<![A-Za-z0-9_])' + re.escape(key) + r'(?![A-Za-z0-9_])', text) is not None


def _block_indent(block):
    m = re.match(r'^(\s*)', block)
    return m.group(1) if m else '    '


def _split_section(section_text, block_sep):
    if block_sep == '\n\n':
        parts = re.split(r'\n\s*\n', section_text)
    else:
        parts = section_text.split(block_sep)
    return [b for b in parts if b.strip()]


def update_java_file(path, credit, new_blocks, anchor_pattern, block_sep='\n'):
    if not new_blocks:
        return

    text = path.read_text()
    missing = [b for b in new_blocks if not _already_registered(text, b)]
    if not missing:
        return

    indent = _block_indent(missing[0])
    header = f'{indent}//{credit}'
    header_exists = any(line.strip() == f'//{credit}' for line in text.splitlines())

    if header_exists:
        lines = text.splitlines()
        header_index = next(i for i, line in enumerate(lines) if line.strip() == f'//{credit}')

        # Find the end of this credit's section (next credit comment or anchor)
        section_end = len(lines)
        for i in range(header_index + 1, len(lines)):
            if lines[i].strip().startswith('//') or re.search(anchor_pattern, lines[i]):
                section_end = i
                break

        section_text = '\n'.join(lines[header_index + 1:section_end]).strip('\n')
        current_blocks = _split_section(section_text, block_sep)

        all_blocks = current_blocks + missing
        all_blocks.sort(key=_sort_key)

        section = header + '\n' + block_sep.join(all_blocks)
        result_lines = lines[:header_index] + [section] + lines[section_end:]
        path.write_text('\n'.join(result_lines) + '\n')
    else:
        missing.sort(key=_sort_key)
        section = header + '\n' + block_sep.join(missing)
        new_text, count = re.subn(anchor_pattern, section + r'\n\n\1', text, count=1, flags=re.MULTILINE)
        if count == 0:
            new_text = re.sub(r'^(}\s*)$', section + r'\n\n\1', text, flags=re.MULTILINE)
        path.write_text(new_text)


# Parse NEW_SONGS into a normalized list
parsed_songs = []
for entry in NEW_SONGS:
    name = entry[0]
    seconds = entry[1]
    ticks = entry[2]

    # Flexible tuple length:
    # 3: (name, seconds, ticks)
    # 4: (name, seconds, ticks, description) or (name, seconds, ticks, comparator)
    # 5: (name, seconds, ticks, description, comparator)
    if len(entry) == 3:
        description = title_case(name)
        comparator = None
    elif len(entry) == 4:
        if isinstance(entry[3], int):
            description = title_case(name)
            comparator = entry[3]
        else:
            description = entry[3]
            comparator = None
    elif len(entry) == 5:
        description = entry[3]
        comparator = entry[4]
    else:
        raise ValueError(f"NEW_SONGS entry for {name!r} must have 3 to 5 elements: {entry}")

    if comparator is None:
        comparator = DEFAULT_COMPARATOR

    parsed_songs.append((name, seconds, ticks, description, comparator))


comparators = {name: comp for name, _, _, _, comp in parsed_songs}


items_insert = []
for name, seconds, ticks, description, comparator in parsed_songs:
    up = name.upper()
    items_insert.append(
        f'    public static final DeferredItem<CustomMusicDiscItem> MUSIC_DISC_{up} = disc("music_disc_{name}", {comparators[name]}, ModSounds.MUSIC_DISC_{up}, {ticks}, ModJukeboxSongs.{up}_KEY);'
    )

keys_insert = []
for name, _, _, _, _ in parsed_songs:
    up = name.upper()
    keys_insert.append(f'    public static final ResourceKey<JukeboxSong> {up}_KEY = createKey("{name}");')

sounds_insert = []
for name, _, _, _, _ in parsed_songs:
    up = name.upper()
    sounds_insert.append(
        f'''    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_{up} = SOUND_EVENTS.register("music_disc_{name}",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(CustomMusicMod.MODID, "music_disc_{name}")));'''
    )

update_java_file(
    ROOT / "src/main/java/com/custommusic/init/ModItems.java",
    CREDIT, items_insert, r'^(}\s*)$', '\n'
)

update_java_file(
    ROOT / "src/main/java/com/custommusic/init/ModJukeboxSongs.java",
    CREDIT, keys_insert, r'^(\s*private static ResourceKey<JukeboxSong> createKey)', '\n'
)

update_java_file(
    ROOT / "src/main/java/com/custommusic/init/ModSounds.java",
    CREDIT, sounds_insert, r'^(}\s*)$', '\n\n'
)

# ----------------------------------------------------------------------------
# sounds.json
# ----------------------------------------------------------------------------
sounds_path = ROOT / "src/main/resources/assets/custommusic/sounds.json"
if sounds_path.exists():
    sounds = json.loads(sounds_path.read_text())
else:
    sounds = {}
for name, _, _, _, _ in parsed_songs:
    key = f"music_disc_{name}"
    if key not in sounds:
        sounds[key] = {"sounds": [{"name": f"custommusic:music/{name}", "stream": True}]}
with sounds_path.open("w") as f:
    json.dump(sounds, f, indent=2)
    f.write("\n")

# ----------------------------------------------------------------------------
# en_us.json
# ----------------------------------------------------------------------------
lang_path = ROOT / "src/main/resources/assets/custommusic/lang/en_us.json"
if lang_path.exists():
    lang = json.loads(lang_path.read_text())
else:
    lang = {}
for name, _, _, _, _ in parsed_songs:
    key = f"item.custommusic.music_disc_{name}"
    lang[key] = "Music Disc"
    lang[f"{key}.credit"] = CREDIT
with lang_path.open("w") as f:
    json.dump(lang, f, indent=2)
    f.write("\n")

# ----------------------------------------------------------------------------
# music_discs.json tag
# ----------------------------------------------------------------------------
tag_path = ROOT / "src/main/resources/data/minecraft/tags/item/music_discs.json"
if tag_path.exists():
    tag = json.loads(tag_path.read_text())
else:
    tag = {"values": []}
for name, _, _, _, _ in parsed_songs:
    entry = f"custommusic:music_disc_{name}"
    if entry not in tag["values"]:
        tag["values"].append(entry)
with tag_path.open("w") as f:
    json.dump(tag, f, indent=2)
    f.write("\n")

# ----------------------------------------------------------------------------
# loot modifiers
# ----------------------------------------------------------------------------
loot_dir = ROOT / "src/main/resources/data/custommusic/loot_modifiers"
if loot_dir.exists():
    for path in loot_dir.glob("random_music_disc_*.json"):
        data = json.loads(path.read_text())
        items = data.get("items", [])
        for name, _, _, _, _ in parsed_songs:
            entry = f"custommusic:music_disc_{name}"
            if entry not in items:
                items.append(entry)
        data["items"] = items
        with path.open("w") as f:
            json.dump(data, f, indent=2)
            f.write("\n")

# ----------------------------------------------------------------------------
# jukebox_song datapack files
# ----------------------------------------------------------------------------
jukebox_dir = ROOT / "src/main/resources/data/custommusic/jukebox_song"
jukebox_dir.mkdir(parents=True, exist_ok=True)
for name, seconds, ticks, description, comparator in parsed_songs:
    path = jukebox_dir / f"{name}.json"
    song = {
        "sound_event": f"custommusic:music_disc_{name}",
        "description": description,
        "length_in_seconds": seconds,
        "comparator_output": comparator
    }
    with path.open("w") as f:
        json.dump(song, f, indent=2)
        f.write("\n")

# ----------------------------------------------------------------------------
# item model JSONs
# ----------------------------------------------------------------------------
models_dir = ROOT / "src/main/resources/assets/custommusic/models/item"
models_dir.mkdir(parents=True, exist_ok=True)
for name, _, _, _, _ in parsed_songs:
    model = {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"custommusic:item/music_disc_{name}"},
    }
    with (models_dir / f"music_disc_{name}.json").open("w") as f:
        json.dump(model, f, indent=2)
        f.write("\n")

# ----------------------------------------------------------------------------
# item definition JSONs (1.20.5+ item model registry)
# ----------------------------------------------------------------------------
items_dir = ROOT / "src/main/resources/assets/custommusic/items"
items_dir.mkdir(parents=True, exist_ok=True)
for name, _, _, _, _ in parsed_songs:
    item_def = {
        "model": {
            "type": "minecraft:model",
            "model": f"custommusic:item/music_disc_{name}",
        }
    }
    with (items_dir / f"music_disc_{name}.json").open("w") as f:
        json.dump(item_def, f)
        f.write("\n")

print("Done")
