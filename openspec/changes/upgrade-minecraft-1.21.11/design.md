---
change-id: upgrade-minecraft-1.21.11
---

Design notes and trade-offs

1) Mapping selection
- Yarn mappings are versioned separately from the numeric MC version (e.g. `1.21.11+build.N`). Choose the canonical Yarn mapping that corresponds to `1.21.11` on the Fabric ecosystem. If no published mapping exists at implementation time, consider one of:
  - Wait for an official Yarn release for `1.21.11` and proceed.
  - Use the nearest compatible mapping and validate runtime behavior.

Implementation note: record the chosen token in `Press2Hold/openspec/changes/upgrade-minecraft-1.21.11/design.md` and in `Press2Hold/openspec/changes/upgrade-minecraft-1.21.11/detailed-design.md`.

2) Fabric API compatibility
- Fabric API releases typically include the target Minecraft version in the artifact coordinate. Use the Fabric API release that explicitly targets `1.21.11` where possible. If not available, test the latest `1.21.x` Fabric API version for compatibility.

3) Build tooling
- `fabric-loom` or Gradle might require an updated plugin version for newer mappings; prefer minimal upgrades to `fabric-loom` only if validation fails. If a plugin bump is required, document the exact version in `Press2Hold/openspec/changes/upgrade-minecraft-1.21.11/design.md`.

4) Backwards compatibility
- This change should be additive to support building against `1.21.11`. If API changes require code edits, keep them as small compatibility shims and document them as separate spec deltas.

5) CI and release
- Update CI workflows to use the new `minecraft_version` value from `gradle.properties` where appropriate. Ensure artifacts are published with a distinct archiveBaseName that includes the MC version.
