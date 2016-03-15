<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.util.StringUtil"%>
<%@ page import="org.silverpeas.util.EncodeHelper"%>
<%@ page import="org.silverpeas.servlet.FileUploadUtil"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>

<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@ page import="org.silverpeas.components.quizz.QuizzException"%>
<%@ page import="org.silverpeas.components.quizz.QuestionForm"%>
<%@ page import="org.silverpeas.components.quizz.QuestionHelper"%>
<%@ page import="org.silverpeas.util.FileServerUtils"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.score.model.ScoreDetail"%>
<%@ page import="com.stratelia.webactiv.question.model.Question"%>
<%@ page import="com.stratelia.webactiv.questionContainer.model.QuestionContainerHeader"%>
<%@ page import="com.stratelia.webactiv.questionContainer.model.QuestionContainerDetail"%>
<%@ page import="com.stratelia.webactiv.questionResult.model.QuestionResult"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.Encode"%>
<%@ page import="org.silverpeas.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.answer.model.Answer"%>
<%@ page import="org.silverpeas.util.SettingBundle" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayPane" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayColumn" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayLine" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellText" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellLink" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.io.IOException" %>

<%@ page import="org.apache.commons.fileupload.FileItem"%>1
<%@ page import="org.silverpeas.components.quizz.control.QuizzSessionController" %>
<%@ page import="org.silverpeas.util.DateUtil" %>
<%@ page import="org.silverpeas.util.LocalizationBundle" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
  GraphicElementFactory gef =
      (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  QuizzSessionController quizzScc = (QuizzSessionController) request.getAttribute("quizz");
  MultiSilverpeasBundle resources = (MultiSilverpeasBundle) request.getAttribute("resources");
  if (quizzScc == null) {
    String sessionTimeout =
        ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(
        request, response);
    return;
  }
  LocalizationBundle surveyResource = ResourceLocator
      .getLocalizationBundle("org.silverpeas.survey.multilang.surveyBundle",
          quizzScc.getLanguage());
%>