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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.forums.forumEntity.ejb;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Clé primaire associée à un forum.
 * @author frageade
 * @since November 2000
 */
public class ForumPK extends WAPrimaryKey {

  private String domain;

  public ForumPK(String component, String domain, String id) {
    super(id, component);
    this.domain = domain;
  }

  public ForumPK(String component, String domain) {
    this(component, domain, "0");
  }

  public String getDomain() {
    return domain;
  }

  public boolean equals(Object other) {
    return ((other instanceof ForumPK)
        && (getInstanceId().equals(((ForumPK) other).getInstanceId()))
        && (domain.equals(((ForumPK) other).getDomain())) && (getId()
        .equals(((ForumPK) other).getId())));
  }

}