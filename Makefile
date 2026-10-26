# Build and release aem-healthcheck.
#   - Build, test and publish targets need Java 11 and Maven; release-major|minor|patch need RTK
SHELL := /bin/bash
MVN ?= mvn -B -ntp
# Lets git run as root in a container on this repository, which is owned by your user
GIT_SAFE_DIRECTORY := GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=safe.directory GIT_CONFIG_VALUE_0=$(CURDIR)

# Clean, download dependencies, then build and install the bundle and content package
ci: clean deps install

clean:
	$(MVN) clean

# Download all dependencies into the local Maven repository
deps:
	$(MVN) dependency:go-offline

# Build the bundle (bundle/target/aem-healthcheck-bundle-<version>.jar) and the content package
# (content/target/aem-healthcheck-content-<version>.zip)
build:
	$(MVN) package

# Build and install the artifacts into the local Maven repository
install:
	$(MVN) install

# Release with RTK (https://github.com/cliffano/rtk), configured in .rtk.json. Sets the release version
# in the pom.xml files and CHANGELOG.md (the `## [Unreleased]` entries are dated as the release) and
# commits that, tags it aem-healthcheck-<version>, then commits the next pre-release version and a new
# empty `## [Unreleased]` section. Nothing is pushed; review, then run `git push --follow-tags`.
# Needs a clean working tree and entries under [Unreleased].
#   release-patch: 1.3.5-pre.0 -> 1.3.5, next 1.3.6-pre.0
#   release-minor: 1.3.5-pre.0 -> 1.4.0, next 1.4.1-pre.0
#   release-major: 1.3.5-pre.0 -> 2.0.0, next 2.0.1-pre.0
define release
	set -euo pipefail; \
	export $(GIT_SAFE_DIRECTORY); \
	git diff --quiet && git diff --cached --quiet \
	  || { echo "Commit or stash your changes before releasing"; exit 1; }; \
	awk '/^## \[Unreleased\]$$/ { found = 1; next } /^## / { found = 0 } found && /^- / { n++ } \
	  END { exit n ? 0 : 1 }' CHANGELOG.md \
	  || { echo "CHANGELOG.md has no entries under ## [Unreleased]"; exit 1; }; \
	rtk release --release-increment-type $(1)
endef

release-major:
	@$(call release,major)

release-minor:
	@$(call release,minor)

release-patch:
	@$(call release,patch)

# Upload test: sign the committed HEAD and upload it to the Central Portal as a pre-release test
# version, never publishing it (-DautoPublish=false), then print the deployment ID to check with
# release-status and drop with release-drop. Checks the GPG key, the Central token, the upload and
# Central's validation. Uploads the current release version with -rc.0 (e.g. 1.3.5-pre.0 -> 1.3.5-rc.0),
# which can be uploaded again once dropped.
# Builds in a temporary copy of the committed HEAD, so the pom.xml files are untouched. Signs with
# MAVEN_GPG_KEY and MAVEN_GPG_PASSPHRASE and uploads with MAVEN_CENTRAL_USERNAME and
# MAVEN_CENTRAL_PASSWORD.
# For example, in aem-platform-buildenv with Docker, with this repository mounted read-only and Java 11:
#   docker run --rm -v "$(pwd)":/workspace/aem-healthcheck:ro -w /workspace/aem-healthcheck \
#     -e JAVA_HOME=/usr/lib/jvm/java-11 \
#     -e MAVEN_GPG_KEY -e MAVEN_GPG_PASSPHRASE -e MAVEN_CENTRAL_USERNAME -e MAVEN_CENTRAL_PASSWORD \
#     shinesolutions/aem-platform-buildenv:6.4.1 make release-test
release-test:
	@set -euo pipefail; \
	work=$$(mktemp -d); trap 'rm -rf "$$work"' EXIT; \
	$(GIT_SAFE_DIRECTORY) git archive HEAD | tar -x -C "$$work"; \
	cd "$$work"; \
	current=$$($(MVN) -q help:evaluate -Dexpression=project.version -DforceStdout); \
	version="$${current%%-*}-rc.0"; \
	echo "Uploading HEAD to the Central Portal as $$version (it is not published)"; \
	$(MVN) -q versions:set -DnewVersion="$$version" -DgenerateBackupPoms=false; \
	$(MVN) -Prelease deploy -DskipTests -Dgpg.signer=bc -DautoPublish=false | tee deploy.log; \
	id=$$(sed -n 's/.*deploymentId: \([0-9a-f-]*\).*/\1/p' deploy.log | tail -1); \
	echo "Deployment $$id ($$version) is validated and not published. Check and drop it with:"; \
	echo "  make release-status DEPLOYMENT_ID=$$id"; \
	echo "  make release-drop DEPLOYMENT_ID=$$id"

# Upload the checked-out release version to the Central Portal, run by the publish workflow on a pushed
# aem-healthcheck-<version> tag. Signs with MAVEN_GPG_KEY and MAVEN_GPG_PASSPHRASE and uploads with
# MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD. Waits until Central has validated the upload,
# which then waits for Publish or Drop in the Central Portal.
publish:
	$(MVN) -Prelease deploy -DskipTests -Dgpg.signer=bc

# Central Portal deployments (`mvn -Prelease deploy` prints the deployment ID). Uses the user token in
# MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD; the Authorization header is passed to curl on
# stdin so the token is not visible in the process list.
#   release-status: show a deployment's state (e.g. VALIDATED, PUBLISHED, FAILED) and any errors
#   release-drop:   drop (delete) a deployment that has not been published
define central_api
	set -euo pipefail; \
	test -n "$(DEPLOYMENT_ID)" || { echo "Set DEPLOYMENT_ID, e.g. make $@ DEPLOYMENT_ID=<id>"; exit 1; }; \
	test -n "$${MAVEN_CENTRAL_USERNAME:-}" && test -n "$${MAVEN_CENTRAL_PASSWORD:-}" \
	  || { echo "Set MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD"; exit 1; }; \
	token=$$(printf '%s:%s' "$$MAVEN_CENTRAL_USERNAME" "$$MAVEN_CENTRAL_PASSWORD" | base64 | tr -d '\n'); \
	printf 'Authorization: Bearer %s\n' "$$token" \
	  | curl -sS --fail-with-body -X $(1) -H @- -w '\nHTTP %{http_code}\n' \
	      "https://central.sonatype.com/api/v1/publisher/$(2)"
endef

release-status:
	@$(call central_api,POST,status?id=$(DEPLOYMENT_ID))

release-drop:
	@$(call central_api,DELETE,deployment/$(DEPLOYMENT_ID))

.PHONY: ci clean deps build install release-major release-minor release-patch \
	release-test publish release-status release-drop
