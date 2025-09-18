# Quarkus demo: enterprise app with Hibernate ORM and Oracle database

This is a minimal CRUD service exposing a couple of endpoints over REST,
with a front-end based on Angular so you can play with it from your browser.

While the code is surprisingly simple, under the hood this is using:
 - RESTEasy to expose the REST endpoints
 - Hibernate ORM to perform the CRUD operations on the database
 - An Oracle database; see below to run one via Docker (or Podman)
 - ArC, the CDI inspired dependency injection tool with zero overhead
 - The high performance Agroal connection pool to manage the database connections
 - All safely coordinated by the Narayana Transaction Manager

## Requirements

To compile and run this demo you will need the Java JDK version 21.

Nothing else is required, although a fast internet connection is recommended as we'll be downloading
some large dependencies.

We're also going to use GraalVM to compile the native executable; if you have it installed we'll use it,
but if not don't worry: as long as you have a container engine (Docker or Podman) installed,
the Quarkus build plugin will download a suitable GraalVM distribution for you transparently.

In addition, you will need either an Oracle database, or a container engine to run one.

So neither Docker nor Podman are required, but if you don't have either of these, then you'll need to
install GraalVM yourself and have access to an Oracle database.

### Configuring GraalVM and JDK 21

Make sure you have `JAVA_HOME` environment variables set
and that a JDK 21 `java` command is available on the path.

If you want to use a GraalVM distribution which you have installed yourself,
make sure you have set the `GRAALVM_HOME` environment variable to point to it;
in this case it might make sense to also set the `JAVA_HOME` variable to point to it.

See the [Building a Native Executable guide](https://quarkus.io/guides/building-native-image)
for help setting up your environment.

JVMs more recent than 21 are also expected to work fine, but might log some additional warnings which are safe to ignore.

## Building the demo

Launch the Maven build on the checked out sources of this demo:

> ./mvnw package

N.B. When building in this way, integration tests will be triggered as well; this implies
the Quarkus will start an Oracle database, and will run the integration tests against it.
While this is all setup transparently, it might take a while as it will download the Oracle
database container image among all other dependencies.
Future builds will be faster, as the Oracle database container image will be cached locally,
like all Maven dependencies.

## Exploring the demo

### Live coding with Quarkus

The Maven Quarkus plugin provides a development mode that supports
live coding called "dev mode."

To try this out:

> ./mvnw quarkus:dev

In this mode you can make changes to the code and have the changes immediately applied, by just refreshing your browser.

Try hitting the "h" key (for help), you'll get a handy reminder of available commands, such as:
 - "w" to open the built application in your browser
 - "d" to toggle the dev UI

Dev Mode automatically starts a container with an Oracle database, as it detects you needing such a DB.
This feature is called ["Dev Services."](https://quarkus.io/guides/dev-services) and is used also to run the integration tests,
unless you explicitly configured a database to use.

    Hot reload works even when modifying your JPA entities.
    Try it! Even the database schema will be updated on the fly.

### Running next in optimised, production mode

As we're now going to run the application in "production mode", we won't be able to benefit from the dev services.
We need to have an Oracle database running; we can either use an existing one should you have one installed, or
we can start a quick "throw away" instance using a container:

> docker run -it --rm=true --name quarkus_test -p 1521:1521 -e ORACLE_PASSWORD=quarkus_test gvenzl/oracle-free:23-slim-faststart

N.B. We set the '--rm=true' option to automatically remove the container when it exits, you wouldn't want to do that with a real database:
all data stored in it will be lost on shutdown. This is great for such explorations.

Connection properties for the datasource are defined in the standard Quarkus configuration file,
`src/main/resources/application.properties`.

In this demo, the application is configured to connect by default to the database which we started using the above docker command.
Should you want to connect to a different database, you can either change the defaults of the built application by editing the `application.properties` file,
or you can pass the following JVM argument to override the defaults you've built with.

> -Dquarkus.datasource.jdbc.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1

As we love consistency, the same approach is valid for both the JVM and native-image modes.


#### Run Quarkus in JVM mode

Once you have a database running, you can run the application in JVM mode.

First, make sure to build it. We'll skip tests this time as we're just exploring:

> ./mvnw package -DskipTests

Now technically we could start the application, but we have no database schema to work with.
The Quarkus application is configured for production mode, so by default it won't try to create a schema; we can ask it to:

> java -Dquarkus.hibernate-orm.schema-management.strategy=create -jar ./target/quarkus-app/quarkus-run.jar

Now we can stop it, and we're finally ready to run it like you would in production:

> java -jar ./target/quarkus-app/quarkus-run.jar

    Have a look at how fast it boots.
    Or measure total native memory consumption...

### Run Quarkus as a native application

You can also create a native executable from this application without making any
source code changes. A native executable removes the dependency on the JVM:
everything needed to run the application on the target platform is included in
the executable, allowing the application to run with minimal resource overhead.

Generating such an application requires the GraalVM native-image tool, which will
need to process your entire application and all its dependencies, including the JDK:
this is fairly compute intensive so it might take a minute or two, but it's
worth it as it performs a comprehensive code analysis to performance advanced
optimisations, and strips out any unnecessary code.

Use the `native` profile to compile a
native executable:

> ./mvnw package -Dnative -DskipTests

After getting a cup of coffee, you'll be able to run this binary directly:

> ./target/hibernate-orm-quickstart-1.0.0-SNAPSHOT-runner

    Please brace yourself: don't choke on that fresh cup of coffee you just got.
    
    Now observe the time it took to boot, and remember: that time was mostly spent to generate the tables in your database and import the initial data.
    If you run it multiple times, you'll see errors logged as the tables already exist and the data import would cause conflicts: this proofs the point.
    
    Next, maybe you're ready to measure how much memory this service is consuming.

N.B. This implies all dependencies have been compiled to native;
that's a whole lot of stuff, not just the couple classes we have in this project.
From the bytecode enhancements that Hibernate ORM applies to your entities, to the lower level essential components such
as the famous Undertow webserver, as the Oracle JDBC driver, the same Transaction Manager implementation powering enterprise JakartaEE servers,
the entire set of JDK classes required to run this (the useless ones will have been stripped!), and all other
components of the JVM runtime, such as a garbage collector implementation.

## See the demo in your browser

Navigate to:

<http://localhost:8080/index.html>

Have fun, build something great and join the team of contributors!
