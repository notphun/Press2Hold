---
change-id: upgrade-minecraft-1.21.11
capability: version-bump
---

# Version bump to Minecraft 1.21.11

## MODIFIED Requirements

### Requirement: Update build properties to target Minecraft 1.21.11

The project `Press2Hold/gradle.properties` SHALL be updated so `minecraft_version` is `1.21.11` and `yarn_mappings` references a `1.21.11` mapping.

#### Scenario: Bump `gradle.properties` and confirm changed version
- Given the project currently uses `minecraft_version=1.21.7` in `Press2Hold/gradle.properties`
- When the implementer updates `minecraft_version` to `1.21.11` and updates `yarn_mappings` to the correct `1.21.11` mapping token
- Then `Press2Hold/gradle.properties` contains `minecraft_version=1.21.11` and `yarn_mappings` references a `1.21.11` mapping

### Requirement: Update `fabric.mod.json` minimum Minecraft dependency

The `Press2Hold/src/main/resources/fabric.mod.json` `depends.minecraft` field SHALL be updated to `">=1.21.11"`.

#### Scenario: Update `src/main/resources/fabric.mod.json`
- Given `Press2Hold/src/main/resources/fabric.mod.json` currently lists `"minecraft": ">=1.21.7"`
- When the implementer updates that field to `">=1.21.11"`
- Then the JSON reflects `">=1.21.11"` and no other unrelated JSON changes are present

### Requirement: Validate build and smoke test on updated MC mappings

Developers SHALL run `./gradlew clean build` and optionally `./gradlew runClient` to verify the project builds and the client starts without mod-related exceptions.

#### Scenario: Build and run a quick client smoke test
- Given the repository has been updated with the new mapping and fabric coordinates
- When a developer runs `./gradlew clean build` and (optionally) `./gradlew runClient`
- Then the build completes successfully and the client launches to the main menu without mod-related exceptions

## ADDED Requirements

### Requirement: Document the upgrade and the chosen Yarn mapping

The implementer MUST document the Yarn mapping token and Fabric API coordinates chosen for `1.21.11` and MUST record smoke-test outcomes and any small code fixes applied in the change folder.

#### Scenario: Document mapping and validation choices
- Given the implementer selected a Yarn mapping token for `1.21.11`
- When the implementer finishes the implementation tasks
- Then `Press2Hold/openspec/changes/upgrade-minecraft-1.21.11/design.md` and `Press2Hold/openspec/changes/upgrade-minecraft-1.21.11/tasks.md` document the chosen mapping token, Fabric API coordinate used, and the test results
