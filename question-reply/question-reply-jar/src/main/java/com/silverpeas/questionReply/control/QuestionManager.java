package com.silverpeas.questionReply.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Recipient;
import com.silverpeas.questionReply.model.Reply;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class QuestionManager 
{
   private static QuestionManager instance;

   private SilverpeasBeanDAO Qdao = null;
   private SilverpeasBeanDAO Rdao = null;
   private SilverpeasBeanDAO Udao = null;

   private QuestionReplyContentManager contentManager = null;

   private QuestionManager()
   {
   }

   private QuestionReplyContentManager getQuestionReplyContentManager() {
		if (contentManager == null)
		{
			contentManager = new QuestionReplyContentManager();
		}
		return contentManager;
   }

   static public QuestionManager getInstance()
   {
		if (instance == null)
			instance = new QuestionManager();
		return instance;
   }
   private SilverpeasBeanDAO getQdao() throws PersistenceException {
		if (Qdao == null)
			Qdao = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.questionReply.model.Question");
		return Qdao;
   }
   private SilverpeasBeanDAO getRdao() throws PersistenceException {
		if (Rdao == null)
			Rdao = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.questionReply.model.Reply");
		return Rdao;
   }
   private SilverpeasBeanDAO getUdao() throws PersistenceException {
		if (Udao == null)
			Udao = SilverpeasBeanDAOFactory.getDAO("com.silverpeas.questionReply.model.Recipient");
		return Udao;
   }
   /*
   * enregistre une question et ses destinataires (attention les destinataires n'ont pas de questionId)
   */
   public long createQuestion(Question question) throws QuestionReplyException
   {
		long idQ = -1;

		boolean succeed = false;
		Connection con = null;

		try
		{
			Collection recipients = question.readRecipients();
			SilverpeasBeanDAO daoQ = getQdao();
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);

			IdPK pkQ = (IdPK) daoQ.add(con, question);
			createQuestionIndex(question);
			idQ = pkQ.getIdAsLong();
			if (recipients != null)
			{
				Iterator it = recipients.iterator();
				while (it.hasNext())
				{
					Recipient recipient = (Recipient) it.next();
					recipient.setQuestionId(idQ);
					createRecipient(con, recipient);
				}
			}
			question.setPK(pkQ);
			getQuestionReplyContentManager().createSilverContent(con, question);
			succeed = true;
		}
		catch( UtilException  e )
		{
			throw new QuestionReplyException("QuestionManager.createQuestion",SilverpeasException.ERROR,"questionReply.EX_CREATE_QUESTION_FAILED","",e);
		}		
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.createQuestion",SilverpeasException.ERROR,"questionReply.EX_CREATE_QUESTION_FAILED","",e);
		}		
		catch( ContentManagerException  e )
		{
			throw new QuestionReplyException("QuestionManager.createQuestion",SilverpeasException.ERROR,"questionReply.EX_CREATE_QUESTION_FAILED","",e);
		}		
		finally
		{
			closeConnection(con, succeed);
		}
		return idQ;
   }

   public void createQuestionIndex(Question question)
	{
		SilverTrace.info("questionReply", "QuestionManager.createQuestionIndex()", "root.MSG_GEN_ENTER_METHOD", "Question = " + question.getTitle());
		FullIndexEntry indexEntry = null;

		if (question != null)
		{
			// Indexer la Question
			indexEntry = new FullIndexEntry(question.getInstanceId(), "Question", question.getPK().getId());
			indexEntry.setTitle(question.getTitle());
			indexEntry.setPreView(question.getContent());
			indexEntry.setCreationDate(question.getCreationDate());
			indexEntry.setCreationUser(question.getCreatorId());
			IndexEngineProxy.addIndexEntry(indexEntry);
		}
	}
   
   public void deleteQuestionIndex(Question question)
	{
	   SilverTrace.info("questionReply", "QuestionManager.deleteQuestionIndex()", "root.MSG_GEN_ENTER_METHOD", "Question = " + question.toString());
		IndexEntryPK indexEntry = new IndexEntryPK(question.getInstanceId(), "Question", question.getPK().getId());

		IndexEngineProxy.removeIndexEntry(indexEntry);
		
		// supprimer les index des r�ponses...
	}
   
   /*
   * enregistre une r�ponse � une question
   * => met � jour publicReplyNumber et/ou privateReplyNumber et replyNumber de la question ainsi que le status � 1 
   */
   public long createReply(Reply reply, Question question) throws QuestionReplyException
   {
		long idR = -1;
		boolean succeed = false;
		Connection con = null;
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			IdPK pkR = (IdPK) daoR.add(con, reply);
			createReplyIndex(reply);
			idR = pkR.getIdAsLong();
			if (question.getStatus() == 0)
				question.setStatus(1);
			updateQuestion(con, question);
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.createReply",SilverpeasException.ERROR,"questionReply.EX_CREATE_REPLY_FAILED","",e);
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.createReply",SilverpeasException.ERROR,"questionReply.EX_CREATE_REPLY_FAILED","",e);
		}
		finally
		{
			closeConnection(con, succeed);
		}
		return idR;
   }
   
   public void createReplyIndex(Reply reply)
	{
		SilverTrace.info("questionReply", "QuestionManager.createReplyIndex()", "root.MSG_GEN_ENTER_METHOD", "Reply = " + reply.getTitle());
		FullIndexEntry indexEntry = null;

		if (reply != null)
		{
			// Indexer la R�ponse
			indexEntry = new FullIndexEntry(reply.getPK().getInstanceId(), "Reply", reply.getPK().getId());
			indexEntry.setTitle(reply.getTitle());
			indexEntry.setPreView(reply.getContent());
			indexEntry.setCreationDate(reply.getCreationDate());
			indexEntry.setCreationUser(reply.getCreatorId());
			IndexEngineProxy.addIndexEntry(indexEntry);
		}
	}
   
   public void deleteReplyIndex(Reply reply)
	{
	   SilverTrace.info("questionReply", "QuestionManager.deleteReplyIndex()", "root.MSG_GEN_ENTER_METHOD", "Reply = " + reply.toString());
	   
		IndexEntryPK indexEntry = new IndexEntryPK(reply.getPK().getInstanceId(), "Reply", reply.getPK().getId());

		IndexEngineProxy.removeIndexEntry(indexEntry);
	}
   
   /*
   * enregistre un destinataire
   */
   private void createRecipient(Connection con, Recipient recipient) throws QuestionReplyException
   {
		try
		{
			SilverpeasBeanDAO daoU = getUdao();
			daoU.add(con, recipient);
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.createQuestion",SilverpeasException.ERROR,"questionReply.EX_CREATE_RECIPIENT_FAILED","",e);
		}		
   }
	/*
   * supprime tous les destinataires d'une question
   */
   private void deleteRecipients(Connection con, long questionId) throws QuestionReplyException
   {
		try
		{
			SilverpeasBeanDAO daoU = getUdao();
			IdPK pk = new IdPK();
			daoU.removeWhere(con, pk, " questionId = " + new Long(questionId).toString());
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.deleteRecipients",SilverpeasException.ERROR,"questionReply.EX_DELETE_RECIPIENTS_FAILED","",e);
		}				
   }
   /*
   * Clos une liste de questions : updateQuestion
   */
   public void closeQuestions(Collection questionIds) throws QuestionReplyException
   {
		if (questionIds != null)
		{
			boolean succeed = false;
			Connection con = null;
			try
			{
					con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
					Iterator it = questionIds.iterator();
					while (it.hasNext())
					{
						long idQ = ((Long) it.next()).longValue();
						Question question = getQuestion(idQ);
						question.setStatus(2);
						updateQuestion(con, question);
					}
					succeed = true;
			}
			catch( UtilException e )
			{
				throw new QuestionReplyException("QuestionManager.closeQuestions",SilverpeasException.ERROR,"questionReply.EX_CLOSE_QUESTIONS_FAILED","",e);
			}		
			finally
			{
			   closeConnection(con, succeed);
			}		
		}
   }
   
   public void openQuestions(Collection questionIds) throws QuestionReplyException
   {
		if (questionIds != null)
		{
			boolean succeed = false;
			Connection con = null;
			try
			{
					con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
					Iterator it = questionIds.iterator();
					while (it.hasNext())
					{
						long idQ = ((Long) it.next()).longValue();
						Question question = getQuestion(idQ);
						question.setStatus(1);
						updateQuestion(con, question);
					}
					succeed = true;
			}
			catch( UtilException e )
			{
				throw new QuestionReplyException("QuestionManager.openQuestions",SilverpeasException.ERROR,"questionReply.EX_OPEN_QUESTIONS_FAILED","",e);
			}		
			finally
			{
			   closeConnection(con, succeed);
			}		
		}
   }
   
   /*
   * Modifie les destinataires d'une question : deleteRecipients, createRecipient
   */
   public void updateQuestionRecipients(Question question) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;
		try
		{
			Collection recipients = question.readRecipients();
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			deleteRecipients(con, ((IdPK) question.getPK()).getIdAsLong());
			if (recipients != null)
			{
				Iterator it = recipients.iterator();
				while (it.hasNext())
				{
					Recipient recipient = (Recipient) it.next();
					recipient.setQuestionId(((IdPK) question.getPK()).getIdAsLong());
					createRecipient(con, recipient);
				}
			}
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestionRecipients",SilverpeasException.ERROR,"questionReply.EX_UPDATE_RECIPIENTS_FAILED","",e);
		}	
		catch( QuestionReplyException e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestionRecipients",SilverpeasException.ERROR,"questionReply.EX_UPDATE_RECIPIENTS_FAILED","",e);
		}	
		finally
		{
			closeConnection(con, succeed);
		}				
   }
   /*
   * Affecte le status public � 0 de toutes les r�ponses d'une liste de questions : updateReply
   * Affecte le nombre de r�ponses publiques de la question � 0 : updateQuestion
   * si question en attente, on a demand� � la supprimer : deleteQuestion
   */
   public void updateQuestionRepliesPublicStatus(Collection questionIds) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;

		try
		{
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			if (questionIds != null)
			{
				Iterator itQ = questionIds.iterator();
				while(itQ.hasNext())
				{
					long idQ = ((Long) itQ.next()).longValue();
					Question question = getQuestion(idQ);
					Collection replies = getQuestionPublicReplies(idQ);
					if (replies != null)
					{
						Iterator itR = replies.iterator();
						while (itR.hasNext())
						{
							Reply reply = (Reply) itR.next();
							reply.setPublicReply(0);
							addComponentId(reply, question.getPK().getInstanceId());
							updateReply(con, reply);
						}
						updateQuestion(con, question);
					}
					
					if (question.getStatus() == 0)
					{
						deleteQuestion(con, idQ);
					}
				}
			}
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPublicStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}		
		catch( QuestionReplyException e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPublicStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}		
		finally
		{
			closeConnection(con, succeed);
		}				
   }
   /*
   * Affecte le status private � 0 de toutes les r�ponses d'une liste de questions : updateReply
   * Affecte le nombre de r�ponses priv�es de la question � 0 : updateQuestion
   */
   public void updateQuestionRepliesPrivateStatus(Collection questionIds) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;
		try
		{
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			if (questionIds != null)
			{
				Iterator itQ = questionIds.iterator();
				while(itQ.hasNext())
				{
					long idQ = ((Long) itQ.next()).longValue();
					Question question = getQuestion(idQ);
					Collection replies = getQuestionPrivateReplies(idQ);
					if (replies != null)
					{
						Iterator itR = replies.iterator();
						while (itR.hasNext())
						{
							Reply reply = (Reply) itR.next();
							reply.setPrivateReply(0);
							addComponentId(reply, question.getPK().getInstanceId());
							updateReply(con, reply);
						}
						updateQuestion(con, question);
					}
				}
			}
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPrivateStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}	
		catch( QuestionReplyException e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestionRepliesPrivateStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}	
		finally
		{
			closeConnection(con, succeed);
		}						
   }
   
   private void addComponentId(Reply reply, String componentId)
   {
	   WAPrimaryKey pk = reply.getPK();
	   pk.setComponentName(componentId);
	   reply.setPK(pk);
   }
   
   /*
   * Affecte le status public � 0 d'une liste de r�ponses : updateReply
   * D�cremente le nombre de r�ponses publiques de la question d'autant : updateQuestion
   */
   public void updateRepliesPublicStatus(Collection replyIds, Question question) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;
		try
		{
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			if (replyIds != null)
			{
				Iterator itR = replyIds.iterator();
				while(itR.hasNext())
				{
					long idR = ((Long) itR.next()).longValue();
					Reply reply = getReply(idR);
					if (reply != null)
					{
						reply.setPublicReply(0);
						addComponentId(reply, question.getPK().getInstanceId());
						updateReply(con, reply);
					}
				}
				updateQuestion(con, question);
			}
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.updateRepliesPublicStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}		
		catch( QuestionReplyException e )
		{
			throw new QuestionReplyException("QuestionManager.updateRepliesPublicStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}		
		finally
		{
			closeConnection(con, succeed);
		}				
   }
   /*
   * Affecte le status private � 0 d'une liste de r�ponses : updateReply
   * D�cremente le nombre de r�ponses priv�es de la question d'autant : updateQuestion
   */
   public void updateRepliesPrivateStatus(Collection replyIds, Question question) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;
		try
		{
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			if (replyIds != null)
			{
				Iterator itR = replyIds.iterator();
				while(itR.hasNext())
				{
					long idR = ((Long) itR.next()).longValue();
					Reply reply = getReply(idR);
					if (reply != null)
					{
						reply.setPrivateReply(0);
						addComponentId(reply, question.getPK().getInstanceId());
						updateReply(con, reply);
					}
				}
				updateQuestion(con, question);
			}
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.updateRepliesPrivateStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}	
		catch( QuestionReplyException e )
		{
			throw new QuestionReplyException("QuestionManager.updateRepliesPrivateStatus",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLYSTATUS_FAILED","",e);
		}	
		finally
		{
			closeConnection(con, succeed);
		}						
   }
   /*
   * Modifie une question
   * => la question est supprim�e si publicReplyNumber et privateReplyNumber sont � 0 et que la question est close
   * => met � jour publicReplyNumber et/ou privateReplyNumber et replyNumber de la question
   */	
   private void updateQuestion(Connection con, Question question) throws QuestionReplyException
   {
		try
		{
			long idQ = ((IdPK) question.getPK()).getIdAsLong();
			question.setReplyNumber(getQuestionRepliesNumber(idQ));
			question.setPublicReplyNumber(getQuestionPublicRepliesNumber(idQ));
			question.setPrivateReplyNumber(getQuestionPrivateRepliesNumber(idQ));

			if ((question.getReplyNumber() == 0)&&(question.getStatus() == 2))
			{
				deleteQuestion(con, idQ);
			}
			else 
			{
				SilverpeasBeanDAO daoQ = getQdao();
				daoQ.update(con, question);
				createQuestionIndex(question);

				question.getPK().setComponentName(question.getInstanceId());
				getQuestionReplyContentManager().updateSilverContentVisibility(question);
			}
		}
		catch( Exception e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestion",SilverpeasException.ERROR,"questionReply.EX_UPDATE_QUESTION_FAILED","",e);
		}		
   }
   
   /*
   * Modifie une question
   * => la question est supprim�e si publicReplyNumber et privateReplyNumber sont � 0 et que la question est close
   * => met � jour publicReplyNumber et/ou privateReplyNumber et replyNumber de la question
   */	
   public void updateQuestion(Question question) throws QuestionReplyException
   {
		try
		{
			long idQ = ((IdPK) question.getPK()).getIdAsLong();
			question.setReplyNumber(getQuestionRepliesNumber(idQ));
			question.setPublicReplyNumber(getQuestionPublicRepliesNumber(idQ));
			question.setPrivateReplyNumber(getQuestionPrivateRepliesNumber(idQ));

			if ((question.getReplyNumber() == 0)&&(question.getStatus() == 2))
			{
				deleteQuestion(idQ);
			}
			else 
			{
				SilverpeasBeanDAO daoQ = getQdao();
				daoQ.update(question);
				createQuestionIndex(question);
				question.getPK().setComponentName(question.getInstanceId());
				getQuestionReplyContentManager().updateSilverContentVisibility(question);
			}
		}
		catch( Exception e )
		{
			throw new QuestionReplyException("QuestionManager.updateQuestion",SilverpeasException.ERROR,"questionReply.EX_UPDATE_QUESTION_FAILED","",e);
		}		
   }
   /*
   * Modifie une r�ponse
   * => La r�ponse est supprim�e si le status public et le status private sont � 0
   */	
   private void updateReply(Connection con, Reply reply) throws QuestionReplyException
   {
		try
		{
			if ((reply.getPublicReply()==0)&&(reply.getPrivateReply()==0))
			{
				deleteReply(con, ((IdPK) reply.getPK()).getIdAsLong());
				deleteReplyIndex(reply);
			}
			else
			{
				SilverpeasBeanDAO daoR = getRdao();
				daoR.update(con, reply);
				createReplyIndex(reply);
			}
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.updateReply",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLY_FAILED","",e);
		}	
   }

   /*
   * Modifie une r�ponse
   * => La r�ponse est supprim�e si le status public et le status private sont � 0
   */	
   public void updateReply(Reply reply) throws QuestionReplyException
   {
		try
		{
			if ((reply.getPublicReply()==0)&&(reply.getPrivateReply()==0))
			{
				deleteReply(((IdPK) reply.getPK()).getIdAsLong());
				deleteReplyIndex(reply);
			}
			else
			{
				SilverpeasBeanDAO daoR = getRdao();
				daoR.update(reply);
				createReplyIndex(reply);
			}
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.updateReply",SilverpeasException.ERROR,"questionReply.EX_UPDATE_REPLY_FAILED","",e);
		}	
   }
   /*
   * supprime une question
   */	
   private void deleteQuestion(Connection con, long questionId) throws QuestionReplyException
   {
		try
		{
			deleteRecipients(con, questionId);
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			pk.setIdAsLong(questionId);
			Question question = getQuestion(questionId);
			String peasId = question.getInstanceId();

			daoQ.remove(con, pk);
			deleteQuestionIndex(question);
			
			pk.setComponentName(peasId);
			getQuestionReplyContentManager().deleteSilverContent(con, pk);
		}
		catch( Exception e )
		{
			throw new QuestionReplyException("QuestionManager.deleteQuestion",SilverpeasException.ERROR,"questionReply.EX_DELETE_QUESTION_FAILED","",e);
		}	
   }
   /*
   * supprime une question
   */	
   private void deleteQuestion(long questionId) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;
		try
		{
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			deleteRecipients(con, questionId);
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			pk.setIdAsLong(questionId);
			Question question = getQuestion(questionId);
			String peasId = question.getInstanceId();

			daoQ.remove(con, pk);
			deleteQuestionIndex(question);

			pk.setComponentName(peasId);
			getQuestionReplyContentManager().deleteSilverContent(con, pk);

			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.deleteQuestion",SilverpeasException.ERROR,"questionReply.EX_DELETE_QUESTION_FAILED","",e);
		}	
		catch( Exception e )
		{
			throw new QuestionReplyException("QuestionManager.deleteQuestion",SilverpeasException.ERROR,"questionReply.EX_DELETE_QUESTION_FAILED","",e);
		}	
		finally
		{
			closeConnection(con, succeed);
		}				
   }
   
   public void deleteQuestionAndReplies(Collection questionIds) throws QuestionReplyException
   {
	   // pour chaque question
	   Iterator it = questionIds.iterator();
	   while (it.hasNext())
	   {
			boolean succeed = false;
			Connection con = null;
			Long questionId = (Long) it.next();
			try
			{
				con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
				long qId = questionId.longValue();
				deleteRecipients(con, qId);
				SilverpeasBeanDAO daoQ = getQdao();
				IdPK pk = new IdPK();
				pk.setIdAsLong(qId);
				Question question = getQuestion(qId);
				String peasId = question.getInstanceId();
	
				// rechercher les r�ponses
				Collection replies = getAllReplies(qId);
				Iterator itR = replies.iterator();
				while (itR.hasNext())
				{
					Reply reply = (Reply) itR.next();
					long replyId = Long.parseLong(reply.getPK().getId());
					WAPrimaryKey pkR = reply.getPK();
					pkR.setComponentName(question.getInstanceId());
					reply.setPK(pkR);
					// supprimer la r�ponse et son index
					deleteReply(replyId);
					deleteReplyIndex(reply);
				}
				
				// supprimer la question
				daoQ.remove(con, pk);
				deleteQuestionIndex(question);
	
				pk.setComponentName(peasId);
				getQuestionReplyContentManager().deleteSilverContent(con, pk);
	
				succeed = true;
			}
			catch( UtilException e )
			{
				throw new QuestionReplyException("QuestionManager.deleteQuestionAndReplies",SilverpeasException.ERROR,"questionReply.EX_DELETE_QUESTION_FAILED","",e);
			}	
			catch( Exception e )
			{
				throw new QuestionReplyException("QuestionManager.deleteQuestionAndReplies",SilverpeasException.ERROR,"questionReply.EX_DELETE_QUESTION_FAILED","",e);
			}	
			finally
			{
				closeConnection(con, succeed);
			}	
	   }
   }
   
   private Collection getAllReplies(long questionId) throws QuestionReplyException
   {
	   Collection allReplies = new ArrayList();
	   try
		{
			Collection privateReplies = getQuestionPrivateReplies(questionId);
			allReplies.addAll(privateReplies);
			Collection publicReplies = getQuestionPublicReplies(questionId);
			allReplies.addAll(publicReplies);
			
			return allReplies;
		}
		catch (Exception e)
		{
			throw new QuestionReplyException("QuestionManager.getAllReplies",SilverpeasException.ERROR,"questionReply.EX_DELETE_QUESTION_FAILED","",e);
		}
   }
   /*
   * supprime une r�ponse
   */	
   private void deleteReply(long replyId) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;
		try
		{
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			pk.setIdAsLong(replyId);
			daoR.remove (con, pk);
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.deleteReply",SilverpeasException.ERROR,"questionReply.EX_DELETE_REPLY_FAILED","",e);
		}	
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.deleteReply",SilverpeasException.ERROR,"questionReply.EX_DELETE_REPLY_FAILED","",e);
		}	
		finally
		{
			closeConnection(con, succeed);
		}				
   }

   /*
   * supprime une r�ponse
   */	
   private void deleteReply(Connection con, long replyId) throws QuestionReplyException
   {
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			pk.setIdAsLong(replyId);
			daoR.remove (con, pk);
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.deleteReply",SilverpeasException.ERROR,"questionReply.EX_DELETE_REPLY_FAILED","",e);
		}	
   }

   /*
   * recup�re une question
   */		
   public Question getQuestion(long questionId) throws QuestionReplyException
   {
		Question question = null;
		try
		{
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			pk.setIdAsLong(questionId);
			question = (Question) daoQ.findByPrimaryKey(pk);
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestion",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_QUESTION","",e);
		}	
		return question;
   }
   
   public Question getQuestionAndReplies(long questionId) throws QuestionReplyException
   {
		Question question = null;
		try
		{
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			pk.setIdAsLong(questionId);
			question = (Question) daoQ.findByPrimaryKey(pk);
			Collection replies = getQuestionReplies(questionId);
			question.writeReplies(replies);
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestion",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_QUESTION","",e);
		}	
		return question;
   }

   public Collection getQuestionsByIds(ArrayList ids) throws QuestionReplyException
   {
		StringBuffer where = new StringBuffer();
		int sizeOfIds = ids.size();
		for (int i = 0; i < sizeOfIds-1; i++) {
			where.append(" id = "+(String) ids.get(i)+" or ");
		}
		if (sizeOfIds != 0)	{
			where.append(" id = "+(String) ids.get(sizeOfIds-1));
		}
		
		Collection questions = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			questions = daoQ.findByWhereClause(pk, where.toString());
		}
		catch(PersistenceException e) {
			throw new QuestionReplyException("QuestionManager.getQuestions",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_QUESTION","",e);
		}
		return questions;
   }
	

   /*
   * recup�re la liste des r�ponses d'une question
   */		
   public Collection getQuestionReplies(long questionId) throws QuestionReplyException
   {
   		Collection replies = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			replies = daoR.findByWhereClause(pk, " questionId = " + new Long(questionId).toString());
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestionReplies",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_REPLIES","",e);
		}	
		return replies;
   }
   /*
   * recup�re la liste des r�ponses publiques d'une question
   */		
   public Collection getQuestionPublicReplies(long questionId) throws QuestionReplyException
   {
      	Collection replies = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			replies = daoR.findByWhereClause(pk, " publicReply = 1 and questionId = " + new Long(questionId).toString());
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestionPublicReplies",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_REPLIES","",e);
		}	
		return replies;
   }
   /*
   * recup�re la liste des r�ponses priv�es d'une question
   */		
   public Collection getQuestionPrivateReplies(long questionId) throws QuestionReplyException
   {
      	Collection replies = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			replies = daoR.findByWhereClause(pk, " privateReply = 1 and questionId = " + new Long(questionId).toString());
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestionPrivateReplies",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_REPLIES","",e);
		}	
		return replies;
   }
   /*
   * recup�re la liste des destinataires d'une question
   */		
   public Collection getQuestionRecipients(long questionId) throws QuestionReplyException
   {
      	Collection recipients = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoU = getUdao();
			IdPK pk = new IdPK();
			recipients = daoU.findByWhereClause(pk, " questionId = " + new Long(questionId).toString());
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestionRecipients",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_RECIPIENTS","",e);
		}	
		return recipients;
   }
   /*
   * recup�re une r�ponse
   */		
   public Reply getReply(long replyId) throws QuestionReplyException
   {
   		Reply reply = null;
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			pk.setIdAsLong(replyId);
			reply = (Reply) daoR.findByPrimaryKey(pk);
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getReply",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_REPLY","",e);
		}	
		return reply;
   }
   /*
   * Recup�re la liste des questions emises par un utilisateur
   * => Q dont il est l'auteur qui ne sont pas closes ou closes avec r�ponses priv�es
   */	
   public Collection getSendQuestions(String userId, String instanceId) throws QuestionReplyException
   {
		Collection questions = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			questions = daoQ.findByWhereClause(pk, " instanceId = '"+instanceId+"' and (status <> 2 or privateReplyNumber > 0) and creatorId = " + userId);
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getSendQuestions",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_QUESTIONS","",e);
		}	
		return questions;
   }
   /*
   * Recup�re la liste des questions recues par un utilisateur
   * => Q dont il est le destinataire et qui ne sont pas closes
   */	
   public Collection getReceiveQuestions(String userId, String instanceId) throws QuestionReplyException
   {
		Collection questions = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			questions = daoQ.findByWhereClause(pk, " instanceId = '"+instanceId+"' and status <> 2 and id IN (select questionId from SC_QuestionReply_Recipient where userId = " + userId + ")");
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getReceiveQuestions",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_QUESTIONS","",e);
		}	
		return questions;
   }
   /*
   * Recup�re la liste des questions qui ne sont pas closes ou closes avec r�ponses publiques
   */	
   public Collection getQuestions(String instanceId) throws QuestionReplyException
   {
		Collection questions = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			questions = daoQ.findByWhereClause(pk, " instanceId = '"+instanceId+"' and  (status <> 2 or publicReplyNumber > 0) order by creationdate desc, id desc");
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestions",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_QUESTIONS","",e);
		}	
		return questions;
   }
        
   /*
    * Recup�re la liste de toutes les questions avec toutes ses r�ponses
    */
   public Collection getAllQuestions(String instanceId) throws QuestionReplyException
   {
		Collection questions = new ArrayList();
		Collection q = getQuestions(instanceId);
		Iterator it = q.iterator();
		while (it.hasNext())
		{
			Question question = (Question) it.next();
			question = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
			questions.add(question);
		}
		return questions;
   }
   
   public Collection getAllQuestionsByCategory(String instanceId, String categoryId) throws QuestionReplyException
   {
		Collection questions = new ArrayList();
		Collection q = getQuestions(instanceId);
		Iterator it = q.iterator();
		while (it.hasNext())
		{
			Question question = (Question) it.next();

			if ((question.getCategoryId() == null || question.getCategoryId().equals("")) && categoryId == null)
			{
				// la question est sans cat�gorie
				question = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
				questions.add(question);
			}
			else if (categoryId != null && (question.getCategoryId() != null && !question.getCategoryId().equals("")))
			{
				if (question.getCategoryId().equals(categoryId))
				{
					question = getQuestionAndReplies(Long.parseLong(question.getPK().getId()));
					questions.add(question);
				}
			}
		}
		return questions;
   } 
   
   /*
   * Recup�re la liste des questions publiques avec r�ponses
   */	
   public Collection getPublicQuestions(String instanceId) throws QuestionReplyException
   {
		Collection questions = new ArrayList();
		try
		{
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pk = new IdPK();
			questions = daoQ.findByWhereClause(pk, " instanceId = '"+instanceId+"' and publicReplyNumber > 0 ");
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getPublicQuestions",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_QUESTIONS","",e);
		}	
		return questions;
   }
   /*
   * enregistre une question et une r�ponse
   */
   public long createQuestionReply(Question question, Reply reply) throws QuestionReplyException
   {
		boolean succeed = false;
		Connection con = null;
		long idQ = -1;
		try
		{
			con= DBUtil.makeConnection(JNDINames.QUESTIONREPLY_DATASOURCE);
			SilverpeasBeanDAO daoQ = getQdao();
			IdPK pkQ = (IdPK) daoQ.add(con, question);
			createQuestionIndex(question);
			idQ = pkQ.getIdAsLong();
			SilverpeasBeanDAO daoR = getRdao();
			reply.setQuestionId(idQ);
			daoR.add(con, reply);
			createReplyIndex(reply);
			
			question = getQuestion(idQ);
			getQuestionReplyContentManager().createSilverContent(con, question);
			succeed = true;
		}
		catch( UtilException e )
		{
			throw new QuestionReplyException("QuestionManager.createQuestion",SilverpeasException.ERROR,"questionReply.EX_CREATE_QUESTION_FAILED","",e);
		}		
		catch( Exception e )
		{
			throw new QuestionReplyException("QuestionManager.createQuestion",SilverpeasException.ERROR,"questionReply.EX_CREATE_QUESTION_FAILED","",e);
		}		
		finally
		{
			closeConnection(con, succeed);
		}	
		return idQ;
   }
   /*
   * recup�re le nombre de r�ponses d'une question
   */		
   private int getQuestionRepliesNumber(long questionId) throws QuestionReplyException
   {
   		int nb = 0;
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			Collection replies = daoR.findByWhereClause(pk, " questionId = " + new Long(questionId).toString());
			nb = replies.size();
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestionReplies",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_REPLIES","",e);
		}	
		return nb;
   }
   /*
   * recup�re le nombre de r�ponses publiques d'une question
   */		
   private int getQuestionPublicRepliesNumber(long questionId) throws QuestionReplyException
   {
   		int nb = 0;
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			Collection replies = daoR.findByWhereClause(pk, " publicReply = 1 and questionId = " + new Long(questionId).toString());
			nb = replies.size();
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestionPublicReplies",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_REPLIES","",e);
		}	
		return nb;
   }
   /*
   * recup�re le nombre de r�ponses priv�es d'une question
   */		
   private int getQuestionPrivateRepliesNumber(long questionId) throws QuestionReplyException
   {
   		int nb = 0;
		try
		{
			SilverpeasBeanDAO daoR = getRdao();
			IdPK pk = new IdPK();
			Collection replies = daoR.findByWhereClause(pk, " privateReply = 1 and questionId = " + new Long(questionId).toString());
			nb = replies.size();
		}
		catch( PersistenceException e )
		{
			throw new QuestionReplyException("QuestionManager.getQuestionPrivateReplies",SilverpeasException.ERROR,"questionReply.EX_CANT_GET_REPLIES","",e);
		}	
		return nb;
   }

   private void closeConnection(Connection con, boolean succeed) throws QuestionReplyException
   {
		if (con != null)
			try
			{
				 con.close(); 
			}
			catch (SQLException e)
			{
				 throw new QuestionReplyException("QuestionManager.closeConnection",SilverpeasException.ERROR,"questionReply.EX_CREATE_QUESTION_FAILED","",e);
			}
   }
}