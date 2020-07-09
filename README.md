# duktape4j

[![duktape4j](https://github.com/webfolderio/duktape4j/workflows/duktape4j/badge.svg)](https://github.com/webfolderio/duktape4j/actions) [![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fwebfolderio%2Fduktape4j.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fwebfolderio%2Fduktape4j?ref=badge_shield) [![License](https://img.shields.io/badge/license-Apache-blue.svg)](https://github.com/webfolderio/duktape4j/blob/master/LICENSE)

Java binding for [Duktape 2.5](https://github.com/svaarala/duktape), a very compact embedded ECMAScript (JavaScript) engine.

ducktape4j is Java port of [duktape-android](https://github.com/square/duktape-android/).

Supported Java Versions
-----------------------

Oracle & OpenJDK Java 8, 11.

Both the JRE and the JDK are suitable for use with this library.

Stability
---------
This library is suitable for use in production systems.

Supported Platforms
-------------------
* Windows 8 & Windows 10 (64-bit) (MSVC 2017)
* Ubuntu (64-bit) (GCC 8)
* macOS High Sierra (10.13) (GCC 8.3.0)

How it is tested
----------------
ducktape4j is regularly tested on [appveyor](https://ci.appveyor.com/project/WebFolder/duktape4j) (Windows), [circleci](https://circleci.com/gh/webfolderio/duktape4j) (Ubuntu) and [travis](https://travis-ci.org/webfolderio/duktape4j) (macOS).

Integration with Maven
----------------------

To use the official release of duktape4j, please use the following snippet in your `pom.xml` file.

Add the following to your POM's `<dependencies>` tag:

```xml
<dependency>
    <groupId>io.webfolder</groupId>
    <artifactId>duktape4j</artifactId>
    <version>1.2.0</version>
</dependency>
```

Download
--------
[duktape4j-1.2.0.jar](https://search.maven.org/remotecontent?filepath=io/webfolder/duktape4j/1.2.0/duktape4j-1.2.0.jar) - 1346 KB

Usage Examples
--------------

```java
public class HelloWorld {

    public static interface Console {
        default void info(String message) {
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        try (Duktape duktape = Duktape.create()) {
            duktape.set("console", Console.class, new Console() { });
            duktape.evaluate("console.info('hello, world!')");
        }
    }
}
```

License
-------
Licensed under the [Apache License](https://github.com/webfolderio/duktape4j/blob/master/LICENSE).

Note: The included C code from [Duktape](https://github.com/svaarala/duktape) is licensed under [MIT](https://github.com/svaarala/duktape/blob/master/LICENSE.txt) and [duktape-android](https://github.com/square/duktape-android) licensed under [Apache](https://github.com/square/duktape-android/blob/master/LICENSE).

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fwebfolderio%2Fduktape4j.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fwebfolderio%2Fduktape4j?ref=badge_large)
