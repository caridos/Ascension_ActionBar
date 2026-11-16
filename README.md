# Ascension Direct Cast

Client-only NeoForge addon for Ascension's public Alpha casting API. Adds six configurable direct-cast keys and an on-screen skill hotbar. **The dedicated server does not need this addon** if it already runs compatible Ascension and ZenithLib versions.

## How it works

Direct-cast keys are registered as local ZenithLib mappings, **not** as new synchronized action identifiers.

1. On key down, the client selects the target slot locally and sends Ascension's built-in `SelectSkillSlotPacket(slot)` to the server.
2. It then sends ZenithLib's built-in `ActionChangedPacket(ascension:skill_cast, true)` and activates the same action locally for casting prediction.
3. On key up, it sends `ActionChangedPacket(ascension:skill_cast, false)` and releases the local action.
4. The current direct-cast key owns the cast; pressing a different direct-cast key first releases the old one.

These are the same server-recognized packets used by Ascension's skill wheel and normal casting input. No new network payload or server-side mod event listener is required.

## Limitations

- This code was developed against Ascension commit `b0eca8b50a75eec56b3a489b345dc9c1935cfedc`. A public Alpha server running a different protocol may still be incompatible.
- Only one direct-cast hold is active at a time.
- Do not hold the native Ascension cast key (normally `V`) and a direct-cast key simultaneously. Both operate on the same `ascension:skill_cast` action; releasing either can end it. Proper independent multi-key aggregation would require additional coordination.
- Charging, channeling, interruption, and multiplayer behavior need live testing. A successful Gradle build does not prove gameplay compatibility.

## Versions

- Minecraft `26.1.2`
- NeoForge `26.1.2.116`
- Java `25`
- Ascension `b0eca8b50a75eec56b3a489b345dc9c1935cfedc`
- ZenithLib `io.github.oliverhesse:zenithlib:0.1.2`

## Controls

Defaults: `Z`, `X`, `C`, `B`, `N`, `M`. Keys can be rebound in Minecraft's Controls screen. HUD uses the current translated names and supports two-line labels.

## Build

```
./gradlew clean build
```

Install the built addon jar **on the client only**, alongside the compatible Ascension/ZenithLib mods. Do not install the previous server-required addon alongside this version.
