# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v26.3.4-mc26.3.x] - 2026-10-15

### Fixed

- Fix container item contents being saved when the storage item is no longer present in the interacted slot, which
  could cause item loss or duplication

## [v26.3.3-mc26.3.x] - 2026-10-15

### Changed

- The selected item contents preview can now span multiple rows instead of always being limited to a single row

## [v26.3.2-mc26.3.x] - 2026-10-15

### Added

- Storage item tooltips now show a rich preview for the currently selected item, configurable to display the item name
  only, the first row of its contents, or all stacks of the same item type combined into a single slot
- The total number of items contained in a storage item on item tooltips (disabled by default)

### Changed

- Play a sound when navigating into and out of nested storage item tooltips

### Fixed

- Fix item interactions with an item held by the cursor not falling through to the hovered container item
- Fix missing spacing after the item name on item storage tooltips

## [v26.3.1-mc26.3.x] - 2026-09-23

### Fixed

- Fix item duplication with backpacks from some mods again (NeoForge only)

## [v26.3.0-mc26.3.x] - 2026-09-22

### Changed

- Update to Minecraft 26.3.x
