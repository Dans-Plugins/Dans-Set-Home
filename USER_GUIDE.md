# Dans Set Home User Guide

## What is Dans Set Home?

Dans Set Home is a simple Spigot plugin that lets players set a home location and teleport back to it at any time.

## Installation

1. Download the latest `DansSetHome-<version>.jar` from the [Releases](https://github.com/Dans-Plugins/Dans-Set-Home/releases) page.
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server.

## Getting Started

1. Stand in the location you want to set as your home.
2. Run `/sethome` to save the location.
3. Run `/home` at any time to teleport back.

`/home` does not teleport you straight away. A short delay runs first — 3 seconds by
default — and you have to stand still for it. If your position changes before the delay
is up, the teleport is cancelled and you are told why. Server administrators can change
the length of the delay with the `teleport-delay-seconds` option described in
[CONFIG.md](CONFIG.md).

Each player has one home. Running `/sethome` again replaces it with your current location.

## Visiting Other Players' Homes

Players with the `dsh.home.others` permission (operators by default) can run `/home <player>`
to teleport to that player's home. The name is not case-sensitive, and the same delay and
stand-still rule apply. Without the permission, `/home` followed by any name is refused, even your
own name. If the player has never set a home, you are told that they don't have one.

## Commands

See [COMMANDS.md](COMMANDS.md) for every command and its permission. If another plugin on the
server also provides `/home` or `/sethome`, use `/danssethome:home` and `/danssethome:sethome`
instead.

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `dsh.help` | `true` | View the help menu. |
| `dsh.home` | `true` | Teleport to your home. |
| `dsh.sethome` | `true` | Set your home location. |
| `dsh.home.others` | `op` | Teleport to another player's home. |
| `dsh.forcesave` | `op` | Force-save player data from the console. |
| `dsh.forceload` | `op` | Force-load player data from the console. |

## Support

Ask questions in the [Discord server](https://discord.gg/xXtuAQ2) or open a [GitHub issue](https://github.com/Dans-Plugins/Dans-Set-Home/issues).
