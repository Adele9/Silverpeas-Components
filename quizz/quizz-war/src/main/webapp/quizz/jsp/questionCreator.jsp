<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.lang.Integer"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.naming.Context,javax.naming.InitialContext,javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>

<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.quizz.control.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.question.model.Question "%>
<%@ page import="com.stratelia.webactiv.util.answer.model.Answer "%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.quizz.QuizzException"%>

<jsp:useBean id="quizzUnderConstruction" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />
<jsp:useBean id="questionsVector" scope="session" class="java.util.Vector" />

<%@ include file="checkQuizz.jsp" %>
<%!
  String displayQuestionContainerHeader(QuizzSessionController quizzScc, QuestionContainerDetail questionContainerDetail, ResourcesWrapper resources, JspWriter out, GraphicElementFactory gef)  throws QuizzException {
      Board board = gef.getBoard();
	  String questionContainerHeaderHTML="";
	  try{
		  QuestionContainerHeader questionContainerHeader = questionContainerDetail.getHeader();
		  String title = Encode.javaStringToHtmlString(questionContainerHeader.getTitle());
		  String creationDate = resources.getOutputDate(new Date());
		  String beginDate = "&nbsp;";
		  if (questionContainerHeader.getBeginDate() != null)
			  beginDate = resources.getOutputDate(questionContainerHeader.getBeginDate());
		  String endDate = "&nbsp;";
		  if (questionContainerHeader.getEndDate() != null)
			  endDate = resources.getOutputDate(questionContainerHeader.getEndDate());
		  String nbQuestions = "&nbsp;";
		  if (questionContainerHeader.getNbQuestionsPerPage() != 0)
			  nbQuestions = new Integer(questionContainerHeader.getNbQuestionsPerPage()).toString();
		  String nbAnswersMax = "&nbsp;";
		  if (questionContainerHeader.getNbMaxParticipations() != 0)
			  nbAnswersMax = new Integer(questionContainerHeader.getNbMaxParticipations()).toString();
		  String nbAnswersNeeded = "&nbsp;";
		  if (questionContainerHeader.getNbParticipationsBeforeSolution() != 0)
			  nbAnswersNeeded = new Integer(questionContainerHeader.getNbParticipationsBeforeSolution()).toString();
		  String description = Encode.javaStringToHtmlParagraphe(questionContainerHeader.getDescription());
		  String notice = Encode.javaStringToHtmlParagraphe(questionContainerHeader.getComment());
			questionContainerHeaderHTML+="<center>";
			questionContainerHeaderHTML += board.printBefore();
			questionContainerHeaderHTML+="<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" width=\"98%\">";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("GML.name")+" :</td><td align=left valign=baseline width=\"50%\">"+title+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("QuizzCreationDate")+" :</td><td align=left valign=baseline width=\"50%\">"+creationDate+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("QuizzCreationBeginDate")+" :</td><td align=left valign=baseline width=\"50%\">"+beginDate+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("QuizzCreationEndDate")+" :</td><td align=left valign=baseline width=\"50%\">"+endDate+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("QuizzCreationNbQuestionPerPage")+" :</td><td align=left valign=baseline width=\"50%\">"+nbQuestions+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("QuizzCreationNbAnswers")+" :</td><td align=left valign=baseline width=\"50%\">"+nbAnswersMax+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("QuizzCreationNbAnswerNeeded")+" :</td><td align=left valign=baseline width=\"50%\">"+nbAnswersNeeded+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\" align=left valign=baseline width=\"50%\">"+resources.getString("GML.description")+" :</td><td align=left valign=baseline width=\"50%\">"+description+"</td></tr>";
			questionContainerHeaderHTML+="<tr><td class=\"textePetitBold\"  width=\"50%\" align=left valign=baseline>"+resources.getString("QuizzCreationNotice")+" :</td><td align=left valign=baseline width=\"50%\">"+notice+"</td></tr>";
			questionContainerHeaderHTML+="</table>";
			questionContainerHeaderHTML += board.printAfter();
		} catch (Exception e){
			throw new QuizzException ("questionCreator_JSP.displayCredits",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_QUESTION_HEADER",e);
		}
		
        return questionContainerHeaderHTML;
  }

boolean isCorrectFile(FilePart filePart) {
    boolean correctFile = false;
    String fileName = filePart.getFileName();
    if (fileName!=null)
    {
      String logicalName = fileName.trim();
    
      if ((logicalName != null) && (logicalName.length() >= 3) && (logicalName.indexOf(".") != -1)) {
        String type = logicalName.substring(logicalName.indexOf(".")+1, logicalName.length());
        if (type.length() >= 3)
            correctFile = true;
      }
    }
    return correctFile;
}
%>

<% 

//R�cup�ration des param�tres
String action = "";
String question = "";
String nbAnswers = "";
String answerInput = "";
String qcmCheck = "";
String qcm = "0";
String penalty = "";
String clue = "";
String nbPointsMin = "";
String nbPointsMax = "";
String nbPoints = "";
String comment ="";

String nextAction = "";

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

int nbZone = 4; // nombre de zones � contr�ler
List galleries = quizzScc.getGalleries();
if (galleries != null)
{
	nbZone = nbZone + 2;
}

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
String ligne = m_context + "/util/icons/colorPix/1px.gif";

ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", quizzScc.getLanguage());
ResourceLocator quizzSettings = quizzScc.getSettings();

Button validateButton = null;
Button cancelButton = null;
Button finishButton = null;
ButtonPane buttonPane = null;

SilverpeasMultipartParser mp = new SilverpeasMultipartParser(request);
Part part;
File dir = null;
String logicalName = null;
String type = null;
String physicalName = null;
String mimeType = null;
boolean file = false;
long size = 0;
int nb = 0;
int attachmentSuffix = 0;
ArrayList imageList = new ArrayList();
ArrayList answers = new ArrayList();
Answer answer = null;
String style = "";

while ((part = mp.readNextPart()) != null) {
  String mpName = part.getName();
  if (part.isParam()) {
    // it's a parameter part
    SilverpeasParamPart paramPart = (SilverpeasParamPart) part;

    if (mpName.equals("Action"))
        action = paramPart.getStringValue();
    else if (mpName.equals("question"))
        question = paramPart.getStringValue();
    else if (mpName.equals("clue"))
        clue = paramPart.getStringValue();
    else if (mpName.equals("penalty"))
        penalty = paramPart.getStringValue();
    else if (mpName.equals("nbPointsMin"))
        nbPointsMin = paramPart.getStringValue();
    else if (mpName.equals("nbPointsMax"))
        nbPointsMax = paramPart.getStringValue();
    else if (mpName.equals("nbAnswers"))
        nbAnswers = paramPart.getStringValue();
    //else if (mpName.equals("qcm"))
        //qcm = paramPart.getStringValue();
    else if (mpName.equals("questionStyle"))
    	style = paramPart.getStringValue();
    else if (mpName.startsWith("answer")) {
        answerInput = paramPart.getStringValue();
        answer = new Answer(null, null, answerInput, 0, false, null, 0, false, null, null);
        answers.add(answer);
    }
    else if (mpName.startsWith("nbPoints")) {
        nbPoints = paramPart.getStringValue();
        answer.setNbPoints(new Integer(nbPoints).intValue());
        if (new Integer(nbPoints).intValue()>0)
          answer.setIsSolution(true);
    } 
    else if (mpName.startsWith("comment")) {
      comment = paramPart.getStringValue();
      answer.setComment(comment);
    } 
    else if (mpName.startsWith("valueImageGallery")) 
    {
    	if (StringUtil.isDefined(paramPart.getStringValue()))
    	{
    		// traiter les images venant de la gallery si pas d'image externe
    		if (!file)
    			answer.setImage(paramPart.getStringValue());
    	}
    }    
    String value = paramPart.getStringValue();
  } else if (part.isFile()) {
    // it's a file part
    FilePart filePart = (FilePart) part;
    boolean correctFile = isCorrectFile(filePart);
    if (correctFile) {
      // the part actually contained a file
      logicalName = filePart.getFileName();
      type = logicalName.substring(logicalName.indexOf(".")+1, logicalName.length());
      physicalName = new Long(new Date().getTime()).toString() + attachmentSuffix + "." +type;
      attachmentSuffix = attachmentSuffix + 1;
      mimeType = filePart.getContentType();
      dir = new File(FileRepositoryManager.getAbsolutePath(quizzScc.getSpaceId(), quizzScc.getComponentId())+quizzSettings.getString("imagesSubDirectory")+File.separator+physicalName);
      size = filePart.writeTo(dir);
      if (size > 0)
      {
          answer.setImage(physicalName);
          file = true;
      }
    } else { 
      // the field did not contain a file
    }
    out.flush();
  }
}

%>
<HTML>
<HEAD>
	<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript1.2">

function sendData() {
    if (isCorrectForm()) {
        document.quizzForm.submit();
    }
}
function sendData2() {
    if (isCorrectForm2()) {
        document.quizzForm.submit();
    }
}

function isCorrectForm() 
{
     var errorMsg = "";
     var errorNb = 0;
     var question = stripInitialWhitespace(document.quizzForm.question.value);
     var nbAnswers = document.quizzForm.nbAnswers.value;
     var clue = document.quizzForm.clue.value;
     var penalty = document.quizzForm.penalty.value;
     var nbPointsMin = document.quizzForm.nbPointsMin.value;
     var nbPointsMax = document.quizzForm.nbPointsMax.value;

     if (isWhitespace(nbAnswers)) 
     {
             errorMsg +="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswers")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
             errorNb++;
     } 
     if (document.quizzForm.questionStyle.options[document.quizzForm.questionStyle.selectedIndex].value=="null") {
     	//choisir au moins un style
	    	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("quizz.style")%>' <%=resources.getString("GML.MustBeFilled")%> \n";
	    	errorNb++;
     }
     else 
     {
        if (isInteger(nbAnswers)==false) 
        {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswers")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
            errorNb++;
        } 
        else 
        {
            if (nbAnswers <= 0) 
            {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswers")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
                errorNb++;
             }
        }
     }
    if (!isWhitespace(penalty))
    {
        if (isInteger(penalty)==false) 
        {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzPenalty")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
            errorNb++;
        } 
        else 
        {
            if (penalty <= 0) 
            {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzPenalty")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
                errorNb++;
            }
        }
        if (isWhitespace(clue))
        {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzClue")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
        }
    }     
   if (!isWhitespace(clue))
    {
        if (!isValidTextArea(document.quizzForm.clue)) 
        {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzClue")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.TextAreaLength%> <%=resources.getString("Caracters")%>\n";
           errorNb++; 
        }
        if (isWhitespace(penalty))
        {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzPenalty")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
        }
    }

    if (!isWhitespace(nbPointsMax))
    {
        if (isSignedInteger(nbPointsMax)==false) 
        {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
            errorNb++;
        } 
        else 
        {
            if (nbPointsMax <= 0) 
            {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
                errorNb++;
            }
        }
    }
    if (!isWhitespace(nbPointsMin))
    {
        if (isSignedInteger(nbPointsMin)==false) 
        {
            errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMin")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
            errorNb++;
        } 
        else 
        {
                if (parseInt(nbPointsMin, 10) >= parseInt(nbPointsMax, 10))
                {
                  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMin")%>' <%=resources.getString("MustContainsStrictlyInfNumber")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>'\n";
                  errorNb++;
                }
        }
    }

     if (isWhitespace(question)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationQuestion")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
     }
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContain")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContain")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
function isCorrectForm2() 
{
     var errorMsg = "";
     var errorNb = 0;
     var nb = Number(document.quizzForm.nbAnswers.value);
     var nbPointsMax = Number(document.quizzForm.nbPointsMax.value);
     var nbPointsMin = Number(document.quizzForm.nbPointsMin.value);
     for (var i = 0; i < nb; i++) 
     {
         //var answer=document.quizzForm.elements[4*i+7].value;
         //var nbPoints=document.quizzForm.elements[4*i+8].value;
         //var comment=document.quizzForm.elements[4*i+9].value;
         var answer=document.quizzForm.elements[<%=nbZone%>*i+7].value;
         var nbPoints=document.quizzForm.elements[<%=nbZone%>*i+8].value;
         var comment=document.quizzForm.elements[<%=nbZone%>*i+9].value;

         if (isWhitespace(nbPoints)) 
         {
                 errorMsg +="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("GML.MustBeFilled")%>\n";
                 errorNb++;
         } 
         else 
         {
            if (isSignedInteger(nbPoints)==false) 
            {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("GML.MustContainsFloat")%>\n";
                errorNb++;
            } 
	    else
	    {
		if((document.quizzForm.nbPointsMax.value!='')&&(parseInt(nbPoints, 10) > parseInt(nbPointsMax, 10)))
		{
	                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("MustContainsInfNumber")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>'\n";
			errorNb++;
		}
		else
		{
			if((document.quizzForm.nbPointsMin.value!='')&&(parseInt(nbPoints, 10) < parseInt(nbPointsMin, 10)))
			{
				errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("MustContainsSupNumber")%> '<%=resources.getString("QuizzCreationNbPointsMin")%>'\n";
				errorNb++;
			}
		}

	    }
         }
         if (isWhitespace(answer)) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationAnswerNb")%> "+String(i+1)+"' <%=resources.getString("GML.MustBeFilled")%>\n";
               errorNb++; 
         }
         if ((!isWhitespace(comment)) && (!isValidTextArea(document.quizzForm.elements[<%=nbZone%>*i+9]))) 
          {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationAnswerComment")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.TextAreaLength%> <%=resources.getString("Caracters")%>\n";
               errorNb++; 
          }
   }
   switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContain")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContain")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}
function goToEnd() {
    document.quizzForm.Action.value = "End";
    document.quizzForm.submit();
}
function confirmCancel()
{
	if (confirm('<%=resources.getString("ConfirmCancel")%>'))
		self.location="Main.jsp";
}

	var galleryWindow = window;
	var currentAnswer;
	
	function choixGallery(liste, idAnswer)
	{
		currentAnswer = idAnswer;
		index = liste.selectedIndex;
		var componentId = liste.options[index].value;
		if (index != 0)
		{
			url = "<%=m_context%>/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language=<%=quizzScc.getLanguage()%>";
			windowName = "galleryWindow";
			larg = "820";
			haut = "600";
			windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
			if (!galleryWindow.closed && galleryWindow.name=="galleryWindow")
				galleryWindow.close();
			galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
		}
	}
	
	function deleteImage(idImage)
	{
		document.getElementById('imageGallery'+idImage).innerHTML = "";
		document.getElementById('valueImageGallery'+idImage).value = "";
	}
	
	//function choixImageInGallery(url)
	//{
		//document.getElementById('imageGallery'+currentAnswer).innerHTML = "<a href=\""+url+"\" target=\"_blank\"><%=resources.getString("quizz.imageGallery")%></a> <a href=\"javascript:deleteImage('"+currentAnswer+"')\"><img src=\"icons/questionDelete.gif\" border=\"0\" align=\"absmiddle\" alt=\"<%=resources.getString("GML.delete")%>\" title=\"<%=resources.getString("GML.delete")%>\"></a>";
		//document.getElementById('valueImageGallery'+currentAnswer).value = url;
	//}
	function choixImageInGallery(url)
	{
		var newLink = document.createElement("a");
		newLink.setAttribute("href", url);
		newLink.setAttribute("target", "_blank");
		
		var newLabel = document.createTextNode("<%=resources.getString("quizz.imageGallery")%>");
		newLink.appendChild(newLabel);
		
		var removeLink =  document.createElement("a");
		removeLink.setAttribute("href", "javascript:deleteImage('"+currentAnswer+"')");
		var removeIcon = document.createElement("img");
		removeIcon.setAttribute("src", "icons/questionDelete.gif");
		removeIcon.setAttribute("border", "0");
		removeIcon.setAttribute("align", "absmiddle");
		removeIcon.setAttribute("alt", "<%=resources.getString("GML.delete")%>");
		removeIcon.setAttribute("title", "<%=resources.getString("GML.delete")%>");
		
		removeLink.appendChild(removeIcon);
		
		document.getElementById('imageGallery'+currentAnswer).appendChild(newLink);
		document.getElementById('imageGallery'+currentAnswer).appendChild(removeLink);
		   
		document.getElementById('valueImageGallery'+currentAnswer).value = url;
	}
</script>
</HEAD>
<%

if (action.equals("FirstQuestion")) {
      session.setAttribute("questionsVector", new Vector(10, 2));
      action = "CreateQuestion";
}
if (action.equals("SendNewQuestion")) {
      //boolean isQCM = false;
      //if (qcm.equals("1"))
           //isQCM = true;
      //boolean isOpen = false;
      Vector questionsV = (Vector) session.getAttribute("questionsVector");
      int questionNb = questionsV.size() + 1;
      int penaltyInt=0;
      int nbPointsMinInt=-1000;
      int nbPointsMaxInt=1000;
      if (!penalty.equals(""))
        penaltyInt=new Integer(penalty).intValue();    
      if (!nbPointsMin.equals(""))
        nbPointsMinInt=new Integer(nbPointsMin).intValue();
      if (!nbPointsMax.equals(""))
        nbPointsMaxInt=new Integer(nbPointsMax).intValue();
      Question questionObject = new Question(null, null, question, null, clue, null, 0, style,penaltyInt,0,questionNb, nbPointsMinInt, nbPointsMaxInt);
    
      questionObject.setAnswers(answers);
      questionsV.add(questionObject);
      action = "CreateQuestion";
} //End if action = ViewResult
else if (action.equals("End")) {
      out.println("<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor=#FFFFFF>");
      QuestionContainerDetail questionContainerDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
      //Vector 2 Collection
      Vector questionsV = (Vector) session.getAttribute("questionsVector");
      ArrayList q = new ArrayList();
      for (int j = 0; j < questionsV.size(); j++) {
            q.add((Question) questionsV.get(j));
      }
      questionContainerDetail.setQuestions(q);
      out.println("</BODY></HTML>");
}
if ((action.equals("CreateQuestion")) || (action.equals("SendQuestionForm"))) {
      out.println("<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor=#FFFFFF>");
      Vector questionsV = (Vector) session.getAttribute("questionsVector");
      int questionNb = questionsV.size() + 1;
      cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:confirmCancel();", false);
      buttonPane = gef.getButtonPane();
      if (action.equals("CreateQuestion")) {
            validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
            finishButton = (Button) gef.getFormButton(resources.getString("Finish"), "javascript:onClick=goToEnd()", false);
            question = "";
            nbAnswers = "";
            penalty = "";
            clue = "";
            nbPointsMin ="";
            nbPointsMax ="";
            nextAction="SendQuestionForm";
            buttonPane.addButton(validateButton);
            if (questionsV.size() != 0) {
                buttonPane.addButton(finishButton);
            }
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      } else if (action.equals("SendQuestionForm")) {
            validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData2()", false);
            nextAction="SendNewQuestion";
            buttonPane.addButton(validateButton);
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      }
      
      Window window = gef.getWindow();
      Frame frame = gef.getFrame();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(quizzScc.getSpaceLabel());
      browseBar.setComponentName(quizzScc.getComponentLabel());
      browseBar.setExtraInformation(resources.getString("QuizzCreation"));

      out.println(window.printBefore());
      
      out.println(frame.printBefore());
      out.println(displayQuestionContainerHeader(quizzScc, (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction"), resources, out, gef));

      Board board = gef.getBoard();
      %>
      <!--DEBUT CORPS -->
      <form name="quizzForm" Action="questionCreator.jsp" method="POST" ENCTYPE="multipart/form-data">
      
        <br>
        
        <% if (action.equals("SendQuestionForm")) {
                    out.println("<center>");
                    out.println(board.printBefore());
		    	    out.println("<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\" width=\"98%\">");
                    out.println("<tr><td class=\"textePetitBold\" valign=top>" + resources.getString("QuizzCreationQuestion") + questionNb + " :</td><td>" + Encode.javaStringToHtmlString(question) + "</td></tr>");
                    out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("quizz.style")+" :</td><td>"+resources.getString("quizz."+style)+"</td></tr>");
 
                    out.println("<tr><td class=\"textePetitBold\" valign=top>"+resources.getString("QuizzCreationNbAnswers")+" :</td><td>"+nbAnswers+"</td></tr>");
                    if (!nbPointsMin.equals(""))
                      out.println("<tr><td class=\"textePetitBold\" valign=top>"+resources.getString("QuizzCreationNbPointsMin") + " :</td><td>"+nbPointsMin+"&nbsp;"+resources.getString("QuizzNbPoints")+"</td></tr>");
                    if (!nbPointsMax.equals(""))
                      out.println("<tr><td class=\"textePetitBold\" valign=top>"+resources.getString("QuizzCreationNbPointsMax") + " :</td><td>"+nbPointsMax+"&nbsp;"+resources.getString("QuizzNbPoints")+"</td></tr>");
                    if (!clue.equals(""))
                      out.println("<tr><td class=\"textePetitBold\" valign=top>"+resources.getString("QuizzClue") + " :</td><td>"+Encode.javaStringToHtmlParagraphe(clue)+"</td></tr>");
                    if (!penalty.equals(""))
                      out.println("<tr><td class=\"textePetitBold\" valign=top>"+resources.getString("QuizzPenalty") + " :</td><td>"+penalty+"&nbsp;"+resources.getString("QuizzNbPoints"));
                    out.println("<input type=\"hidden\" name=\"question\" value=\"" + Encode.javaStringToHtmlString(question) + "\">");
                    //out.println("<input type=\"hidden\" name=\"qcm\" value=\""+qcm+"\">");
                    out.println("<input type=\"hidden\" name=\"questionStyle\" value=\""+style+"\">");
                    out.println("<input type=\"hidden\" name=\"nbAnswers\" value=\""+nbAnswers+"\">");
                    out.println("<input type=\"hidden\" name=\"nbPointsMin\" value=\""+nbPointsMin+"\">");
                    out.println("<input type=\"hidden\" name=\"nbPointsMax\" value=\""+nbPointsMax+"\">");
                    out.println("<input type=\"hidden\" name=\"clue\" value=\""+Encode.javaStringToHtmlString(clue)+"\">");
                    out.println("<input type=\"hidden\" name=\"penalty\" value=\""+penalty+"\">");
                    out.println("</td></tr>");
                    nb = new Integer(nbAnswers).intValue();
                    String inputName = "";
                    int j=0;
                    for (int i = 0; i < nb; i++) {
                        j = i + 1;
                        inputName = "answer"+i;
	                    out.println("<tr><td colspan=3><br></td></tr>");
                        out.println("<tr align=\"center\"><td class=\"intfdcolor\" colspan=\"3\" align=\"center\"  height=\"1\"><img src=\"" + ligne + "\" width=\"100%\" height=\"1\"></td></tr>");
                        out.println("<tr><td colspan=3><br></td></tr>");
                        out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzCreationAnswerNb")+"&nbsp;"+j+" :</td><td valign=top><textarea name=\""+inputName+"\" cols=\"49\" wrap=\"VIRTUAL\" rows=\"3\"></textarea>&nbsp;<img border=\"0\" src=\"" + mandatoryField + "\" width=\"5\" height=\"5\"></td><td class=\"txtlibform\" align=center valign=top><input type=\"text\" name=\"nbPoints"+i+"\" value=\"\" size=\"5\" maxlength=\"3\">&nbsp;"+resources.getString("QuizzNbPoints")+"&nbsp;<img border=\"0\" src=\"" + mandatoryField + "\" width=\"5\" height=\"5\"></td></tr>");
                        out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzCreationAnswerComment")+"&nbsp;"+j+" :</td><td><textarea name=\"comment"+i+"\" cols=\"49\" wrap=\"VIRTUAL\" rows=\"3\"></textarea></td><td></td></tr>");
                        
                        String visibility = "visibility: visible;";
                        if (style.equals("list"))
                        {
                        	visibility = "visibility: hidden;";
                        }
                        out.println("<tr style=\""+visibility+"\"><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzCreationAnswerImage")+"&nbsp;"+j+" :</td><td><input type=\"file\" size=\"50\" name=\"image"+i+"\"></td><td></td></tr>");
                        //zone pour le lien vers l'image
                        out.println("<tr style=\""+visibility+"\"><td></td><td><span id=\"imageGallery"+i+"\"></span>");
                        

	                    //List galleries = quizzScc.getGalleries();
	                    if (galleries != null)
	                    {
	                    	out.println("<input type=\"hidden\" id=\"valueImageGallery"+i+"\" name=\"valueImageGallery"+i+"\" >");
							out.println(" <select id=\"galleries\" name=\"galleries\" onchange=\"choixGallery(this, '"+i+"');this.selectedIndex=0;\"> ");
	    					out.println(" <option selected>"+resources.getString("quizz.galleries")+"</option> ");
	   						for(int k=0; k < galleries.size(); k++ ) 
	   						{
	   							ComponentInstLight gallery = (ComponentInstLight) galleries.get(k);
	   							out.println(" <option value=\""+gallery.getId()+"\">"+gallery.getLabel()+"</option> ");
	   						}
	    					out.println("</select>");
	    					out.println("</td>");
	    				}
	                    out.println("</tr>");
					    out.println("<tr><td colspan=\"3\" align=\"left\">(<img border=\"0\" src=\""+mandatoryField+"\" width=\"5\" height=\"5\">&nbsp;=&nbsp;"+resources.getString("GML.requiredField") + ")</td></tr>");

                    }
                    out.println("</table>");
                    out.println(board.printAfter());

              } else {
                out.println("<center>");
                out.println(board.printBefore());
			    out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" width=\"98%\">");
                out.println("<tr><td class=\"txtlibform\" valign=top>" + resources.getString("QuizzCreationQuestion") + questionNb + " :</td><td><textarea name=\"question\" cols=\"49\" wrap=\"VIRTUAL\" rows=\"3\">"+Encode.javaStringToHtmlString(question)+"</textarea>&nbsp;<img border=\"0\" src=\"" + mandatoryField + "\" width=\"5\" height=\"5\"></td></tr>");
                //out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("GML.type")+" :</td>");
                //out.println("<td><input type=\"radio\" name=\"qcm\" value=\"0\" checked> "+resources.getString("QuizzCreationQuestionTypeOne")+"<BR>");
                //out.println("<input type=\"radio\" name=\"qcm\" value=\"1\"> "+resources.getString("QuizzCreationQuestionTypeMany")+"&nbsp;<img border=\"0\" src=\"" + mandatoryField + "\" width=\"5\" height=\"5\"></td></tr>");
                //out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzCreationQuestionTypeMany")+" :</td><td><input type=\"checkbox\" name=\"qcm\" value=\"1\" "+qcmCheck+"></td></tr>");
 
				out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("quizz.style")+" :</td><td>");
                out.println(" <select id=\"questionStyle\" name=\"questionStyle\" > ");
    			out.println(" <option selected value=\"null\">"+resources.getString("quizz.style")+"</option> ");
				out.println(" <option value=\"radio\">"+resources.getString("quizz.radio")+"</option> ");
				out.println(" <option value=\"checkbox\">"+resources.getString("quizz.checkbox")+"</option> ");
				out.println(" <option value=\"list\">"+resources.getString("quizz.list")+"</option> ");
    			out.println("</select>");
                out.println("</td></tr>");
                	
                out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzCreationNbAnswers")+" :</td><td><input type=\"text\" name=\"nbAnswers\" value=\""+nbAnswers+"\" size=\"5\" maxlength=\"3\">&nbsp;&nbsp;&nbsp;<img border=\"0\" src=\"" + mandatoryField + "\" width=\"5\" height=\"5\"></td></tr>");
                out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzCreationNbPointsMin") + " :</td><td><input type=\"text\" name=\"nbPointsMin\" value=\""+nbPointsMin+"\" size=\"5\" maxlength=\"3\">&nbsp;"+resources.getString("QuizzNbPoints")+"</td></tr>");
                out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzCreationNbPointsMax") + " :</td><td><input type=\"text\" name=\"nbPointsMax\" value=\""+nbPointsMax+"\" size=\"5\" maxlength=\"3\">&nbsp;"+resources.getString("QuizzNbPoints")+"</td></tr>");
                out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzClue") + " :</td><td><textarea name=\"clue\" cols=\"49\" wrap=\"VIRTUAL\" rows=\"3\">"+Encode.javaStringToHtmlString(clue)+"</textarea></td></tr>");
                out.println("<tr><td class=\"txtlibform\" valign=top>"+resources.getString("QuizzPenalty") + " :</td><td><input type=\"text\" name=\"penalty\" value=\""+penalty+"\" size=\"5\" maxlength=\"3\">&nbsp;"+resources.getString("QuizzNbPoints")+"</td></tr>");
                String inputName = "answer"+0;
                out.println("<tr><td><input type=\"hidden\" name=\""+inputName+"\"</td></tr>");
				out.println("<tr><td>(<img border=\"0\" src=\""+mandatoryField+"\" width=\"5\" height=\"5\">&nbsp;=&nbsp;"+resources.getString("GML.requiredField") + ")</td></tr>");
                out.println("</table>");
                out.println(board.printAfter());
           }
        %>                                                                             
		<input type="hidden" name="Action" value="<%=nextAction%>">
      </form>
      <!-- FIN CORPS -->
<%
      out.println(frame.printMiddle());
%>
<%    
            out.println("<br><center>"+buttonPane.print());
out.println(frame.printAfter());
      
      out.println(window.printAfter());
      out.println("</BODY></HTML>");
 } //End if action = ViewQuestion
if (action.equals("End")) {
%>
<HTML>
<HEAD>
<script language="Javascript">
    function goToQuizzPreview() {
        document.questionForm.submit();
    }
</script>
</HEAD>
<BODY onLoad="goToQuizzPreview()">
<Form name="questionForm" Action="quizzQuestionsNew.jsp" Method="POST">
<input type="hidden" name="Action" value="PreviewQuizz">
</Form>
</BODY>
</HTML>
<% } %>