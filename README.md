# Lab 2

### Build and Run

Lab 2 requires [Tomcat](https://tomcat.apache.org/download-90.cgi) v9+ to run.
And [Maven](https://maven.apache.org/download.cgi) to build it.

Build *.war file.

```sh
$ cd Lab1
$ mvn package
```
In `target/` folder you can find *.war file. Put this file in `webapp/` directiry of Tomcat and run Tomcat.
Also you must set the environment variables - `$CATALINA_HOME`.
