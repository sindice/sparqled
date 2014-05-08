# SPARQLed Demo using Sesame Native repository as a SPARQL Endpoint

This demo uses Sesame [Native repository](http://openrdf.callimachus.net/sesame/2.7/docs/users.docbook?view#Creating_a_Native_RDF_Repository) as the SPARQL endpoint. The data comes from the [corpus](http://datahub.io/dataset/semantic-web-dog-food) of the Semantic Web Dog Food.

## Requirements

You must have a Tomcat instance up and running.
You must have downloaded or created the binaries. If not, follow those [steps](https://github.com/sindice/sparqled/wiki/Getting-Binaries). In this demo, we will use two binaries: `sesame-backend-xxx.jar` and `sparqled.war`.

## Create the Graph Schema

The data in the tarball `swdf.tar.bz2` is in N-Triples format. The graph schema has been created already using the [hadoop-based](https://github.com/sindice/summary) implementation of the graph schema computation, and is stored in the file `swdf-types.nq.bz2` in `N-Quads` format.

## Deploy SPARQLed

1. Create the Native repository with the data.
```sh
# Uncompress the bzip2 tarball
42sh$ tar xjvf swdf.tar.bz2
# Create the Native repository into the folder `native` with the statements in the files under `./swdf/` folder.
# The statements are added to the named graph `http://data.semanticweb.org/`.
42sh$ java -cp sesame-backend-xxx.jar org.sindice.core.sesame.backend.SesameBackendCLI --format N-TRIPLES --type NATIVE --args native --contexts http://data.semanticweb.org/ --add-rdf ./swdf/
```
2. Add the graph schema to the repository.
```sh
# Uncompress the data
42sh$ bunzip2 swdf-types.nq.bz2
# Add the statements to the named graph `http://data.semanticweb.org/summary`.
42sh$ java -cp sesame-backend-xxx.jar org.sindice.core.sesame.backend.SesameBackendCLI --format N-QUADS --type NATIVE --args native-summary --contexts http://data.semanticweb.org/summary --add-rdf ./swdf-types.nq
```
3. Update the path to the `native` folder in `config.xml`.
4. Update the paths to `sparqled.war` and to the sparqled home `demo/swdf/` in `swdf.xml`.
5. Copy `swdf.xml` to `$CATALINA_HOME/conf/Catalina/localhost`.
6. The application can be accessed at [http://localhost:8080/swdf/](http://localhost:8080/swdf/).
