# Tasks: Switch to MC version 1.21.11

This change introduces a series of small tasks to upgrade the project to target Minecraft `1.21.11`, updating mappings and Fabric API as needed.

## Implementation Tasks

### 1. Prepare branch and review current state

[x] create a new branch `upgrade/1.21.11` for this, push it and create a PR draft.

### 2. Identify all version references

- [x] Inspect current references to `1.21.7` and record targets to update (for example `Press2Hold/gradle.properties` and `Press2Hold/src/main/resources/fabric.mod.json`).

**Validation:** list of files containing `1.21.7` (manual verification).

### 3. Update build properties

- [x] Update `Press2Hold/gradle.properties` values:
- [x] `minecraft_version` -> `1.21.11`
- [x] `yarn_mappings` -> choose a suitable mapping token (e.g., `1.21.11+build.1`)
- [x] `fabric_version` -> the Fabric API coordinate matching `1.21.11` if applicable.

**Validation:** `git diff` showing only the property bumps.

### 4. Update mod metadata

- [x] Update `Press2Hold/src/main/resources/fabric.mod.json` `depends.minecraft` to `>=1.21.11`.

**Validation:** `git diff` shows only the JSON change.

### 5. Update CI/workflow files

[ ] Update any build scripts or CI that inject `minecraft_version` (e.g., `.github/workflows/release.yml`).

**Validation:** workflow file uses the `gradle.properties` value or explicit `1.21.11` consistently.

### 6. Document the chosen mapping and API versions

[ ] Record the selected Yarn mapping token and Fabric API version in `Press2Hold/openspec/changes/upgrade-minecraft-1.21.11/design.md`.
[ ] Validation: design notes document the choices made.
[ ] Run local build: `./gradlew clean build` and `./gradlew runClient` smoke-check (manual).

**Validation:** build succeeds and client runs to the main menu without mod-related errors.

### 7. Address any issues and finalize

[ ] Address any minor runtime or compile issues exposed by the mapping/API changes (small code fixes only).

**Validation:** tests and `./gradlew build` pass.

### 8. Finalize and submit change

[ ] Update `Press2Hold/openspec/changes/upgrade-minecraft-1.21.11` with implementation notes and close the change.

**Validation:** `openspec validate upgrade-minecraft-1.21.11 --strict` passes and change marked ready.

## Notes

- Keep changes minimal: prefer bumping coordinates first, then applying the smallest code changes required to compile and run.
- If significant API changes are required, stop and open a follow-up change with a design decision and targeted specs.
