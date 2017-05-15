# Rapid.io SDK for Android
[![Build Status](https://travis-ci.org/Rapid-SDK/android.svg?branch=master)](https://travis-ci.org/Rapid-SDK/android)

Rapid.io SDK for Android is an SDK written in Java for accessing Rapid.io realtime database.

## Features

- [-] Connect to Rapid.io database
- [-] Subscribe to changes
- [-] Mutate database
- [-] Authenticate
- [-] Complete Documentation

## Requirements

- Android API level 15

## Communication

- If you **found a bug**, open an issue.
- If you **have a feature request**, open an issue.
- If you **want to contribute**, submit a pull request.

## Installation

```groovy
compile 'io.rapid:rapid-sdk-android:0.0.1-alpha'
```

## Usage

### Initialization
```java
Rapid.initialize("<API_KEY>");
```
or add this to your AndroidManifest.xml file
```xml
<meta-data android:name="io.rapid.apikey" android:value="<API_KEY>" />
```

### API Reference
[JavaDoc](https://rapid-sdk.github.io/android/)

## Build
## Publishing
`./gradlew install` - deploy to local Maven Repository

`./gradlew bintrayUpload` - deploy to jCenter using Bintray API (for this to work you need to have Bintray API key set as an environment variable named `RAPID_BINTRAY_API_KEY`)

### Version name
Library version name is always based on the last Git tag name in current branch

## Documentation
`./gradlew generateReleaseJavadoc` - generates sdk API reference to `/docs` directory


## Changelog

## Credits

Rapid iOS SDK is owned and maintained by the [Rapid.io](http://www.rapid.io).

### Security Disclosure

If you believe you have identified a security vulnerability with Rapid Android SDK, you should report it as soon as possible via email to security@rapid.io. Please do not post it to a public issue tracker.

## License
    Copyright 2017 Rapid.io
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
      http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
