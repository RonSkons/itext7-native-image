To generate required metadata for native image generation, install GraalVM and run the following command:
```bash
mvn clean test \
    -Dmaven.test.failure.ignore=true \
    -DITEXT_GS_EXEC="gs" \
    -DITEXT_MAGICK_COMPARE_EXEC="magick compare" \
    -Pnative
```
(configure `ITEXT_GS_EXEC` and `ITEXT_MAGICK_COMPARE_EXEC` as described in [BUILDING.md](BUILDING.md))

Ensure that both `JAVA_HOME` and `GRAALVM_HOME` environment variables are set, and that they refer to the same GraalVM installation.

The resulting metadata will be stored in `target/classes/META-INF/native-image` folder.