# SPARQLed Demo using a SPARQL HTTP Endpoint

This demo uses the SPARQL endpoint of iRefIndex [1] located at [2].

## Requirements

You must have a Tomcat instance up and running.
You must have downloaded or created the binaries. If not, follow the steps below:

1. Initialize submodules
```sh
42sh$ git submodule init
42sh$ git submodule update
```
2. Create the binaries
```sh
42sh$ mvn package
```

## Deploy SPARQLed

The application must be configured to interact with [2].

1. Edit the file `irefindex.xml`
Update the path to the `sparqled.war` file and replace `BASEDIR` with the path to the sparqled root folder.
2. The file `config.xml` is already configured for [2].
3. Copy `irefindex.xml` to `$CATALINA_HOME/conf/Catalina/localhost`.
4. The application can be accessed at http://localhost:8080/irefindex/

[1] http://download.bio2rdf.org/release/2/irefindex/irefindex.html
[2] http://cu.irefindex.bio2rdf.org/sparql
