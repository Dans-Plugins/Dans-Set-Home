# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Changed

- Player-facing messages no longer use the plugin's former name. The unrecognised `/dsh` subcommand reply, and the `/dsh forcesave` and `/dsh forceload` confirmations, now say `Dans Set Home`. The unrecognised-subcommand reply also points at `/dsh help`, matching what `/dsh` with no arguments already did
- `USER_GUIDE.md` now tells players that `/home` waits out a short delay before teleporting and that moving during it cancels the teleport

### Fixed

- Saved homes are now kept alongside `config.yml` in `plugins/DansSetHome/`. Home records were written to `plugins/Medieval-Set-Home/`, a folder named after the plugin's former name, while `config.yml` was created in the folder Bukkit derives from the current name — so an administrator found the configuration in one directory and the saved homes in another. Records already on disk under the old name are moved into the current folder the first time the plugin starts, and nothing is moved if the current folder is already in use
- `/sethome` now explains itself when run from the console instead of returning silently, matching `/home`
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.

### Added

- The plugin now reports usage events — `startup` on enable, `command` on each of its commands — to the author's trace server so it is known which plugins are in use. Events carry the plugin name, the event name, and the plugin version or command name; nothing about players or the server. Reporting runs off the main thread, never delays a tick, drops silently when the server is unreachable, and is turned off with `usage-reporting.enabled: false` in `config.yml`. The default config carries the plugin's key, so reporting is active out of the box unless turned off — including on servers upgraded from a version before the `usage-reporting` block existed, whose `config.yml` is never rewritten: the plugin reads the bundled defaults for any key the file lacks

- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get danssethome --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed

- `/sethome` now works for players who were already online when the plugin was enabled. A home record is created on demand when one is missing, instead of the command failing without any message
- Home locations with a coordinate of exactly `0` are no longer discarded when loaded from disk. A home set on the `x = 0` or `z = 0` axis survives a restart instead of coming back as `Home location was null`

## [2.0.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- Dans-Set-Home is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The major version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `2.0.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

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
