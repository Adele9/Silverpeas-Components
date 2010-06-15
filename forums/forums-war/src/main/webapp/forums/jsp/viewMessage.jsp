<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp"%>
<%@ include file="forumsListManager.jsp"%>
<%@ include file="messagesListManager.jsp"%>
<%
    int messageId = 0;

    int forumId = getIntParameter(request, "forumId");
    String call = request.getParameter("call");
    int action = getIntParameter(request, "action", 1);
    int params = getIntParameter(request, "params");
    int currentMessageId = -1;
    String nbModeratorsString = (String) request.getAttribute("NbModerators");
    int nbModerators = 0;
    if (StringUtil.isDefined(nbModeratorsString)) {
      nbModerators = Integer.parseInt(nbModeratorsString);
    }
    
    boolean scrollToMessage = false;
    boolean displayAllMessages = false;
    
    try
    {
        switch (action)
        {
            case 1 :
                // Affichage de la liste
                messageId = params;
                if ("true".equals(request.getParameter("addStat")))
                {
                    // depuis la page de forums ou de messages
                    int parentId = fsc.getMessageParentId(messageId);
                    while (parentId > 0)
                    {
                        messageId = parentId;
                        parentId = fsc.getMessageParentId(messageId);
                    }
                    fsc.addMessageStat(messageId, userId);
                    messageId = params;
                }
                if ("true".equals(request.getParameter("changeDisplay")))
                {
                    // changement du type d'affichage
                    fsc.changeDisplayAllMessages();
                }
                break;
                
            case 8 :
                int parentId = getIntParameter(request, "parentId", 0);
                String messageTitle = request.getParameter("messageTitle").trim();
                String messageText = request.getParameter("messageText").trim();
                String subscribe = request.getParameter("subscribeMessage");
                
                if ((messageTitle.length() > 0) && (messageText.length() > 0))
                {
                    if (params == -1)
                    {
                        // Cr�ation
                        int result = fsc.createMessage(
                            messageTitle, userId, forumId, parentId, messageText, null);
                        messageId = result;
                    }
                    else
                    {
                        // Modification
                        messageId = params;
                        fsc.updateMessage(messageId, parentId, messageTitle, messageText);
                    }
                    if (subscribe == null)
                    {
                        subscribe = "0";
                    }
                    else
                    {
                        subscribe = "1";
                        if (messageId != 0)
                        {
                            fsc.subscribeMessage(messageId, userId);
                        }
                    }
                    if (parentId > 0)
                    {
                        fsc.deployMessage(parentId);
                    }
                }
                call = "viewForum";
                scrollToMessage = true;
                break;
                
            case 9 :
                messageId = fsc.getMessageParentId(params);
                fsc.deleteMessage(params);
                call = "viewForum";
                scrollToMessage = "true".equals(request.getParameter("scroll"));
                break;
                
            case 10 :
                fsc.deployMessage(params);
                messageId = params;
                break;
                
            case 11 :
                fsc.undeployMessage(params);
                messageId = params;
                break;
                
            case 13 :
                fsc.unsubscribeMessage(params, userId);
                messageId = params;
                break;
                
            case 14 :
                fsc.subscribeMessage(params, userId);
                messageId = params;
                break;
                
            case 15 :
                // Notation d'un message.
                int note = getIntParameter(request, "note", -1);
                if (note > 0)
                {
                    fsc.updateMessageNotation(params, note);
                }
                messageId = params;
                break;
        }
    }
    catch (NumberFormatException nfe)
    {
        SilverTrace.info("forums", "JSPviewMessage", "root.EX_NO_MESSAGE", null, nfe);
    }
    
    String backURL = ActionUrl.getUrl(call, -1, forumId);
    
    Message message = fsc.getMessage(messageId);
    if (forumId == -1)
    {
        forumId = message.getForumId();
    }
    
    boolean forumActive = false;
    
    int[] forumNotes = new int[0];

    if (message == null)
    {
%>
<script type="text/javascript">window.location.href = "Main";</script><%

    }
    else
    {
        int reqForum = (forumId != -1 ? forumId : 0);
        int folderId = message.getForumId();
        boolean isModerator = fsc.isModerator(userId, folderId);
        
        displayAllMessages = fsc.isDisplayAllMessages();
        
        forumActive = fsc.isForumActive(folderId);
        
        String folderName = Encode.javaStringToHtmlString(
            fsc.getForumName(folderId > 0 ? folderId : params));

        ResourceLocator settings = fsc.getSettings();
        String configFile = SilverpeasSettings.readString(settings, "configFile",
            URLManager.getApplicationURL() + "/wysiwyg/jsp/javaScript/myconfig.js");
        
        // Messages
        currentMessageId = messageId;
        int parent = fsc.getMessageParentId(currentMessageId);
        while (parent > 0)
        {
            currentMessageId = parent;
            parent = fsc.getMessageParentId(currentMessageId);
        }
        Message[] messages = fsc.getMessagesList(folderId, currentMessageId);
        int messagesCount = messages.length;
%>

<%@page import="java.util.List"%><html>
<head>
    <title>_________________/ Silverpeas - Corporate portal organizer \_________________/</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"><%

        out.println(graphicFactory.getLookStyleSheet());
        if (!graphicFactory.hasExternalStylesheet())
        {
		%>
    		<link rel="stylesheet" type="text/css" href="styleSheets/forums.css">
    	<% } %>
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/viewMessage.js"></script>
    <script type="text/javascript" src="<%=context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
    <script type="text/javascript">
        function init()
        {
            parentMessageId = <%=currentMessageId%>;
        }
        
        function validateMessage()
        {
            if (document.forms["forumsForm"].elements["messageTitle"].value == "")
            {
                alert('<%=resource.getString("emptyMessageTitle")%>');
            }
            else if (!isTextFilled())
            {
                alert('<%=resource.getString("emptyMessageText")%>');
            }
            else
            {
                document.forms["forumsForm"].submit();
            }
        }
    
        function deleteMessage(messageId, parentId, scroll)
        {
            if (confirm('<%=resource.getString("confirmDeleteMessage")%>'))
            {
                window.location.href = (parentId == 0 ? "viewForum.jsp" : "viewMessage.jsp")
                    + "?action=9"
                    + "&params=" + messageId
                    + "&forumId=<%=reqForum%>"
                    + "&scroll=" + (scroll && parentId != 0);
            }
        }
            
        function initFCKeditor()
        {
            if (oFCKeditor == null)
            {
                oFCKeditor = new FCKeditor("messageText");
                oFCKeditor.Width = "500";
                oFCKeditor.Height = "300";
                oFCKeditor.BasePath = "<%=URLManager.getApplicationURL()%>/wysiwyg/jsp/FCKeditor/";
                oFCKeditor.DisplayErrors = true;
                oFCKeditor.Config["AutoDetectLanguage"] = false;
                oFCKeditor.Config["DefaultLanguage"] = "<%=fsc.getLanguage()%>";
                oFCKeditor.Config["CustomConfigurationsPath"] = "<%=configFile%>";
                oFCKeditor.ToolbarSet = "quickinfo";
                oFCKeditor.Config["ToolbarStartExpanded"] = true;
                oFCKeditor.ReplaceTextarea();
            }
        }

        function callResizeFrame()
        {
            <%addJsResizeFrameCall(out, fsc);%>
        }

        function loadNotation()
        {
            if (document.getElementById(NOTATION_PREFIX + "1") == undefined)
            {
                setTimeout("loadNotation()", 200);
            }
            else
            {
                var img;
                var i;
                for (i = 1; i <= NOTATIONS_COUNT; i++)
                {
                    notationFlags[i - 1] = false;
                    img = document.getElementById(NOTATION_PREFIX + i);
                    img.alt = "<%=resource.getString("forums.giveNote")%> " + i + "/" + NOTATIONS_COUNT;
                    img.title = "<%=resource.getString("forums.giveNote")%> " + i + "/" + NOTATIONS_COUNT;
                    if (!readOnly)
                    {
                        img.onclick = function() {notationNote(this);};
                        img.onmouseover = function() {notationOver(this);};
                        img.onmouseout = function() {notationOut(this);};
                    }
                }
            }
        }

        function notationNote(image) {
            var index = getNotationIndex(image);
            var updateNote = false;
            if (userNote > 0) {
                if (index == userNote) {
                    alert("<%=resource.getString("forums.sameNote")%> " + userNote + ".");
                } else {
                    updateNote = confirm("<%=resource.getString("forums.replaceNote")%> " + userNote + " <%=resource.getString("forums.by")%> " + index + ".");
                }
            } else {
                updateNote = true;
            }
            if (updateNote) {
                currentNote = index;
                document.forms["notationForm"].elements["note"].value = currentNote;
                document.forms["notationForm"].submit();
            }
        }

        function editMessage(messageId)
        {
            window.location.href = "modifyMessage.jsp?params=" + messageId;
        }

        function valideMessage(messageId)
        {
          if (confirm('<%=resource.getString("confirmValideMessage")%>'))
            {
            window.location.href = "ValidateMessage?params=" + messageId;
            }
        }

        function refuseMessage(messageId)
        {
            window.location.href = "refuseMessage.jsp?params=" + messageId;
        }
                
    </script>
</head>

<body id="forum" <%addBodyOnload(out, fsc);%>><%

        Window window = graphicFactory.getWindow();
        Frame frame=graphicFactory.getFrame();
    
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(fsc.getSpaceLabel());
        browseBar.setComponentName(fsc.getComponentLabel(), ActionUrl.getUrl("main"));
        browseBar.setPath(navigationBar(reqForum, resource, fsc));
        
        out.println(window.printBefore());
        out.println(frame.printBefore());
        
        int previousMessageId = -1;
        int nextMessageId = -1;
        if (!displayAllMessages && messagesCount > 1)
        {
            int i = 0;
            while (i < messagesCount && previousMessageId == -1 && nextMessageId == -1)
            {
                if (messages[i].getId() == messageId)
                {
                    if (i > 0)
                    {
                        previousMessageId = messages[i - 1].getId();
                    }
                    if (i < (messagesCount - 1))
                    {
                    nextMessageId = messages[i + 1].getId();
                    }
                }
                i++;
            }
        }
    
        // Liste des messages
        String formAction = (reqForum > 0
            ? ActionUrl.getUrl("viewMessage", 8, forumId) : ActionUrl.getUrl("main", 8, -1));
%>
    <center>
        <table class="intfdcolor4" border="0" cellpadding="0" cellspacing="0" width="98%">
            <form name="forumsForm" action="<%=formAction%>" method="post">
              <input type="hidden" name="type" value="sendNotif" />
              <input type="hidden" name="forumId" value="<%=forumId%>" />
            <tr class="notationLine">
                <td align="right"><%

        forumNotes = displayMessageNotation(out, resources, currentMessageId, fsc, isReader);

              %></td>
            </tr>
            <tr>
                <td valign="top"><%

        displaySingleMessageList(out, resource, userId, isAdmin, isModerator, isReader,
            false, folderId, messageId, true, "viewForum", fsc, resources);
%>
                </td>
            </tr>
        </table><%

        if (messagesCount > 1)
        {
%>
        <table class="intfdcolor4" border="0" cellpadding="0" cellspacing="0" width="98%">
            <tr>
                <td width="250">&nbsp;</td>
                <td align="center" class="texteLabelForm">&nbsp;<%

            if (!displayAllMessages)
            {
                if (previousMessageId != -1)
                {
%>
                    <a href="<%=ActionUrl.getUrl("viewMessage", "viewForum", 1, previousMessageId, forumId)%>"><%=resource.getString("forums.previous")%></a><%

                }
                else
                {
%>
                    <%=resource.getString("forums.previous")%><%

                }
%>
                    &nbsp;<%

                if (nextMessageId != -1)
                {
%>
                    <a href="<%=ActionUrl.getUrl("viewMessage", "viewForum", 1, nextMessageId, forumId)%>"><%=resource.getString("forums.next")%></a><%

                }
                else
                {
%>
                    <%=resource.getString("forums.next")%><%

                }
            }
%>
                </td>
                <td align="right" class="texteLabelForm" width="250"><a href="<%=ActionUrl.getUrl("viewMessage", (StringUtil.isDefined(call)? call : "viewForum"), 1, messageId, forumId, false, true)%>"><%=resource.getString(displayAllMessages ? "forums.displayCurrentMessage" : "forums.displayAllMessages")%></a></td>
            </tr>
        </table><%

        }
%>
    </center>
    <br>
    <center>
        <table width="98%" border="0" cellspacing="0" cellpadding="0" class="intfdcolor4">
            <tr>
                <td valign="top"><%

        
        if (!displayAllMessages)
        {
            messages = new Message[] {message};
        }
        Message currentMessage;
        int currentId;
        int parentId;
        String author;
        String authorLabel;
        String text;
        String status;
        boolean isSubscriber;
        boolean hasChildren;
        Hashtable authorNbMessages = new Hashtable();
        int nbMessages;
        for (int i = 0, n = messages.length; i < n; i++)
        {
            currentMessage = messages[i];
            currentId = currentMessage.getId();
            parentId = currentMessage.getParentId();
            author = currentMessage.getAuthor();
            authorLabel = fsc.getAuthorName(author);
            status = currentMessage.getStatus();
            if (authorLabel == null)
            {
                authorLabel = resource.getString("inconnu");
            }
            text = currentMessage.getText();
            isSubscriber = fsc.isSubscriber(currentId, userId);
            hasChildren = hasMessagesChildren(messages, currentId);
            if (!authorNbMessages.containsKey(author))
            {
                nbMessages = fsc.getAuthorNbMessages(author);
                authorNbMessages.put(author, new Integer(nbMessages));
            }
            else
            {
                nbMessages = ((Integer)authorNbMessages.get(author)).intValue();
            }
%>
                    
                    <div id="msgContent<%=currentId%>">
                        <a name="msg<%=currentId%>"/>
                        <table width="100%" border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor">
                            <tr>
								<td valign="top" width="150px" bgcolor="#EEEEEE">
									<span class="txtnav"><%=authorLabel%></span><br/>
									<span class="txtnote"><%=resource.getString("forums.nbMessages")%> : <%=nbMessages%></span>
								</td>
                                <td valign="top">
                                    <table border="0" cellspacing="0" cellpadding="5" width="100%">
                                        <tr>
                                            <td><span class="txtnav"><%=currentMessage.getTitle()%></span>&nbsp;<span class="txtnote"><%=convertDate(currentMessage.getDate(), resources)%></span></td>
                                            <td valign="top" align="right">&nbsp;<%

            if (displayAllMessages) {
%>
                                                <a href="javascript:scrollTop()"><img src="<%=context%>/util/icons/arrow/arrowUp.gif" align="middle" border="0"></a>&nbsp;<%

            }
%>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="2"><img src="<%=context%>/util/icons/colorPix/1px.gif" width="100%" height="1" class="intfdcolor"></td>
                                        </tr>
                                        <tr>
                                            <td colspan="2">
                                                <table border="0" cellspacing="5" cellpadding="0" width="100%">
                                                    <tr>
                                                        <td>&nbsp;</td>
                                                        <td width="100%" valign="top" class="msgText"><span class="txtnote"><%=text%></span></td>
                                                        <td>&nbsp;</td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr><%

            if (!isReader) {
%>
                                        <tr>
                                            <td colspan="2"><img src="<%=context%>/util/icons/colorPix/1px.gif" width="100%" height="1" class="intfdcolor"></td>
                                        </tr>
                                        <tr>
                                            <td valign="top" nowrap><input name="checkbox" type="checkbox" <%if (isSubscriber) {%>checked<%}%>
                                                onclick="javascript:window.location.href='viewMessage.jsp?action=<%=(isSubscriber ? 13 : 14)%>&params=<%=currentId%>&forumId=<%=forumId%>'">
                                                <span class="texteLabelForm"><%=resource.getString("subscribeMessage")%></span></td>
                                            <td valign="top" align="right">&nbsp;<%

            
            if (forumActive) 
            {
	            if (!isReader)
	            {
				         %> 
				          <a href="javascript:replyMessage(<%=currentId%>)"><img
				           src="<%=context%>/util/icons/reply.gif" align="middle" border="0" alt="<%=resource.getString("replyMessage")%>" title="<%=resource.getString("replyMessage")%>"></a>&nbsp;
				         <%
	            }
              if (userId.equals(author) || isAdmin || isModerator)
              {
                if (STATUS_FOR_VALIDATION.equals(status)) {
                  // afficher les ic�nes pour valider ou refuser un message
                  %>
                    <a href="javascript:valideMessage(<%=currentId%>)"><img
                      src="<%=context%>/util/icons/ok.gif" align="middle" border="0" alt="<%=resource.getString("valideMessage")%>" title="<%=resource.getString("valideMessage")%>"></a>&nbsp;
                    <a href="javascript:refuseMessage(<%=currentId%>)"><img
                      src="<%=context%>/util/icons/wrong.gif" align="middle" border="0" alt="<%=resource.getString("refuseMessage")%>" title="<%=resource.getString("refuseMessage")%>"></a>&nbsp;
                  <%

                }
%>
                 <a href="javascript:editMessage(<%=currentId%>)"><img
                   src="<%=context%>/util/icons/update.gif" align="middle" border="0" alt="<%=resource.getString("editMessage")%>" title="<%=resource.getString("editMessage")%>"></a>&nbsp;
                 <a href="javascript:deleteMessage(<%=currentId%>, <%=parentId%>, true)"><img
                   src="<%=context%>/util/icons/delete.gif" align="middle" border="0" alt="<%=resource.getString("deleteMessage")%>" title="<%=resource.getString("deleteMessage")%>"></a>&nbsp;<%
                }
                  
               }
            }
%>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                        <br>
                    </div><%

        }

        if (forumActive)
        {
%>
                    <div id="responseTable">
                        <table width="100%" border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor">
                            <tr>
                                <td valign="top">
                                    <table border="0" cellspacing="0" cellpadding="5" width="100%">
                                        <!-- REPONSE -->
                                        <!-- ligne s�paratrice
                                        <tr>
                                            <td colspan="2"><img src="<%=context%>/util/icons/colorPix/1px.gif" width="100%" height="1" class="intfdcolor"></td>
                                        </tr>
                                        -->
                                        <tr>
                                            <td colspan="2"><span class="txtnav"><!-- <img src="icons/fo_flechebas.gif" width="11" height="6">&nbsp;<%=resource.getString("repondre")%> --></span></td>
                                        </tr>
                                        <input type="hidden" name="forumId" value="<%=message.getForumId()%>"/>
                                        <input type="hidden" name="parentId" value="<%=messageId%>"/>
                                        <tr>
                                            <td align="left" valign="top"><span class="txtlibform"><%=resource.getString("messageTitle")%> :&nbsp;</span></td>
                                            <td valign="top"><input type="text" name="messageTitle" value="Re : <%=Encode.javaStringToHtmlString(message.getTitle())%>" size="88" maxlength="<%=DBUtil.TextFieldLength%>"></td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top"><span class="txtlibform"><%=resource.getString("messageText")%> :&nbsp;</span></td>
                                            <td valign="top"><font size=1><textarea name="messageText" id="messageText"></textarea></font></td>
                                        </tr>
                                        <tr>
                                            <td align="left" valign="top"><span class="txtlibform"><%=resource.getString("subscribeMessage")%> :&nbsp;</span></td>
                                            <td valign="top"><input type="checkbox" name="subscribeMessage"></td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                        <br>
                        <center>
<%
            ButtonPane msgButtonPane = graphicFactory.getButtonPane();
            msgButtonPane.addButton(graphicFactory.getFormButton(
                resource.getString("valider"), "javascript:validateMessage();", false));
            msgButtonPane.addButton(graphicFactory.getFormButton(
                resource.getString("annuler"), "javascript:cancelMessage();", false));
            msgButtonPane.setHorizontalPosition();
            out.println(msgButtonPane.print());
%>
                        </center>
                    </div><%

        }
%>
                </td>
            </tr>
        </form>
        </table>
    </center>
<%
        out.println(frame.printMiddle());
%>
    <br>
    <div id="backButton">
        <center>
<%
        ButtonPane backButtonPane = graphicFactory.getButtonPane();
        backButtonPane.addButton(graphicFactory.getFormButton("Retour", backURL, false));
        backButtonPane.setHorizontalPosition();
        out.println(backButtonPane.print());
%>
        </center>
    </div>
    <br>
<%
        out.println(frame.printAfter());
        out.println(window.printAfter());
    }
%>  
    <script type="text/javascript">init();scrollMessageList(<%=messageId%>);</script><%

    if (displayAllMessages && scrollToMessage)
    {
%>
    <script type="text/javascript">scrollMessage(<%=messageId%>);</script><%

    }
    
    if (!isReader && forumNotes.length > 0)
    {
%>
    <form name="notationForm" action="viewMessage" method="post">
        <input name="call" type="hidden" value="viewForum"/>
        <input name="action" type="hidden" value="15"/>
        <input name="forumId" type="hidden" value="<%=forumId%>"/>
        <input name="params" type="hidden" value="<%=currentMessageId%>"/>
        <input name="note" type="hidden" value=""/>
    </form>
    <script type="text/javascript">
        readOnly = <%=isReader%>;
        currentNote = <%=forumNotes[0]%>;
        userNote = <%=forumNotes[1]%>;
        loadNotation();
    </script><%

    }
%>
</body>
</html>