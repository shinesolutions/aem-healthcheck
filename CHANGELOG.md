# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2026-10-27
### Added
- Added Makefile targets for ci, clean, deps, build, install, release-test, publish, release-status, release-drop and release-major/minor/patch
- Added Maven Central credentials to `.mvn/settings.xml`, read from environment variables
- Added Release Major/Minor/Patch GitHub workflows, and a Publish to Maven Central workflow that runs `make publish` on a pushed release tag with the Maven Central and GPG secrets

### Changed
- Builds, publishes and releases in aem-platform-buildenv 6.4.1, with its Java 11
- Publish to Maven Central through the Central Portal (central-publishing-maven-plugin) instead of the retired OSSRH
- Maven Central deployments wait for a manual Publish or Drop in the Central Portal, use `-DautoPublish=true` to publish automatically
- Artifact signing now runs in the `release` Maven profile, replacing `sign-artifacts`, with maven-gpg-plugin 3.2.8
- Releases are now made with RTK, by `make release-major|minor|patch` or the Release Major/Minor/Patch GitHub workflows, replacing maven-release-plugin, and tagged `aem-healthcheck-<version>`
- Development versions are now pre-release versions such as `1.3.5-pre.0` instead of `-SNAPSHOT`
- CI now runs `make ci` in aem-platform-buildenv 6.4.1 with Java 11, through cliffano/command-docker-action
- Simplify CI workflow to not use action files anymore
- Restructured README into Installation, Usage, Result Model, Error Handling, Testing and Versions History
- Converted CHANGELOG.md to the Keep a Changelog format, which RTK updates

### Fixed
- Fixed the bundle build on Java 11 by upgrading maven-scr-plugin from 1.20.0 to 1.26.4
- Fixed the missing metatype localization in the bundle with maven-scr-plugin 1.26.4, which writes it to `OSGI-INF/l10n`

## [1.3.4] - 2017-04-21
### Changed
- Specified vendor property for bundle and services

## [1.3.3] - 2017-04-10
### Added
- Introduced bundle whitelist for ignoring bundles in the bundle health-check [#16]

## [1.3.2] - 2017-03-24
### Added
- Introduced `deep` tag [#10]

## [1.3.1] - 2017-03-09
### Added
- Introduced `shallow` tag [#10]

### Deprecated
- `devops` tag will be retired in a later version

## [1.1.0] - 2016-12-21
### Changed
- Changed `groupId` to `com.shinesolutions`

## [1.0.0] - 2016-12-21
### Added
- Initial version

[#10]: https://github.com/shinesolutions/aem-healthcheck/issues/10
[#16]: https://github.com/shinesolutions/aem-healthcheck/issues/16

[Unreleased]: https://github.com/shinesolutions/aem-healthcheck/compare/aem-healthcheck-2.0.0...HEAD
[2.0.0]: https://github.com/shinesolutions/aem-healthcheck/compare/aem-healthcheck-1.3.4...aem-healthcheck-2.0.0
[1.3.4]: https://github.com/shinesolutions/aem-healthcheck/compare/aem-healthcheck-1.3.3...aem-healthcheck-1.3.4
[1.3.3]: https://github.com/shinesolutions/aem-healthcheck/compare/aem-healthcheck-1.3.2...aem-healthcheck-1.3.3
[1.3.2]: https://github.com/shinesolutions/aem-healthcheck/compare/aem-healthcheck-1.3.1...aem-healthcheck-1.3.2
[1.3.1]: https://github.com/shinesolutions/aem-healthcheck/compare/aem-healthcheck-1.1.0...aem-healthcheck-1.3.1
[1.1.0]: https://github.com/shinesolutions/aem-healthcheck/compare/aem-healthcheck-1.0.0...aem-healthcheck-1.1.0
[1.0.0]: https://github.com/shinesolutions/aem-healthcheck/releases/tag/aem-healthcheck-1.0.0
