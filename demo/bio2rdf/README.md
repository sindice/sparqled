# SPARQLed Demo using a SPARQL HTTP Endpoint

This demo uses the SPARQL endpoint of [iRefIndex](http://download.bio2rdf.org/release/2/irefindex/irefindex.html).

## Requirements

You must have a Tomcat instance up and running.
You must have downloaded or created the binaries. If not, follow those [steps](https://github.com/sindice/sparqled/wiki/Getting-Binaries). In this demo, we will use the `sparqled.war` binary.

## Create the Graph Schema

The graph schema of **iRefIndex** is already loaded into the SPARQL endpoint of **iRefIndex**, under the named graph `http://sindice.com/analytics`.

## Deploy SPARQLed

The application is configured to interact with the SPARQL endpoint of [iRefIndex](http://download.bio2rdf.org/release/2/irefindex/irefindex.html) at [http://cu.irefindex.bio2rdf.org/sparql](http://cu.irefindex.bio2rdf.org/sparql). This is done in the file `config.xml`.

1. Edit the file `irefindex.xml`: update the path to the `sparqled.war` file and replace `BASEDIR` with the path to the sparqled root folder.
2. Copy `irefindex.xml` to `$CATALINA_HOME/conf/Catalina/localhost`.
3. The application can be accessed at [http://localhost:8080/irefindex/](http://localhost:8080/irefindex/).
