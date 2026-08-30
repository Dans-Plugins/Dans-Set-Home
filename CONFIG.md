# Dans Set Home Configuration

Dans Set Home creates a `config.yml` in `plugins/DansSetHome/` on first run. Saved homes are kept in
the same folder. Servers upgrading from a version that stored homes in `plugins/Medieval-Set-Home/`
have those files moved across automatically the first time the plugin starts.

| Key | Default | Description |
|-----|---------|-------------|
| `teleport-delay-seconds` | `3` | Number of seconds a player must stand still after running `/home` before being teleported. |
