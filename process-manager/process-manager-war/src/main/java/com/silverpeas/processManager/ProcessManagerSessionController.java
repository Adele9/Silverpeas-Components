package com.silverpeas.processManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.form.HtmlForm;
import com.silverpeas.form.form.XmlForm;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;
import com.silverpeas.processManager.record.QuestionRecord;
import com.silverpeas.processManager.record.QuestionTemplate;
import com.silverpeas.util.StringUtil;
import com.silverpeas.workflow.api.UpdatableProcessInstanceManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowEngine;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.error.WorkflowError;
import com.silverpeas.workflow.api.event.QuestionEvent;
import com.silverpeas.workflow.api.event.ResponseEvent;
import com.silverpeas.workflow.api.event.TaskDoneEvent;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.Question;
import com.silverpeas.workflow.api.instance.UpdatableProcessInstance;
import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.Participant;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.QualifiedUsers;
import com.silverpeas.workflow.api.model.RelatedUser;
import com.silverpeas.workflow.api.model.Role;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.model.UserInRole;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.user.UserInfo;
import com.silverpeas.workflow.api.user.UserSettings;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;


/**
 * The ProcessManager Session controller
 */
public class ProcessManagerSessionController extends AbstractComponentSessionController
{ 
	private ResourceLocator resources 	= new ResourceLocator("com.silverpeas.processManager.settings.processManagerSettings", "");
	
	/**
	 * Builds and init a new session controller
	 */
	public ProcessManagerSessionController(MainSessionController mainSessionCtrl, ComponentContext context) throws ProcessManagerException
	{ 
		super(mainSessionCtrl, context, "com.silverpeas.processManager.multilang.processManagerBundle", "com.silverpeas.processManager.settings.processManagerIcons");

		// the peasId is the current component id.
		peasId = context.getCurrentComponentId();
		processModel = getProcessModel(peasId);
		
		SilverTrace.info("processManager","ProcessManagerSessionController.constructor()", "root.MSG_GEN_PARAM_VALUE", "apr�s getProcessModel()");

		// the current user is given by the main session controller.
		currentUser = getUser(mainSessionCtrl.getUserId());

		// the user roles are given by the context.
		userRoles = context.getCurrentProfile();
		if (userRoles == null || userRoles.length == 0)
		{
		 throw new ProcessManagerException("ProcessManagerSessionController",
										   "processManager.UNAUTHORIZED_USER");
		}
		currentRole = userRoles[0];

		// Reset the user rights for creation
		resetCreationRights();
		
		SilverTrace.info("processManager","ProcessManagerSessionController.constructor()", "root.MSG_GEN_PARAM_VALUE", "apr�s resetCreationRights()");

		// Load user informations
		try
		{
			userSettings = Workflow.getUserManager().getUserSettings(mainSessionCtrl.getUserId(), peasId);
		}
		catch (WorkflowException we)
		{
			SilverTrace.warn("processManager", "SessionController", "processManager.EX_ERR_GET_USERSETTINGS", we);
		}
		
		SilverTrace.info("processManager","ProcessManagerSessionController.constructor()", "root.MSG_GEN_EXIT_METHOD");
	}

   /**
    * Builds a ill session controller.
    * Initialization is skipped and this session controller
    * can only display the fatal exception.
    *
    * Used by the request router when a full session controller
    * can't be built.
    */
   public ProcessManagerSessionController(MainSessionController mainSessionCtrl,
                                          ComponentContext context,
                                          ProcessManagerException fatal) 
   { 
      super(mainSessionCtrl,
            context,
            "com.silverpeas.processManager.multilang.processManagerBundle",
            "com.silverpeas.processManager.settings.processManagerIcons");

      fatalException = fatal;
   }

	/**
	 * Get the creation rights
	 * @return	true if user can do the "Creation" action
	 */
	public boolean getCreationRights()
	{
		return creationRights;
	}

	/**
	 * Compute the creation rights
	 * set creationRight to true if user can do the "Creation" action
	 */
	public void resetCreationRights()
	{
		creationRights = false;

		try
		{
			String[] roles = processModel.getCreationRoles();
			
			for (int i=0; i<roles.length; i++)
			{
				if (roles[i].equals(currentRole))
					creationRights = true;
			}
		}
		catch (Exception e)
		{
			creationRights = false;
		}
	}
	
	/**
	 * L'historique peut �tre filtr�. Dans ce cas, les formulaires associ�s � chaque �tat sont visibles uniquement si l'utilisateur
	 * courant �tait un working user ou un interested user.
	 */
	public boolean filterHistory()
	{
		String parameterValue = this.getComponentParameterValue("filterHistory");
		return (parameterValue != null && parameterValue.length() > 0 && "yes".equalsIgnoreCase(parameterValue));
	}
	
	public boolean isHistoryTabVisible()
	{
		String parameterValue = this.getComponentParameterValue("historyTabEnable");
		if (StringUtil.isDefined(parameterValue))
			return "yes".equalsIgnoreCase(parameterValue);
		else
			return true;
	}

   /**
    * Returns the last fatal exception
    */
   public ProcessManagerException getFatalException()
   {
      return fatalException;
   }

   /**
    * Updates the current process instance from the given id
    * and returns the associated ProcessInstance object.
    *
    * If the instanceId parameter is null,
    * updates nothing and returns the current ProcessInstance.
    *
    * Throws ProcessManagerException when the instanceId is unkwown
    * and when the current user is not allowed to access the instance.
    *
    * Doesn't change the current process instance when an error occures.
    */
   public ProcessInstance resetCurrentProcessInstance(String instanceId)
      throws ProcessManagerException
   {
	   SilverTrace.info("processManager","ProcessManagerSessionController.resetCurrentProcessInstance()", "root.MSG_GEN_ENTER_METHOD", "instanceId = "+instanceId);
      if (instanceId != null)
      {
         ProcessInstance instance;

         try
         {
            instance = Workflow.getProcessInstanceManager()
               .getProcessInstance(instanceId);
         }
         catch (WorkflowException e)
         {
            throw new ProcessManagerException("ProcessManagerSessionControler",
                "processManager.UNKNOWN_PROCESS_INSTANCE", instanceId, e);
         }

         if (isAllowed(instance))
         {
            currentProcessInstance = instance;
         }
         else
         {
            throw new ProcessManagerException("ProcessManagerSessionController",
               "processManager.NO_ACCESS_TO_PROCESS_INSTANCE");
         }
      }

      if (currentProcessInstance == null)
      {
         throw new ProcessManagerException("ProcessManagerSessionController",
            "processManager.NO_CURRENT_PROCESS_INSTANCE");
      }

      return currentProcessInstance;
   }

	/**
	 * Get the current process instance
	 */
	public ProcessInstance getCurrentProcessInstance()
	{
		return currentProcessInstance;
	}

   /**
    * Returns the current instance list rows template.
    */
	public RecordTemplate getProcessListHeaders()
	{
	   if (currentListHeaders == null) resetCurrentProcessListHeaders();

		return currentListHeaders;
	}

   /**
    * Reset the current instance list rows template.
    */
	public void resetCurrentProcessListHeaders()
	{
		currentListHeaders = processModel.getRowTemplate(currentRole, getLanguage());
	}

	/**
	 * Returns the current process instance list.
	 */
	public DataRecord[] getCurrentProcessList()
      throws ProcessManagerException
	{
		if (currentProcessList == null)	return resetCurrentProcessList();
		else							return currentProcessList;
	}

   /**
    * Updates the current process instance list with current filter
    * and returns this list.
    *
    * Doesn't change the current process instance when an error occures.
    */
   public DataRecord[] resetCurrentProcessList()
      throws ProcessManagerException
   {
		try
		{
         ProcessInstance[] processList
			   = Workflow.getProcessInstanceManager()
				   .getProcessInstances(peasId, currentUser, currentRole);

		   currentProcessList=getCurrentFilter().filter(processList, currentRole, getLanguage());
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("ProcessManagerSessionController",
				 "processManager.ERR_GET_PROCESS_LIST", peasId, e);
		}

      return currentProcessList;
   }

	/**
	 * Get the role name of task referred by the todo with the given todo id
	 */
	public String getRoleNameFromExternalTodoId(String externalTodoId)
      throws ProcessManagerException
	{
		try
		{
			return Workflow.getTaskManager().getRoleNameFromExternalTodoId(externalTodoId);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("ProcessManagerSessionController",
				 "processManager.ERR_GET_ROLENAME_FROM_TODO", "externalTodoId : " + externalTodoId, e);
		}
	}

	/**
	 * Get the process instance Id referred by the todo with the given todo id
	 */
	public String getProcessInstanceIdFromExternalTodoId(String externalTodoId)
      throws ProcessManagerException
	{
		try
		{
			return Workflow.getTaskManager().getProcessInstanceIdFromExternalTodoId(externalTodoId);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("ProcessManagerSessionController",
				 "processManager.ERR_GET_PROCESS_FROM_TODO", "externalTodoId : " + externalTodoId, e);
		}
	}

	/**
	 * Get the active states
	 */
	public String[] getActiveStates()
	{
		String[] states = currentProcessInstance.getActiveStates();
		if (states == null)
			return new String[0];

		String[] stateLabels = new String[states.length];
		for (int i=0; i<states.length; i++)
		{
			stateLabels[i] = getState(states[i]).getLabel(currentRole, getLanguage());
		}

		return stateLabels;
	}
	
	public String[] getActiveRoles()
	{
		String[] states = currentProcessInstance.getActiveStates();
		if (states == null)
			return new String[0];

		String[] roles = new String[states.length];
		for (int i=0; i<states.length; i++)
		{
			try 
			{
				State state = getState(states[i]);
				
				QualifiedUsers workingUsers = state.getWorkingUsers();
				
				RelatedUser[] relatedUsers = workingUsers.getRelatedUsers();
				RelatedUser relatedUser = null;
				String role = "";
				for (int r=0; r<relatedUsers.length; r++)
				{
					relatedUser = relatedUsers[r];
					if (r!=0)
						role += ", ";
					
					//Process participants
					Participant participant = relatedUser.getParticipant();
					String relation = relatedUser.getRelation();
					if (participant != null && relation == null)
						role += participant.getLabel(currentRole, getLanguage());
					else if (participant != null && relation != null)
					{
						UserInfo userInfo = userSettings.getUserInfo(relation);
						if (userInfo != null)
							role += getUserDetail(userInfo.getValue()).getDisplayedName();
						//role += relatedUser.getFolderItem();//.getLabel(currentRole, getLanguage());
					}
					
					//Process folder item
					Item item = relatedUser.getFolderItem();
					if (item != null)
					{
						String userId = currentProcessInstance.getField(item.getName()).getStringValue();
						if (userId != null)
						{
							UserDetail user = getUserDetail(userId);
							if (user != null)
								role += user.getDisplayedName(); 
						}
					}
				}
				
				UserInRole[] userInRoles = workingUsers.getUserInRoles();
				UserInRole userInRole = null;
				for (int u=0; u<userInRoles.length; u++)
				{
					userInRole = userInRoles[u];
					if(u!=0 || role.length()>0)
						role += ", ";
					role += processModel.getRole(userInRole.getRoleName()).getLabel(currentRole, getLanguage());
				}
				
				roles[i] = role;
			}
			/*catch (FormException fe)
			{
				continue;
			}*/
			catch (WorkflowException ignored)
			{
			   // ignore unknown state
			   continue;
			}
		}

		return roles;
	}

	/**
	 * Get the active user names
	 */
	public boolean isActiveUser()
	{
		if (currentProcessInstance == null)
			return false;
		
		String[] states = currentProcessInstance.getActiveStates();
		if (states == null)
			return false;

		Actor[] users;
		for (int i=0; i<states.length; i++)
		{
		   try
			{
		      users = currentProcessInstance.getWorkingUsers(states[i]);
			  for (int j=0; j<users.length ; j++)
			  {
				  if (getUserId().equals(users[j].getUser().getUserId()))
					  return true;
			  }
			}
			catch (WorkflowException ignored)
			{
			   // ignore unknown state
			   continue;
			}
		}

		return false;
	}
	
	private List getActiveUsers(String stateName)
	{
		List activeUsers = new ArrayList();
		
		State state = getState(stateName);
		
		activeUsers.addAll(getUsers(state.getWorkingUsers()));
		activeUsers.addAll(getUsers(state.getInterestedUsers()));
				
		return activeUsers;
	}
	
	private List getUsers(QualifiedUsers qualifiedUsers)
	{
		List users = new ArrayList();
		RelatedUser[] relatedUsers = qualifiedUsers.getRelatedUsers();
		RelatedUser relatedUser = null;
		List roles = new ArrayList();
		for (int r=0; r<relatedUsers.length; r++)
		{
			relatedUser = relatedUsers[r];
			
			//Process participants
			Participant participant = relatedUser.getParticipant();
			String relation = relatedUser.getRelation();
			/*if (participant != null && relation == null)
				role += participant.getLabel(currentRole, getLanguage());
			else*/ if (participant != null && relation != null)
			{
				UserInfo userInfo = userSettings.getUserInfo(relation);
				if (userInfo != null)
					users.add(userInfo.getValue());
			}
			
			//Process folder item
			Item item = relatedUser.getFolderItem();
			if (item != null)
			{
				try
				{
					String userId = currentProcessInstance.getField(item.getName()).getStringValue();
					users.add(userId);
				}
				catch (WorkflowException we)
				{
					//ignore it.
				}
			}
		}
		
		UserInRole[] userInRoles = qualifiedUsers.getUserInRoles();
		UserInRole userInRole = null;
		for (int u=0; u<userInRoles.length; u++)
		{
			userInRole = userInRoles[u];
			roles.add(userInRole.getRoleName());
		}
		String[] userIds = getOrganizationController().getUsersIdsByRoleNames(getComponentId(), roles);
		for (int u=0; u<userIds.length; u++)
		{
			users.add(userIds[u]);
		}
		
		return users;
	}

   /**
    * Returns the workflow user having the given id.
    */
   public User getUser(String userId)
      throws ProcessManagerException
   {
      try
      {
         return Workflow.getUserManager().getUser(userId);
      }
      catch (WorkflowException e)
      {
         throw new ProcessManagerException("ProcessManagerSessionControler",
             "processManager.UNKNOWN_USER", userId, e);
      }
   }

   /**
    * Returns the process model having the given id.
    */
   public ProcessModel getProcessModel(String modelId)
      throws ProcessManagerException
   {
      try
      {
         return Workflow.getProcessModelManager().getProcessModel(modelId);
      }
      catch (WorkflowException e)
      {
         throw new ProcessManagerException("ProcessManagerSessionControler",
            "processManager.UNKNOWN_PROCESS_MODEL", modelId, e);
      }
   }

   /**
    * Returns the current role name.
    */
   public String getCurrentRole()
   {
      return currentRole ;
   }

   /**
    * Returns the current role name.
    */
   public void resetCurrentRole(String role)
	   throws ProcessManagerException
   {
      if (role!=null && role.length()>0)
		  this.currentRole = role;
		
	  resetCreationRights();
	  resetProcessFilter();
	  resetCurrentProcessList();
	  resetCurrentProcessListHeaders();
   }

   /**
    * Returns the user roles as a list of (name, label) pair.
    */
   public NamedValue[] getUserRoleLabels()
   {
      if (userRoleLabels == null)
      {
         String lang  = getLanguage();
         Role[] roles = processModel.getRoles();

         List       labels = new ArrayList();
         NamedValue label;

         // quadratic search ! but it's ok : the list are about 3 or 4 length.
         for (int i=0; i<userRoles.length ; i++)
         {
			if (userRoles[i].equals("supervisor"))
			{
                  label = new NamedValue("supervisor", getString("processManager.supervisor"));
                  labels.add(label);
			}
            
			else for (int j=0; j<roles.length ; j++)
            {
               if (userRoles[i].equals(roles[j].getName()))
               {
                  label = new NamedValue(userRoles[i],
                                         roles[j].getLabel(currentRole, lang));
                  labels.add(label);
               }
            }
         }

		 Collections.sort(labels, NamedValue.ascendingValues);
         userRoleLabels = (NamedValue[]) labels.toArray(new NamedValue[0]);
      }

      return userRoleLabels;
   }

   /**
    * Returns the form presenting the folder of the current process instance.
    */
   public Form getPresentationForm()
	   throws ProcessManagerException
   {
	   try
		{
		   Form form = getPresentationForm("presentationForm");
			if (form != null) return form;

			XmlForm xmlForm = new XmlForm(processModel.getDataFolder().toRecordTemplate(
									     currentRole, getLanguage(), true));
			xmlForm.setTitle(getString("processManager.folder"));
			return xmlForm;
		}
		catch (FormException e)
		{
		   throw new ProcessManagerException("SessionController",
				"processManager.PRESENTATION_FORM_UNAVAILABLE", e);
		}
		catch (WorkflowException e)
		{
		   throw new ProcessManagerException("SessionController",
				"processManager.PRESENTATION_FORM_UNAVAILABLE", e);
		}
   }
   
   public Item[] getFolderItems() throws ProcessManagerException
   {
	   return processModel.getDataFolder().getItems();
   }

   /**
    * Returns the folder data of the current process instance.
    */
   public DataRecord getFolderRecord()
	   throws ProcessManagerException
   {
		DataRecord data = null;
		try
		{
			if (currentProcessInstance != null)
			{
		      data = currentProcessInstance.getFormRecord(
				    "presentationForm", currentRole, getLanguage());

				if (data == null)
				{
				   data = currentProcessInstance.getFolder();
				}
			}
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.PRESENTATION_DATA_UNAVAILABLE", e);
		}

		if (data == null)
		{
			throw new ProcessManagerException("SessionController", "processManager.PRESENTATION_DATA_UNAVAILABLE");
		}

		return data;
   }

	/**
	* Returns the creation task.
	*/
	public Task getCreationTask()
		throws ProcessManagerException
	{
		try
		{
			Task creationTask = Workflow.getTaskManager().getCreationTask(currentUser, currentRole, processModel);

			return creationTask;
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.CREATION_TASK_UNAVAILABLE", e);
		}
	}

   /**
    * Returns the form which starts a new instance.
    */
   public Form getCreationForm()
	   throws ProcessManagerException
   {
	   try
		{
			Action creation = processModel.getCreateAction();
			return processModel.getPublicationForm(creation.getName(), currentRole, getLanguage());
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController",
			    "processManager.ERR_NO_CREATION_FORM", e);
		}
   }

   /**
    * Returns the an empty creation record
    * which will be filled with the creation form.
    */
   public DataRecord getEmptyCreationRecord()
	   throws ProcessManagerException
   {
	   try
		{
			Action creation = processModel.getCreateAction();
			return processModel.getNewActionRecord(creation.getName(),
		                                          currentRole, getLanguage(), null);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController",
			    "processManager.UNKNOWN_ACTION", e);
		}
   }

   /**
    * Returns the form to ask a question
    */
   public Form getQuestionForm(boolean readonly)
	   throws ProcessManagerException
   {
	   try
	   {
		   return new XmlForm( (RecordTemplate) new QuestionTemplate( getLanguage(), readonly ) );
	   }
	   catch (FormException fe)
	   {
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_QUESTION_FORM", fe);
	   }
   }

   /**
    * Returns the an empty question record
    * which will be filled with the question form.
    */
   public DataRecord getEmptyQuestionRecord()
	   throws ProcessManagerException
   {
	   return new QuestionRecord("");
   }


	/**
	 * get the question with the given id
	 * @param	questionId		question id
	 * @return the found question
	 */
	public Question getQuestion(String questionId)
	{
		Question[] questions = currentProcessInstance.getQuestions();
		Question foundQuestion = null;

		for (int i=0; foundQuestion==null && i<questions.length; i++)
		{
			if (questions[i].getId().equals(questionId))
				foundQuestion = questions[i];
		}

		return foundQuestion;
	}
	
	/**
	* Returns the an empty question record
	* which will be filled with the question form.
	*/
	public DataRecord getQuestionRecord(String questionId)
	   throws ProcessManagerException
	{
		Question question = getQuestion(questionId);
		return new QuestionRecord(question.getQuestionText());
	}

	/**
	 * Get assign template (for the re-affectations)
	 */
	public GenericRecordTemplate getAssignTemplate()
	   throws ProcessManagerException
	{
		try
		{
			String[] activeStates = currentProcessInstance.getActiveStates();
			GenericRecordTemplate rt = new GenericRecordTemplate();

			for (int i=0; i<activeStates.length; i++)
			{
				State state = getState(activeStates[i]);
				Actor[] actors = currentProcessInstance.getWorkingUsers(activeStates[i]);

				for (int j=0; actors!=null && j<actors.length; j++)
				{
					GenericFieldTemplate fieldTemplate = new GenericFieldTemplate( state.getName() + "_" + actors[j].getUserRoleName() + "_" + j, "user" );
					fieldTemplate.addLabel(state.getLabel( currentRole, getLanguage() ), getLanguage());
					fieldTemplate.setDisplayerName("user");
					fieldTemplate.setMandatory(true);

					rt.addFieldTemplate(fieldTemplate);
				}
			}

			return rt;
		}
		catch (FormException ex)
		{
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_ASSIGN_TEMPLATE", ex);
		}
		catch (WorkflowException ex)
		{
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_ASSIGN_TEMPLATE", ex);
		}
	}
	
	/**
	 * Get assign form (for the re-affectations)
	 */
	public Form getAssignForm()
	   throws ProcessManagerException
	{
		try
		{
			return new XmlForm(getAssignTemplate());
		}
		catch (FormException ex)
		{
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_ASSIGN_FORM", ex);
		}
	}

	/**
	 * Get assign data (for the re-affectations)
	 */
	public DataRecord getAssignRecord()
	{
		String[] activeStates = currentProcessInstance.getActiveStates();
		Actor[] actors = null;

		try
		{
			DataRecord data = getAssignTemplate().getEmptyRecord();

			for (int i=0; activeStates!=null && i<activeStates.length; i++)
			{
				actors = currentProcessInstance.getWorkingUsers(activeStates[i]);
				
				for (int j=0; actors!=null && j<actors.length; j++)
				{
					Field field = data.getField(activeStates[i] + "_" + actors[j].getUserRoleName() + "_" + j);
					String value = actors[j].getUser().getUserId();

					if (value!=null)
					{
						field.setStringValue(value);
					}
				}
			}

			return data;
		}
		catch (WorkflowException e)
		{
			SilverTrace.warn("processManager", "SessionController.getAssignRecord", "processManager.EX_ERR_GET_DATARECORD", e);
			return null;
		}
		catch (ProcessManagerException e)
		{
			SilverTrace.warn("processManager", "SessionController.getAssignRecord", "processManager.EX_ERR_GET_DATARECORD", e);
			return null;
		}
		catch (FormException e)
		{
			SilverTrace.warn("processManager", "SessionController.getAssignRecord", "processManager.EX_ERR_GET_FIELD");
			return null;
		}
	}

	/**
	 * Get assign data (for the re-affectations)
	 */
	public void reAssign(DataRecord data)
	   throws ProcessManagerException
	{
		Actor[] oldUsers = null;
		Vector oldActors = new Vector();
		Vector newActors = new Vector();

		try
		{
			WorkflowEngine wfEngine = Workflow.getWorkflowEngine();
			String[] activeStates = currentProcessInstance.getActiveStates();
			
			for (int i=0; activeStates!=null && i<activeStates.length; i++)
			{
				// unassign old working users
				oldUsers = currentProcessInstance.getWorkingUsers(activeStates[i]);

				for (int j=0; j<oldUsers.length; j++)
				{
					oldActors.add(oldUsers[j]);
				}

				// assign new working users
				for (int j=0; oldUsers!=null && j<oldUsers.length; j++)
				{
					Field field = data.getField(activeStates[i] + "_" + oldUsers[j].getUserRoleName() + "_" + j);
					String userId = field.getStringValue();
					User user = Workflow.getUserManager().getUser(userId);
					Actor newActor = Workflow.getProcessInstanceManager().createActor(user,
																					oldUsers[j].getUserRoleName(),
																					oldUsers[j].getState() );
					newActors.add(newActor);
				}
			}
			
			wfEngine.reAssignActors((UpdatableProcessInstance) currentProcessInstance, (Actor[]) oldActors.toArray(new Actor[0]), (Actor[]) newActors.toArray(new Actor[0]), currentUser );
		}
		catch (WorkflowException we)
		{
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.EX_ERR_RE_ASSIGN", we);
		}
		catch (FormException fe)
		{
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.EX_ERR_RE_ASSIGN", fe);
		}
	}

	/**
	 * Create a new process instance with the filled form.
	 */
	public String createProcessInstance(DataRecord data)
		throws ProcessManagerException
	{
		try
		{
			Action creation = processModel.getCreateAction();
			TaskDoneEvent event = getCreationTask().buildTaskDoneEvent(creation.getName(), data);
			Workflow.getWorkflowEngine().process(event);
			return event.getProcessInstance().getInstanceId();
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.CREATION_PROCESSING_FAILED", e);
		}
	}

	/**
	 * Get all tasks assigned for current user on current process instance
	 */
	public Task[] getTasks()
		 throws ProcessManagerException
	{
		try
		{
			return Workflow.getTaskManager().getTasks(currentUser, currentRole, currentProcessInstance);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.ERR_GET_TASKS_FAILED", e);
		}
	}

	/**
	 * Search for an hypothetic action of kind "delete", allowed for the current user with the given role
	 * @return an array of 2 String { action.name, state.name }, null if no action found
	 */
	public String[] getDeleteAction()
	{
		Task[] tasks = null;
		State state = null;
		Action[] actions = null;

		try
		{
			tasks = getTasks();
			for (int i=0; tasks!=null && i<tasks.length; i++)
			{
				state = tasks[i].getState();
				actions = state.getAllowedActions();
	   			for (int j=0; actions!=null && j<actions.length ; j++)
				{
					if (actions[j].getKind().equals("delete"))
					{
						String[] result = new String[3];
						result[0] = actions[j].getName();
						result[1] = state.getName();
						result[2] = actions[j].getLabel(currentRole, getLanguage());
						return result;
					}
				}
			}

			return null;
		}
		catch (ProcessManagerException e)
		{
			SilverTrace.warn("processManager", "SessionController", "processManager.EX_CAN_BE_REMOVED_FAILED", e);
			return null;
		}
	}

	/**
	 * Returns the named task.
	 */
	public Task getTask(String stateName)
	   throws ProcessManagerException
	{
		Task[] tasks = getTasks();
		for (int i=0; i<tasks.length ; i++)
		{
			if (tasks[i].getState().getName().equals(stateName))
			{
				return tasks[i];
			}
		}
		return null;
	}

   /**
    * Returns the named form (Read only).
	 *
	 * Throws an exception if the form is unknown.
    */
   public Form getPresentationForm(String name)
	   throws ProcessManagerException
   {
	   try
		{
         return processModel.getPresentationForm(name,
		                                          currentRole, getLanguage());
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController",
			    "processManager.UNKNOWN_ACTION", e);
		}
   }

   /**
    * Returns the form associated to the named action.
	 *
	 * Returns null if this action has no form.
	 * Throws an exception if the action is unknown.
    */
   public Form getActionForm(String stateName, String actionName)
	   throws ProcessManagerException
   {
	   try
		{
         return processModel.getPublicationForm(actionName,
		                                          currentRole, getLanguage());
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController",
			    "processManager.UNKNOWN_ACTION", e);
		}
   }

   /**
    * Returns a new DataRecord filled whith the folder data
    * and which will be be completed by the action form.
    */
   public DataRecord getActionRecord(String stateName, String actionName)
	   throws ProcessManagerException
   {
	   try
		{
	      return currentProcessInstance.getNewActionRecord(actionName);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController",
			    "processManager.UNKNOWN_ACTION", e);
		}
   }

   /**
    * Create a new history step instance with the filled form.
    */
	public void processAction(String stateName, String actionName, DataRecord data)
		throws ProcessManagerException
	{
		try
		{
			Task task = getTask(stateName);
			TaskDoneEvent event = task.buildTaskDoneEvent(actionName, data);
			Workflow.getWorkflowEngine().process(event);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.CREATION_PROCESSING_FAILED", e);
		}
	}

   /**
	 * Returns the required step
	 */
	public HistoryStep getStep(String stepId)
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();

		for (int i=0; i<steps.length; i++)
		{
		   if (steps[i].getId().equals(stepId)) return steps[i];
		}

		return null;
	}
	
   /**
    * Send the question as a QuestionEvent to the workflowEngine.
    */
	public void processQuestion(String stepId, String state, DataRecord data)
		throws ProcessManagerException
	{
		try
		{
			Task task = getTask(state);
			QuestionEvent event = task.buildQuestionEvent(stepId, data);
			Workflow.getWorkflowEngine().process(event);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.CREATION_PROCESSING_FAILED", e);
		}
	}

   /**
    * Send the answer as a ResponseEvent to the workflowEngine.
    */
	public void processResponse(String questionId, DataRecord data)
		throws ProcessManagerException
	{
		try
		{
			Question question = getQuestion(questionId);
			Task task = getTask(question.getTargetState().getName());
			ResponseEvent event = task.buildResponseEvent(questionId, data);
			Workflow.getWorkflowEngine().process(event);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.CREATION_PROCESSING_FAILED", e);
		}
	}

	/**
	 * Lock the current instance for current user and given state
	 * @param	stateName	state name
	 */
	public void lock(String stateName) 
		throws ProcessManagerException
	{
		try
		{
			State state = processModel.getState(stateName);
			( (UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager() ).lock(currentProcessInstance, state, currentUser);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.ERR_LOCK_FAILED", e);
		}
	}

	/**
	 * Un-Lock the current instance for current user and given state
	 * @param	stateName	state name
	 */
	public void unlock(String stateName) 
		throws ProcessManagerException
	{
		try
		{
			State state = processModel.getState(stateName);
			( (UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager() ).unlock(currentProcessInstance, state, currentUser);
		}
		catch (WorkflowException e)
		{
			throw new ProcessManagerException("SessionController", "processManager.ERR_LOCK_FAILED", e);
		}
	}

	/**
	 * Get the list of activities resolved in History Step of current process instance
	 * @return an array of string containing activity names
	 */
	public String[] getStepActivities()
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();
		String[] activities = new String[steps.length];
		State resolvedState = null;

		for (int i=0; i<steps.length; i++)
		{
			if (steps[i].getResolvedState() != null)
			{
				resolvedState = processModel.getState(steps[i].getResolvedState());
				activities[i] = resolvedState.getLabel(currentRole, getLanguage());
			}
			else
				activities[i] = "";
		}

		return activities;
	}
	
	public String[] getStepVisibles()
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();
		String[] stepVisibles = new String[steps.length];

		String stateName = null;
		for (int i=0; i<steps.length; i++)
		{
			if (filterHistory())
			{
				stepVisibles[i] = "false";
				stateName = steps[i].getResolvedState();
				if (stateName != null)
				{
					if (getActiveUsers(stateName).contains(getUserId()))
						stepVisibles[i] = "true";
				}
				else
				{
					//action kind=create
					try {
						Action createAction = processModel.getCreateAction();
						QualifiedUsers qualifiedUsers = createAction.getAllowedUsers();
						if (getUsers(qualifiedUsers).contains(getUserId()))
							stepVisibles[i] = "true";
					} 
					catch (WorkflowException we)
					{
						//no action ok kind create
						stepVisibles[i] = "true";
					}
				}
			}
			else
			{
				stepVisibles[i] = "true";
			}
		}

		return stepVisibles;
	}

	/**
	 * Get the list of actors in History Step of current process instance
	 * @return an array of string containing actors full name
	 */
	public String[] getStepActors()
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();
		String[] actors = new String[steps.length];

		for (int i=0; i<steps.length; i++)
		{
			try
			{
				actors[i] = steps[i].getUser().getFullName();
			}
			catch (WorkflowException we)
			{
				actors[i] = "##";
			}
		}

		return actors;
	}

	/**
	 * Get the list of actions in History Step of current process instance
	 * @return an array of string containing actions names
	 */
	public String[] getStepActions()
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();
		String[] actions = new String[steps.length];
		Action action = null;

		for (int i=0; i<steps.length; i++)
		{
			try
			{
				if (steps[i].getAction().equals("#question#"))
					actions[i] = getString("processManager.question");

				else if (steps[i].getAction().equals("#response#"))
					actions[i] = getString("processManager.response");

				else if (steps[i].getAction().equals("#reAssign#"))
					actions[i] = getString("processManager.reAffectation");

				else
				{
					action = processModel.getAction(steps[i].getAction());
					actions[i] = action.getLabel(currentRole, getLanguage());
				}
			}
			catch (WorkflowException we)
			{
				actions[i] = "##";
			}
		}

		return actions;
	}

	/**
	 * Get the list of action dates in History Step of current process instance
	 * @return an array of string containing action dates
	 */
	public String[] getStepDates()
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();
		String[] dates = new String[steps.length];
		Date date = null;

		for (int i=0; i<steps.length; i++)
		{
			date = steps[i].getActionDate();
			dates[i] = DateUtil.getOutputDate(date, getLanguage());
		}

		return dates;
	}

	/**
	 * Returns the form used to display the i-th step.
	 * Returns null if the step is unkwown.
	 */
	public Form getStepForm(int index) throws ProcessManagerException
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();

		if (0 <= index && index < steps.length)
		{
		   try
			{
				if (steps[index].getAction().equals("#question#") || steps[index].getAction().equals("#response#"))
				{
					return getQuestionForm(true);
				}
				else
					return processModel.getPresentationForm(steps[index].getAction(), currentRole, getLanguage());
		   }
			catch (WorkflowException e)
			{
			   SilverTrace.info("processManager", "sessionController",
	                 "processManager.ILL_DATA_STEP", e);

			   return null;
			}
		}
		else return null;
	}

	/**
	 * Returns the data filled during the i-th step.
	 * Returns null if the step is unkwown.
	 */
	public DataRecord getStepRecord(int index) throws ProcessManagerException
	{
		HistoryStep[] steps = currentProcessInstance.getHistorySteps();

		if (0 <= index && index < steps.length)
		{
			try
			{
				if (steps[index].getAction().equals("#question#") )
				{
					Question question = null;
					Question[] questions = currentProcessInstance.getQuestions();
					for (int j=0; question==null && j<questions.length; j++)
					{
						if ( steps[index].getResolvedState().equals(questions[j].getFromState().getName()) )
						{
							if ( ( (questions[j].getQuestionDate().getTime() - steps[index].getActionDate().getTime()) < 30000 )
							  && ( (questions[j].getQuestionDate().getTime() - steps[index].getActionDate().getTime()) > 0 )
								)
								question = questions[j];
						}
					}

					if (question == null)
						return null;
					else
						return new QuestionRecord(question.getQuestionText());
				}
				else
				if (steps[index].getAction().equals("#response#") )
				{
					Question question = null;
					Question[] questions = currentProcessInstance.getQuestions();
					for (int j=0; question==null && j<questions.length; j++)
					{
						if ( steps[index].getResolvedState().equals(questions[j].getTargetState().getName()) )
						{
							if ( ( (questions[j].getResponseDate().getTime() - steps[index].getActionDate().getTime()) < 30000 )
							  && ( (questions[j].getResponseDate().getTime() - steps[index].getActionDate().getTime()) > 0 )
								)
								question = questions[j];
						}
					}

					if (question == null)
						return null;
					else
						return new QuestionRecord(question.getResponseText());
				}
				else
					return steps[index].getActionRecord();
			}
			catch (WorkflowException e)
			{
				SilverTrace.info("processManager", "sessionController",
					 "processManager.ILL_DATA_STEP", e);
				return null;
			}
		}
		else return null;
	}

	/**
	 * Returns the form defined to print
	 */
	public Form getPrintForm(HttpServletRequest request) throws ProcessManagerException
	{
		try
		{
			Workflow.getProcessModelManager();
			com.silverpeas.workflow.api.model.Form form = processModel.getForm("printForm");
			if (form==null)
			{
				throw new ProcessManagerException("ProcessManagerSessionController",
										   "processManager.NO_PRINTFORM_DEFINED_IN_MODEL");
			}

			else
			{
				//HtmlForm htmlForm = new HtmlForm( processModel.getAllDataTemplate( currentRole, getLanguage() ) );
				HtmlForm htmlForm = new HtmlForm(processModel.getDataFolder().toRecordTemplate(currentRole, getLanguage(), true));
				
				htmlForm.setFileName("http://" + request.getServerName() + ":" + request.getServerPort() + form.getHTMLFileName());
				return htmlForm;
			}
		}
		catch (Exception e)
		{
	         throw new ProcessManagerException("ProcessManagerSessionController",
                                           "processManager.EX_GET_PRINT_FORM", e);
		}
	}

	/**
	 * Returns the data of instance
	 */
	public DataRecord getPrintRecord() throws ProcessManagerException
	{
		try
		{
			return currentProcessInstance.getAllDataRecord( currentRole, getLanguage() );
		}
		catch (WorkflowException we)
		{
	         throw new ProcessManagerException("ProcessManagerSessionController",
                                           "processManager.EX_GET_PRINT_RECORD", we);
		}
	}

	/**
	 * Get the label of given action
	 * @param	actionName	action name
	 * @return action label	
	 */
	public String getActionLabel(String actionName)
	{
		try
		{
			Action action = processModel.getAction(actionName);
			if (action == null)
				return actionName;

			else
				return action.getLabel(currentRole, getLanguage());
		}
		catch (WorkflowException we)
		{
			return actionName;
		}
	}

	/**
	 * Get the state with the given name
	 * @param	stateName	state name
	 * @return State object
	 */
	public State getState(String stateName)
	{
		return processModel.getState(stateName);
	}

	/**
	 * Get the named action
	 * @param	actionName	action name
	 * @return action	
	 */
	public Action getAction(String actionName)
	{
		try
		{
		   return processModel.getAction(actionName);
		}
		catch (WorkflowException we)
		{
			return null;
		}
	}

	/**
	 * Tests if there is some question for current user in current processInstance
	 * @return true if there is one or more question
	 */
	public boolean hasPendingQuestions()
	{
		try
		{
			Task[] tasks = getTasks();
			if (tasks==null || tasks.length==0)
				return false;

			for (int i=0; i<tasks.length; i++)
			{
				if (tasks[i].getPendingQuestions()!=null && tasks[i].getPendingQuestions().length>0)
					return true;
			}

			return false;
		}
		catch (ProcessManagerException pme)
		{
			return false;
		}
	}

   /**
    * Returns the form to fill user settings
    */
   public Form getUserSettingsForm()
	   throws ProcessManagerException
   {
	   try
	   {
		   com.silverpeas.workflow.api.model.DataFolder
			    userInfos = processModel.getUserInfos();
			if (userInfos == null) return null;

		   return new XmlForm( userInfos.toRecordTemplate(currentRole, getLanguage(), false) );
	   }
	   catch (FormException we)
	   {
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_USERSETTINGS_FORM", we);
	   }
	   catch (WorkflowException fe)
	   {
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_USERSETTINGS_FORM", fe);
	   }
   }

   /**
    * Returns the an empty date record
    * which will be filled with the user settings form.
    */
   public DataRecord getEmptyUserSettingsRecord()
	   throws ProcessManagerException
   {
	   try
	   {
		   return processModel.getNewUserInfosRecord(currentRole, getLanguage());
	   }
	   catch (WorkflowException we)
	   {
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_EMPTY_USERSETTINGS_RECORD");
	   }
   }

   /**
    * Returns the an empty data record
    * which will be filled with the user settings form.
    */
   public DataRecord getUserSettingsRecord()
	   throws ProcessManagerException
   {
	   try
	   {
		   DataRecord data = getEmptyUserSettingsRecord();
		   userSettings.load(data, processModel.getUserInfos().toRecordTemplate(currentRole, getLanguage(), false));
	
		   return data;
	   }
	   catch (WorkflowException we)
	   {
		   throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_EMPTY_USERSETTINGS_RECORD");
	   }
   }

	/**
	 * Save the user settings
	 * which have been filled with the user settings form.
	 */
	public void saveUserSettings(DataRecord data)
	   throws ProcessManagerException
	{
		try
		{
			userSettings.update(data, processModel.getUserInfos().toRecordTemplate(currentRole, getLanguage(), false));
			userSettings.save();
			
			Workflow.getUserManager().resetUserSettings(getUserId(), getComponentId());
		}
		catch (WorkflowException we)
		{
			throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_SAVE_USERSETTINGS");
		}
	}

   /**
	 * Returns the current ProcessFilter.
	 */
	public ProcessFilter getCurrentFilter() throws ProcessManagerException
	{
		if (currentProcessFilter == null)
		{
		   currentProcessFilter = new ProcessFilter(processModel, currentRole, getLanguage());
		}
		return currentProcessFilter;
	}

   /**
	 * Reset the current ProcessFilter.
	 */
	public void resetProcessFilter() throws ProcessManagerException
	{
	   ProcessFilter oldFilter = currentProcessFilter;
		currentProcessFilter = new ProcessFilter(processModel, currentRole, getLanguage());

		if (oldFilter != null)
		{
		   currentProcessFilter.setCollapse(oldFilter.getCollapse());
		   currentProcessFilter.copySharedCriteria(oldFilter);
		}
	}

	/**
	 * Remove process instance with given id
	 */
	public void removeProcess(String processId)
	   throws ProcessManagerException
	{
		try
		{
			( (UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager() ).removeProcessInstance(processId);
		}
		catch (WorkflowException we)
		{
			throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_REMOVE_PROCESS", we);
		}
	}

	/**
	 * Get all the errors occured while processing the current process instance
	 */
	public WorkflowError[] getProcessInstanceErrors(String processId)
	   throws ProcessManagerException
	{
		try
		{
			return Workflow.getErrorManager().getErrorsOfInstance(processId);
		}
		catch (WorkflowException we)
		{
			throw new ProcessManagerException("ProcessManagerSessionController", "processManager.ERR_GET_PROCESS_ERRORS", we);
		}
	}

	/**
	 * Returns true if :
	 *    the instance is built from the process model of this session.
	 *    the user is allowed to access to this instance 
	 *    with his current role.
	 */
	private boolean isAllowed(ProcessInstance instance)
	{
		return true; // xoxox
	}

	/**
	 * Returns true if :
	 *    the user settings are correct
	 */
	public boolean isUserSettingsOK()
	{
		return (userSettings!=null && userSettings.isValid());
	}

	/**
	 * Returns true if :
	 *    the model has any user settings.
	 */
	public boolean hasUserSettings()
	{
	   return processModel.getUserInfos() != null;
	}
	
	public boolean isVersionControlled() {
		String strVersionControlled = this.getComponentParameterValue("versionControl");
		return ((strVersionControlled != null) && !("").equals(strVersionControlled) && !("no").equals(strVersionControlled.toLowerCase()));
	}
	
	public boolean isAttachmentTabEnable()
	{
		String param = this.getComponentParameterValue("attachmentTabEnable");
		if (param == null)
			return true;
		return param != null && !("").equals(param) && !("no").equals(param.toLowerCase());
	}
	
	public boolean isProcessIdVisible()
	{
		String param = this.getComponentParameterValue("processIdVisible");
		return "yes".equalsIgnoreCase(param);
	}
	
	public boolean isViewReturn()
	{
		boolean viewReturn = true;
		
		// r�cup�rer le param�tre global
		boolean hideReturnGlobal = "yes".equalsIgnoreCase(resources.getString("hideReturn"));
		
		viewReturn = !hideReturnGlobal;
		
		if (viewReturn)
		{
			// au global, on voit les boutons "retour", regarder si cette instance les cache
			boolean hideReturnLocal = "yes".equalsIgnoreCase(getComponentParameterValue("hideReturn"));
			viewReturn = !hideReturnLocal;
		}
		return viewReturn;
	}

   /**
    * The session controller saves any fatal exception.
    */
   private ProcessManagerException fatalException = null;

   /**
    * All the process instance of this work session
    * are built from a same process model : the processModel.
    */
   private String       peasId       = null;
   private ProcessModel processModel = null;

   /**
    * The session saves a current User (workflow user)
    */
   private User currentUser = null;

   /**
    * The session saves a list of user roles.
    */
   private String[] userRoles = null;
   private NamedValue[] userRoleLabels = null;

   /**
    * The session saves a current User role.
    */
   private String currentRole = null;

   /**
    * The creation rights (true if user can create new instances)
    */
   private boolean creationRights = false;

   /**
    * The session saves a current process instance.
    */
    private ProcessInstance currentProcessInstance = null;

   /**
    * The session saves a current process filter.
    */
    private ProcessFilter currentProcessFilter = null;

   /**
    * The session saves a current process instance list rows template.
    */
    private RecordTemplate currentListHeaders = null;

   /**
    * The session saves a current process instance list.
    */
    private DataRecord[] currentProcessList = null;

   /**
    * The user settings
    */
    private UserSettings userSettings = null;
}
