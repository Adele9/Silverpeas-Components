package com.silverpeas.blog.control;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.blog.control.ejb.BlogBm;
import com.silverpeas.blog.control.ejb.BlogBmHome;
import com.silverpeas.blog.model.Archive;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.ejb.MyLinksBmHome;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.comment.ejb.CommentBm;
import com.stratelia.silverpeas.comment.ejb.CommentBmHome;
import com.stratelia.silverpeas.comment.ejb.CommentRuntimeException;
import com.stratelia.silverpeas.comment.model.Comment;
import com.stratelia.silverpeas.comment.model.CommentPK;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class BlogSessionController extends AbstractComponentSessionController
{
	private Calendar currentBeginDate = Calendar.getInstance();  //format = yyyy/MM/ddd
	private Calendar currentEndDate = Calendar.getInstance(); //format = yyyy/MM/ddd
	private String serverURL  = null;
	
    /**
     * Standard Session Controller Constructeur
     *
     *
     * @param mainSessionCtrl   The user's profile
     * @param componentContext  The component's profile
     *
     * @see
     */
	public BlogSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		super(mainSessionCtrl, componentContext,  "com.silverpeas.blog.multilang.blogBundle", "com.silverpeas.blog.settings.blogIcons");
		AdminController admin = new AdminController("useless");
    	Domain defaultDomain = admin.getDomain(getUserDetail().getDomainId());
    	serverURL = defaultDomain.getSilverpeasServerURL();
	}
	
	public Collection<PostDetail> lastPosts ()
	{
		try
		{
			// mettre � jour les variables currentBeginDate et currentEndDate
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			setMonthFirstDay(calendar);
			setMonthLastDay(calendar);
			
			//return getBlogBm().getLastPosts(getComponentId());
			return getBlogBm().getAllPosts(getComponentId(), 10);
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.lastPosts()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	private void setMonthFirstDay(Calendar calendar)
	 {
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		currentBeginDate.setTime(calendar.getTime());
	 }
	 
	 private void setMonthLastDay(Calendar calendar)
	 {
		int monthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, monthLastDay);
		currentEndDate.setTime(calendar.getTime());
	 }
	
	public Collection<PostDetail> postsByCategory (String categoryId)
	{
		try
		{
			// rechercher les billets de la cat�gorie
			if (categoryId.equals("0"))
			{
				// on veux arriver sur l'accueil
				return lastPosts ();
			}
			else
			{
				return getBlogBm().getPostsByCategory(categoryId, getComponentId());
			}
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.postsByCategory()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	public Collection<PostDetail> postsByArchive (String beginDate, String endDate)
	{
		if (endDate == null || endDate.length() == 0 || "null".equals(endDate))
		{
			beginDate = getCurrentBeginDateAsString();
			endDate = getCurrentEndDateAsString();
		}
		else
		{
			setCurrentBeginDate(beginDate);
			setCurrentEndDate(endDate);
		}
		try 
		{
			return getBlogBm().getPostsByArchive(beginDate, endDate, getComponentId());
		} 
		catch (RemoteException e) 
		{
			throw new BlogRuntimeException("BlogSessionController.postsByArchive()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	public Collection<PostDetail> postsByDate (String date)
	{
		try 
		{
			return getBlogBm().getPostsByDate(date, getComponentId());
		} 
		catch (RemoteException e) 
		{
			throw new BlogRuntimeException("BlogSessionController.postsByArchive()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	public PostDetail getPost (String postId)
	{
		try
		{
			// rechercher la publication associ� au billet
			PublicationPK pk = new PublicationPK(postId, getComponentId());
			PostDetail post = getBlogBm().getPost(pk);
			
			// mettre � jours les dates de d�but et de fin en fonction de la date du post
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(post.getPublication().getCreationDate());
			setMonthFirstDay(calendar);
			setMonthLastDay(calendar);
			
			return post;
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.getPost()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_OBJECT", e);
		}
	}
	
	public synchronized String createPost(String title, String categoryId)
	{
		return createPost(title, categoryId, new Date());
	}
	
	public synchronized String createPost(String title, String categoryId, Date dateEvent)
	{
		try
		{
			//cr�ation du billet 
        	PublicationDetail pub = new PublicationDetail("X", title, "", null, null, null, null, "1", null, null, "", null, "");
    	    pub.getPK().setComponentName(getComponentId());
    	    pub.setCreatorId(getUserId());
    	    pub.setCreatorName(getUserDetail(getUserId()).getDisplayedName());
    	    pub.setCreationDate(new Date());
    	    SilverTrace.info("blog", "BlogSessionContreller.createPost()", "root.MSG_GEN_PARAM_VALUE", "CreatorName=" + pub.getCreatorName());
     	    PostDetail newPost = new PostDetail(pub, categoryId, dateEvent);
    	    
			// cr�ation du billet
			return getBlogBm().createPost(newPost);
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.createPost()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_CREATE_OBJECT", e);
		}
	}
	public synchronized void updatePost(String postId, String title, String categoryId)
	{
		updatePost(postId, title, categoryId, new Date());
	}
	public synchronized void updatePost(String postId, String title, String categoryId, Date dateEvent)
	{
		try
		{
			PostDetail post = getPost(postId);
			PublicationDetail pub = post.getPublication();
			pub.setName(title);
			pub.setUpdaterId(getUserId());
			
			post.setCategoryId(categoryId);
			post.setDateEvent(dateEvent);
			
			// modification du billet
			getBlogBm().updatePost(post);
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.updatePost()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_UPDATE_OBJECT", e);
		}
	}
	
	public String initAlertUser(String postId) throws RemoteException 
	{
        AlertUser sel = getAlertUser();
		// Initialisation de AlertUser
        sel.resetAll();
		sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
		sel.setHostComponentId(getComponentId()); // set id du composant pour appel selectionPeas (extra param permettant de filtrer les users ayant acces au composant)
		PairObject hostComponentName = new PairObject(getComponentLabel(),  null); // set nom du composant pour browsebar (PairObject(nom_composant, lien_vers_composant)) NB : seul le 1er element est actuellement utilis� (alertUserPeas est toujours pr�sent� en popup => pas de lien sur nom du composant)
		sel.setHostComponentName(hostComponentName);
		SilverTrace.debug("blog","BlogSessionController.initAlertUser()","root.MSG_GEN_PARAM_VALUE","name = "+hostComponentName+" componentId="+ getComponentId());
		sel.setNotificationMetaData(getAlertNotificationMetaData(postId)); // set NotificationMetaData contenant les informations � notifier
		// fin initialisation de AlertUser
		// l'url de nav vers alertUserPeas et demand�e � AlertUser et retourn�e
        return AlertUser.getAlertUserURL(); 
    }
	
	private synchronized NotificationMetaData getAlertNotificationMetaData(String postId) throws RemoteException  
	{
		String senderName = getUserDetail().getDisplayedName();
		PostDetail post = getPost(postId);
      	
      	ResourceLocator	message		= new ResourceLocator("com.silverpeas.blog.multilang.blogBundle", "fr");
		ResourceLocator	message_en	= new ResourceLocator("com.silverpeas.blog.multilang.blogBundle", "en");
      	
      	String subject = message.getString("blog.notifSubject");
      	String body = getNotificationBody(post, message, senderName);

      	//english notifications
      	String subject_en = message_en.getString("blog.notifSubject");
      	String body_en = getNotificationBody(post, message_en, senderName);

		NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL, subject, body);
		notifMetaData.addLanguage("en", subject_en, body_en);
		
		// TODO : post.getLink() � faire
		notifMetaData.setLink(URLManager.getURL(null, getComponentId())+post.getURL());
		notifMetaData.setComponentId(getComponentId());
		notifMetaData.setSender(getUserId());
		
		return notifMetaData;
    }
	
	private String getNotificationBody(PostDetail post, ResourceLocator message, String senderName)
    {
    	StringBuffer messageText = new StringBuffer();
	    messageText.append(senderName).append(" ");
	    messageText.append(message.getString("blog.notifInfo")).append("\n\n");
	    messageText.append(message.getString("blog.notifName")).append(" : ").append(post.getPublication().getName()).append("\n");
	    if (StringUtil.isDefined(post.getPublication().getDescription()))
	    	messageText.append(message.getString("blog.notifDesc")).append(" : ").append(post.getPublication().getDescription()).append("\n");
	    return messageText.toString();
    }
	
	public synchronized void deletePost(String postId)
	{
		try
		{
			getBlogBm().deletePost(postId, getComponentId());
			// supprimer les commentaires 
			Collection<Comment> comments = getAllComments(postId);
			Iterator<Comment> it = (Iterator<Comment>) comments.iterator();
	    	 while (it.hasNext()) 
	    	 {
	    		 Comment comment = (Comment) it.next();
	    		 CommentPK commentPK = comment.getCommentPK();
	    		 getCommentBm().deleteComment(commentPK);	
	    	 }
			
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.deletePost()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_DELETE_OBJECT", e);
		}
	}
	
	public Collection<Comment> getAllComments(String postId)
	{
		try
		{
			CommentPK foreign_pk = new CommentPK(postId, null, getComponentId());
			
			Vector<Comment> vComments = null;
			Comment comment;
			vComments = getCommentBm().getAllComments(foreign_pk);
			Vector vReturn = new Vector(vComments.size());
			for (Enumeration e = vComments.elements(); e.hasMoreElements();)
			{
				comment = (Comment) e.nextElement();
				comment.setOwner(getUserDetail(Integer.toString(comment.getOwnerId())).getDisplayedName());
				vReturn.addElement(comment);
			}
			return vReturn;
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.getAllComments()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	public Collection<NodeDetail> getAllCategories()
	{
		try
		{
			return getBlogBm().getAllCategories(getComponentId());
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.getAllCategories()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
				
	public Category getCategory (String categoryId)
	{
		try
		{
			// rechercher la cat�gorie
			NodePK nodePK = new NodePK(categoryId, getComponentId());
			return getBlogBm().getCategory(nodePK);
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.getCategory()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_OBJECT", e);
		}
	}
	
	public synchronized void createCategory(Category category)
	{
		try
		{
			category.setCreationDate(DateUtil.date2SQLDate(new Date()));
			category.setCreatorId(getUserId());
			category.getNodePK().setComponentName(getComponentId());

			getBlogBm().createCategory(category);
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.createCategory()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_CREATE_OBJECT", e);
		}
	}
	
	public synchronized void deleteCategory(String categoryId)
	{
		try
		{
			getBlogBm().deleteCategory(categoryId, getComponentId());
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.deleteCategory()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_DELETE_OBJECT", e);
		}
	}
	
	public synchronized void updateCategory(Category category)
	{
		try
		{
			getBlogBm().updateCategory(category);
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.updateCategory()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_UPDATE_OBJECT", e);
		}
	}
	
	public Collection<Archive> getAllArchives()
	{
		try
		{
			return getBlogBm().getAllArchives(getComponentId());
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.getAllArchives()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	public Collection getAllLinks()
	{
		try
		{
			return getMyLinksBm().getAllLinksByInstance(getComponentId());
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.getAllLinks()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	public synchronized void addComment(String postId, String message)
	{
		try
		{
			CommentPK foreign_pk = new CommentPK(postId);
	        CommentPK pk = new CommentPK("X");
	        pk.setComponentName(getComponentId());
	        Date dateToday = new Date();
	        String date = DateUtil.date2SQLDate(dateToday);
	        String owner = getUserDetail(getUserId()).getDisplayedName();
	   	    SilverTrace.info("blog", "BlogSessionContreller.createPost()", "root.MSG_GEN_PARAM_VALUE", "owner=" + owner);

	        Comment comment = new Comment(pk, foreign_pk, Integer.parseInt(getUserId()), owner, message, date, date);
			getCommentBm().createComment(comment);
			SilverTrace.info("blog", "BlogSessionContreller.createPost()", "root.MSG_GEN_PARAM_VALUE", "owner comment=" + comment.getOwner());
			
		 	// envoie notification si abonnement
			PostDetail post = getPost(postId);
			PublicationDetail pub = post.getPublication();
			NodePK father = new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().getInstanceId());
			getBlogBm().sendSubscriptionsNotification(father, pub, "commentCreate");

		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.addComment()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_ADD_OBJECT", e);
		}
	}
	
	public void sendSubscriptionsNotification(String postId, String type)
	{
		// envoie notification si abonnement
		try {
			PostDetail post = getPost(postId);
			PublicationDetail pub = post.getPublication();
			NodePK father = new NodePK("0", pub.getPK().getSpaceId(), pub.getPK().getInstanceId());
		
			getBlogBm().sendSubscriptionsNotification(father, pub, type);
		} catch (RemoteException e) {
			throw new BlogRuntimeException("BlogSessionController.sendSubscriptionsNotification()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_ADD_OBJECT", e);
		}
	}
	
	public void deleteComment(String commentId)
	{
		try {
			CommentPK pk = new CommentPK(commentId, "useless", getComponentId());
			getCommentBm().deleteComment(pk);
		} catch (RemoteException e) {
			throw new BlogRuntimeException("BlogSessionController.deleteComment()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_REMOVE_OBJECT", e);
		}
	}
	
	public Collection<PostDetail> getResultSearch(String word) 
	{
		try
		{
		 	SilverTrace.info("blog", "BlogSessionController.getResultSearch()", "root.MSG_GEN_PARAM_VALUE", "word =" + word);
			return getBlogBm().getResultSearch(word, getUserId(), getSpaceId(), getComponentId());
		}
		catch (RemoteException e)
		{
			throw new BlogRuntimeException("BlogSessionController.getResultSearch()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
	}
	
	public synchronized void addSubscription(String topicId) throws RemoteException {
		getBlogBm().addSubscription(new NodePK(topicId, getSpaceId(), getComponentId()), getUserId());
    }
	
	private boolean isUseRss()
	{
		return "yes".equalsIgnoreCase(getComponentParameterValue("rss"));
	}
	
	public String getRSSUrl()
	{
		if (isUseRss())
			return super.getRSSUrl();
		return null;
	}
	
	public Boolean isPdcUsed()
	{
		return new Boolean("yes".equalsIgnoreCase(getComponentParameterValue("usePdc")));
	}
	
	public int getSilverObjectId(String objectId) 
	{
		
		int silverObjectId = -1;
		try
		{
			silverObjectId = getBlogBm().getSilverObjectId(new PublicationPK(objectId, getSpaceId(), getComponentId()));
		} 
		catch (Exception e)
		{
			SilverTrace.error("blog", "BlogSessionController.getSilverObjectId()", "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
		} 
		return silverObjectId;
	}
		
	public CommentBm getCommentBm()
    {
		CommentBm commentBm = null;
        {
            try
            {
              CommentBmHome commentHome = (CommentBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.COMMENT_EJBHOME, CommentBmHome.class);
              commentBm =  commentHome.create();
            }
            catch (Exception e)
            {
                throw new CommentRuntimeException("BlogSessionController.getCommentBm()", SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
            }
        }

        return commentBm;
    }
	
	public MyLinksBm getMyLinksBm()
    {
		MyLinksBm myLinksBm = null;
        {
            try
            {
            	MyLinksBmHome myLinksHome = (MyLinksBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.MYLINKSBM_EJBHOME, MyLinksBmHome.class);
            	myLinksBm =  myLinksHome.create();
            }
            catch (Exception e)
            {
                throw new CommentRuntimeException("BlogSessionController.getMyLinksBm()", SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
            }
        }

        return myLinksBm;
    }
	
	private BlogBm getBlogBm()
	{
		BlogBm blogBm = null;
		try 
		{
			BlogBmHome blogBmHome = (BlogBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.BLOGBM_EJBHOME, BlogBmHome.class);
			blogBm = blogBmHome.create();
		} 
		catch (Exception e) 
		{
			throw new BlogRuntimeException("BlogSessionController.getBlogBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
		}
		return blogBm;
	}

	public void setCurrentBeginDate(String beginDate) {
		try {
			this.currentBeginDate.setTime(DateUtil.parse(beginDate));
		} catch (ParseException e) {
			throw new BlogRuntimeException("BlogSessionController.setCurrentBeginDate()",SilverpeasRuntimeException.ERROR, "blog.DATE_FORMAT_ERROR", e);
		}
	}

	public void setCurrentEndDate(String endDate) {
		try {
			this.currentEndDate.setTime(DateUtil.parse(endDate));
		} catch (ParseException e) {
			throw new BlogRuntimeException("BlogSessionController.setCurrentEndDate()",SilverpeasRuntimeException.ERROR, "blog.DATE_FORMAT_ERROR", e);
		}
	}
	
	public String getCurrentBeginDateAsString()
	{
		return DateUtil.date2SQLDate(currentBeginDate.getTime());
	}
	
	public String getCurrentEndDateAsString()
	{
		return DateUtil.date2SQLDate(currentEndDate.getTime());
	}
	
	public Date getDateEvent(String pubId) throws RemoteException
	{
		return getBlogBm().getDateEvent(pubId);
	}
	
	public void nextMonth()
	{
		currentBeginDate.add(Calendar.MONTH, 1);
		currentBeginDate.set(Calendar.DATE, 1);
		
		currentEndDate.add(Calendar.MONTH, 1);
		currentEndDate.set(Calendar.DAY_OF_MONTH, currentEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
	}
	
	public void previousMonth()
	{
		currentBeginDate.add(Calendar.MONTH, -1);
		currentBeginDate.set(Calendar.DATE, 1);
		
		currentEndDate.add(Calendar.MONTH, -1);
		currentEndDate.set(Calendar.DAY_OF_MONTH, currentEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
	}

	public String getServerURL() {
		return serverURL;
	}

}