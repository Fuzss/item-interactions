# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v20.4.4-1.20.4] - 2024-03-17
### Changed
- Rewrite ender chest syncing logic to allow for properly supporting modified ender chest sizes

## [v20.4.3-1.20.4] - 2024-03-16
### Changed
- Empty item container providers no longer show a tooltip
- Some internal refactors
### Fixed
- Fix dynamic ender chest size support from last release

## [v20.4.2-1.20.4] - 2024-03-16
### Added
- Add `iteminteractions:none` for preventing an item that would otherwise support item interactions from being able to do so
### Changed
- Allow for dynamically supporting larger ender chest sizes if modified by another mod like [Carpet](https://github.com/gnembon/fabric-carpet)
### Fixed
- Fix occasional crash when opening ender chest inventory menu

## [v20.4.1-1.20.4] - 2024-03-04
### Fixed
- Fix data pack values failing to synchronize to clients during lan play

## [v20.4.0-1.20.4] - 2024-02-14
- Port to Minecraft 1.20.4
- Port to NeoForge
