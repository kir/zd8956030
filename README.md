## TL;DR

This Maven server plugin logs requests that explicitly accept `application/json` but complete with `text/xml`, `application/xml`, or an XML media type ending in `+xml`.

[`JsonXmlMismatchInterceptor`](src/main/java/jetbrains/maxkir/zd8956030/JsonXmlMismatchInterceptor.java) extends `SkippableInterceptor` in package `jetbrains.maxkir.zd8956030`. It uses **`WebUtil.getRequestDump()`** for request details and logs a **stack trace for every mismatch**, including HTTP 200 responses. A controller exception, when present, is attached as the cause.

Install **`target/zd8956030-1.0.1-plugin.zip`**. Search **`teamcity-server.log`** for **`ZD8956030 JSON_XML_MISMATCH`**, at WARN level under **`jetbrains.buildServer.zd8956030`**.

## Build

Requires JDK 21, Maven 3.9+, and matching TeamCity internal Maven artifacts. The default is `2026.1-SNAPSHOT`; `SkippableInterceptor` and `RequestInterceptors` are internal `web-core` APIs.

```bash
mvn clean verify
```

Using the APIs already installed in this workspace:

```bash
mvn -Dteamcity.repository.url=file:///Users/kir/.m2/repository/2026.1 clean verify
```

Override `-Dteamcity.version=<matching-version>` and `-Dteamcity.repository.url=<repository-url>` for another target. Repository credentials belong in Maven settings. The TeamCity BOM supplies dependency versions.

The ZIP contains `teamcity-plugin.xml` and a single plugin JAR under `server/`. TeamCity, Spring, and servlet dependencies are provided by the server, and test dependencies are excluded.

The compatibility target is the customer's TeamCity 2026.1.1, build 222577, using `javax.servlet`. Local verification uses the available 2026.1 branch snapshot, not the exact customer's binaries. Other release lines require checking internal API compatibility.

## Install and collect evidence

1. Upload `target/zd8956030-1.0.1-plugin.zip` through Administration → Plugins, or install it in `<TeamCity Data Directory>/plugins`. Replace the previous `zd8956030` version if installed.
2. Confirm `ZD8956030 diagnostics enabled` in the server log.
3. Reproduce the problem and collect matching warnings, browser HAR, and logs from the same time period. On multiple nodes, install on each node serving the affected requests and collect each node's logs.
4. Disable or remove the plugin after collection. It unregisters on unload and supports runtime reload.

