# DirShortcut

A desktop app to organize folder shortcuts into named groups. Click a shortcut to open the folder in your file explorer.

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
- **Open** — single click opens the folder in Explorer / Finder
- **Search** — real-time filter across all groups with match highlighting
- **Persistence** — data saved automatically to `~/.dirshortcut/data.json`

## Usage

1. Click **+ Group** to create a group
2. Click **+ Add** inside a group to add a folder shortcut
3. Paste the folder path (e.g. `C:\Users\you\Projects` or `/Users/you/Documents`)
4. Optionally set a label; otherwise the folder name is used
5. Click a shortcut to open it
6. Hover a shortcut to reveal **edit (✏)** and **delete (✕)** buttons
7. Use the search bar to filter shortcuts by name or path

## Build from source

Requires JDK 17+.

```bash
./gradlew jar
# Output: build/libs/DirShortcut.jar
```
