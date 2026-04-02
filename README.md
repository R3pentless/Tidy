# Tidy

Lightweight Fabric client mod for inventory management — sort, merge, auto-refill, slot locking, and configurable GUI buttons.

## Usage Guide

1. Open your inventory or a supported container screen.
2. Press `R` to sort the player inventory section.
3. Use the side icon buttons beside the GUI to sort the player inventory, sort the open container, or open settings.
4. Middle-click a slot inside an inventory screen to sort that area quickly, similar to older inventory helper mods.
5. Press `O` to open Tidy settings in-game, or use **Mod Menu -> Tidy -> Config**.
6. Use **Move Buttons** in the config screen to drag the icon stack around a live preview, Lunar-style.
6. Hover a player inventory slot and press `F` to lock or unlock that exact slot.
7. Locked slots show a padlock overlay and cannot be moved, dropped, swapped, or sorted.
8. Make sure your mouse cursor is not holding an item stack before sorting.
9. If your selected hotbar stack runs out or a tool breaks, Tidy refills that hotbar slot from your main inventory on the next tick.

## Features

### Inventory Sort
Press **R** (rebindable) to sort your inventory.

- Works **outside and inside** handled GUIs by sorting the player inventory section
- Adds polished side icon buttons for player sorting, container sorting, and config access
- Supports per-slot locking with a padlock overlay and `F` toggle while hovering
- Stacks of the same item are merged before sorting
- Hotbar can be locked so only the main inventory (slots 9–35) is sorted
- Three built-in sort modes selectable via config:

| Mode | Description |
|------|-------------|
| `category` *(default)* | Weapons → tools → ranged → armor → blocks → food → potions → misc |
| `alpha` | Alphabetical by item ID |
| `count` | Highest stack count first |

### Auto-Refill
When the item in your selected hotbar slot runs out (blocks, food, arrows) or a tool breaks, Tidy automatically swaps in an identical item from your main inventory.

- **autoRefill** — refill tools and weapons on break
- **refillBlocks** — refill stackable items (blocks, food, arrows) when the stack is exhausted

## Keybinds

| Action | Default key | Category |
|--------|-------------|----------|
| Sort inventory | **R** | Tidy |
| Open settings | **O** | Tidy |

Rebind in **Options → Controls → Tidy**.

## Config

Config file: `.minecraft/config/tidy.json`

You can configure Tidy in three ways:

- In-game with `O`
- Through **Mod Menu**
- By editing `tidy.json` manually

```json
{
  "sortMode": "category",
  "lockHotbar": true,
  "middleClickSort": true,
  "showContainerButtons": true,
  "showPlayerSortButton": true,
  "showStorageSortButton": true,
  "showConfigButton": true,
  "buttonsLeftSide": false,
  "buttonScalePercent": 75,
  "buttonOffsetX": 0,
  "buttonOffsetY": 0,
  "buttonSpacing": 2,
  "autoRefill": true,
  "refillBlocks": true,
  "lowDurabilityRefill": true,
  "lowDurabilityThreshold": 10,
  "elytraBreakWarning": true
}
```

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `sortMode` | string | `"category"` | Sort algorithm: `category`, `alpha`, or `count` |
| `lockHotbar` | boolean | `true` | When `true`, hotbar slots (0–8) are untouched during sort |
| `middleClickSort` | boolean | `true` | Enables middle-click sorting inside handled inventory screens |
| `showContainerButtons` | boolean | `true` | Master switch for all side icon buttons beside handled inventory screens |
| `showPlayerSortButton` | boolean | `true` | Shows or hides the player inventory sort button |
| `showStorageSortButton` | boolean | `true` | Shows or hides the container sort button when supported |
| `showConfigButton` | boolean | `true` | Shows or hides the config button |
| `buttonScalePercent` | int | `75` | Scales the side buttons from compact to oversized |
| `buttonSpacing` | int | `2` | Controls the gap between side buttons |
| `autoRefill` | boolean | `true` | Refill tools/weapons when they break |
| `refillBlocks` | boolean | `true` | Refill consumable stacks (blocks, food, arrows) when exhausted |
| `lowDurabilityRefill` | boolean | `true` | Swaps damageable items before they fully break |
| `lowDurabilityThreshold` | int | `10` | Remaining durability threshold used for proactive swapping |
| `elytraBreakWarning` | boolean | `true` | Warns when Elytra is about to break and no replacement exists |

Locked slots are stored in the same config file under `lockedSlots`.

Button position is best adjusted in the in-game **Move Buttons** editor, which lets you drag the stack directly on a live preview instead of typing offsets by hand.

## Compatibility

- Minecraft **1.21.x** (Fabric)
- Requires **Fabric API**
- Client-side only — safe on any server
- Container button currently targets vanilla generic container handlers such as chests, barrels, and shulker boxes

## Localization

Built-in translations currently include:

- English (`en_us`)
- Polish (`pl_pl`)
- German (`de_de`)
- French (`fr_fr`)
- Spanish (`es_es`)
- Brazilian Portuguese (`pt_br`)
- Russian (`ru_ru`)
- Ukrainian (`uk_ua`)
- Simplified Chinese (`zh_cn`)
- Japanese (`ja_jp`)
- Korean (`ko_kr`)

## License

MIT
