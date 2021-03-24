Releasing
=========

Releasing a new build to maven central is semi-automated by the [release] GitHub workflow which uses the [semantic-release] workflow.

1. When a new commit lands on the `main` branch, depending on the commit message, the semantic version number
of the artifacts will be updated (see [semantic-release] for what kind of commit messages trigger a version bump).
2. Upon updating the version number, a git tag and GitHub release with a changelog is generated.
3. Once the new tag is pushed, the [publish] workflow gets triggered which upon succeeding uploads the new artifacts to the Sonatype staging repository.
4. Lastly, the staged artifacts require manual promotion in the Nexus Repository Manager to publish the builds to maven central.

[release]: .github/workflows/release.yml
[publish]: .github/workflows/publish.yml
[semantic-release]: https://github.com/semantic-release/semantic-release
