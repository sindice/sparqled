package org.sindice.summary;

import java.io.IOException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.sindice.core.sesame.backend.SesameBackendException;
import org.sindice.core.sesame.backend.SesameBackendFactory;
import org.sindice.core.sesame.backend.SesameBackendFactory.BackendType;

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
public class QueryMemory extends Query {

	/**
	 * This constructor load a local SPRAQL repository in a SailRepository on
	 * the path defined by memoryStore. If there is no repository, it will be
	 * created.
	 * 
	 * @param d
	 *            The dump object.
	 * @param memoryStore
	 *            The path of the memoryStore.
	 * @throws RepositoryException
	 * @throws IOException
	 * @throws RDFParseException
	 * @throws SesameBackendException
	 */

	public QueryMemory(Dump d, String memoryStoreLocation)
	        throws RepositoryException, RDFParseException, IOException,
	        SesameBackendException {
		super(d);
		_repository = SesameBackendFactory.getDgsBackend(BackendType.MEMORY,
		        memoryStoreLocation);
		_repository.initConnection();
	}

	/**
	 * This constructor load a local SPRAQL repository in a SailRepository on
	 * the path defined by memoryStore. If there is no repository, it will be
	 * created.
	 * 
	 * @param memoryStore
	 *            The path of the memoryStore.
	 * @throws RepositoryException
	 * @throws IOException
	 * @throws RDFParseException
	 * @throws SesameBackendException
	 */

	public QueryMemory(String memoryStoreLocation) throws RepositoryException,
	        RDFParseException, IOException, SesameBackendException {
		super();
		_repository = SesameBackendFactory.getDgsBackend(BackendType.MEMORY,
		        memoryStoreLocation);
		_repository.initConnection();
	}

}
