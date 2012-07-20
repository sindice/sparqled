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
package org.sindice.analytics.queryProcessor;

import org.openrdf.sindice.query.parser.sparql.ast.SimpleNode;

/**
 * @author bibhas [Jul 12, 2012]
 * @email bibhas.das@deri.org
 * 
 */
public class POFMetadata {
  // the POF ast node
  private SimpleNode pofNode;
  /*
   * The class attribute of the POF, in case of CLASS recommendation
   */
  private SimpleNode pofClassAttribute;

  /**
   * @param pofNode
   *          the pofNode to set
   */
  public void setPofNode(SimpleNode pofNode) {
    this.pofNode = pofNode;
  }

  /**
   * @return the pofNode
   */
  public SimpleNode getPofNode() {
    return pofNode;
  }

  /**
   * @param pofClassAttribute
   *          the pofClassAttribute to set
   */
  public void setPofClassAttribute(SimpleNode pofClassAttribute) {
    this.pofClassAttribute = pofClassAttribute;
  }

  /**
   * @return the pofClassAttribute
   */
  public SimpleNode getPofClassAttribute() {
    return pofClassAttribute;
  }
}
