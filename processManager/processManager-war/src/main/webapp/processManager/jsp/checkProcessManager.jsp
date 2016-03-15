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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching
%>

<%@ page isELIgnored="false"%>

<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.StringTokenizer"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>

<%@ page import="com.silverpeas.workflow.api.instance.ProcessInstance" %>
<%@ page import="com.silverpeas.form.PagesContext" %>
<%@ page import="com.silverpeas.form.DataRecord" %>
<%@ page import="com.silverpeas.workflow.api.error.WorkflowError" %>
<%@ page import="org.silverpeas.util.FileRepositoryManager" %>
<%@ page import="com.silverpeas.workflow.api.model.State" %>
<%@ page import="com.silverpeas.workflow.api.model.Action" %>
<%@ page import="com.silverpeas.workflow.api.instance.HistoryStep" %>
<%@ page import="com.silverpeas.workflow.api.instance.Question" %>
<%@page import="org.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.form.RecordTemplate" %>
<%@ page import="com.silverpeas.form.FieldTemplate" %>
<%@ page import="com.silverpeas.workflow.api.model.Item" %>
<%@ page import="com.silverpeas.workflow.api.task.Task" %>
<%@ page import="com.silverpeas.form.Field" %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="org.silverpeas.util.viewGenerator.html.Encode"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="com.silverpeas.workflow.engine.dataRecord.ProcessInstanceRowRecord"%>
<%@ page import="com.silverpeas.form.fieldType.DateField"%>
<%@ page import="org.silverpeas.processmanager.NamedValue" %>
<%@ page import="org.silverpeas.util.LocalizationBundle" %>
<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String language = (String) request.getAttribute("language");
MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String componentId = browseContext[3];
String processManagerUrl = browseContext[4];

LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(language);

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
TabbedPane tabbedPane = gef.getTabbedPane();
Board board = gef.getBoard();

// Shared attributes
NamedValue[] roles = (NamedValue[]) request.getAttribute("roles");
if (roles == null) roles = new NamedValue[0];

String currentRole  = (String) request.getAttribute("currentRole");
if (currentRole == null) currentRole = "";
%>
