# SPARQLed Demo using a SPARQL HTTP Endpoint

This demo uses the SPARQL endpoint of [iRefIndex](http://download.bio2rdf.org/release/2/irefindex/irefindex.html).

## Requirements

You must have a Tomcat instance up and running.
You must have downloaded or created the binaries. If not, follow those [steps](https://github.com/sindice/sparqled/wiki/Getting-Binaries).

## Deploy SPARQLed

The application must be configured to interact with [2].

1. Edit the file `irefindex.xml`
Update the path to the `sparqled.war` file and replace `BASEDIR` with the path to the sparqled root folder.
2. The file `config.xml` is already configured for [2].
3. Copy `irefindex.xml` to `$CATALINA_HOME/conf/Catalina/localhost`.
4. The application can be accessed at http://localhost:8080/irefindex/
