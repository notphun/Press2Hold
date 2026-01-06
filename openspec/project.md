# Project Context

## Purpose

Press2Hold is a lightweight Fabric Minecraft **client** mod that enables players to "latch" currently pressed keys and mouse buttons so they remain held until the latch keybind is pressed again. Its primary goal is to provide a small, unobtrusive quality-of-life utility for players who want to simulate holding inputs without continuous manual input.

## Tech Stack

- Language: **Java 21**
- Build: **Gradle** with **fabric-loom** plugin
- Target platform: **Minecraft (Fabric)** — tested for **1.21.7**
- Mappings: **Yarn**
- Loader: **Fabric Loader**
- API: **Fabric API**
- Mixins: **SpongePowered Mixin** (mixin configs live in `src/main/resources`)
- Logging: **SLF4J**
- Source layout: standard Gradle layout with `src/main/java` (common) and `src/client/java` (client-only code)

## Project Conventions

### Code Style

- Follow standard Java conventions (camelCase for variables/methods, PascalCase for types).
- Keep methods small and focused; prefer clear, descriptive names for variables and methods.
- Use a single logger per mod class: `public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);`
- Run an automatic formatter in CI (e.g., Google Java Format or IDE formatter) and add an editorconfig if desired.

### Architecture Patterns

- Single-mod structure: keep the project focused and minimal.
- Separate client-only logic into `src/client/java` and register it via `ClientModInitializer`.
- Use Fabric entrypoints (`ModInitializer`, `ClientModInitializer`) and Mixins for targeted runtime modifications.
- Keep mixins minimal and isolated; prefer explicit method injections over broad transformations.

### Testing Strategy

- Manual integration testing using Loom (`./gradlew runClient`) for most behavior that depends on Minecraft runtime.
- Add unit tests (JUnit 5) for pure Java logic where practical; mock or isolate Minecraft-dependent code.
- For CI, run `./gradlew build` and any unit tests; consider adding a headless smoke test or run a configured client in GitHub Actions for critical integration checks.

### Git Workflow

- Branch naming: use prefixes such as `feature/`, `fix/`, `chore/`, `hotfix/` (e.g., `feature/support_1.21.7`).
- Keep branches short-lived and open PRs against `main` for review.
- Use Conventional Commits style for commit messages where possible (`feat:`, `fix:`, `docs:`, `chore:`, etc.).
- Require at least one reviewer before merging; enable branch protection on `main` if appropriate.

## Domain Context

- Client-side mod that manipulates `KeyBinding` state to simulate held inputs and replays them until unlatched.
- Known UX quirks: changing the latch keybind may appear as if that key is held; certain keys (e.g., function keys) may not have human-friendly names and show GLFW codes instead.

## Important Constraints

- **Java 21** minimum (set by Gradle `release` and `sourceCompatibility`).
- Target Minecraft version **1.21.7**; bump Yarn/mappings and Fabric API together when upgrading MC.
- Client-only: do not reference client-only classes in common/server runtime code.
- Keep the mod backward-compatible with Fabric Loader constraints declared in `fabric.mod.json`.

## External Dependencies

- Fabric Loader (declared in `gradle.properties`)
- Fabric API
- Yarn mappings (used by Loom)
- Gradle + fabric-loom plugin (build tooling)
- Mixin runtime (for mixin-based modifications)
