![Rapid.io](extras/rapid.png)

# Rapid.io real-time database SDK  for Android
[![Build Status](https://travis-ci.org/Rapid-SDK/android.svg?branch=master)](https://travis-ci.org/Rapid-SDK/android)


## Installation

```groovy
compile 'io.rapid:rapid-sdk-android:0.0.7-alpha'
```


## Documentation

For complete documentation visit [Rapid.io](https://www.rapid.io)

### API Reference
[JavaDoc API Reference](https://rapid-sdk.github.io/android/)


## Communication

- If you **found a bug**, open an issue.
- If you **have a feature request**, open an issue.
- If you **want to contribute**, submit a pull request.

### Security Disclosure

If you believe you have identified a security vulnerability with Rapid Android SDK, you should report it as soon as possible via email to [security@rapid.io](mailto:security@rapid.io). Please do not post it to a public issue tracker.


## Building

### Publishing
`./gradlew install` - deploy to local Maven Repository

`./gradlew bintrayUpload` - deploy to jCenter using Bintray API (for this to work you need to have Bintray API key set as an environment variable named `RAPID_BINTRAY_API_KEY`)

### Documentation
`./gradlew generateReleaseJavadoc` - generates sdk API reference to `/docs` directory

### Version name
Library version name is always based on the last Git tag name in current branch

## Changelog


## Credits

Rapid.io Android SDK is owned and maintained by [Rapid.io](http://www.rapid.io)


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
