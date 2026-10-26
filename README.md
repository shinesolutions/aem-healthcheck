[![Build Status](https://github.com/shinesolutions/aem-healthcheck/workflows/CI/badge.svg)](https://github.com/shinesolutions/aem-healthcheck/actions?query=workflow%3ACI)
[![Known Vulnerabilities](https://snyk.io/test/github/shinesolutions/aem-healthcheck/badge.svg)](https://snyk.io/test/github/shinesolutions/aem-healthcheck)

# HealthCheck Servlet for AEM

This servlet makes the JMX health check results accessible via HTTP.

It is used to execute Sling Health Checks based on provided tags (if no tags are provided, all registered health checks will be executed).
For the full list of provided health checks in AEM, go to [http://localhost:4502/system/console/healthcheck](http://localhost:4502/system/console/healthcheck).

Learn more about AEM HealthCheck:

* [Installation](#installation)
* [Usage](#usage)
* [Result Model](#result-model)
* [Error Handling](#error-handling)
* [Testing](#testing)
* [Versions History](#versions-history)

## Installation

### Requirements

This project requires Java 11 and Apache Maven. Run the `make` targets inside the
`shinesolutions/aem-platform-buildenv:6.4.1` Docker image, which has Java 11, Maven and RTK, started
from the directory above this repository. The image's default Java is 21, so this sets `JAVA_HOME`
to its Java 11 in `/usr/lib/jvm/java-11`. It also passes the release variables through by name and
your git identity for the release commits, and marks the mounted repository as safe for git (it is
owned by your user, not root):

```
$ docker run --rm -it -v "$(pwd)":/workspace -w /workspace/aem-healthcheck \
    -v aem-healthcheck-m2:/root/.m2 -v ~/.gitconfig:/root/.gitconfig:ro \
    -e GIT_CONFIG_COUNT=1 -e GIT_CONFIG_KEY_0=safe.directory \
    -e GIT_CONFIG_VALUE_0=/workspace/aem-healthcheck \
    -e JAVA_HOME=/usr/lib/jvm/java-11 \
    -e MAVEN_GPG_KEY -e MAVEN_GPG_PASSPHRASE -e MAVEN_CENTRAL_USERNAME -e MAVEN_CENTRAL_PASSWORD \
    shinesolutions/aem-platform-buildenv:6.4.1 bash
```

### Download

Releases are published to [Maven Central](https://central.sonatype.com/namespace/com.shinesolutions)
under `com.shinesolutions:aem-healthcheck-content` (the content package) and
`com.shinesolutions:aem-healthcheck-bundle` (the OSGi bundle).

### Build from source

| Target | Description |
|---|---|
| `make deps` | Download all dependencies |
| `make build` | Build the bundle and the content package, and run the unit tests |
| `make install` | Build and install the artifacts into the local Maven repository |

```
$ make build
```

This creates the bundle `bundle/target/aem-healthcheck-bundle-<version>.jar` and the content package
`content/target/aem-healthcheck-content-<version>.zip`, which embeds the bundle.

## Usage

### Install to AEM

Install the content package by uploading `aem-healthcheck-content-<version>.zip` with
[CRX Package Manager](http://localhost:4502/crx/packmgr/index.jsp), or with Maven from the root directory:

| Command | Description |
|---|---|
| `mvn -PautoInstallPackage clean install` | Build the bundle and content package and install the package to the author instance |
| `mvn -PautoInstallPackagePublish clean install` | Build and install the content package to the publish instance |
| `mvn -PautoInstallBundle clean install` (from the `bundle` directory) | Build *just* the bundle and install it to the author instance |

### Specifying CRX Host/Port

The CRX host, port and credentials default to `localhost:4502` with `admin`/`admin` for author and
`localhost:4503` for publish. Override them on the command line:

```
$ mvn -Dcrx.host=otherhost -Dcrx.port=5502 -Dcrx.username=admin -Dcrx.password=admin <goals>
$ mvn -Dpublish.crx.host=otherhost -Dpublish.crx.port=5503 <goals>
```

### Requests

Sample requests:

 * http://host:port/system/health
 * http://host:port/system/health?tags=shallow
 * http://host:port/system/health?tags=shallow,security
 * http://host:port/system/health?tags=shallow,security&combineTagsOr=false

Note: It is assumed that all `/system/*` paths are only accessible from a local network and not routed to the Internet.

### Ignored Bundles Configuration

This healthcheck can take an optional OSGi configuration file.

/apps/healthcheck/config/com.shinesolutions.healthcheck.hc.impl.ActiveBundleHealthCheck:

```
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          bundles.ignored="[com.day.cq.dam.dam-webdav-support]"/>
```
With this configuration, the `com.day.cq.dam.dam-webdav-support` bundle would be ignored in the `ActiveBundleHealthCheck`.

### Available Tags

Besides the out-of-the-box tags, two new tags have been incorporated to the health check:
#### Custom tags

|       Tag       | Health Checks    | Description |
|:---------------:|------------------|-------------|
| shallow, devops | SmokeHealthCheck | This tag runs a simple Smoke health check that determines if an instance is running.                                                                                                                                                                                                                                          |
|   deep          | <ul><li>ActiveBundleHealthCheck</li><li>ReplicationQueueHealthCheck</li><li>SlingJobsHealthCheck</li></ul> | This tag runs three different health checks.<ul><li>The first health check scans the current OSGi bundles and reports if there is any inactive bundles.</li><li>The second health check verifies the replication queue of agents.</li><li>The third health check tests the number of active jobs and their general health in the queue.</li></ul> |

#### Out-of-the-box tags

|       Tags      | Health Checks |
|:---------------:|---------------|
| sling           | Sling Get Servlet, Sling Java Script Handler, Sling Jsp Script Handler, Sling Jobs, Sling Referrer Filter |
| jobs            | Sling Jobs |
| dispatcher      | CQ Dispatcher Configuration |
| cq              | CQ HTML Library Manager Config, Replication and Transport Users, WCM Filters Configuration |
| bundles         | CRXDE Support, DavEx Health Check, WebDAV Health Check |
| packages, startup | Default CQ content packages are present |
| content         | Default CQ content packages are present, Example Content Packages |
| login           | Default Login Accounts, Example Content Packages |
| example         | Example Content Packages |
| replication     | Replication and Transport Users |
| dos             | Sling Get Servlet |
| webserver, clickjacking | Web Server Configuration |
| deserialization | Deserialization Firewall Attach API Readiness, Deserialization Firewall Functional, Deserialization Firewall Loaded |
| queries         | Query Traversal Limits |
| csrf            | Sling Referer Filter |
| acl             | User Profile Default Access |
| production      | CQ Dispatcher Configuration, CQ HTML Library Manager Config, CRXDE Support,Default Login Accounts, Default Login Accounts, Example Content Packages, Sling Get Servlet, Sling Java Script Handler, Sling Jsp Script Handler, WCM Filters Configuration, Web Server Configuration, Authorizable Node Name Generation, DavEx Health Check, Sling Referrer Filter, WebDAV Health Check |
| security        | CQ Dispatcher Configuration, CQ HTML Library Manager Config, CRXDE Support, Default Login Accounts, Example Content Packages, Replication and Transport Users, Sling Get Servlet, Sling Java Script Handler, Sling Jsp Script Handler, WCM Filters Configuration, Web Server Configuration, Authorizable Node Name Generation, DavEx Health Check, Deserialization Firewall Attach API Readiness, Deserialization Firewall Functional, Deserialization Firewall Loaded, Sling Referrer Filter, User Profile Default Access, WebDAV Health Check |
| system          | Maintenance Task com.day.cq.audit.impl.AuditLogMaintenanceTask, Maintenance Task WorkflowPurgeTask, Maintenance Task RevisionCleanupTask |

## Result Model

Sample responses:

###### shallow:
```
{
  "results": [
    {
      "name": "Smoke Health Check",
      "status": "OK",
      "timeInMs": 1
    }
  ]
}
```
###### deep:
```
{
  "results": [
    {
      "name": "Bundle Health Check",
      "status": "OK",
      "timeMs": 0
    },
    {
      "name": "Replication Queue Health Check",
      "status": "OK",
      "timeMs": 18
    },
    {
      "name": "Sling Jobs Health Check",
      "status": "OK",
      "timeMs": 0
    },
    {
      "name": "Startup Listener Check",
      "status": "OK",
      "timeMs": 1
    }
  ]
}
```

## Error Handling

A `200` status code will be returned if ALL health checks return the status OK. Otherwise a `503` is returned.

The `autoInstall*` profiles fail the Maven build when AEM rejects the package or bundle
(content-package-maven-plugin runs with `failOnError`). Most likely causes are:

* AEM is not running or not reachable at the CRX host and port
* Invalid CRX credentials
* The bundle doesn't start in AEM: check its state in the OSGi console at `/system/console/bundles`

## Testing

The bundle has JUnit unit tests of the health checks and the servlet, with AEM's APIs mocked by
Mockito. `make ci` cleans, downloads dependencies, then builds, runs the unit tests and installs
the bundle and content package into the local Maven repository:

```
$ make ci
```

To test the package itself, install it to a local AEM instance (see [Install to AEM](#install-to-aem))
and request `/system/health?tags=shallow` and `/system/health?tags=deep`.

## Versions History

Changes in each version are described in [CHANGELOG.md](CHANGELOG.md).

Releases are published to [Maven Central](https://central.sonatype.com/namespace/com.shinesolutions)
under `com.shinesolutions:aem-healthcheck`, `aem-healthcheck-bundle` and `aem-healthcheck-content`, and
tagged `aem-healthcheck-<version>` in git. Versions follow [Semantic Versioning](https://semver.org/):
use a major release for breaking changes, minor for new features and patch for fixes.
Between releases, the `pom.xml` files hold the next pre-release version, e.g. `1.3.5-pre.0`.

Versions are managed with [RTK](https://github.com/cliffano/rtk), configured in `.rtk.json`: it sets
the release version in the `pom.xml` files and CHANGELOG.md, commits and tags it
`aem-healthcheck-<version>`, then commits the next pre-release version.

### Release targets

| Target | Description |
|---|---|
| `make release-test` | Sign `HEAD` and upload it to the Maven Central Portal as a pre-release test version without publishing it, then print the deployment ID for `release-status` and `release-drop`. Uploads the current release version with `-rc.0`, such as `1.3.5-rc.0` for `1.3.5-pre.0` |
| `make release-status DEPLOYMENT_ID=<id>` | Show a Maven Central Portal deployment's state (e.g. `VALIDATED`, `PUBLISHED`, `FAILED`) and errors, using `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD` |
| `make release-drop DEPLOYMENT_ID=<id>` | Drop (delete) a Maven Central Portal deployment that has not been published |
| `make publish` | Sign the checked-out version and upload it to the Maven Central Portal, then wait until it is validated. Run by the **Publish to Maven Central** workflow on a pushed `aem-healthcheck-<version>` tag |
| `make release-major`, `release-minor`, `release-patch` | Release the next major, minor or patch version with RTK. Dates the `## [Unreleased]` CHANGELOG.md entries as the release, then commits and tags `aem-healthcheck-<version>` locally; push with `git push --follow-tags` |

### Release requirements
- A GPG key for signing, with its public key on a keyserver such as `keyserver.ubuntu.com`
  (Maven Central verifies signatures against it)
- A Maven Central Portal user token for the `com.shinesolutions` namespace
- Both are read from environment variables only, never from files. Enter them in your shell, so
  the values stay hidden and out of your shell history:

  ```
  # GPG private key: run this, paste the whole key including the
  # -----BEGIN/END PGP PRIVATE KEY BLOCK----- lines, press Enter, then Ctrl-D
  $ stty -echo; MAVEN_GPG_KEY="$(cat)"; stty echo; export MAVEN_GPG_KEY

  # GPG passphrase and Central Portal user token: type or paste each value, then press Enter
  $ read -rs MAVEN_GPG_PASSPHRASE; export MAVEN_GPG_PASSPHRASE
  $ read -rs MAVEN_CENTRAL_USERNAME; export MAVEN_CENTRAL_USERNAME
  $ read -rs MAVEN_CENTRAL_PASSWORD; export MAVEN_CENTRAL_PASSWORD
  ```

  Check the key arrived complete without printing it: `printf '%s\n' "$MAVEN_GPG_KEY" | head -1` and
  `| tail -1` should show the BEGIN and END lines.

Run the release targets in the aem-platform-buildenv container from [Requirements](#requirements),
started after you set the variables so they are passed through.

### Release steps
1. Start from an up-to-date `main` with a green CI build and a clean working tree. Check that the
   `## [Unreleased]` entries in CHANGELOG.md describe everything in this release.
2. Check signing and Maven Central validation with a test upload of `HEAD`, in aem-platform-buildenv:
   ```
   $ make release-test
   ```
   Then drop the test deployment with `make release-drop DEPLOYMENT_ID=<id>`, or **Drop** in the
   Central Portal.
3. Create the release, either:
   - In GitHub Actions, run the **Release Major**, **Release Minor** or **Release Patch** workflow.
     It runs RTK and pushes the commits and the tag. Then `git pull --tags` to fetch them.
   - Or locally in aem-platform-buildenv, choosing `release-major`, `release-minor` or `release-patch`:
     ```
     $ make release-patch
     ```
     This dates the `## [Unreleased]` entries as the new version in CHANGELOG.md, commits the release
     version, tags it `aem-healthcheck-<version>` and commits the next pre-release version. Nothing
     is pushed. Review the result with `git log -3` and `git show aem-healthcheck-<version>:CHANGELOG.md`.
4. If you released locally, push the commits and the tag from your machine, outside the container,
   where your GitHub credentials are:
   ```
   $ git push --follow-tags
   ```
5. Upload the tagged version to Maven Central. The pushed tag triggers the **Publish to Maven
   Central** workflow, which runs `make publish` in aem-platform-buildenv with the
   `SHINEOPENSOURCE_MAVEN_CENTRAL_USERNAME`, `SHINEOPENSOURCE_MAVEN_CENTRAL_PASSWORD`,
   `SHINEOPENSOURCE_MAVEN_GPG_KEY` and `SHINEOPENSOURCE_MAVEN_GPG_PASSPHRASE` GitHub secrets. To
   upload it yourself instead, in aem-platform-buildenv:
   ```
   $ git checkout aem-healthcheck-<version>
   $ make publish
   $ git checkout main
   ```
   Maven Central validates the upload (signatures, checksums and POM) and the build waits until it
   is validated. The deployment then waits at [central.sonatype.com](https://central.sonatype.com)
   → Publishing → Deployments: click **Publish** to release it, or **Drop** to discard it without
   publishing. A published version can't be changed or removed. To publish automatically once
   validated, run `make publish MVN="mvn -B -ntp -DautoPublish=true"`.

   Without a Central Portal login, use the user token and the deployment ID printed by `make publish`,
   in aem-platform-buildenv:
   ```
   $ make release-status DEPLOYMENT_ID=<id>
   $ make release-drop DEPLOYMENT_ID=<id>
   ```
6. Unset the secrets when you are done:
   `unset MAVEN_GPG_KEY MAVEN_GPG_PASSPHRASE MAVEN_CENTRAL_USERNAME MAVEN_CENTRAL_PASSWORD`
