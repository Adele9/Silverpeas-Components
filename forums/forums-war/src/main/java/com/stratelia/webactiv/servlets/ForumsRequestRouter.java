package com.stratelia.webactiv.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.models.Category;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.sessionController.ForumsSessionController;
import com.stratelia.webactiv.forums.url.ActionUrl;
import com.stratelia.webactiv.util.node.model.NodeDetail;

public class ForumsRequestRouter extends ComponentRequestRouter
{
	
	private static final String ROOT_DEST = "/forums/jsp/";
	
	public ComponentSessionController createComponentSessionController(
		MainSessionController mainSessionCtrl, ComponentContext context)
	{
		return (ComponentSessionController) new ForumsSessionController(mainSessionCtrl, context);
	}

	/**
     * This method has to be implemented in the component request rooter class.
     * returns the session control bean name to be put in the request object
     */
    public String getSessionControlBeanName()
    {
		return "forumsSessionClientController";
	}

    public String getDestination(String function, ComponentSessionController componentSC,
    	HttpServletRequest request)
    {
    	String destination = "";
		ForumsSessionController forumsSC = (ForumsSessionController) componentSC;

		if ((function.startsWith("Main")) || (function.startsWith("main")))
		{
			destination = ROOT_DEST + "main.jsp";
		}
		
		else if (function.startsWith("external"))
		{
			forumsSC.setExternal(true);
			String mailType = request.getParameter("mailType");
			if (mailType != null)
			{
				forumsSC.setMailType(mailType);
			}
			forumsSC.setResizeFrame("true".equals(request.getParameter("resizeFrame")));
			String actionUrl = null;
			String messageId = request.getParameter("id");
			if (messageId != null)
			{
				Message message = forumsSC.getMessage(Integer.parseInt(messageId));
				if (message != null)
				{
					actionUrl = ActionUrl.getUrl("viewMessage", "main", 1, message.getId(),
						message.getForumId(), true, false);
				}
			}
			else
			{
				String forumId = request.getParameter("forumId");
				if (forumId != null)
				{
					actionUrl = ActionUrl.getUrl("viewForum", "main", Integer.parseInt(forumId));
				}
			}
			destination = ROOT_DEST + (actionUrl != null ? actionUrl : "main.jsp");
		}
		
		else if (function.startsWith("portlet"))
		{
			destination = ROOT_DEST + "portlet.jsp";
		}
		
		else if (function.startsWith("viewForum"))
		{
			destination = ROOT_DEST + "viewForum.jsp";
		}
		else if (function.startsWith("editForumInfo"))
		{
			destination = ROOT_DEST + "editForumInfo.jsp";
		}
		
		else if (function.startsWith("viewMessage"))
		{
			// mise � jour de la table des consultations
			String messageId = request.getParameter("params");
			if (!StringUtil.isDefined(messageId))
			{
				messageId = (String) request.getAttribute("params");
			}
			if (StringUtil.isDefined(messageId))
			{
				SilverTrace.info("forums", "ForumsRequestRouter", "root.MSG_GEN_PARAM_VALUE",
					"messageId (pour last visite) = " + messageId);
				forumsSC.setLastVisit(componentSC.getUserId(), Integer.parseInt(messageId));
			}
			destination = ROOT_DEST + "viewMessage.jsp";
		}
		else if (function.startsWith("editMessageKeywords"))
		{
			destination = ROOT_DEST + "editMessageKeywords.jsp";
		}
		else if (function.startsWith("editMessage"))
		{
			destination = ROOT_DEST + "editMessage.jsp";
		}
		else if (function.startsWith("modifyMessage"))
		{
			destination = ROOT_DEST + "modifyMessage.jsp";
		}
		

		// gestion des cat�gories
		// ----------------------
		else if (function.equals("ViewCategory"))
		{
			destination = ROOT_DEST + "main.jsp";
		}
		else if (function.equals("NewCategory"))
		{
			destination = ROOT_DEST + "categoryManager.jsp";
		}
		else if (function.equals("CreateCategory"))
		{
			// r�cup�ration des param�tres
			String name = (String) request.getParameter("Name");
			String description = (String) request.getParameter("Description");
			NodeDetail node = new NodeDetail(
				"unknown", name, description, null, null, null, "0", "unknown");
			Category category = new Category(node);
			forumsSC.createCategory(category);

			destination = getDestination("ViewCategory", componentSC, request);
		}
		else if (function.equals("EditCategory"))
		{
			// r�cup�ration des param�tres
			String categoryId = (String) request.getParameter("CategoryId");
			Category category = forumsSC.getCategory(categoryId);
			request.setAttribute("Category", category);

			destination = ROOT_DEST + "categoryManager.jsp";
		}
		else if (function.equals("UpdateCategory"))
		{
			String categoryId = (String) request.getParameter("CategoryId");
			Category category = forumsSC.getCategory(categoryId);
			String name = request.getParameter("Name");
			category.setName(name);
			String desc = request.getParameter("Description");
			category.setDescription(desc);
			forumsSC.updateCategory(category);

			destination = getDestination("ViewCategory", componentSC, request);
		}
		else if (function.equals("DeleteCategory"))
		{
			String categoryId = (String) request.getParameter("CategoryId");
			SilverTrace.debug("forums", "ForumsRequestRouter", "root.MSG_GEN_PARAM_VALUE",
				"categoryId = " + categoryId);
			forumsSC.deleteCategory(categoryId);

			destination = getDestination("ViewCategory", componentSC, request);
		}
		
		else if (function.startsWith("searchResult"))
		{
			String id = request.getParameter("Id");
			String type = request.getParameter("Type");
			if (type.equals("Forum"))
			{
				destination = ROOT_DEST + "viewForum.jsp?forumId=" + id;
			}
			else if (type.equals("ForumsMessage"))
			{
				request.setAttribute("params", id);
				destination = getDestination("viewMessage", componentSC, request);
			}
			else
			{
				destination = ROOT_DEST + "viewMessage.jsp?action=1&params=" + id;
			}
		}
		else
		{
			destination = ROOT_DEST + function;
		}

		return destination;
	}
    
}