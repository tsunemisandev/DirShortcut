# DirShortcut

A desktop app to organize folder shortcuts into named groups. Double-click a shortcut to open the folder in your file explorer.

## Requirements

- Java 17 or later ([download](https://adoptium.net))

## Run

### Windows
Double-click `run.bat`, or run from a terminal:
```bat
run.bat
```

The first run will build the jar automatically. After that it starts instantly.

### macOS / Linux
```bash
./gradlew run
```

Or build and run the jar manually:
```bash
./gradlew jar
java -jar build/libs/DirShortcut.jar
```

## Features

- **Groups** — organize shortcuts into named, collapsible groups
- **Shortcuts** — paste any folder path; set an optional custom label
- **Display** — shows custom label if set, otherwise the folder name
- **Open** — double-click opens the folder in Explorer / Finder
- **Select** — single click highlights an item; only one item selected at a time
- **Search** — real-time filter across all groups with match highlighting
- **Japanese UI** — all interface text in Japanese
- **Layout** — window opens fullscreen height, pinned to the right side of the screen
- **Persistence** — data saved automatically to `~/.dirshortcut/data.json`
- **Unicode paths** — supports Japanese and other non-ASCII folder names

## Usage

1. Click **＋ グループ** to create a group
2. Click **＋ 追加** inside a group to add a folder shortcut
3. Paste the folder path (e.g. `C:\Users\you\Projects` or `/Users/you/書類`)
4. Optionally set a label; otherwise the folder name is used
5. **Single click** a shortcut to select it
6. **Double click** a shortcut to open it in the file explorer
7. Hover a shortcut to reveal **edit (✏)** and **delete (✕)** buttons
8. Use the search bar to filter shortcuts by name or path

## Build from source

Requires JDK 17+.

```bash
./gradlew jar
# Output: build/libs/DirShortcut.jar
```
