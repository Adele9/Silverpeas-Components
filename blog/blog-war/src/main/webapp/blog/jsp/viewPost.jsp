<%@ include file="check.jsp" %>

<% 
// r�cup�ration des param�tres
PostDetail	post		= (PostDetail) request.getAttribute("Post");
Collection	categories	= (Collection) request.getAttribute("Categories");
Collection	archives	= (Collection) request.getAttribute("Archives");
Collection	links		= (Collection) request.getAttribute("Links");
Collection 	comments	= (Collection) request.getAttribute("AllComments");
String 		profile		= (String) request.getAttribute("Profile");
String		blogUrl		= (String) request.getAttribute("Url");
String		rssURL		= (String) request.getAttribute("RSSUrl");
List		events		= (List) request.getAttribute("Events");
String 		dateCal		= (String) request.getAttribute("DateCalendar");

//d�claration des boutons
Button validateComment 	= (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
Button cancelButton 	= (Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false);

Date 	   dateCalendar	= new Date(dateCal);

String categoryId = "";
if (post.getCategory() != null)
	categoryId = post.getCategory().getNodePK().getId();
String postId = post.getPublication().getPK().getId();
String link	= post.getPermalink();

%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">

	var notifyWindow = window;
	
	// fonctions de contr�le des zones du formulaire avant validation
	function sendData() 
	{
		if (isCorrectForm()) 
		{
			document.commentForm.action = "AddComment";
			document.commentForm.submit();
		}
	}

	function isCorrectForm() 
	{
     	var errorMsg = "";
     	var errorNb = 0;
     	var message = stripInitialWhitespace(document.commentForm.Message.value);

     	if (message == "") 
     	{ 
			errorMsg+="  - '<%=resource.getString("blog.message")%>'  <%=resource.getString("GML.MustBeFilled")%>\n";
           	errorNb++;
     	}
   				     			     				    
     	switch(errorNb) 
     	{
        	case 0 :
            	result = true;
            	break;
        	case 1 :
            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            	window.alert(errorMsg);
            	result = false;
            	break;
        	default :
            	errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            	window.alert(errorMsg);
            	result = false;
            	break;
     	} 
     	return result;
	}

	function updateComment(id, postId)
	{
	    SP_openWindow("<%=m_context%>/comment/jsp/newComment.jsp?id="+id+"&IndexIt=1", "blank", "600", "250","scrollbars=no, resizable, alwaysRaised");
	    document.commentForm.action = "UpdateComment";
	   	document.commentForm.PostId.value = postId;
		document.commentForm.submit();
	}
	
	function removeComment(id)
	{
	    if (window.confirm("<%=resource.getString("blog.confirmDeleteComment")%>"))
	    {
	    	document.commentForm.action = "DeleteComment";
	    	document.commentForm.CommentId.value = id;
			document.commentForm.submit();
	    }
	}

	function commentCallBack()
	{
		location.href="<%=m_context+URLManager.getURL("useless", instanceId)%>ViewPost?PostId=<%=postId%>";
	}
	
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
	function deletePost(postId)
	{
		if (window.confirm("<%=resource.getString("blog.confirmDeletePost")%>"))
	    {
	    	document.postForm.action = "DeletePost";
	    	document.postForm.PostId.value = postId;
			document.postForm.submit();
	    }
	}
	
</script>
</head>

<body id="blog">
<div id="<%=instanceId %>">
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="blog">
	<tr>
		<td colspan="3" id="bandeau" align="center"><a href="<%="Main"%>"><%=componentLabel%></a></td>
		<% if ("admin".equals(profile)) { 
			String url = "ToAlertUser?PostId="+postId; 
			%>
			<td align="left" rowspan="3" valign="top">
				&nbsp;<a href="EditPost?PostId=<%=postId%>"><img src="<%=resource.getIcon("blog.updatePost")%>" border="0" alt="<%=resource.getString("blog.updatePost")%>" title="<%=resource.getString("blog.updatePost")%>" /></a><br>
				&nbsp;<a href="javascript:onClick=deletePost('<%=postId%>')"><img src="<%=resource.getIcon("blog.delPost")%>" border="0" alt="<%=resource.getString("blog.deletePost")%>" title="<%=resource.getString("blog.deletePost")%>" /></a>
				&nbsp;<a href="javaScript:onClick=goToNotify('<%=url%>')"><img src="<%=resource.getIcon("blog.alert")%>" border="0" alt="<%=resource.getString("GML.notify")%>"  title="<%=resource.getString("GML.notify")%>" /></a>
			</td>
		<% } %>
  	</tr>
  	<tr>
  		<td colspan="3">&nbsp;</td>
	</tr>
  	<tr>
    	<td valign="top" class="colonneGauche">
	    	<table width="100%" border="0" cellspacing="0" cellpadding="0">
			    <tr>
			       	<td class="titreTicket"><%=post.getPublication().getName()%>
			       	<%	if ( link != null && !link.equals("")) {	%>
							<a href=<%=link%> ><img src=<%=resource.getIcon("blog.link")%> border="0" alt='<%=resource.getString("blog.CopyPostLink")%>' title='<%=resource.getString("blog.CopyPostLink")%>' ></a>
						<%	}	%>
					</td>
			    </tr>
			    <tr>
	    			<td class="infoTicket"><%=post.getCreatorName()%> - <%=resource.getOutputDate(post.getDateEvent())%></td>
	    		</tr>
	    		<tr>
			    	<td>&nbsp;</td>
			    </tr>
			    <tr>
			        <td>
			        <%
			        	out.flush();
		        		getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId="+postId+"&ComponentId="+instanceId).include(request, response);
		        	%>
		        	</td>
				</tr>
				<tr>
			    	<td>&nbsp;</td>
			    </tr>
			    <tr>
			    	<td>
						<span class="versCommentaires">
							&gt;&gt; <%=resource.getString("blog.comments")%> (<%=post.getNbComments()%>) 
						</span>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<%
						if (!categoryId.equals(""))
						{  %>
							<a href="<%="PostByCategory?CategoryId="+categoryId%>" class="versTopic">&gt;&gt; <%=post.getCategory().getName()%> </a>
						<% } %>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<span class="versCommentaires"> 
							<% // date de cr�ation et de modification %>
							<%=resource.getString("GML.creationDate")%> <%=resource.getOutputDate(post.getPublication().getCreationDate())%> - 
							<%=resource.getString("GML.updateDate")%> <%=resource.getOutputDate(post.getPublication().getUpdateDate())%>
						</span>
					</td>
			    </tr>
				<tr>
	    			<td class="separateur">&nbsp;</td>
	   			</tr>
	   			<tr>
	   				<td>
		   				<!--Afficher les commentaires-->
		   				<table width="100%" border="0" cellspacing="0" cellpadding="0">
						       	<%
						      	if (comments != null)
						      	{
						      		Iterator itCom = (Iterator) comments.iterator();
						      		while (itCom.hasNext()) 
									{
						      			Comment unComment = (Comment) itCom.next();
						      			String commentDate = resource.getOutputDate(unComment.getCreationDate());
						      			String commentAuthor = unComment.getOwner();
						      			String ownerId = Integer.toString(unComment.getOwnerId());
						      			%>
						      			<tr>
								   			<td class="versCommentaires"><%=resource.getString("blog.de")%> <%=commentAuthor%> <%=resource.getString("blog.postLe")%> <%=commentDate%> 
											<% if ("admin".equals(profile) || ownerId.equals(userId) ) { %>
								                <A href="javascript:updateComment(<%=unComment.getCommentPK().getId()%>,<%=postId%>)"><IMG SRC="<%=resource.getIcon("blog.smallUpdate") %>" border="0" alt="<%=resource.getString("GML.update")%>" title="<%=resource.getString("GML.update")%>" align="absmiddle"/></A>
								                <A href="javascript:removeComment(<%=unComment.getCommentPK().getId()%>)"><IMG SRC="<%=resource.getIcon("blog.smallDelete") %>" border="0" alt="<%=resource.getString("GML.delete")%>" title="<%=resource.getString("GML.delete")%>" align="absmiddle"/></A>
								            <% } %>
											</td>
								   		</tr>
						      				<td><%=Encode.javaStringToHtmlParagraphe(unComment.getMessage())%></td>
						      			</tr>
								    	<tr>
								    		<td class="separateur">&nbsp;</td>
								   		</tr>
						       			<%
									}
						      	}
						      	%>
							   	<tr>
						   			<td>
							   			<table width="100%" border="0" cellspacing="0" cellpadding="0">
									    	<form Name="commentForm" action="AddComment" Method="POST">	
										    	<tr>
										    		<td class="infoTicket"><%=resource.getString("blog.addComment")%></td>
										    	</tr>
												<tr>
													<td><TEXTAREA ROWS="8" COLS="100" name="Message"></TEXTAREA></td>
														<input type="hidden" name="PostId" value="<%=postId%>"><input type="hidden" name="CommentId" value=""></td>
										    	</tr>
									    	</form>
								    	</table>
						   			</td>
								</tr>
								<tr>
						    		<td>
							    		<%
									   	ButtonPane buttonPaneComment = gef.getButtonPane();
							    		buttonPaneComment.addButton(validateComment);
							    		buttonPaneComment.addButton(cancelButton);
										out.println("<BR><center>"+buttonPaneComment.print()+"</center><BR>");
										%>
						    		</td>
					    		</tr>
				   				<tr>
				   			 	  	<td class="separateur">&nbsp;</td>
				   				</tr>
						</table>
					</td>
	   			</tr>
			</table>
		</td>
		<td>&nbsp;&nbsp;</td>
		<td valign="top" class="colonneDroite">
			<%@ include file="colonneDroite.jsp.inc" %>
		</td>
	</tr>
</table>
</div>

<form name="postForm" action="DeletePost" Method="POST">
	<input type="hidden" name="PostId">
</form>

</body>
</html>