/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.control;

import java.util.List;

import com.silverpeas.mailinglist.model.MailingListComponent;
import com.silverpeas.mailinglist.service.job.MessageChecker;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerProvider;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.trigger.TimeUnit;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.initialization.Initialization;
import org.silverpeas.util.ResourceLocator;

import javax.inject.Inject;

public class MailCheckerInitialize implements Initialization {

  public static final String MAILING_LIST_JOB_NAME = "mailingListScheduler";
  @Inject
  private MessageChecker messageChecker;
  @Inject
  private MailingListService mailingListService;
  private static ResourceLocator settings;
  private static final int DEFAULT_FREQUENCY = 1;

  static {
    settings = new ResourceLocator("org.silverpeas.mailinglist.notification", "");
  }

  public int getFrequency() {
    return settings.getInteger("mail.check.frequency", DEFAULT_FREQUENCY);
  }

  public MessageChecker getMessageChecker() {
    return messageChecker;
  }

  public MailingListService getMailingListService() {
    return mailingListService;
  }

  @Override
  public void init() throws Exception {
    SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
        "mailinglist.initialization.start");
    MessageChecker checker = getMessageChecker();
    try {
      SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.start", " " + checker);
      Scheduler scheduler = SchedulerProvider.getScheduler();
      if (scheduler.isJobScheduled(MAILING_LIST_JOB_NAME)) {
        scheduler.unscheduleJob(MAILING_LIST_JOB_NAME);
      }
      JobTrigger trigger = JobTrigger.triggerEvery(getFrequency(), TimeUnit.MINUTE);
      scheduler.scheduleJob(MAILING_LIST_JOB_NAME, trigger, checker);
      List<MailingList> mailingLists = getMailingListService().listAllMailingLists();
      SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.existing.lists", " " + mailingLists.size());
      for (MailingList list : mailingLists) {
        SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
            "mailinglist.initialization.start",
            " : " + list.getSubscribedAddress() + " " + list.getDescription());
        MailingListComponent component = new MailingListComponent(list.getComponentId());
        checker.addMessageListener(component);
      }
    } catch (SchedulerException e) {
      SilverTrace.error("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.error", e);
    }
  }

}
