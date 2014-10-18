/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.scheduleevent.service;

import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import com.silverpeas.scheduleevent.service.model.dao.ScheduleEventDao;
import org.silverpeas.util.ResourceLocator;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

@Named("scheduleEventService")
public class ScheduleEventServiceImpl implements ScheduleEventService {

  private static final String MESSAGES_PATH
      = "com.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle";
  private static final String SETTINGS_PATH
      = "com.silverpeas.components.scheduleevent.settings.ScheduleEventSettings";
  private static final ResourceLocator settings = new ResourceLocator(SETTINGS_PATH, "");
  @Inject
  private ScheduleEventDao scheduleEventDao;

  @Override
  public String createScheduleEvent(ScheduleEvent scheduleEvent) {
    return scheduleEventDao.createScheduleEvent(scheduleEvent);
  }

  @Override
  public void deleteScheduleEvent(String scheduleEventId) {
    ScheduleEvent event = scheduleEventDao.getScheduleEvent(scheduleEventId);
    scheduleEventDao.deleteScheduleEvent(event);
  }

  @Override
  public ScheduleEvent findScheduleEvent(String scheduleEventId) {
    return scheduleEventDao.getScheduleEvent(scheduleEventId);
  }

  @Override
  public Set<ScheduleEvent> listAllScheduleEventsByUserId(String userId) {
    Set<ScheduleEvent> events = scheduleEventDao.listScheduleEventsByCreatorId(userId);
    events.addAll(scheduleEventDao.listScheduleEventsByContributorId(userId));
    return events;
  }

  @Override
  public void updateScheduleEventStatus(String scheduleEventId, int newStatus) {
    ScheduleEvent event = scheduleEventDao.getScheduleEvent(scheduleEventId);
    event.setStatus(newStatus);
    scheduleEventDao.updateScheduleEvent(event);
  }

  @Override
  public ScheduleEvent purgeOldResponseForUserId(ScheduleEvent scheduleEvent, int userId) {
    scheduleEventDao.purgeResponseScheduleEvent(scheduleEvent, userId);
    return scheduleEventDao.getScheduleEvent(scheduleEvent.getId());
  }

  @Override
  public void updateScheduleEvent(ScheduleEvent scheduleEvent) {
    scheduleEventDao.updateScheduleEvent(scheduleEvent);
  }

  @Override
  public void setLastVisited(ScheduleEvent event, int userId) {
    Set<Contributor> contributors = event.getContributors();
    Iterator<Contributor> iter = contributors.iterator();
    boolean finish = false;
    while (iter.hasNext() && !finish) {
      Contributor contrib = (Contributor) iter.next();
      if (userId == contrib.getUserId()) {
        contrib.setLastVisit(new Date());
        finish = true;
      }
    }
    scheduleEventDao.updateScheduleEvent(event);
  }

  @Override
  public void deleteContributor(String id) {
    Contributor contrib = scheduleEventDao.getContributor(id);
    scheduleEventDao.deleteContributor(contrib);
  }

  @Override
  public ScheduleEvent getContentById(String contentId) {
    return findScheduleEvent(contentId);
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return settings;
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    return new ResourceLocator(MESSAGES_PATH, language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("scheduleEvent");
  }
}
