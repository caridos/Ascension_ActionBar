# Ascension Direct Cast

Standalone NeoForge addon for Ascension's `development` branch. It adds six configurable direct-cast keys and a compact HUD backed by Ascension's existing synchronized `SkillCastHandler`.

## Source inspection

1. Ascension registers `V` with `InputHandler.registerAction(ascension:skill_cast, KeyMapping)`. ZenithLib's client tick detects the edge, and `ActionHandler` sends `ActionChangedPacket` while also updating the client `PlayerActionManager`. The packet updates the server manager. Each manager posts `ActionEvent.Start` or `ActionEvent.End`; Ascension calls `castSelectedSkill()` on start and `releaseCastInput()` on end on both sides.
2. ZenithLib accepts multiple mappings with one identifier, but it cannot aggregate them safely. Every mapping sends its own `false`, while `ActionManager` stores only one active entry per action identifier. Releasing either mapping therefore ends the shared action.
3. This addon registers one synchronized ZenithLib action per direct slot. Its common-side listener validates the slot and calls `handler.select(slot)` followed by `handler.castSelectedSkill()`. The latest pressed cast input owns held state until that same input is released. An `isActive` query bridge reports that owner when Ascension asks about `ascension:skill_cast`, preserving charge, channel, release protection, and held-cast visuals without merging independent keys into the same action entry.
4. Ascension's wheel reads icons from `<skill namespace>:textures/spells/icon/<skill path>.png` with `ascension:textures/spells/icon/placeholder.png` as fallback. Its resolver is package-private, so the addon reuses the resource convention and original resources, not copied textures or a second icon registry.
5. One narrow `ActionManager.isActive` mixin is necessary because Ascension hardcodes its cast action identifier when checking held state. No accessor, access transformer, or custom payload is used.

## Versions

- Minecraft `26.1.2`
- NeoForge `26.1.2.116`
- Java `25`
- Ascension `b0eca8b50a75eec56b3a489b345dc9c1935cfedc`
- ZenithLib `003d6549874597fd63ea2d3039a694cb1f0a6b3e`

The versions are pinned to the Ascension commit reviewed on 2026-11-15 and a compatible ZenithLib revision. ZenithLib exposes `ActionEvent.getEntity()` and the `ActionManager` attachment.

The addon requires Ascension `0.2.0-Alpha-Release` or newer. Holding and releasing charge/channel skills should be tested on both client and server after rebuilding.

## Controls

Defaults are `Z`, `X`, `C`, `B`, `N`, and `M`. All six use `KeyConflictContext.IN_GAME` and can be rebound in Minecraft's Controls screen. The HUD always renders each mapping's current translated key name.

## Build

Use Java 25 and run:

```text
gradlew build
```
