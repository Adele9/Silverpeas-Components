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
package com.silverpeas.processManager;

import java.util.Comparator;

/**
 * A named value pair.
 */
public final class NamedValue {
  public final String name;
  public final String value;

  public NamedValue(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public boolean equals(Object o) {
    if (o instanceof NamedValue) {
      return name.equals(((NamedValue) o).name);
    } else
      return false;
  }

  public int hashCode() {
    return name.hashCode();
  }

  static Comparator ascendingValues = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 instanceof NamedValue && o2 instanceof NamedValue) {
        return ((NamedValue) o1).value.compareTo(((NamedValue) o2).value);
      } else
        throw new ClassCastException();
    }
  };
}
