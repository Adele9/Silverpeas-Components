	<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
	%>
	<%@ include file="checkAlmanach.jsp" %>

<%

	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(almanach.getLanguage());

	// r�cup�ration du user
	String user = (String) request.getParameter("flag");
	if (user == null) {
		user = "";
	}
	
	EventDetail event = (EventDetail) request.getAttribute("CompleteEvent");
	Date dateDebutIteration = (Date) request.getAttribute("DateDebutIteration");
	Date dateFinIteration = (Date) request.getAttribute("DateFinIteration");

	String dateDebutIterationString = DateUtil.date2SQLDate(dateDebutIteration);

	Periodicity periodicity = event.getPeriodicity();
	String id = event.getPK().getId();

  //initialisation de l'objet event
	String title = null;
	String link = null; 
	String description = "";
	try{
		event = almanach.getEventDetail(id);
		title = Encode.javaStringToHtmlString(event.getTitle());
		if (title.length() > 30) {
			title = title.substring(0,30) + "....";
		}
		link = event.getPermalink();
		if (StringUtil.isDefined(event.getWysiwyg())) {
			description = event.getWysiwyg();
		}
		else if (StringUtil.isDefined(event.getDescription())) {
			description = Encode.javaStringToHtmlParagraphe(event.getDescription());
		}
	} catch(AlmanachPrivateException ace){
		request.setAttribute("error", ace);
		getServletConfig().getServletContext().getRequestDispatcher(almanach.getComponentUrl()+"erreurSaisie.jsp").forward(request, response);
		return;
	 }
%>

<HTML>
<HEAD>
<% out.println(graphicFactory.getLookStyleSheet());%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">

var notifyWindow = window;

function goToNotify(url) 
{
	windowName = "notifyWindow";
	larg = "740";
	haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
        notifyWindow.close();
    notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}
</script>
</HEAD>
<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5" bgcolor="#FFFFFF" >
  <% 
    Window 	window 	= graphicFactory.getWindow();
    Frame 	frame	= graphicFactory.getFrame();
    Board 	board 	= graphicFactory.getBoard();
    OperationPane operationPane = window.getOperationPane();
        
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "almanach.jsp");
	browseBar.setExtraInformation(title);
	    
    String url = "ToAlertUser?Id="+id;
    operationPane.addOperation(m_context+"/util/icons/alert.gif", resources.getString("GML.notify"),"javaScript:onClick=goToNotify('"+url+"')") ;

    out.println(window.printBefore());
    
    if (!user.equals("user"))
    {
    	TabbedPane tabbedPane = graphicFactory.getTabbedPane();
		tabbedPane.addTab(almanach.getString("evenement"), "viewEventContent.jsp?Id="+id+"&Date="+dateDebutIterationString, true);
		tabbedPane.addTab(almanach.getString("entete"), "editEvent.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		tabbedPane.addTab(resources.getString("GML.attachments"), "editAttFiles.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
  		if (almanach.isPdcUsed()) {
			tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositions.jsp?Id="+id+"&Date="+dateDebutIterationString, false);
		}
		out.println(tabbedPane.print());
    }
    
    out.println(frame.printBefore());
%>
<center>
<table border="0"><tr><td width="100%">
<%
        out.println(board.printBefore());
%>
<table CELLPADDING=5 width="100%">
		<tr> 
      <td nowrap class="txtlibform" width="20%"><%=resources.getString("GML.name")%> :</td>
      <td><%=Encode.javaStringToHtmlString(event.getTitle())%></td>
    </tr>
    <tr> 
      <td nowrap class="txtlibform" valign="top"><%=resources.getString("GML.description")%> :</td>
      <td><%=description%></td>
    </tr>
    <tr> 
      <td nowrap class="txtlibform"><%=resources.getString("GML.dateBegin")%> :</td>
      <td><%=resources.getOutputDate(dateDebutIteration)%>
	  <%if (event.getStartHour() != null && event.getStartHour().length() != 0) {%>
	  	<%=almanach.getString("ToHour")%> 
		<%=Encode.javaStringToHtmlString(event.getStartHour())%>
	  <%}%>
	  </td>
    </tr>
    <tr> 
      <td nowrap class="txtlibform"><%=resources.getString("GML.dateEnd")%> :</td>
      <td><%=resources.getOutputDate(dateFinIteration)%>
	  <%if (event.getEndHour() != null && event.getEndHour().length() != 0) {%>
		   	<%=almanach.getString("ToHour")%> 
			<%=Encode.javaStringToHtmlString(event.getEndHour())%>
		  <%}%>
	  </td>
    </tr>   
    <tr> 
    	<td nowrap class="txtlibform"><%=almanach.getString("lieuEvenement")%> :</td>
      	<td><%=Encode.javaStringToHtmlString(event.getPlace())%></td>
    </tr>
    <tr> 
    	<td nowrap class="txtlibform"><%=almanach.getString("urlEvenement")%> :</td>
    	<% if (StringUtil.isDefined(event.getEventUrl())) {
    		String eventURL = event.getEventUrl();
    		if (eventURL.indexOf("://") == -1)
    			eventURL = "http://"+eventURL;
    		%>
    		<td>
    		<a href="<%=Encode.javaStringToHtmlString(eventURL)%>" target="_blank"><%=Encode.javaStringToHtmlString(event.getEventUrl())%></a>
			</td>
		<%}%>
    </tr>
    <tr> 
      <td nowrap class="txtlibform"><%=resources.getString("GML.priority")%> :</td>
      <%if (event.getPriority() != 0){ %>
        <td><%=almanach.getString("prioriteImportante")%></td>
	<% } else{ %>
        <td><%=almanach.getString("prioriteNormale")%></td>
	<% } %>
    </tr>
    
    <%	if (StringUtil.isDefined(link)) {	%>
    	<tr>
    		<td nowrap class="txtlibform"><%=almanach.getString("permalink")%> :</td>
    		<td><a href="<%=link%>"><img  src="<%=m_context%>/util/icons/link.gif" border="0" alt='<%=resources.getString("CopyEventLink")%>' title='<%=resources.getString("CopyEventLink")%>' ></a></td>
    	</tr>
	<% } %>

		<tr>
          <td nowrap class="txtlibform"><%=resources.getString("periodicity")%>&nbsp;:&nbsp;</td>
		  <td align=left>
			<%
			if(periodicity == null) {
				out.print(resources.getString("noPeriodicity"));
			}
			else
			{
				if (periodicity.getUnity() == Periodicity.UNITY_DAY) {
					out.print(resources.getString("allDays"));
				} else if (periodicity.getUnity() == Periodicity.UNITY_WEEK) {
					out.print(resources.getString("allWeeks"));
				} else if (periodicity.getUnity() == Periodicity.UNITY_MONTH) {
					out.print(resources.getString("allMonths"));
				} else if (periodicity.getUnity() == Periodicity.UNITY_YEAR) {
					out.print(resources.getString("allYears"));
				}
			}
			%>
		  </td>
        </tr>
		<%
			if(periodicity != null) {
		%>
			<tr>
	          <td nowrap align=right class="txtlibform"><%=resources.getString("frequency")%>&nbsp;:&nbsp;</td>
			  <td align=left><% out.print(periodicity.getFrequency());  %> </td> 
	        </tr>
			
			<%
				if (periodicity.getUnity() == Periodicity.UNITY_WEEK) {
			%>
			<tr>
	          <td nowrap align=right class="txtlibform"><%=resources.getString("choiceDaysWeek")%>&nbsp;:&nbsp;</td>
			  <td align=left>
			<%
					String days = "";
					if(periodicity.getDaysWeekBinary().charAt(0) == '1') { 
						days += resources.getString("GML.jour2")+", ";//Monday
					}
					if(periodicity.getDaysWeekBinary().charAt(1) == '1') {
						days += resources.getString("GML.jour3")+", "; //Tuesday
					}
					if(periodicity.getDaysWeekBinary().charAt(2) == '1') {
						days += resources.getString("GML.jour4")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(3) == '1') {
						days += resources.getString("GML.jour5")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(4) == '1') {
						days += resources.getString("GML.jour6")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(5) == '1') {
						days += resources.getString("GML.jour7")+", ";
					}
					if(periodicity.getDaysWeekBinary().charAt(6) == '1') {
						days += resources.getString("GML.jour1")+", ";
					}
					if(days.length() > 0) {
						days = days.substring(0, days.length() - 2);
					}
					out.print(days);
			%>
			 </td>
	        </tr>
			<%
				} else if (periodicity.getUnity() == Periodicity.UNITY_MONTH) {
					if(periodicity.getDay() > 0) {
			%>
			<tr>
			  <td nowrap align=right class="txtlibform"><%=resources.getString("choiceDayMonth")%>&nbsp;:&nbsp;</td>
			  <td align=left>	
			<%
						if(periodicity.getNumWeek() == 1) {
							out.print(resources.getString("first"));
						} else if (periodicity.getNumWeek() == 2) {
							out.print(resources.getString("second"));
						} else if (periodicity.getNumWeek() == 3) {
							out.print(resources.getString("second"));
						} else if (periodicity.getNumWeek() == 2) {
							out.print(resources.getString("third"));
						} else if (periodicity.getNumWeek() == -1) {
							out.print(resources.getString("fifth"));
						} 
						out.print(" ");
						if(periodicity.getDay() == 2) {
							out.print(resources.getString("GML.jour2"));
						} else if (periodicity.getDay() == 3) {
							out.print(resources.getString("GML.jour3"));
						} else if (periodicity.getDay() == 4) {
							out.print(resources.getString("GML.jour4"));
						} else if (periodicity.getDay() == 5) {
							out.print(resources.getString("GML.jour5"));
						} else if (periodicity.getDay() == 6) {
							out.print(resources.getString("GML.jour6"));
						} else if (periodicity.getDay() == 7) {
							out.print(resources.getString("GML.jour7"));
						} else if (periodicity.getDay() == 1) {
							out.print(resources.getString("GML.jour1"));
						}
			%>
			  </td> 
			</tr>
			<%
					}
				} 
			%>
			<tr> 
	          <td nowrap align=right class="txtlibform"><span><%=resources.getString("beginDatePeriodicity")%>&nbsp;:&nbsp;</td>
	          <td valign="baseline">  
				<% if (event.getStartDate() != null) out.print(resources.getInputDate(event.getStartDate()));%>
	          </td>
	        </tr>
			<tr> 
	          <td nowrap align=right class="txtlibform"><span><%=resources.getString("endDatePeriodicity")%>&nbsp;:&nbsp;</td>
	          <td valign="baseline"> 
	           <% if (periodicity.getUntilDatePeriod() != null) out.print(resources.getInputDate(periodicity.getUntilDatePeriod()));%>
	          </td>
	        </tr>
		<%
		}
		%>
  </table>
  <%
		out.println(board.printAfter());
  %>
  </td><td valign="top">
  <%
  		out.flush();
  		getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachments.jsp?Id="+event.getId()+"&ComponentId="+instanceId+"&Context=Images").include(request, response);
  %>
  </td></tr>
  </table>
  <%
		out.println("<br>");
 		ButtonPane buttonPane = graphicFactory.getButtonPane();
		buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.back"), "almanach.jsp", false));
		out.println(buttonPane.print());
		out.println("<br>");
		out.println("</center>");
		out.println(frame.printAfter());				
		out.println(window.printAfter());
	%>
</BODY>
</HTML>