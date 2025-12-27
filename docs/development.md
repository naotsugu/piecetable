
# Development

## Update version

Update the version numbers of the following files.

```
com.mammb.code.piecetable.Version
```

```
build.gradle.kts
```

```
README.md
```

```
examples/fx-editor/app/build.gradle.kts
```


Commit changes.

```shell
git add -A
git commit -m "Release v0.6.0"
git push origin main:main
```

By pushing a tag, the github action creates a release.

```shell
git tag v0.6.0
git push origin v0.6.0
```


## Maven publish

```shell
./gradlew publishAndReleaseToMavenCentral
```

[Central Portal](https://central.sonatype.com)


