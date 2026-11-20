# Dans Set Home Configuration

Dans Set Home creates a `config.yml` in `plugins/DansSetHome/` on first run. Saved homes are kept in
the same folder: one `<player>.txt` file per player, listed in `home-record-filenames.txt`. Homes are
written to disk when the server stops (or the plugin is disabled) and when `/dsh forcesave` is run,
not each time `/sethome` is used, so a crash loses homes set since the last save. A home file that
cannot be read or parsed is reported in the server log and not loaded, but is kept: it can be repaired
by hand and picked up on the next start or `/dsh forceload`, until the player runs `/sethome`, which
replaces it. Servers upgrading from a version that stored homes in `plugins/Medieval-Set-Home/` have
those files moved across automatically the first time the plugin starts.

Homes are read on every start and on `/dsh forceload`, and reading a home looks up its world. A
world that is not loaded but whose folder is still in the server's world container is loaded at that
point, even if the server or a world-management plugin keeps it unloaded on purpose (see
[#131](https://github.com/Dans-Plugins/Dans-Set-Home/issues/131)). A home whose world has no folder
there is reported in the server log, naming the player and the world, and is not loaded. No new world
is generated under that name. The home is kept on disk until the player runs `/sethome`, so it loads
again if the world is restored. Until then, `/home` says the world no longer exists.

| Key | Default | Description |
|-----|---------|-------------|
| `teleport-delay-seconds` | `3` | Number of seconds a player must stand still after running `/home` before being teleported. `0` teleports on the next tick; a negative value counts as `0`. |
| `usage-reporting.enabled` | `true` | Whether the plugin reports usage events (see below). Set to `false` to turn it off. Only `true`, `on` or `yes` turns it on; any other value, including a quoted `"false"`, turns it off, and the plugin never writes the switch back on once it is off in the file. Other spellings of the block or the switch (`usage_reporting:`, `usageReporting:`, `enable: false`, `disabled: true`), or `usage-reporting: false`, also count as off. |
| `usage-reporting.endpoint` | `https://trace.danielstephenson.dev` | The trace server events are sent to. |
| `usage-reporting.key` | the plugin's key | Identifies this plugin to the trace server so reports are attributed to it. Not a secret: it ships in the default config and can only report as DansSetHome. Empty means reporting is off regardless of `enabled`. |

## Usage reporting

When the plugin is enabled, and each time a player uses one of its commands, a small event is sent to the
author's [trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which
plugins are actually in use. An event carries the plugin's name, the event name (`startup` or
`command`), the plugin version, and for a command the command name, plus a random server ID (the
`server-id` line in `plugins/trace/config.yml`, which identifies no person, account or IP address;
deleting the line gets a new one) — nothing about players or the world. Repeats of the same command
within a minute are counted rather than sent one by one: when the minute is up they are sent as a single
event with the tag `coalesced` holding the count, and any counts still open are sent when the plugin is
disabled. The `startup` event is always sent at once. Sending happens off the main
thread, never delays a tick, and is dropped silently if the
server cannot be reached. Set `usage-reporting.enabled` to `false` to turn it off; the plugin says
on every startup whether reporting is on, and why not when it is off.

A `config.yml` written by a version before the block existed is completed with the bundled
`usage-reporting` values the next time the plugin starts, so the switch is always on disk. A
`config.yml` with a YAML error in it is never overwritten; the console says so, and reporting stays
off until the error is fixed. Two
further switches win over this file: `enabled: false` in `plugins/trace/config.yml` (created the
first time a plugin that reports this way starts) turns reporting off for every such plugin on the
server, and the environment variables `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1` turn it off
for the whole process. Details: https://danielstephenson.dev/usage-reporting

The plugin also includes bStats, which this file does not configure. See [bStats](README.md#bstats)
in the README for what it sends and how to turn it off in `plugins/bStats/config.yml`.
