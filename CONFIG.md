# Dans Set Home Configuration

Dans Set Home creates a `config.yml` in `plugins/DansSetHome/` on first run. Saved homes are kept in
the same folder. Servers upgrading from a version that stored homes in `plugins/Medieval-Set-Home/`
have those files moved across automatically the first time the plugin starts.

| Key | Default | Description |
|-----|---------|-------------|
| `teleport-delay-seconds` | `3` | Number of seconds a player must stand still after running `/home` before being teleported. |
| `usage-reporting.enabled` | `true` | Whether the plugin reports usage events (see below). Set to `false` to turn it off. |
| `usage-reporting.endpoint` | `https://trace.danielstephenson.dev` | The trace server events are sent to. |
| `usage-reporting.key` | `""` | The key that identifies this plugin to the trace server. Empty means reporting is off regardless of `enabled`. |

## Usage reporting

When the plugin is enabled, and each time one of its commands is used, a small event is sent to the
author's [trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which
plugins are actually in use. An event carries the plugin's name, the event name (`startup` or
`command`), and either the plugin version or the command name — nothing about players, the world, or
the server. Sending happens off the main thread, never delays a tick, and is dropped silently if the
server cannot be reached. Set `usage-reporting.enabled` to `false` to turn it off.
