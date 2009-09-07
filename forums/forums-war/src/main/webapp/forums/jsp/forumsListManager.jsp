<%@ page import="java.io.IOException"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Date"
%><%@ page import="com.silverpeas.notation.model.NotationDetail"
%><%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"
%><%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"
%><%@ page import="com.stratelia.webactiv.forums.forumsException.ForumsException"
%><%@ page import="com.stratelia.webactiv.forums.models.Forum"
%><%@ page import="com.stratelia.webactiv.forums.sessionController.ForumsSessionController"
%><%@ page import="com.stratelia.webactiv.forums.url.ActionUrl"
%><%@ page import="com.stratelia.webactiv.util.ResourceLocator"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"
%><%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"
%>
<%!
public String navigationBar(int forumId, ResourceLocator resource, ForumsSessionController fsc)
    throws ForumsException
{
    boolean loop = false;
    String result = "";
    int currentId = forumId;
    String base = "";

    if (forumId != 0)
    {
        result = "<a href=\"" + ActionUrl.getUrl("viewForum", -1, forumId) + "\">"
            + Encode.javaStringToHtmlString(fsc.getForumName(forumId))
            + "</a>";
        loop = true;
    }
    
    while (loop)
    {
        int forumParent = fsc.getForumParentId(currentId);
        if (forumParent == 0)
        {
            result = base + result;
            loop = false;
        }
        else
        {
            String parentName = fsc.getForumName(forumParent);
            String line = "<a href=\"" + ActionUrl.getUrl("viewForum", -1, forumParent) + "\">"
                + Encode.javaStringToHtmlString(parentName) + "</a> &gt; ";
            result = line + result;
            currentId = forumParent;
        }
    }
    return result;
}

public void displayForumLine(Forum forum, ResourcesWrapper resources, JspWriter out,
    int currentPage, String call, boolean admin, boolean moderator, boolean reader, int depth,
    boolean hasChildren, boolean deployed, ForumsSessionController fsc)
{
    try
    {
        int forumId = forum.getId();
        String forumName = forum.getName();
        String forumDescription = forum.getDescription();
        boolean forumActive = forum.isActive();
        
        String nbSubjects = Integer.toString(fsc.getNbSubjects(forumId));
        String nbMessages = Integer.toString(fsc.getNbMessages(forumId));
        
        int lastMessageId = -1;
        Date dateLastMessage = null;
        String lastMessageDate = "";
        String lastMessageUser = "";
        Object[] lastMessage = fsc.getLastMessage(forumId);
        if (lastMessage != null)
        {
            lastMessageId = Integer.parseInt((String)lastMessage[0]);
            lastMessageDate = convertDate((Date)lastMessage[1], resources);
            lastMessageUser = (String)lastMessage[2];
        }
        
        out.println("<tr>");

        // 1�re colonne : �tat des messages (lus / non lus)
        out.print("<td>");

        if (!fsc.isExternal() || !reader)
        {
            // rechercher si l'utilisateur a des messages non lus sur ce forum
            boolean isNewMessage = fsc.isNewMessageByForum(fsc.getUserId(), forumId);
            out.print("<img src=\"icons/" + (isNewMessage ? "buletRed" : "buletColoredGreen") + ".gif\">");
        }

        // Icone de deploiement
        out.print("<img src=\"icons/1px.gif\">");
        if (depth > 0)
        {
            out.print("<img src=\"icons/1px.gif\" width=\"" + (depth * 10) + "\" height=\"1\">");
        }
        /*
        if (hasChildren) {
            out.print("<a href=\"");
            out.print(ActionUrl.getUrl((currentPage > 0 ? "viewForum" : "main"), call,
                (deployed ? 2 : 1), forumId, (currentPage > 0 ? currentPage : -1)));
            out.print("\">");
            if (!deployed)
            {
                out.print("<img src=\"icons/topnav_r.gif\" width=\"6\" height=\"11\" border=\"0\">");
            }
            out.print("</a>");
        }
        else
        {
            out.print("&nbsp;");
        }
        */
        out.println("</td>");

        // 2�me colonne : nom et description
        out.print("<td width=\"100%\" >");
        out.print("<a href=\"" + ActionUrl.getUrl("viewForum", call, forumId) + "\">");
        out.print("<span class=\"titreForum\">");
        out.print(Encode.javaStringToHtmlString(forumName));
        out.print("</span>");
        out.print("<br>");
        // description du forum
        out.print("<span class=\"descriptionForum\">");
        out.print(Encode.javaStringToHtmlString(forumDescription));
        out.print("</span>");
        out.print("</a>");
        out.println("</td>");
        
        // 3�me colonne : nombre de sujets
        out.print("<td align=\"center\" class=\"fondClair\"><span class=\"txtnote\">");
        out.print(Encode.javaStringToHtmlString(nbSubjects));
        out.println("</span></td>");
        
        // 4�me colonne : nombre de sujets
        out.print("<td align=\"center\" class=\"fondFonce\"><span class=\"txtnote\">");
        out.print(Encode.javaStringToHtmlString(nbMessages));
        out.println("</span></td>");
        
        // 5�me colonne : dernier message
        out.print("<td nowrap=\"nowrap\" align=\"center\" class=\"fondClair\"><span class=\"txtnote\">");
        if (lastMessageDate != null)
        {
            out.print("<a href=\"" + ActionUrl.getUrl(
                "viewMessage", call, 1, lastMessageId, forumId, true, false) + "\">");
            out.print(Encode.javaStringToHtmlString(lastMessageDate));
            out.print("<br/>");
            out.print(Encode.javaStringToHtmlString(lastMessageUser));
            out.print("</a>");
        }
        out.println("</span></td>");
        
        // 6�me colonne : notation
        NotationDetail notation = fsc.getForumNotation(forumId);
        int globalNote = notation.getRoundGlobalNote();
        int userNote = notation.getUserNote();
        String cellLabel = notation.getNotesCount() + " " + resources.getString("forums.note");
        if (userNote > 0) {
            cellLabel += " - " + resources.getString("forums.yourNote") + " : " + userNote;
        }
        out.print("<td align=\"center\" class=\"fondFonce\" alt=\"" + cellLabel + "\" title=\""
            + cellLabel + "\"><span class=\"txtnote\">");
        for (int i = 1; i <= 5; i++) {
            out.print("<img class=\"notation_" + (i <= globalNote ? "on" : "off")
                + "\" src=\"" + IMAGE_NOTATION_EMPTY + "\"/>");
        }
        out.println("</span></td>");
            
        // 7�me colonne : boutons d'admin
        if (admin || moderator)
        {
            out.print("<td align=\"center\" nowrap class=\"fondClair\">");
        
            // icone de modification
            out.print("<a href=\"");
            out.print(ActionUrl.getUrl("editForumInfo", call, 2, forumId, currentPage));
            out.println("\"><img src=\"" + IMAGE_UPDATE + "\" border=\"0\" align=\"middle\" alt=\""
                + resources.getString("editForum") + "\" title=\""
                + resources.getString("editForum") + "\"/></a>");
            out.print("&nbsp;");
            // icone de suppression
            out.print("<a href=\"javascript:confirmDeleteForum('" + forumId + "');\">");
            out.print("<img src=\""+ IMAGE_DELETE +"\" border=\"0\" align=\"middle\" alt=\""
                + resources.getString("deleteForum") + "\" title=\""
                + resources.getString("deleteForum") + "\"/></a>");
            out.print("&nbsp;");
            // icone de verrouillage
            out.print("<a href=\"");
            out.print(ActionUrl.getUrl((currentPage > 0 ? "viewForum" : "main"), call,
                (forumActive ? 5 : 6), forumId, currentPage));
            out.print("\">");
            if (forumActive) 
            {
                out.print("<img src=\"" + IMAGE_UNLOCK + "\" border=\"0\" align=\"middle\" alt=\"");
                out.print(resources.getString("lockForum"));
                out.print("\" title=\"" + resources.getString("lockForum") + "\"/>");
            }
            else 
            {
                out.print("<img src=\"" + IMAGE_LOCK + "\" border=\"0\" align=\"middle\" alt=\"");
                out.print(resources.getString("unlockForum"));
                out.print("\" title=\"" + resources.getString("unlockForum") + "\"/>");
            }
            out.print("</a>");
            
            out.println("</td>");
        }
        out.println("</tr>");
    }
    catch (IOException ioe)
    {
        SilverTrace.info(
            "forums", "JSPforumsListManager.displayForumLine()", "root.EX_NO_MESSAGE", null, ioe);
    }
}

public void displayForumsList(JspWriter out, ResourcesWrapper resources, boolean admin,
    boolean moderator, boolean reader, int currentForumId, String call, ForumsSessionController fsc,
    String categoryId, String nom, String description) 
{
    try 
    {
        Forum[] forums = fsc.getForumsListByCategory(categoryId);

        if (forums != null)
        {
            out.println("<tr>");
            out.print("<td colspan=\"6\" class=\"titreCateg\">" + nom);
            if (description != null && description.length() > 0)
            {
                out.print(" - <i>" + description + "<i>");
            }
            out.println("</td>");

            // boutons d'admin
            if (admin || moderator) 
            {
                out.print("<td class=\"titreCateg\" align=\"center\" nowrap>");
                if (categoryId != null)
                {
                    out.print("<a href=\"EditCategory?CategoryId=" + categoryId + "\">");
                    out.print("<img src=\"" + IMAGE_UPDATE + "\" border=\"0\" align=\"middle\" alt=\""
                        + resources.getString("forums.editCategory") + "\" title=\""
                        + resources.getString("forums.editCategory") + "\"/></a>");
                    out.print("&nbsp;&nbsp;");
                    out.print("<a href=\"javascript:confirmDeleteCategory('"
                        + String.valueOf(categoryId) + "');\">");
                    out.print("<img src=\"" + IMAGE_DELETE + "\" border=\"0\" align=\"middle\" alt=\""
                        + resources.getString("forums.deleteCategory") + "\" title=\""
                        + resources.getString("forums.deleteCategory") + "\"/></a>");
                    out.print("&nbsp;");
                    out.print("<img src=\"icons/1px.gif\" width=\"15\" height=\"15\" border=\"0\" align=\"middle\"/>");
                }
                out.println("</td>");
            }
            out.println("</tr>");
                    
            scanForum(forums, resources, out, currentForumId, call, admin, moderator, reader,
                currentForumId, 0, fsc);
        }
    }
    catch (IOException ioe) 
    {
        SilverTrace.info("forums", "JSPforumsListManager.displayForumsList()",
            "root.EX_NO_MESSAGE", null, ioe);
    }
}
  
public void displayForums(JspWriter out, ResourcesWrapper resources, boolean admin,
    boolean moderator, boolean reader, int currentForumId, String call, ForumsSessionController fsc,
    String categoryId)
{
    Forum[] forums = fsc.getForumsListByCategory(categoryId);
    scanForum(forums, resources, out, currentForumId, call, admin, moderator, reader,
        currentForumId, 0, fsc);
}

public void scanForum(Forum[] forums, ResourcesWrapper resources, JspWriter out, int currentPage,
    String call, boolean admin, boolean moderator, boolean reader, int currentForumId, int depth,
    ForumsSessionController fsc)
{
    Forum forum;
    for (int i = 0; i < forums.length; i++) 
    {
        forum = forums[i];
        int forumParent = forum.getParentId();
        if (forumParent == currentForumId)
        {
            int forumId = forum.getId();
            boolean hasChildren = hasChildren(forums, forumId);
            boolean isDeployed = fsc.forumIsDeployed(forumId);
            displayForumLine(forum, resources, out, currentPage, call, admin, moderator, reader,
                depth, hasChildren, isDeployed, fsc);
            if (hasChildren && isDeployed)
            {
                scanForum(forums, resources, out, currentPage, call, admin, moderator, reader,
                    forumId, depth + 1, fsc);
            }
        }
    }
}

public boolean hasChildren(Forum[] forums, int currentForumId)
{
    int i = 0;
    while (i < forums.length)
    {
        if (forums[i].getParentId() == currentForumId)
        {
            return true;
        }
        i++;
    }
    return false;
}

public void displayForumsAdminButtons(boolean moderator, OperationPane operationPane,
    int currentFolderId, String call, ResourceLocator resource) 
{
    operationPane.addOperation(IMAGE_ADD_FORUM, resource.getString("newForum"),
        ActionUrl.getUrl("editForumInfo", call, 1, currentFolderId, currentFolderId));
}
  
public void displayForumsAdminButtonsMain(boolean moderator, OperationPane operationPane,
    int currentFolderId, String call, ResourceLocator resource) 
{
    operationPane.addOperation(IMAGE_ADD_FORUM, resource.getString("newForum"),
        ActionUrl.getUrl("editForumInfo", call, 1, currentFolderId, currentFolderId));
    operationPane.addOperation(IMAGE_ADD_CATEGORY, resource.getString("forums.addCategory"), "NewCategory");
}

public int[] displayForumNotation(JspWriter out, ResourcesWrapper resources, int forumId,
    ForumsSessionController fsc, boolean reader)
{
    try 
    {
        NotationDetail notation = fsc.getForumNotation(forumId);
        int globalNote = notation.getRoundGlobalNote();
        int userNote = notation.getUserNote();
        out.print("<span class=\"txtnote\">" + resources.getString("forums.forumNote") + " : ");
        for (int i = 1; i <= 5; i++) {
            out.print("<img");
            if (!reader)
            {
                out.print(" id=\"notationImg" + i + "\"");
            }
            out.print(" style=\"margin-bottom: 0px\" class=\"notation_" + (i <= globalNote ? "on" : "off")
                + "\" src=\"" + IMAGE_NOTATION_EMPTY + "\"/>");
        }
        out.print(" (" + notation.getNotesCount() + " " + resources.getString("forums.note"));
        if (userNote > 0) {
            out.print(" - " + resources.getString("forums.yourNote") + " : " + userNote);
        }
        out.println(")</span>");
        return new int[] {globalNote, userNote};
    }
    catch (IOException ioe) 
    {
        SilverTrace.info("forums", "JSPforumsListManager.displayForumNotation()",
            "root.EX_NO_MESSAGE", null, ioe);
        return new int[0];
    }
}
%>