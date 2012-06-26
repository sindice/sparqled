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
package org.sindice.analytics.servlet;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletOutputStream;

/**
 * A custom servlet output stream that stores its data in a buffer,
 * rather than sending it directly to the client.
 *
 * @author Eric M. Burke
 */
public class BufferedServletOutputStream extends ServletOutputStream {
    // the actual buffer
    private ByteArrayOutputStream bos = new ByteArrayOutputStream( );

    /**
     * @return the contents of the buffer.
     */
    public byte[] getBuffer( ) {
        return this.bos.toByteArray( );
    }

    /**
     * This method must be defined for custom servlet output streams.
     */
    public void write(int data) {
        this.bos.write(data);
    }

    // BufferedHttpResponseWrapper calls this method
    public void reset( ) {
        this.bos.reset( );
    }

    // BufferedHttpResponseWrapper calls this method
    public void setBufferSize(int size) {
        // no way to resize an existing ByteArrayOutputStream
        this.bos = new ByteArrayOutputStream(size);
    }
}