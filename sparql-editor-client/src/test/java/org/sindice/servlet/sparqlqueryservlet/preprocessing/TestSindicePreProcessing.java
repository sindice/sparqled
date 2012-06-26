package org.sindice.servlet.sparqlqueryservlet.preprocessing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

/*******************************************************************************
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * @author Stephane Campinas <stephane.campinas@deri.org>
 */
public class TestSindicePreProcessing {

	private final SindicePreProcessing pre = new SindicePreProcessing() {
		{
			init("http://purl.org/dc/elements/1.1/isPartOf");
		}
	};

	@Test
	public void testQueryNoGraphs() throws Exception {
		String query = "SELECT * { ?s ?p ?o }";
		String processed = pre.process(query);
		assertEquals("SELECT *\nWHERE {\n  ?s ?p ?o .\n}\n", processed);
	}

	@Test
	public void testPropertyListPath() throws Exception {
		String query = "SELECT * { ?s ?p ?o , ?o2, <toto> ;?b ?c;<name> ?e }";
		String processed = pre.process(query);
		assertEquals(
		        "SELECT *\nWHERE {\n  ?s ?p ?o ,?o2 ,<toto> ;?b ?c ;<name> ?e .\n}\n",
		        processed);
	}

	@Test
	public void testBlankNodePropertyList() throws Exception {
		String query = "SELECT * { ?s ?p [ ?p ?type1 , ?type2 ; <name> \"titi\" ] }";
		String processed = pre.process(query);
		assertEquals(
		        "SELECT *\nWHERE {\n  ?s ?p [?p ?type1 ,?type2 ;<name> \"titi\" ].\n}\n",
		        processed);
	}

	@Test
	public void testQueryWithGraphs() throws Exception {
		String query1 = "SELECT * { GRAPH <http://sindice.com> { ?a ?b ?c } ?s ?p ?o  }";
		String processed = pre.process(query1);

		final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(),
		        "a", "b", "c", "s", "p", "o");
		assertEquals(1, vars.length);
		assertEquals("SELECT *\nWHERE {\nGRAPH ?" + vars[0]
		        + " {\n  ?a ?b ?c .\n}\n  ?s ?p ?o .\n  ?" + vars[0] + " <"
		        + SindicePreProcessing.isPartOf.getValue()
		        + "> <http://sindice.com> .\n}\n", processed);
	}

	@Test
	public void testQueryWithGraphs2() throws Exception {
		String query1 = "SELECT * { GRAPH <http://acme.com> { ?a ?b ?c } graph ?g { ?s ?p ?o }  }";
		String processed = pre.process(query1);

		final String[] vars = filter(ASTVarGenerator.getCurrentVarNames(),
		        "a", "b", "c", "s", "p", "o");
		assertEquals(2, vars.length);
		assertEquals("SELECT *\nWHERE {\nGRAPH ?" + vars[0]
		        + " {\n  ?a ?b ?c .\n}\n" + "GRAPH ?" + vars[1]
		        + " {\n  ?s ?p ?o .\n}\n" + "  ?" + vars[0] + " <"
		        + SindicePreProcessing.isPartOf.getValue()
		        + "> <http://acme.com> .\n" + "  ?" + vars[1] + " <"
		        + SindicePreProcessing.isPartOf.getValue() + "> ?g .\n}\n",
		        processed);
	}

	@Test(expected = Exception.class)
	public void testQueryWithFROM() throws Exception {
		String query = "SELECT * FROM <http://sindice.com> { ?s ?p ?o }";
		pre.process(query);
	}

	private final String[] filter(String[] vars, String... toFilter) {
		final ArrayList<String> v = new ArrayList<String>();
		final HashSet<String> f = new HashSet<String>(Arrays.asList(toFilter));

		for (String var : vars) {
			if (!f.contains(var)) {
				v.add(var);
			}
		}
		return v.toArray(new String[0]);
	}

}
