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
<%@ page import="java.lang.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.*"%>

<%@ include file="checkScc.jsp" %>

<!-- dictionnaireIcones -->

 <html>
<title><%=resources.getString("GML.popupTitle")%></title>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
</head>

 <body bgcolor="FFFFFF" topmargin="5" leftmargin="5">

<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    //CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
    //CBO : UPDATE
//browseBar.setComponentName(scc.getComponentLabel());
browseBar.setComponentName(componentLabel);
    browseBar.setPath(resources.getString("NotationSites"));
    
    
    Frame frame = gef.getFrame();
    
    
    out.println(window.printBefore());
    out.println(frame.printBefore()); 
 
    Collection icones = scc.getAllIcons();
    Iterator i = icones.iterator();
    i.next(); //saute la premiere icone reference (site important) 
    while (i.hasNext()) {
        IconDetail ic = (IconDetail) i.next();
        out.println("<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor><tr><td><TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4><tr><td><p align=justify><img src=\""+ic.getAddress()+"\" align=absmiddle>&nbsp;&nbsp;<font face=verdana size=2><b>"+resources.getString(ic.getName())+" :</b></font><br><font face=verdana size=1>");
        out.println(resources.getString(ic.getDescription())+"</p></font></td></tr></table></td></tr></table><br>");
    }
 
    out.println(frame.printMiddle());
    out.println(frame.printAfter());
    out.println(window.printAfter());

%>

</body>
</html>