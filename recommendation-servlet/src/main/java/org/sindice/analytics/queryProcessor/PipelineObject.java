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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.sindice.analytics.backend.DGSQueryResultProcessor;
import org.sindice.analytics.backend.DGSQueryResultProcessor.Context;
import org.sindice.analytics.ranking.Label;
import org.sindice.core.sesame.backend.SesameBackend;

/**
 * @author bibhas [Jul 11, 2012]
 * @email bibhas.das@deri.org
 * 
 */
public class PipelineObject {
  private ASTQueryContainer ast;
  private List<String> varsToProject;
  private RecommendationType type;
  private POFMetadata meta;
  private int MAX_HOPS;
  private SesameBackend<Label, Context> backend = null;

  public PipelineObject(ASTQueryContainer ast, List<String> list,
      RecommendationType type, POFMetadata meta, int hops,
      SesameBackend<Label, DGSQueryResultProcessor.Context> backend) {
    this.setAst(ast);
    this.setVarsToProject(list);
    this.setType(type);
    this.setMeta(meta);
    this.setMAX_HOPS(hops);
    this.setBackend(backend);
  }

  /**
   * @param ast
   *          the ast to set
   */
  public void setAst(ASTQueryContainer ast) {
    this.ast = ast;
  }

  /**
   * @return the ast
   */
  public ASTQueryContainer getAst() {
    return ast;
  }

  /**
   * @param varsToProject
   *          the varsToProject to set
   */
  public void setVarsToProject(List<String> varsToProject) {
    this.varsToProject = varsToProject;
    if (this.varsToProject == null)
      this.varsToProject = new ArrayList<String>();
  }

  /**
   * @return the varsToProject
   */
  public List<String> getVarsToProject() {
    return varsToProject;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(RecommendationType type) {
    this.type = type;
  }

  /**
   * @return the type
   */
  public RecommendationType getType() {
    return type;
  }

  /**
   * @param meta
   *          the meta to set
   */
  public void setMeta(POFMetadata meta) {
    this.meta = meta;
    if (this.meta == null)
      this.meta = new POFMetadata();
  }

  /**
   * @return the meta
   */
  public POFMetadata getMeta() {
    return meta;
  }

  /**
   * @param mAX_HOPS
   *          the mAX_HOPS to set
   */
  public void setMAX_HOPS(int mAX_HOPS) {
    MAX_HOPS = mAX_HOPS;
  }

  /**
   * @return the mAX_HOPS
   */
  public int getMAX_HOPS() {
    return MAX_HOPS;
  }

  /**
   * @param backend
   *          the backend to set
   */
  public void setBackend(
      SesameBackend<Label, DGSQueryResultProcessor.Context> backend) {
    this.backend = backend;
  }

  /**
   * @return the backend
   */
  public SesameBackend<Label, DGSQueryResultProcessor.Context> getBackend() {
    return backend;
  }

}
