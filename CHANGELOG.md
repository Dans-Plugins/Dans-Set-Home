# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Changed

- The vendored trace client is now 0.7.0, which combines repeated usage events. The first use of a command is still sent at once, but further uses of the same command within the following minute are only counted, then sent as one event carrying the tag `coalesced` with the count; any counts still open are sent when the plugin is disabled. `startup` events are never combined. Nothing else is added to what is sent
- Usage reporting now counts a `command` event only when a player runs the command; console, command-block and plugin-dispatched commands are no longer reported.

## [1.6.0] – 2026-10-28

### Fixed

- `/home <player>` without the `dsh.home.others` permission now replies "You don't have permission to teleport to other players' homes." instead of "Only operators can teleport to other players' homes.", which was wrong whenever a permissions plugin granted the node to a non-operator or revoked it from an operator
- Refusals and error replies (`You don't have a home set.`, a missing permission, `/sethome` or `/home` from the console, `/dsh` with no or an unrecognised subcommand) are no longer followed by a bare `/home`, `/sethome` or `/dsh` line. Each of these replies told the server the command had failed, and the server then printed the command's usage, which is just its name
- Homes now keep the direction the player was facing at `/sethome` after a restart or `/dsh forceload`. Only the position was written to disk, so every home read back from disk faced south and level. Each home file now ends with two more lines, the yaw and the pitch; files written by earlier versions have neither and still load, facing south as before until `/sethome` is run again
- `/dsh help` now says what `/dsh forcesave` and `/dsh forceload` do instead of "Force a save from the console." and "Force a load from the console.", which read as if the commands could not be used in game
- A negative `teleport-delay-seconds` now counts as `0`, so `/home` says "Teleporting in 0 seconds..." instead of a negative countdown such as "Teleporting in -5 seconds..."; the teleport already happened on the next tick. With a delay of `1`, `/home` now says "Teleporting in 1 second..." instead of "1 seconds"
- A home in a world that no longer exists no longer generates a new, empty world of that name on startup or `/dsh forceload`, with `/home` teleporting the player into it. A world that is not loaded is still loaded if its folder is in the server's world container; otherwise the home is not loaded, a warning naming the player and the world is logged, and `/home` replies "Your home is in the world <world>, which no longer exists." (or "That player's home is in the world <world>, which no longer exists." for `/home <player>`). The home is kept on disk until the player runs `/sethome` again, so it comes back if the world is restored

## [1.5.1] – 2026-10-16

### Fixed

- `/danssethome:home`, `/danssethome:sethome` and `/danssethome:dsh` now work. Commands were matched against the text typed rather than the command's name, so the namespaced forms, which are the only way to reach these commands on a server where another plugin also registers `/home` or `/sethome`, did nothing but print the command's name back

## [1.5.0] – 2026-10-08

### Added

- Minecraft 26.3 is now supported: it is listed in `minecraft-versions.json`, so every stable release is booted on a real 26.3 server and every build checks the plugin against the 26.3 Spigot API

### Changed

- When usage reporting is on, every event now carries a random server ID as the tag `install`, so distinct servers can be counted. The first time reporting starts, the ID is appended as a `server-id:` line (under an explanatory comment) to `plugins/trace/config.yml`; it is shared by every plugin on the server that reports this way, identifies no person, account or IP address, and deleting the line gets a new one. A server with reporting turned off by any of the existing switches never creates, reads or sends an ID. The vendored trace client is 0.5.0 for this change
- The startup notice and the default `config.yml` comment now say that the random server ID is sent; they previously said nothing about the server was sent
- The usage-reporting "Details" link (startup notice, `config.yml` and the docs) now points at https://danielstephenson.dev/usage-reporting, a public page; the previous link led to a private repository and returned 404 for everyone. The vendored trace client is now 0.6.1, which carries the same link in the `plugins/trace/config.yml` header it writes. Details: https://github.com/Stephenson-Software/trace-client-java/releases/tag/0.6.1.

## [1.4.0] – 2026-10-02

### Changed

- The vendored trace client is now 0.4.0. Every usage event now carries the plugin version, so a `command` event can be tied to a release as well as a `startup` one; previously a `command` event carried only the command name. Nothing else is added to what is sent
- `plugins/trace/config.yml` can now carry a `tags:` block whose entries are added to every usage event the plugin sends, so a test server can mark its own events (the release gates write `ci: "true"`) and be left out of the figures for real installations. Nothing changes for a server whose `plugins/trace/config.yml` has no `tags:` block.

### Fixed

- A home record that cannot be saved, loaded or migrated is now reported in the server log as a warning, naming the player and the file. Every failure in the storage code was previously swallowed behind a debug flag that could not be turned on, so a data folder that was not writable, a record left behind by the legacy-folder migration, or a corrupt record file all looked like a clean start while the affected players simply found their home missing. A partial migration now also says how many files were left in `plugins/Medieval-Set-Home/` and that they have to be moved by hand, since the migration is not retried once the current folder is in use
- `/home` run before `/sethome` now says `You don't have a home set.` instead of `Home location was null. Please contact the developer.`, and `/home <player>` for a player who has joined but never set a home says `That player doesn't have a home set.` Every player is given an empty home record when they join, so the "no home set" replies were only reached for names that had never joined

## [1.3.0] – 2026-09-19

### Changed

- Usage reporting is now disclosed on every startup: the plugin logs whether reporting is on (and what is sent, and how to turn it off) or off (and why). A `config.yml` from before the `usage-reporting` block existed is completed with the bundled values so the switch is visible on disk. Two new ways to turn reporting off: `enabled: false` in `plugins/trace/config.yml` (created on first start, shared by every plugin that reports this way) and the environment variables `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`. The vendored trace client is 0.2.0. Nothing about what is sent changed; see the README's Usage reporting section
- Player-facing messages no longer use the plugin's former name. The unrecognised `/dsh` subcommand reply, and the `/dsh forcesave` and `/dsh forceload` confirmations, now say `Dans Set Home`. The unrecognised-subcommand reply also points at `/dsh help`, matching what `/dsh` with no arguments already did
- `USER_GUIDE.md` now tells players that `/home` waits out a short delay before teleporting and that moving during it cancels the teleport

### Added

- The plugin now reports usage events — `startup` on enable, `command` on each of its commands — to the author's trace server so it is known which plugins are in use. Events carry the plugin name, the event name, and the plugin version or command name; nothing about players or the server. Reporting runs off the main thread, never delays a tick, drops silently when the server is unreachable, and is turned off with `usage-reporting.enabled: false` in `config.yml`. The default config carries the plugin's key, so reporting is active out of the box unless turned off — including on servers upgraded from a version before the `usage-reporting` block existed, whose `config.yml` is never rewritten: the plugin reads the bundled defaults for any key the file lacks
- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get danssethome --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed

- Saved homes are now kept alongside `config.yml` in `plugins/DansSetHome/`. Home records were written to `plugins/Medieval-Set-Home/`, a folder named after the plugin's former name, while `config.yml` was created in the folder Bukkit derives from the current name — so an administrator found the configuration in one directory and the saved homes in another. Records already on disk under the old name are moved into the current folder the first time the plugin starts, and nothing is moved if the current folder is already in use
- `/sethome` now explains itself when run from the console instead of returning silently, matching `/home`
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.
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
