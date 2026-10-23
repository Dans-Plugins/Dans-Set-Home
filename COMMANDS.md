# Dans Set Home Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/sethome` | Set your home to your current location. | `dsh.sethome` |
| `/home` | Teleport to your home location. | `dsh.home` |
| `/home <player>` | Teleport to another player's home location. | `dsh.home` and `dsh.home.others` |
| `/dsh help` | View a list of commands. | `dsh.help` |
| `/dsh forcesave` | Write every home to disk now instead of waiting for the server to stop. | `dsh.forcesave` (not needed from the console) |
| `/dsh forceload` | Read homes back from disk, losing any `/sethome` since the last save. Every player who had joined by the last save is on disk, so each of them goes back to the home saved then, or to no home at all if they had not set one yet. Players who first joined after the last save keep their current home. | `dsh.forceload` (not needed from the console) |

Each command can also be typed with the plugin's prefix: `/danssethome:sethome`,
`/danssethome:home` and `/danssethome:dsh`. Use these forms when another plugin on the server also
registers `/home` or `/sethome` and takes the short name.
