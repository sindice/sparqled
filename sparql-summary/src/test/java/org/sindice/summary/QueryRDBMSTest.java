//package org.sindice.summary;
//
//import static org.junit.Assert.fail;
//
//import java.util.Stack;
//
//import org.junit.Test;
//import org.openrdf.query.BindingSet;
//import org.openrdf.query.TupleQueryResult;
//import org.openrdf.repository.RepositoryException;

/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
 */
/**
 * @author Pierre Bailly <pierre.bailly@deri.org>
 */
//public class QueryRDBMSTest {
//
//	protected Logger _logger;
//
//	@Before
//	public void initLogger() {
//		_logger = Logger.getLogger(QueryRDBMSTest.class);
//	}
//	
//	@Test
//	public void test() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT1", "testuser", "miaou");
//			try {
//				q.stopConnexion();
//			} catch (RepositoryException e) {
//				// Nothing for this test
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("Something wrong append");
//		}
//	}
//
//	@Test
//	public void testLaunchQuery() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT2", "testuser", "miaou");
//
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//			q._queriesResults = new Stack<TupleQueryResult>();
//			q.launchQuery("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 1");
//
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = q._queriesResults.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("Something wrong append");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				// Nothing for this test
//			}
//		}
//	}
//
//	@Test
//	public void testMakeGroupConcat() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT3", "testuser", "miaou");
//		} catch (Exception e) {
//		}
//		String s = q.makeGroupConcat("?s", "?init", "?group");
//		String ref = "        SELECT ?s (GROUP_CONCAT(IF(isURI(?init),\n"
//		        + "                concat('<', str(?init), '>'),\n"
//		        + "                concat('\"', ENCODE_FOR_URI(?init), '\"'))) AS ?group)\n";
//		if (!s.equals(ref))
//			fail("invalid RDBMS group concat.");
//	}
//
//	@Test
//	public void testConstructSearchSequence() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT4", "testuser", "miaou");
//		} catch (Exception e) {
//		}
//		if (!q.constructSearchSequence(0) || !q._searchSequence.equals("")) {
//			fail("Invalid result when there is neither limit, neither pagination.");
//		}
//
//		q.setLimit(10);
//		if (!q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 10\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid result when there is only a limit.");
//		}
//
//		q.setLimit(0);
//		q.setPagination(10);
//		if (q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 10\nOFFSET 0\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid result when there is only a pagination at the "
//			        + "iteration 0.");
//		}
//
//		if (q.constructSearchSequence(1)
//		        || !q._searchSequence.equals("LIMIT 10\nOFFSET 10\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid result when there is only a pagination at the "
//			        + "iteration 1.");
//		}
//
//		q.setPagination(15);
//		if (q.constructSearchSequence(10)
//		        || !q._searchSequence.equals("LIMIT 15\nOFFSET 150\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid result when there is only a pagination at the "
//			        + "iteration 15.");
//		}
//
//		q.setLimit(5);
//		q.setPagination(10);
//		if (!q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 5\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid result when there is a limit lower than the pagination.");
//		}
//
//		q.setLimit(7);
//		q.setPagination(5);
//		if (q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 5\nOFFSET 0\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid result when there is limit and pagination "
//			        + "at the first iteration.");
//		}
//		if (!q.constructSearchSequence(1)
//		        || !q._searchSequence.equals("LIMIT 2\nOFFSET 5\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid result when there is limit and pagination at "
//			        + "the last iteration.");
//		}
//	}
//
//	@Test
//	public void testAddFileToRepository() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT5", "testuser", "miaou");
//			try {
//				q.addFileToRepository("src/test/resources/unit_test_name.nt");
//				q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//				fail("Cannot add files.");
//			}
//			q._queriesResults = new Stack<TupleQueryResult>();
//			q.launchQuery("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 1");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("Something wrong append");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				// Nothing for this test
//			}
//		}
//	}
//
//	@Test
//	public void testSetLimitName() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT6", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setLimit(1);
//		if (!q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 1\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"Animal\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//
//	}
//
//	@Test
//	public void testUnsetLimitName() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT7", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setLimit(10);
//		q.unsetLimit();
//		if (!q.constructSearchSequence(0) || !q._searchSequence.equals("")) {
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"Animal\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"Thing\",<http://opengraphprotocol.org/schema/type>}\" \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"double\",<http://opengraphprotocol.org/schema/type>} {\"type\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testSetLimitPred() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT8", "testuser", "miaou");
//			;
//			q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setLimit(1);
//		if (!q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 1\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//
//	}
//
//	@Test
//	public void testUnsetLimitPred() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT9", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setLimit(10);
//		q.unsetLimit();
//		if (!q.constructSearchSequence(0) || !q._searchSequence.equals("")) {
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/like \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Human\"\" \n"
//			        + "http://opengraphprotocol.org/schema/think_at \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Animal\"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testSetPaginationName() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT10", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setPagination(1);
//		if (q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 1\nOFFSET 0\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.peek();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (!q._queriesResults.empty()) {
//				queryResult = queriesResult.pop();
//				while (queryResult.hasNext()) {
//					BindingSet bindingSet = queryResult.next();
//					for (String name : queryResult.getBindingNames()) {
//						result += bindingSet.getValue(name) + " ";
//					}
//					result += "\n";
//				}
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"double\",<http://opengraphprotocol.org/schema/type>} {\"type\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"Thing\",<http://opengraphprotocol.org/schema/type>}\" \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"Animal\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//
//	}
//
//	@Test
//	public void testUnsetPaginationName() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT11", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setPagination(10);
//		q.unsetPagination();
//		if (!q.constructSearchSequence(0) || !q._searchSequence.equals("")) {
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.peek();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (!q._queriesResults.empty()) {
//				queryResult = queriesResult.pop();
//				while (queryResult.hasNext()) {
//					BindingSet bindingSet = queryResult.next();
//					for (String name : queryResult.getBindingNames()) {
//						result += bindingSet.getValue(name) + " ";
//					}
//					result += "\n";
//				}
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"Animal\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"Thing\",<http://opengraphprotocol.org/schema/type>}\" \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"double\",<http://opengraphprotocol.org/schema/type>} {\"type\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testSetPaginationPred() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT12", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setPagination(1);
//		if (q.constructSearchSequence(0)
//		        || !q._searchSequence.equals("LIMIT 1\nOFFSET 0\n")) {
//			_logger.error(q._searchSequence);
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.peek();
//
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (!q._queriesResults.empty()) {
//				queryResult = queriesResult.pop();
//				while (queryResult.hasNext()) {
//					BindingSet bindingSet = queryResult.next();
//					for (String name : queryResult.getBindingNames()) {
//						result += bindingSet.getValue(name) + " ";
//					}
//					result += "\n";
//				}
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/think_at \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Animal\"\" \n"
//			        + "http://opengraphprotocol.org/schema/like \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Human\"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n";
//
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//
//	}
//
//	@Test
//	public void testUnsetPaginationPred() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT13", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setPagination(10);
//		q.unsetPagination();
//		if (!q.constructSearchSequence(0) || !q._searchSequence.equals("")) {
//			fail("Invalid constructSearchSequence");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/like \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Human\"\" \n"
//			        + "http://opengraphprotocol.org/schema/think_at \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Animal\"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testSetGraphName() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT14", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setGraph("sindice:domain", "sparql.sindice.org");
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.peek();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "null \"0\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testUnsetGraphName() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT15", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setGraph("sindice:domain", "sparql.sindice.org");
//		q.unsetGraph();
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"Animal\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"Thing\",<http://opengraphprotocol.org/schema/type>}\" \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"double\",<http://opengraphprotocol.org/schema/type>} {\"type\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testSetGraphPred() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT6", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setGraph("sindice:domain", "sparql.sindice.org");
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.peek();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "null \"0\"^^<http://www.w3.org/2001/XMLSchema#integer> null null \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testUnsetGraphPred() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT17", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		q.setGraph("sindice:domain", "sparql.sindice.org");
//		q.unsetGraph();
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/like \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Human\"\" \n"
//			        + "http://opengraphprotocol.org/schema/think_at \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Animal\"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testGetName() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT18", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_name.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"Animal\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"Thing\",<http://opengraphprotocol.org/schema/type>}\" \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"double\",<http://opengraphprotocol.org/schema/type>} {\"type\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testGetPredicate() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT19", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_pred.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Animal\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/firstName \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/like \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Human\"\" \n"
//			        + "http://opengraphprotocol.org/schema/think_at \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\"\" \"\"Animal\"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/test \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"double\" \"type\"\" \"\" \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testNameMultipleDomain() {
//		Query q = null;
//		try {
//			q = new QueryMemory("./testUNIT/RDBMSstore20");
//			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"Human\",<http://opengraphprotocol.org/schema/type>} {\"Thing\",<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n"
//			        + "\"{\"Thing\",<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>} {\"Thing\",<http://ogp.me/ns#type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testPredMultipleDomain() {
//		Query q = null;
//		try {
//			q = new QueryMemory("./testUNIT/RDBMSstore21");
//			q.addFileToRepository("src/test/resources/unit_test_multidomain.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://ogp.me/ns#type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Thing\" \"Thing\"\" \"\" \n"
//			        + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Thing\" \"Thing\"\" \"\" \n"
//			        + "http://purl.org/dc/elements/1.1/like \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Thing\" \"Thing\"\" \"\"Human\" \"Thing\"\" \n"
//			        + "http://ogp.me/ns#like \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\" \"Thing\"\" \"\"Thing\" \"Thing\"\" \n"
//			        + "http://opengraphprotocol.org/schema/type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\" \"Thing\"\" \"\" \n"
//			        + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Human\" \"Thing\"\" \"\" \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testNameBlankNode() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT23", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_blank.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getName();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality \n"
//			        + "\"{\"Thing\",<http://opengraphprotocol.org/schema/type>}\" \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \n";
//
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testPredBlankNode() {
//		Query q = null;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT24", "testuser", "miaou");
//			q.addFileToRepository("src/test/resources/unit_test_blank.nt");
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("wrong initialisation");
//		}
//
//		String result = "";
//		try {
//			Stack<TupleQueryResult> queriesResult = q.getPredicate();
//			if (q._queriesResults.empty()) {
//				fail("No result");
//			}
//			TupleQueryResult queryResult = queriesResult.pop();
//			if (!queryResult.hasNext()) {
//				fail("Empty Query");
//			}
//			for (String name : queryResult.getBindingNames()) {
//				result += name + " ";
//			}
//			result += "\n";
//
//			while (queryResult.hasNext()) {
//				BindingSet bindingSet = queryResult.next();
//				for (String name : queryResult.getBindingNames()) {
//					result += bindingSet.getValue(name) + " ";
//				}
//				result += "\n";
//			}
//			queryResult.close();
//			String ref = "label cardinality source target \n"
//			        + "http://opengraphprotocol.org/schema/type \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Thing\"\" \"\" \n"
//			        + "http://opengraphprotocol.org/schema/link \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> \"\"Thing\"\" \"\" \n";
//			if (!result.equals(ref)) {
//				_logger.error(result);
//				fail("The query and thee ref have differents outputs");
//			}
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			_logger.error(result);
//			fail("Cannot parse the query.");
//		} finally {
//			try {
//				q.stopConnexion();
//			} catch (Exception e) {
//				_logger.error(e.getMessage());;
//			}
//		}
//	}
//
//	@Test
//	public void testStopConnexion() {
//		Query q;
//		try {
//			q = new QueryRDBMS("localhost", "testUNIT22", "testuser", "miaou");
//			q.stopConnexion();
//		} catch (Exception e) {
//			_logger.error(e.getMessage());;
//			fail("Cannot close the connection");
//		}
//	}
//
// }
