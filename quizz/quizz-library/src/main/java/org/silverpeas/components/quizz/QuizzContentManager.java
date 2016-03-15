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

package org.silverpeas.components.quizz;

import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.questionContainer.control.QuestionContainerService;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerPK;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * The kmelia implementation of ContentInterface.
 */
public class QuizzContentManager implements ContentInterface {

  private ContentManager contentManager = null;
  @Inject
  private QuestionContainerService questionContainerService;

  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String peasId,
      String userId, List<String> userRoles) {
    if (getContentManager() == null) {
      return new ArrayList<>();
    }

    return getHeaders(makePKArray(ids, peasId));
  }

  /**
   * return a list of publicationPK according to a list of silverContentId
   * @param idList a list of silverContentId
   * @param peasId the id of the instance
   * @return a list of publicationPK
   */
  private List<QuestionContainerPK> makePKArray(List<Integer> idList, String peasId) {
    List<QuestionContainerPK> pks = new ArrayList<>();
    // for each silverContentId, we get the corresponding publicationId
    for (Integer contentId : idList) {
      try {
        String id = getContentManager().getInternalContentId(contentId);
        QuestionContainerPK qcPK = new QuestionContainerPK(id, "useless", peasId);
        pks.add(qcPK);
      } catch (ClassCastException | ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   * @param ids a list of publicationPK
   * @return a list of publicationDetail
   */
  private List<SilverContentInterface> getHeaders(List<QuestionContainerPK> ids) {
    ArrayList<QuestionContainerHeader> questionHeaders =
        new ArrayList<>(getQuestionBm().getQuestionContainerHeaders(ids));
    List headers = new ArrayList(questionHeaders.size());
    for (QuestionContainerHeader questionContainerHeader : questionHeaders) {
      questionContainerHeader.setIconUrl("quizzSmall.gif");
      headers.add(questionContainerHeader);
    }
    return headers;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("quizz", "QuizzContentManager", "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private QuestionContainerService getQuestionBm() {
    return questionContainerService;
  }
}