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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.score.model.*"%>
<%@ page import="com.stratelia.webactiv.util.score.control.*"%>
<%@ page import="com.stratelia.webactiv.quizz.QuizzException"%>

<%@ include file="checkQuizz.jsp" %>


<%!
    String displayCredits(int nb_max_user_votes , int nb_user_votes) throws QuizzException
    {
	String Html_display = null;
	if (nb_user_votes == nb_max_user_votes)
		Html_display = "<img src=\"icons/feuRouge.gif\">&nbsp;";
	else
		Html_display = "<img src=\"icons/feuVert.gif\">&nbsp;";
	for (int i=0; i<nb_max_user_votes; i++)
	{
		if (i < (nb_max_user_votes - nb_user_votes))
		  Html_display += "<img src=\"icons/creditOn.gif\">";
		else
		  Html_display += "<img src=\"icons/creditOff.gif\">";
	}
      return Html_display;
}
%>
  <%
  ResourceLocator settings = quizzScc.getSettings();
  String m_Context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel();

 %>
<HTML>
<HEAD>
<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/animation.js"></script>
</head>

<SCRIPT LANGUAGE="JAVASCRIPT">
function notifyPopup2(context,compoId,users,groups)
{
    SP_openWindow(context+'/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=' + compoId + '&theTargetsUsers='+users+'&theTargetsGroups='+groups, 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
}
</SCRIPT>

<body bgcolor=#FFFFFF leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
 <%
  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");

  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(resources.getString("QuizzList"));

  out.println(window.printBefore());

  Frame frame = gef.getFrame();
  
  //onglets
  TabbedPane tabbedPane1 = gef.getTabbedPane();
  tabbedPane1.addTab(resources.getString("QuizzOnglet1"),"quizzAdmin.jsp",false);
  tabbedPane1.addTab(resources.getString("QuizzSeeResult"),"quizzResultAdmin.jsp",true);
  
  out.println(tabbedPane1.print());
  out.println(frame.printBefore());

 //Tableau
  ArrayPane arrayPane = gef.getArrayPane("QuizzResult","",request,session);

  ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
  arrayColumn0.setSortable(false);
  arrayPane.addArrayColumn(resources.getString("GML.name"));
  arrayPane.addArrayColumn(resources.getString("GML.user"));
  arrayPane.addArrayColumn(resources.getString("ScoreDate"));
  arrayPane.addArrayColumn(resources.getString("QuizzCredits"));
  arrayPane.addArrayColumn(resources.getString("ScoreLib"));
  arrayPane.addArrayColumn(resources.getString("ScorePosition"));

  Collection quizzList = quizzScc.getAdminResults();
  Iterator i = quizzList.iterator();
  while (i.hasNext()) {
    QuestionContainerHeader quizzHeader = (QuestionContainerHeader) i.next();
    int nb_max_participations = quizzHeader.getNbMaxParticipations();
    Collection scoreDetails = quizzHeader.getScores();
    if (scoreDetails != null)
    {
      Iterator j = scoreDetails.iterator();
      while (j.hasNext()) {
        ScoreDetail scoreDetail = (ScoreDetail) j.next();
	int nb_user_votes = quizzScc.getUserNbParticipationsByFatherId(quizzHeader.getPK().getId(), scoreDetail.getUserId());
        UserDetail userDetail=quizzScc.getUserDetail(scoreDetail.getUserId());
      String firstName = "";
      String lastName = resources.getString("UserUnknown");
      String recipient="";
      if (userDetail != null)
      {
	      firstName = userDetail.getFirstName();
	      lastName = userDetail.getLastName();
	      recipient=userDetail.getId();
      }

        ArrayLine arrayLine = arrayPane.addArrayLine();
    	arrayLine.addArrayCellLink("<img src=\"icons/palmares_30x15.gif\" border=0>","palmaresAdmin.jsp?quizz_id="+quizzHeader.getPK().getId());
        arrayLine.addArrayCellText(quizzHeader.getTitle());
        ArrayCellText arrayCellText2;
        if (!recipient.equals(""))
		{
			arrayCellText2 = arrayLine.addArrayCellText("<A HREF=\"javascript:notifyPopup2('" + m_Context + "','" + quizzScc.getComponentId() + "','" + recipient + "','')\">" + lastName + " " + firstName +"</A>");
		}
        else
	         arrayCellText2 = arrayLine.addArrayCellText(lastName + " " + firstName);
        arrayCellText2.setCompareOn((String) (lastName + " " + firstName).toLowerCase());
        
        Date participationDate = DateUtil.parse(scoreDetail.getParticipationDate());
        arrayLine.addArrayCellLink(resources.getOutputDate(participationDate),"quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&Action=ViewResultAdmin&Page=1"+"&UserId="+scoreDetail.getUserId()+"&ParticipationId="+new Integer(scoreDetail.getParticipationId()).toString());
        
        arrayLine.addArrayCellText(displayCredits(nb_max_participations, nb_user_votes));
        ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(new Integer(scoreDetail.getScore()).toString()+"/"+quizzHeader.getNbMaxPoints());
        arrayCellText1.setCompareOn(new Integer(scoreDetail.getScore()));
        ArrayCellText arrayCellText3 = arrayLine.addArrayCellText(new Integer(scoreDetail.getPosition()).toString());
	arrayCellText3.setCompareOn(new Integer(scoreDetail.getPosition()));
      }
    }
  }
  out.println(arrayPane.print());
%>
  <blockquote> <img src="icons/feuVert.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzParticipateYes")%><br>
      <img src="icons/feuRouge.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzParticipateNo")%><br>
      <img src="icons/creditOff.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzCreditOver")%><br>
      <img src="icons/creditOn.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzCreditAvailable")%>
  </blockquote>
<!--  FIN TAG FORM-->
<% out.println(frame.printMiddle());
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</BODY>
</HTML>


