# Press2Hold
When you press a specific keybind, all currently pressed keys stay held until you release them manually by pressing the keybind again.\
Because of the Java 25 requirement, only supports versions 26.1.2 and up.

## Local Java Setup

This project targets Java 25. The repository intentionally does not commit a machine-specific
`org.gradle.java.home` path in `gradle.properties`.

Use one of these local-only options instead:

1. Set `JAVA_HOME` to a Java 25 JDK before running Gradle.
2. Add `org.gradle.java.home=<your-local-jdk-path>` in your user-level `~/.gradle/gradle.properties`.

CI should provide Java 25 via its own environment setup (for example through `actions/setup-java`).

![file](https://github.com/user-attachments/assets/cf37e945-8b3d-4703-9fa9-01c8a89db29e)

Known issues:\
When the keybind is changed from the default the mod will also hold down that key. Doesn't affect functionality, just looks ugly. \
Some keys (most notably the entire function row) won't show the key, but their GLFW key token.
