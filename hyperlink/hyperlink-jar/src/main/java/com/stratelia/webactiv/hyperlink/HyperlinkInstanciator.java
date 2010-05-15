/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * HyperlinkInstanciator.java
 *
 * Created on 22 juin 2001, 09:54
 */

package com.stratelia.webactiv.hyperlink;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

/**
 * @author nchaix
 * @version
 */
public class HyperlinkInstanciator extends Object implements ComponentsInstanciatorIntf {

  /** Creates new HyperlinkInstanciator */
  public HyperlinkInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("hyperlink", "HyperlinkInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "space = " + spaceId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("hyperlink", "HyperlinkInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "space = " + spaceId);
  }
}