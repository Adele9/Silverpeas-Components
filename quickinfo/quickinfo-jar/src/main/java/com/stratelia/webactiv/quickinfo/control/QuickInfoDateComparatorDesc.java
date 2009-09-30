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
package com.stratelia.webactiv.quickinfo.control;

import java.util.Comparator;
import com.stratelia.webactiv.util.publication.model.*;

public class QuickInfoDateComparatorDesc implements Comparator {
  static public QuickInfoDateComparatorDesc comparator = new QuickInfoDateComparatorDesc();

  /**
   * This result is reversed as we want a descending sort.
   */
  public int compare(Object o1, Object o2) {
    PublicationDetail qI1 = (PublicationDetail) o1;
    PublicationDetail qI2 = (PublicationDetail) o2;

    int compareResult = qI1.getUpdateDate().compareTo(qI2.getUpdateDate());

    return 0 - compareResult;
  }

  /**
   * This comparator equals self only.
   * 
   * Use the shared comparator QuickInfoDateComparatorDesc.comparator if
   * multiples comparators are used.
   */
  public boolean equals(Object o) {
    return o == this;
  }
}