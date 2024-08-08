# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v21.0.5-1.21] - 2024-08-08
### Fixed
- Fix selected item container slot occasionally failing to sync to servers on Fabric, resulting in interactions with incorrect slots

## [v21.0.4-1.21] - 2024-07-26
### Changed
- Update Puzzles Lib to v21.0.18
### Fixed
- Fix memory issues when drawing item decorations

## [v21.0.3-1.21] - 2024-07-11
### Fixed
- Fix unable to interact with shulker boxes in the inventory in survival mode

## [v21.0.2-1.21] - 2024-07-10
### Changed
- Adjust `BundleProvider` implementation to not require modifying vanilla `BundleContents`

## [v21.0.1-1.21] - 2024-07-09
### Changed
- Fully support custom bundle sizes for any item

## [v21.0.0-1.21] - 2024-07-09
- Port to Minecraft 1.21
- Forge is no longer supported in favor of NeoForge
### Changed
- Greatly simplified item contents types
- Background colors now support hex codes
