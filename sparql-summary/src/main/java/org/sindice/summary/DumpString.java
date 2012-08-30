package org.sindice.summary;

import java.io.IOException;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

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
public class DumpString extends Dump {
	private String _outputstr;

	public DumpString() {
	}

	/**
	 * Write a node in the output.
	 * 
	 * @param BindingSet
	 *          Result of the query from computeName()
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public void dumpRDFNode(BindingSet bindingSet)
	    throws QueryEvaluationException, IOException {

		if (bindingSet.hasBinding("label")) {
			_outputstr += bindingSet.getValue("label").toString() + "\t";
		} else {
			_outputstr += "Nothing\t";
		}
		if (bindingSet.hasBinding("cardinality")) {
			_outputstr += bindingSet.getValue("cardinality").toString() + "\n";
		} else {
			_outputstr += "Nothing\n";
		}
		// print other result, if exist
		Boolean otherResult = false;
		for (String name : bindingSet.getBindingNames()) {
			if (!name.equals("label") && !name.equals("cardinality")) {
				otherResult = true;
				_outputstr += name + " " + bindingSet.getValue(name).toString() + "\t";
			}

		}
		if (otherResult) {
			_outputstr += "\n";
		}
	}

	/**
	 * Write an edge in the output..
	 * 
	 * @param bindingSet
	 *          Result of the query from computePredicate()
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public void dumpRDFPred(BindingSet bindingSet)
	    throws QueryEvaluationException, IOException {

		if (bindingSet.hasBinding("label")) {
			_outputstr += bindingSet.getValue("label").toString() + "\t";
		} else {
			_outputstr += "Nothing\t";
		}
		if (bindingSet.hasBinding("cardinality")) {
			_outputstr += bindingSet.getValue("cardinality").toString() + "\t";
		} else {
			_outputstr += "Nothing\t";
		}
		if (bindingSet.hasBinding("source")) {
			_outputstr += bindingSet.getValue("source").toString() + "\t";
		} else {
			_outputstr += "Nothing\t";
		}
		if (bindingSet.hasBinding("target")) {
			_outputstr += bindingSet.getValue("target").toString() + "\n";
		} else {
			_outputstr += "Nothing\n";
		}

	}

	/**
	 * Reset the output string.
	 * 
	 * @param outputFile
	 *          Useless.
	 * @param domain
	 *          Useless.
	 */
	public void openRDF(String outputFile, String domain) {
		_outputstr = "";
	}

	/**
	 * Get the result string.
	 * 
	 * @return result string
	 */
	public String getResult() {
		return _outputstr;
	}

}
