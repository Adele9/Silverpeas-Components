/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.notification;

import java.util.Collection;

import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaDefermentPublicationNotification extends AbstractKmeliaActionPublicationNotification {

  private final String refusalMotive;

  public KmeliaDefermentPublicationNotification(final PublicationDetail resource, final String refusalMotive) {
    super(null, resource, NotifAction.SUSPEND);
    this.refusalMotive = refusalMotive;
  }

  @Override
  protected String getSubjectKey() {
    return "kmelia.PublicationSuspended";
  }

  @Override
  protected String getFileName() {
    return "notification";
  }

  @Override
  protected void perform(final PublicationDetail resource) {
    super.perform(resource);
    getNotification().setOriginalExtraMessage(refusalMotive);
  }

  @Override
  protected void performTemplateData(final String language, final PublicationDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("refusalMotive", refusalMotive);
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return false;
  }

  @Override
  protected Collection<String> getUserIdToNotify() {
    return null;
  }
}
