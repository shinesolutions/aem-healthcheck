# HealthCheck Servlet for AEM

[![Build Status](https://travis-ci.org/shinesolutions/aem-healthcheck.svg?branch=master)](https://travis-ci.org/shinesolutions/aem-healthcheck)

This servlet makes the JMX health check results accessible via HTTP.

It is used to execute Sling Health Checks based on provided tags (if no tags are provided, all registered health checks will be executed).
For the full list of provided health checks in AEM, go to [http://localhost:4502/system/console/healthcheck](http://localhost:4502/system/console/healthcheck).

Sample requests:
 * http://host:port/system/health
 * http://host:port/system/health?tags=devops
 * http://host:port/system/health?tags=devops,security
 * http://host:port/system/health?tags=devops,security&combineTagsOr=false

Sample response:
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

A `200` status code will be returned if ALL health checks return the status OK. Otherwise a `503` is returned.

Note: It is assumed that all `/system/*` paths are only accessible from a local network and not routed to the Internet.

## Building

This project uses Maven for building. Common commands:

From the root directory, run ``mvn -PautoInstallPackage clean install`` to build the bundle and content package and install to a CQ instance.

From the bundle directory, run ``mvn -PautoInstallBundle clean install`` to build *just* the bundle and install to a CQ instance.

## Specifying CRX Host/Port

The CRX host and port can be specified on the command line with:
mvn -Dcrx.host=otherhost -Dcrx.port=5502 <goals>


