---
change-id: upgrade-minecraft-1.21.11
title: Upgrade project to Minecraft 1.21.11
author: automated-agent
status: draft
summary: Update build and metadata to target Minecraft 1.21.11 and corresponding mappings/FA
created: 2026-01-06
---

Background
----------
The project currently targets Minecraft `1.21.7` (see `gradle.properties` and `fabric.mod.json`). The requested product goal is to upgrade the mod to target Minecraft `1.21.11`.

Motivation
----------
- Keep compatibility with recent Minecraft client releases and ensure the mod builds cleanly on `1.21.11`.
- Update Yarn mappings, Fabric API coordinates and any build metadata that encode the MC version.

Scope
-----
In-scope (proposal stage):
- Create OpenSpec change that captures concrete requirements for a minimal, non-invasive upgrade to `1.21.11`.
- Produce tasks and design notes to guide a surgical implementation (no code changes in this stage).

Out-of-scope (for this proposal):
- Applying code or build changes (implementation), unless explicitly approved.
- Any API-level refactors beyond what the upgrade strictly demands.

Deliverables (this proposal)
----------------------------
- `proposal.md` (this file)
- `tasks.md` — ordered small tasks with verifications
- `design.md` — notes and trade-offs for updating mappings and dependencies
- `specs/version-bump/spec.md` — concrete spec deltas (requirements + scenarios)

Risks and unknowns
-------------------
- Fabric API or Yarn mapping changes between `1.21.7` and `1.21.11` may require minor code adjustments.
- The exact Yarn mapping token for `1.21.11` needs to be selected (e.g. `1.21.11+build.X`) — this proposal assumes a canonical mapping will be chosen at implementation time.
- CI/tooling may require updated Gradle/fabric-loom versions; we will keep changes minimal and validate by running `./gradlew build` during implementation.

Next steps
----------
1. Review this proposal and confirm the change-id `upgrade-minecraft-1.21.11`.
2. After approval, proceed to implementation tasks in `tasks.md`.
