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
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.quizz.control.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.question.model.*"%>
<%@ page import="com.stratelia.webactiv.util.question.control.*"%>
<%@ page import="com.stratelia.webactiv.quizz.QuizzException"%>

<%@ include file="checkQuizz.jsp" %>
<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<HTML>
<HEAD>
	<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor=#FFFFFF leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%!
Vector infos(JspWriter out, Collection Questions, String questionId)  throws QuizzException {
    String clue = null;
    String label = null;
    Iterator i = Questions.iterator();
    Vector infos = new Vector();
    int questionNum = 0;
	try{
		int questionInt=new Integer(questionId).intValue();
		while ((i.hasNext()) && (questionNum < questionInt)) {
			Question quizzQuestion = (Question) i.next();
			questionNum++;
			if (questionNum == questionInt)
			{
			  clue = Encode.javaStringToHtmlParagraphe(quizzQuestion.getClue());
			  label = quizzQuestion.getLabel();
			  infos.add(label);
			  infos.add(clue);
			}
		}
	} catch (Exception e){
			throw new QuizzException ("questionCluePreview_JSP.infos",QuizzException.WARNING,"Quizz.EX_CANNOT_OBTAIN_QUESTIONS_INFOS",e);
	}

    return infos;
}
%>

<%
//R�cup�ration des param�tres
  String question_id = (String) request.getParameter("question_id");
  QuestionContainerDetail quizzDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
  

  //get SessionController, Language & Settings
  ResourceLocator settings = quizzScc.getSettings();
  
  //Get Space, component, and current quizz_id 
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel(); 

    //Questions
  Collection quizzQuestions = quizzDetail.getQuestions();
  Vector infos = infos(out, quizzQuestions, question_id);

  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");
  window.printBefore();

  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(quizzDetail.getHeader().getTitle());

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  String title = resources.getString("QuizzClue")+":"+"&nbsp;"+infos.get(0)+"<br><br>";
  //title += "<a href=\"javascript:window.close()\"><div align=right><img align=middle src=\"icons/windowClose.gif\" width=16 height=14 border=0></a></div>";
  frame.addTitle(title);
  out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<tr> 
	<td nowrap>
		<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
    <tr>
      <td width="30"></td>
      <td class=textePetitBold><%=infos.get(1)%></td>
      <td align="right"><img src="icons/silverProf_rvb.gif" width="69" height="70"> 
      </td>
    </tr>
  </table></td></tr></table>
  <br><center>
<%
  Button closeButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(closeButton);
  out.println(closeButton.print());
%>

<%
  out.println(frame.printMiddle());
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</BODY>
</HTML>


