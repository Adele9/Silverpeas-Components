<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.quizz.control.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.question.model.*"%>
<%@ page import="com.stratelia.webactiv.util.question.control.*"%>
<%@ page import="com.stratelia.webactiv.util.answer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.answer.control.*"%>

<%@ include file="checkQuizz.jsp" %>
<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>
<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>

<% out.println(gef.getLookStyleSheet()); %>
<script language="JavaScript">
<!--
function MM_openBrWindow(theURL,winName,features) { //v2.0
  window.open(theURL,winName,features);
}
//-->
</script>
</head>
<body bgcolor=#FFFFFF leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<SCRIPT language="JavaScript">
<!--
//  InitBulle("txtnote","000000","intfdcolor2",2,90);
//-->
  function validate_form()
  {
    return;
  }
</SCRIPT>
<!--  -->

<%
  //get SessionController, Language & Settings
  ResourceLocator settings = quizzScc.getSettings();
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel(); 
  String quizz_id = (String) request.getParameter("quizz_id");
  QuestionContainerDetail quizzDetail = quizzScc.getQuizzDetail(quizz_id);

  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");
  window.printBefore();

  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(resources.getString("QuizzParticipate")+" "+quizzDetail.getHeader().getTitle());
  browseBar.setPath("<a href=\"Main.jsp\">"+resources.getString("QuizzList")+"</a>");

  //operation pane
  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation("icons/imprim.gif",resources.getString("GML.print"),"javascript:window.print()");
  operationPane.addLine(); 

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());

  // Quizz Header %>
  <span class="titreFenetre"><br>&nbsp;&nbsp;&nbsp;<%=quizzDetail.getHeader().getTitle()%></span>
  <blockquote> 
    <p><span class="sousTitreFenetre"><%=quizzDetail.getHeader().getDescription()%></span></p>
    <%
    if (quizzDetail.getHeader().getComment() != null)
    {
      out.println("<p>"+resources.getString("QuizzNotice")+"&nbsp;&nbsp;");
      out.println(quizzDetail.getHeader().getComment()); %>
      <br>
      <br>
      </p>
    <% } %>
  </blockquote>
  <FORM>
  <table width="100%" border="0">
  <%  //Questions
    Collection quizzQuestions = quizzDetail.getQuestions();
    Iterator i = quizzQuestions.iterator();
    while (i.hasNext()) {
      Question quizzQuestion = (Question) i.next(); %>
      <tr><td class="intfdcolor4" nowrap width="41%"><span class="txtlibform">&nbsp;<img src="icons/1pxRouge.gif" width=5 height=5>&nbsp;<%=quizzQuestion.getLabel()%>&nbsp;</td>
          <td class="intfdcolor4" align="center" nowrap><%=quizzQuestion.getNbPointsMax()%> pts</td>
          <td class="intfdcolor4" align="center" nowrap> <%
            if (quizzQuestion.getClue() != null){ %>
              <a href="#" onClick="MM_openBrWindow('quizzClue.jsp?quizz_id='+<%=quizzDetail.getHeader().getPK().getId()%>+'&question_id='+<%=quizzQuestion.getPK().getId()%>,'indice','width=570,height=220')"><%=resources.getString("QuizzSeeClue")%></a> (<%=resources.getString("QuizzPenalty")%> = <%=quizzQuestion.getCluePenalty()%> pts)
              <% } %>
          </td>
      </tr> <%
      //Answers
      Collection questionAnswers = quizzQuestion.getAnswers();
      Iterator j = questionAnswers.iterator();
      while (j.hasNext()) {
        Answer questionAnswer = (Answer) j.next(); %>
        <tr><td colspan="3"><table><tr>
            <% if (questionAnswer.getImage() != null) { %>
                <td><img src="icons/<%=questionAnswer.getImage()%>" align="left"></td>
            <% } else { %>
                <td width=50>&nbsp;</td>
            <% } %>
            <% if (quizzQuestion.isQCM()) { %>
                <td><input type="checkbox" name="chk_<%=quizzQuestion.getPK().getId()%>_<%=j%>" value="on">&nbsp;<%=questionAnswer.getLabel()%></td>
            <% } else { %>
                <td><input type="radio" name="opt_question<%=quizzQuestion.getPK().getId()%>" value="<%=questionAnswer.getPK().getId()%>">&nbsp;<%=questionAnswer.getLabel()%>
                <% if (questionAnswer.isOpened()) { %>
                    <br><textarea rows=5 cols=40 name="txa_question<%=quizzQuestion.getPK().getId()%>"></textarea><% } %>
                </td><% } %>
            </tr></table></td>
        </tr><%
      } %>
      <tr><td colspan="3"><hr noshade size=1 width=98% align=center></td></tr><%
    } %>
    <tr> 
      <td colspan="3"><span class="txtnote">(<img src="icons/1pxRouge.gif" width="5" height="5">&nbsp;=&nbsp;<%=resources.getString("GML.requiredField")%>)</td>
    </tr>
   </table>
     <%
      out.println(frame.printMiddle());
      ButtonPane buttonPane = gef.getButtonPane();
      buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:validate_form()", true));
      buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false));
      out.println("<br>");
      out.println("<table width=\"100%\"><tr><td align=\"center\">"+buttonPane.print()+"</td></tr></table>");
      out.println("<br><br>");
      out.println(frame.printAfter());
      out.println(window.printAfter());
      %>
</FORM>
</BODY>
</HTML>


