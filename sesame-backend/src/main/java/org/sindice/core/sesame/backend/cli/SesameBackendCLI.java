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
package org.sindice.core.sesame.backend.cli;

import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.sindice.core.sesame.backend.SesameBackendException;

public class SesameBackendCLI
extends AbstractSesameBackendCLI {

  @Override
  protected void initializeOptionParser(OptionParser parser) {}

  @Override
  protected void execute(OptionSet options) {}

  public static void main(String[] args)
  throws IOException, SesameBackendException {
    SesameBackendCLI cli = new SesameBackendCLI();
    cli.run(args);
  }

}
