# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added
- `config.yml` with a `teleport-delay-seconds` option controlling how long `/home` waits before teleporting

### Fixed
- `/dsh forceload` now reloads player data from disk instead of overwriting it with the in-memory records
- `/dsh forcesave` now writes the home record filename index as well as the individual records, so homes created since the last shutdown survive a restart

## [1.2.0]

### Added
- `/sethome` and `/home` commands
- `/home <player>` for operators to visit another player's home
- `/dsh forcesave` and `/dsh forceload` for console administration
