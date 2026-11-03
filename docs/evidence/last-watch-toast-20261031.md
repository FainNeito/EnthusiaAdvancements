# Last Watch toast sound scope

When a player earns `holidays/last_watch`, its registered node remains a challenge. Its celebration alone uses UAA's task-style toast with the same title and icon so the client should not play the vanilla challenge chime over the separately supplied custom sound. All other namespaces, keys, frames, ownership checks, chat and Discord paths retain their existing behavior. No client-wide or pack-wide sound replacement is introduced.

The different toast style was explicitly selected by the user. Native Java client verification of toast appearance, custom audio and another challenge's unchanged chime is required; this source change is not a runtime acceptance result. The server operator must deploy compatible Holidays and pilot artifacts together.

SPEAR record: expected behavior and scope above; policy tests target the exception and unaffected namespace/key/frame cases. Offline Maven clean verify passed 46 tests with no failures or skips on Java 25. The production UAA 2.8.2 JAR exposes the required toast and display methods; its SHA-256 is `3b4f8e17c8ca714497f59219069f36ca7c9e72247c7f53375951a5c60560ec02`. Native acceptance remains pending. No historical test-first result is claimed.
