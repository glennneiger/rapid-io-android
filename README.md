<p align="center">
  <img alt="logo" src="https://raw.githubusercontent.com/Rapid-SDK/android/master/extras/logo.png" />
</p>
<hr/>


<p align="center">
  <strong>Android client for <a href="https://rapid.io">Rapid.io</a></strong> realtime database 
</p>
<h3 align="center">
	<a href="https://rapid.io">
	  Website
	</a>
	<span> | </span>
	<a href="https://rapid.io/docs">
	  Documentation
	</a>
	<span> | </span>
	<a href="https://rapid.io/docs/api-reference/android">
	  Reference
	</a>
</h3>

[![Build Status](https://travis-ci.org/Rapid-SDK/android.svg?branch=master)](https://travis-ci.org/Rapid-SDK/android) [![Download](https://api.bintray.com/packages/rapid/io.rapid/rapid-sdk-android/images/download.svg)](https://bintray.com/rapid/io.rapid/rapid-sdk-android/_latestVersion) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) 

# What
Rapid.io is a cloud-hosted service that allows app developers to build realtime user interfaces without having to worry about the underlying infrastructure. It works as a non-relational data store accessible from a client-side code.


# Why
Clients can create, update, delete and subscribe to a set of data and receive updates in realtime.


# How

### Gradle

```groovy
compile 'io.rapid:rapid-sdk-android:1.0.17'
```

See [Getting Started](https://rapid.io/docs/getting-started) for more information.

## Building

### Deployment
`./gradlew install` - deploy to local Maven Repository

`./gradlew bintrayUpload` - deploy to jCenter using Bintray API (for this to work you need to have Bintray API key set as an environment variable named `RAPID_BINTRAY_API_KEY`)

### Generate JavaDoc
`./gradlew generateReleaseJavadoc` - generates sdk API reference to `/docs` directory

### Version name
Library version name is always based on the last Git tag name in current branch


## Caught a bug? 
Open an issue.


## License
[The MIT License](/LICENSE.md)
