# Dans Set Home Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/sethome` | Set your home to your current location. | `dsh.sethome` |
| `/home` | Teleport to your home location. | `dsh.home` |
| `/home <player>` | Teleport to another player's home location. | `dsh.home` and `dsh.home.others` |
| `/dsh help` | View a list of commands. | `dsh.help` |
| `/dsh forcesave` | Write every home to disk now instead of waiting for the server to stop. | `dsh.forcesave` (not needed from the console) |
| `/dsh forceload` | Read homes back from disk. Any player whose home is on disk goes back to that saved location, losing any `/sethome` since the last save. | `dsh.forceload` (not needed from the console) |

Each command can also be typed with the plugin's prefix: `/danssethome:sethome`,
`/danssethome:home` and `/danssethome:dsh`. Use these forms when another plugin on the server also
registers `/home` or `/sethome` and takes the short name.
