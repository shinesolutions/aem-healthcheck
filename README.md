# HealthCheck Servlet for AEM

[![Build Status](https://travis-ci.org/shinesolutions/aem-healthcheck.svg?branch=master)](https://travis-ci.org/shinesolutions/aem-healthcheck)

This servlet makes the JMX health check results accessible via HTTP.

It is used to execute Sling Health Checks based on provided tags (if no tags are provided, all registered health checks will be executed).
For the full list of provided health checks in AEM, go to [http://localhost:4502/system/console/healthcheck](http://localhost:4502/system/console/healthcheck).

Sample requests:

 * http://host:port/system/health
 * http://host:port/system/health?tags=shallow
 * http://host:port/system/health?tags=shallow,security
 * http://host:port/system/health?tags=shallow,security&combineTagsOr=false

Sample responses:

###### Shallow:
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
###### Deep:
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

A `200` status code will be returned if ALL health checks return the status OK. Otherwise a `503` is returned.

Note: It is assumed that all `/system/*` paths are only accessible from a local network and not routed to the Internet.

## Building

This project uses Maven for building. Common commands:

From the root directory, run ``mvn -PautoInstallPackage clean install`` to build the bundle and content package and install to a CQ instance.

From the bundle directory, run ``mvn -PautoInstallBundle clean install`` to build *just* the bundle and install to a CQ instance.

## Release

Create a release branch off the `master` branch

```
git branch release/X.X
```

Prepare the release (use vX.X for the tag)

```
mvn release:prepare
```

Push the branch to the repository

```
git push origin
```

Checkout the newly created tag and build the project

```
git checkout tags/vX.X
```

```
mvn clean package
```

Attach the CRX package to the release on Github and merge back to `master`.


## Specifying CRX Host/Port

The CRX host and port can be specified on the command line with:

```
mvn -Dcrx.host=otherhost -Dcrx.port=5502 <goals>
```

## Available Tags

Besides the out-of-the-box tags, two new tags have been incorporated to the health check:
### Custom tags

|       Tag       | Health Checks    | Description |
|:---------------:|------------------|-------------|
| Shallow, Devops | SmokeHealthCheck | This tag runs a simple Smoke health check that determines if an instance is running.                                                                                                                                                                                                                                          |
|   Deep          | <ul><li>ActiveBundleHealthCheck</li><li>ReplicationQueueHealthCheck</li><li>SlingJobsHealthCheck</li></ul> | This tag runs three different health checks.<ul><li>The first health check scans the current OSGi bundles and reports if there is any inactive bundles.</li><li>The second health check verifies the replication queue of agents.</li><li>The third health check tests the number of active jobs and their general health in the queue.</li></ul> |

### Out-of-the-box tags

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
