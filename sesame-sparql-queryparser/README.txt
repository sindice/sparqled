This software is copyright Aduna (http://www.aduna-software.com/) � 2001-2011.
Licensed under the Aduna BSD-style license.

For the full text of the license, see the file LICENSE.txt.

By using, modifying or distributing this software you agree to be bound by the
terms of this license.

This software may include 3rd-party components that are licensed under
different conditions. For details see the file NOTICE.txt.


###################### SPARQL grammar for Recommendation ######################

# build from Sesame sparql 2.6.3 #

The Sesame SPARQL grammar has been changed for the SPARQL editor Recommendation
use case.

* Added on the 17 May 2012
- The "IRIref" state has an additional choice: the POF, i.e., the word "<" in
  the query.
- The "Object" state has an additional choice: the FillVar, i.e., a variable
  named ?FILL_VAR which is added when the object is missing. This allows to
  write "?s < " instead of adding a mandatory object, e.g., "?s < ?o".
- The "PathElt" state has been changed to specify a LOOKAHEAD of 2 for PathMod,
  in order to remove a choice conflict with the character '{'.
* Added on the 29 May 2012
- Updated the POF() clause: Recommendation with prefix filtering, on the QName
  and on an Incomplete URI_REF. Added LOOKAHEAD of 2 for the PrefixedName()
  clause.

# How to generate the grammar
- Download javacc from http://javacc.java.net/
- In the src/main/java/org/openrdf/sindice/query/parser/sparql/ast/ folder
-- Run jjtree on sparql.jjt
-- Run javacc on sparql.jj to create the AST classes
