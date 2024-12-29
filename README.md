## Intro
Java 21 introduced the Foreign Function and Memory API which greatly simplifies interacting
with native code and libraries. With the ability to call system APIs directly from Java, it
is now much easier to generate and execute machine code on the fly without relying on C/C++
as an intermediary.

This is a simple hello-world prototype that prints "Hello Machine!" from a function defined
in native x86-64 code. It works like this:

1. Map a `W|X` region of memory and copy the machine code there.* 
2. Register a signal handler with the callback pointing to the function that's present
   in the sample machine code. Make sure not to mask any signal that JVM itself would
   intercept for its own purposes. I am using SIGCONT, it looked (relatively) harmless to me.
3. Raise the signal.

\* While good enough for a prototype, having `W|X` pages in your program for an extended period
   of time violates Data Execution Prevention (DEP/NX) and is generally a bad idea.

## Build and Run
* The prototype only targets x86-64 Linux.
```shell
$ uname -a
Linux ubuntu-amd64 6.12.5-orbstack-00287-gf8da5d508983 # ...
```
I am testing this on guest Ubuntu running on OrbStack. Host is Apple Silicon Mac.

* The code uses APIs only available in Java 23, though with some changes you can get it to work
  on Java 21 as well.
```shell
$ java -version
openjdk version "23.0.1" 2024-10-15
OpenJDK Runtime Environment Zulu23.30+13-CA (build 23.0.1+11)
OpenJDK 64-Bit Server VM Zulu23.30+13-CA (build 23.0.1+11, mixed mode, sharing)
```

* Compile:
```shell
$ javac Demo.java
```

* Run:
```shell
$ java --enable-native-access=ALL-UNNAMED Demo
```

## Disclaimer
This prototype utilizes a new, experimental feature that is still under evaluation. The method
used here may not be reliable, and unexpected behavior or failures could occur. The author
disclaims any responsibility for potential damage, data loss, or unintended consequences
resulting from its use. Use at your own risk and exercise caution when deploying on critical
environments.
