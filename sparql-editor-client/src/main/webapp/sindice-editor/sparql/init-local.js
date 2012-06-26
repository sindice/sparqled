$(document).ready(function() {

  var sampleName1_1 = "Example Query";
  var sampleDescription1_1 = "How to get the classes ?";
  var sampleQuery1_1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n\nSELECT DISTINCT ?type WHERE {\n\t?s a ?type.\n}\nLIMIT 10";

  var flintConfig = {
    "editorTitle" : "SparQLed",
    "interface" : {
      "toolbar" : true,
      "menu" : true
    },
    "namespaces" : [{
      "name" : "Friend of a friend",
      "prefix" : "foaf",
      "uri" : "http://xmlns.com/foaf/0.1/"
    }, {
      "name" : "XML schema",
      "prefix" : "xsd",
      "uri" : "http://www.w3.org/2001/XMLSchema#"
    }, {
      "name" : "SIOC",
      "prefix" : "sioc",
      "uri" : "http://rdfs.org/sioc/ns#"
    }, {
      "name" : "Resource Description Framework",
      "prefix" : "rdf",
      "uri" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    }, {
      "name" : "Resource Description Framework schema",
      "prefix" : "rdfs",
      "uri" : "http://www.w3.org/2000/01/rdf-schema#"
    }, {
      "name" : "Dublin Core",
      "prefix" : "dc",
      "uri" : "http://purl.org/dc/elements/1.1/"
    }, {
      "name" : "Dublin Core terms",
      "prefix" : "dct",
      "uri" : "http://purl.org/dc/terms/"
    }, {
      "name" : "Creative Commons",
      "prefix" : "cc",
      "uri" : "http://www.creativecommons.org/ns#"
    }, {
      "name" : "Web Ontology Language",
      "prefix" : "owl",
      "uri" : "http://www.w3.org/2002/07/owl#"
    }, {
      "name" : "Simple Knowledge Organisation System",
      "prefix" : "skos",
      "uri" : "http://www.w3.org/2004/02/skos/core#"
    }, {
      "name" : "Geography",
      "prefix" : "geo",
      "uri" : "http://www.w3.org/2003/01/geo/wgs84_pos#"
    }, {
      "name" : "Geonames",
      "prefix" : "geonames",
      "uri" : "http://www.geonames.org/ontology#"
    }, {
      "name" : "DBPedia property",
      "prefix" : "dbp",
      "uri" : "http://dbpedia.org/property/"
    }, {
      "name" : "Open Provenance Model Vocabulary",
      "prefix" : "opmv",
      "uri" : "http://purl.org./net/opmv/ns#"
    }, {
      "name" : "Functional Requirements for Bibliographic Records",
      "prefix" : "frbr",
      "uri" : "http://purl.org/vocab/frbr/core#"
    }],
    "defaultEndpointParameters" : {
      "queryParameters" : {
        "format" : "output",
        "query" : "query"
      },
      "selectFormats" : [{
        "name" : "Table",
        "format" : "text",
        "type" : "text/formatted"
      }, {
        "name" : "Plain text",
        "format" : "text",
        "type" : "text/plain"
      },
      //{"name": "SPARQL-XML", "format": "sparql", "type": "application/sparql-results+xml"},
      {
        "name" : "JSON",
        "format" : "json",
        "type" : "application/sparql-results+json"
      }],
      "constructFormats" : [{
        "name" : "Plain text",
        "format" : "text",
        "type" : "text/plain"
      }, {
        "name" : "RDF/XML",
        "format" : "rdfxml",
        "type" : "application/rdf+xml"
      }, {
        "name" : "Turtle",
        "format" : "turtle",
        "type" : "application/turtle"
      }]
    },
    "endpoints" : [{
      "name" : "servlet",
      "uri" : "../AssistedSparqlEditorServlet",
      queries : [{
        "name" : sampleName1_1,
        "description" : sampleDescription1_1,
        "query" : sampleQuery1_1
      }]
    },
    //{"name": "graph-summary", "uri": "http://sparql.sindice.com/sparql"},
    {
      "name" : "submit-endpoint",
      "uri" : "../SparqlQueryServlet"
    }],

  }
  var flintEd = new FlintEditor("flint-test", "sparql/images", flintConfig);
});
