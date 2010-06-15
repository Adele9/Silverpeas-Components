package com.stratelia.webactiv.forums.sessionController;

import static org.junit.Assert.*;

import org.junit.Test;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;

import static org.mockito.Mockito.*;

public class TestForumsSessionController {

  @Test
  public void testIsVisible() throws Exception{
    int forumId = 12;
    MainSessionController mainController = mock(MainSessionController.class);
    UserDetail user = mock(UserDetail.class);
    when(mainController.getCurrentUserDetail()).thenReturn(user);
    when(user.getId()).thenReturn("5");
    ComponentContext context = mock(ComponentContext.class);
    ForumsSessionController controller = new ForumsSessionController(mainController, context);
    ForumsBM forum = mock(ForumsBM.class);
    when(forum.isModerator(eq("5"), any(ForumPK.class))).thenReturn(false);
    when(forum.getForumParentId(forumId)).thenReturn(new Integer(0));
    controller.setForumsBM(forum);
    boolean result = controller.isVisible(ForumsSessionController.STATUS_VALIDATE, forumId);    
    assertEquals(true, result);
    verify(forum, times(1)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(1)).getForumParentId(forumId);
    result = controller.isVisible(ForumsSessionController.STATUS_FOR_VALIDATION, forumId);
    assertEquals(false, result);
    verify(forum, times(2)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(2)).getForumParentId(forumId);
    forum = mock(ForumsBM.class);
    when(forum.isModerator(eq("5"), any(ForumPK.class))).thenReturn(true);
    when(forum.getForumParentId(forumId)).thenReturn(new Integer(0));
    controller.setForumsBM(forum);
    result = controller.isVisible(ForumsSessionController.STATUS_VALIDATE, forumId);
    assertEquals(true, result);
    verify(forum, times(1)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(1)).getForumParentId(forumId);
    result = controller.isVisible(ForumsSessionController.STATUS_FOR_VALIDATION, forumId);
    assertEquals(true, result);
    verify(forum, times(2)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(2)).getForumParentId(forumId);
  }

 /* @Test
  public void testIsModerator() {
    fail("Not yet implemented");
  }*/

}
