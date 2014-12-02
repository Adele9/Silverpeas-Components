/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.kmax;

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.publication.control.PublicationService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.exception.SilverpeasException;

import javax.inject.Inject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 * @author
 */
public class KmaxStatistics implements ComponentStatisticsInterface {

  @Inject
  private PublicationService publicationBm = null;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    Collection<PublicationDetail> publications = getPublications(spaceId, componentId);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>(publications.
        size());
    for (PublicationDetail detail : publications) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(detail.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private PublicationService getPublicationBm() {
    if (publicationBm == null) {
      SilverTrace.error("kmax", "KmaxStatistics.getPublicationBm", "root.MSG_EJB_CREATE_FAILED",
          JNDINames.PUBLICATIONBM_EJBHOME);
      throw new KmeliaRuntimeException("kmax", SilverpeasException.ERROR,
          "KmaxStatistics.getPublicationBm", "root.MSG_EJB_CREATE_FAILED");
    }
    return publicationBm;
  }

  /**
   * Method declaration
   * @param spaceId
   * @param componentId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection<PublicationDetail> getPublications(String spaceId, String componentId) {
    return getPublicationBm()
        .getAllPublications(new PublicationPK("useless", spaceId, componentId));
  }
}
