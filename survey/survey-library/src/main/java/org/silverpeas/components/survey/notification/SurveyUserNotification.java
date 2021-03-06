/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.survey.notification;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The centralization of the construction of the survey notifications
 * @author Cécile Bonin
 */
public class SurveyUserNotification
    extends AbstractTemplateUserNotificationBuilder<QuestionContainerDetail> {

  private final String componentInstanceId;
  private final String pathToSurvey;
  private final UserDetail userDetail;
  private final String senderId;
  private final Collection<String> userIdsToNotify;
  private final String fileName;
  private final NotifAction action;


  public SurveyUserNotification(final String componentInstanceId,
      final QuestionContainerDetail surveyDetail, final String pathToSurvey,
      final UserDetail userDetail, final UserDetail[] participants) {
    super(surveyDetail);
    this.componentInstanceId = componentInstanceId;
    this.pathToSurvey = pathToSurvey;
    this.userDetail = userDetail;
    this.senderId = userDetail.getId();
    Collection<String> userIds = new ArrayList<>();
    for (UserDetail participant : participants) {
      userIds.add(participant.getId());
    }
    this.userIdsToNotify = userIds;
    this.fileName = "alertResultSurvey";
    this.action = NotifAction.REPORT;
  }

  @Override
  protected String getTemplateFileName() {
    return this.fileName;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return this.userIdsToNotify;
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return !NotifAction.REPORT.equals(action);
  }

  @Override
  protected void performTemplateData(final String language, final QuestionContainerDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getTitle(language), "");
    template.setAttribute("UserDetail", this.userDetail);
    template.setAttribute("userName",
        this.userDetail != null ? this.userDetail.getDisplayedName() : "");
    template.setAttribute("SurveyDetail", resource);
    template.setAttribute("surveyName", resource.getHeader().getName());
    String surveyDesc = resource.getHeader().getDescription();
    if (StringUtil.isDefined(surveyDesc)) {
      template.setAttribute("surveyDesc", surveyDesc);
    }
    template.setAttribute("htmlPath", this.pathToSurvey);
  }

  @Override
  protected void performNotificationResource(final String language,
      final QuestionContainerDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getHeader().getName());
  }

  @Override
  protected String getTemplatePath() {
    return "survey";
  }

  @Override
  protected NotifAction getAction() {
    return this.action;
  }

  @Override
  protected String getComponentInstanceId() {
    return this.componentInstanceId;
  }

  @Override
  protected String getSender() {
    return this.senderId;
  }

  @Override
  protected String getLocalizationBundlePath() {
    return "org.silverpeas.survey.multilang.surveyBundle";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "survey.notifSurveyLinkLabel";
  }
}
