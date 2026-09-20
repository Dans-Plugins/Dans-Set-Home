# Dan's Set Home
This Minecraft plugin allows players to set and teleport to home locations. 

## Download
- [SpigotMC](https://www.spigotmc.org/resources/dans-set-home.79986/)
- [GitHub releases](https://github.com/Dans-Plugins/Dans-Set-Home/releases)

## Usage reporting

Usage reporting is on by default: when the plugin is enabled, and each time one of its commands is
used, it sends its name, its version and the command's name (`startup` and `command` events) to
https://trace.danielstephenson.dev so it is known which plugins are actually in use. Nothing about
players, worlds, IPs, the server, or anything typed after a command is sent. The plugin says on
every startup whether reporting is on. To turn it off:

- for this plugin: `usage-reporting.enabled: false` in `plugins/DansSetHome/config.yml`
- for every plugin on the server that reports this way: `enabled: false` in `plugins/trace/config.yml`
  (created the first time such a plugin starts)
- for the whole process: the environment variable `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`

Details: https://github.com/Stephenson-Software/trace#usage-reporting
