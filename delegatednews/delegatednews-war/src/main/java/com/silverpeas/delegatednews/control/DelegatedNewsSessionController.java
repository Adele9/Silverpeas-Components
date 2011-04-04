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

package com.silverpeas.delegatednews.control;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.silverpeas.delegatednews.model.DelegatedNew;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.silverpeas.delegatednews.service.ServicesFactory;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.webactiv.util.DateUtil;

import static com.stratelia.webactiv.SilverpeasRole.*;

public class DelegatedNewsSessionController extends AbstractComponentSessionController {

	private DelegatedNewsService service = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public DelegatedNewsSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.delegatednews.multilang.DelegatedNewsBundle",
        "com.silverpeas.delegatednews.settings.DelegatedNewsIcons");

    service = ServicesFactory.getDelegatedNewsService();
  }
  
  public boolean isUser() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (user.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAdmin() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (admin.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Récupère toutes les actualités déléguées inter Theme Tracker
   *
   * @return List<DelegatedNew> : liste d'actualités déléguées
   */
  public List<DelegatedNew> getAllDelegatedNew() {
    List<DelegatedNew> list = service.getAllDelegatedNew();
    return list;
  }
  
  /**
   * Valide l'actualité déléguée passée en paramètre
   *
   */
  public void validateDelegatedNew(int pubId) {
    service.validateDelegatedNew(pubId);
  }
  
  /**
   * Refuse l'actualité déléguée passée en paramètre
   *
   */
  public void refuseDelegatedNew(int pubId, String refuseReasonText) {
    service.refuseDelegatedNew(pubId);
  }
  
  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   * @throws ParseException 
   *
   */
  public void updateDateDelegatedNew(int pubId, String beginDate, String beginHour, String endDate, String endHour) throws ParseException {
    
    Date dBegin = DateUtil.stringToDate(beginDate, beginHour, this.getLanguage());
    Date dEnd = DateUtil.stringToDate(endDate, endHour, this.getLanguage());

    service.updateDateDelegatedNew(pubId, dBegin, dEnd);
  }
}
