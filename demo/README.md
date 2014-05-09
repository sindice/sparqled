# Demos of SPARQLed applications

- `bio2rdf` uses a Bio2RDF SPARQL endpoint as a showcase. This demo is the quickest to setup, given that the endpoint is up.
- `swdf` uses a Sesame Native repository SPARQL endpoint as a showcase, with the Semantic Web Dog Food corpus. This demo shows how to create repositories for quick tests.

## Graph Schema

SPARQLed relies on a `graph schema` for providing suggestions. It is basically a graph that describes the structure of the data, e.g., what are the classes, how do they relate to each other, etc. The demos are shipped with a pre-computed graph schema. Read this [page](https://github.com/sindice/sparqled/wiki/Graph-Schema-Computation) for details on how to create a graph schema.
