# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v26.1.1-mc26.1.x] - 2026-05-16

### Fixed

- Fix bundle capacity multiplier not being respected sometimes
- Fix items with contents blocking scrolling in screens when hovered under some circumstances

## [v26.1.0-mc26.1.x] - 2026-05-16

### Added

- Scrolling on tooltips is now supported vertically while holding any `Shift` key (configurable)
- The selected item on tooltips can now be changed using the arrow keys on your keyboard
- All items may now feature a bar representing the fill level just like vanilla bundles (disabled by default)

### Changed

- Update to Minecraft 26.1.x
- This release features a large overhaul of the mod to be more inline with the updated bundle mechanics introduced back
  in Minecraft 1.21.2
- Most notably, this changes the mouse buttons used for interactions: the right button now only inserts items, while the
  left button removes items
- The selected item tooltip now only shows the item name to be inline with the vanilla bundle ui
- The selected item slot on tooltips is no longer stored globally, but instead per item like vanilla bundles
- Bundle tooltips now mimic the vanilla tooltip design, while still featuring a glance at all items and an improved fill
  bar
- Reworked and simplified all config options
- Key-based config options now feature a `NEVER` setting for disabling them completely
- Modifier keys can now once again use `Command` on a Mac
- Greatly improved performance when rendering item contents on tooltips
- Move the data pack directory from `iteminteractions/item_container_providers` to `iteminteractions/item_storage`

### Fixed

- Fix some quirks with the single item movement, especially when combined with the mouse dragging feature
- Remove the `allow_slot_cycling` & `selected_item_tooltips` config options, as it's now always enabled to be inline
  with vanilla
- The `iteminteractions:bundle` type no longer supports defining a `background_color` property
