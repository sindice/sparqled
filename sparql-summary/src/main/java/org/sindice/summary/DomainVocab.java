package org.sindice.summary;

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
public enum DomainVocab {
	// always RDF definition in the first place
	rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"), og(
	        "http://opengraphprotocol.org/schema/"), ogp("http://ogp.me/ns#"), ogorg(
	        "http://opengraph.org/schema/"), dc(
	        "http://purl.org/dc/elements/1.1/"), dbp(
	        "http://dbpedia.org/property/"), voca(
	        "http://vocab.sindice.net/analytics#");

	private String uri;

	private DomainVocab(String uri) {
		this.uri = uri;
	}

	public String uri(String term) {
		return "PREFIX " + term + ": <" + this.uri + ">\n";
	}

	public String type() {
		return this.uri + "type";
	}
}