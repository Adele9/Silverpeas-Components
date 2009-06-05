<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_MailingList_<c:out value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><fmt:message key="mailingList.tab.moderation.title" /></title>
<view:looknfeel />
<script type="text/javascript">
function deleteMessage(){
  if(confirm('<fmt:message key="mailingList.moderation.delete.confirm"/>')){
    document.moderate.action=document.moderate.action + '/message/delete';
    document.moderate.submit();
  }
}
function moderateMessage(){
  if(confirm('<fmt:message key="mailingList.moderation.moderate.confirm"/>')){
    document.moderate.action=document.moderate.action + '/message/put';
    document.moderate.submit();
  }
}
</script>
</head>
<body>
<fmt:message key="mailingList.tab.list.title" var="listTabTitle" />
<fmt:message key="mailingList.tab.activity.title" var="activityTabTitle" />
<fmt:message key="mailingList.tab.moderation.title" var="moderationTabTitle" />
<fmt:message key="mailingList.icons.message.delete" var="deleteMessageIcon" bundle="${icons}" />
<fmt:message key="mailingList.icons.attachmentSmall" var="attachmentIcon" bundle="${icons}" />
<fmt:message key="mailingList.icons.message.delete.alt" var="deleteMessageAltText" />
<fmt:message key="mailingList.icons.message.moderate" var="acceptIcon" bundle="${icons}" />
<fmt:message key="mailingList.icons.message.moderate.alt" var="acceptMessageAltText" />
<c:url var="deleteIconUrl" value="${deleteMessageIcon}" />
<c:url var="acceptIconUrl" value="${acceptIcon}" />
<c:url var="browseBarLink" value="/Rmailinglist/${componentId}/Main" />
<view:browseBar link="${browseBarLink}" path="${moderationTabTitle}" />
<view:operationPane>
  <view:operation altText="${deleteMessageAltText}" icon="${deleteIconUrl}" action="javascript: deleteMessage();" />
  <view:operation altText="${acceptMessageAltText}" icon="${acceptIconUrl}" action="javascript: moderateMessage();" />
</view:operationPane>
<view:window>
  <c:url var="listAction" value="/Rmailinglist/${componentId}/list/${componentId}" />
  <c:url var="activityAction" value="/Rmailinglist/${requestScope.componentId}/Main" />
  <view:tabs>
    <view:tab label="${activityTabTitle}" action="${activityAction}" selected="false" />
    <view:tab label="${listTabTitle}" action="${listAction}" selected="false" />
    <view:tab label="${moderationTabTitle}" action="${'#'}" selected="true" />
    <c:if test="${requestScope.currentUserIsAdmin}">
      <fmt:message key="mailingList.tab.users.title" var="usersTabTitle" />
      <c:url var="usersAction" value="/Rmailinglist/${componentId}/users/${componentId}" />
      <view:tab label="${usersTabTitle}" action="${usersAction}" selected="false" />
    </c:if>
    <c:if test="${requestScope.currentUserIsAdmin}">
      <fmt:message key="mailingList.tab.subscribers.title" var="subscribersTabTitle" />
      <c:url var="subscriberssAction" value="/Rmailinglist/${componentId}/subscription/${componentId}/subscription/put" />
      <view:tab label="${subscribersTabTitle}" action="${subscriberssAction}" selected="false" />
    </c:if>
  </view:tabs>
  <view:frame>
    <center>
    <table width="98%" border="0" cellpadding="0" cellspacing="2">
      <tr>
        <td>
        <form id="moderate" name="moderate" method="POST" action="<c:out value="${pageContext.request.contextPath}/Rmailinglist/${componentId}/destination/moderation"/>">
        <table id="list" class="tableArrayPane" width="100%" cellspacing="2" cellpadding="2" border="0">
          <tr>
            <td class="ArrayColumn" align="center">&nbsp;</td>
            <td class="ArrayColumn" width="100%"><fmt:message key="mailingList.moderation.messages.title" /></td>
            <td width="10%" class="ArrayColumn" align="center" nowrap="nowrap"><a
              href='<c:url value="/Rmailinglist/${componentId}/moderationList/${componentId}">
                    <c:param name="orderBy" value="attachmentsSize" />
                    <c:param name="ascendant">
                      <c:choose>
                        <c:when test="${requestScope['attachmentsSize'] != null}">
                          <c:out value="${requestScope['attachmentsSize']}" />
                        </c:when>
                        <c:otherwise>true</c:otherwise>
                      </c:choose>
                     </c:param></c:url>' /><fmt:message
              key="mailingList.moderation.attachments.title" /></a></td>
            <td width="20%" class="ArrayColumn" align="center" nowrap="nowrap"><a
              href='<c:url value="/Rmailinglist/${componentId}/moderationList/${componentId}">
                    <c:param name="orderBy" value="sender"/>
                    <c:param name="ascendant"><c:choose><c:when test="${requestScope.sender != null}"><c:out value="${requestScope.sender}" /></c:when><c:otherwise>true</c:otherwise></c:choose></c:param></c:url>' /><fmt:message
              key="mailingList.moderation.sender.title" /></a></td>
            <td class="ArrayColumn" align="center" nowrap="nowrap"><a
              href='<c:url value="/Rmailinglist/${componentId}/moderationList/${componentId}">
                    <c:param name="orderBy" value="sentDate"/>
                    <c:param name="ascendant"><c:choose><c:when test="${requestScope.sentDate != null}"><c:out value="${requestScope.sentDate}" /></c:when><c:otherwise>true</c:otherwise></c:choose></c:param></c:url>' /><fmt:message
              key="mailingList.moderation.sentDate.title" /></a></td>
          </tr>
          <c:forEach items="${requestScope['currentMessageList']}" var="message" varStatus="messageIndex">
            <c:set var="lineClass" value="ArrayCell" />
            <c:choose>
              <c:when test="${(messageIndex.index%2) == 0}">
                <c:set var="lineClass" value="ArrayCell" />
              </c:when>
              <c:otherwise>
                <c:set var="lineClass" value="ArrayCell" />
              </c:otherwise>
            </c:choose>
            <tr>
              <td rowspan="2" valign="top"><input type="checkbox" name="message" value="<c:out value="${message.id}" />" /></td>
              <td><b><a href="<c:url value="/Rmailinglist/${componentId}/destination/moderation/message/${message.id}"/>"><c:out value="${message.title}" /></a></b></td>
              <td align="right"><c:choose>
                <c:when test="${message.attachmentsSize <= 0}">&nbsp;</c:when>
                <c:otherwise>
                  <c:out value="${message.attachmentsSizeToDisplay}" />
                  <img src="<c:url value="${attachmentIcon}" />" />
                </c:otherwise>
              </c:choose></td>
              <td rowspan="2" align="center" valign="top"><c:out value="${message.sender}" /></td>
              <td valign="top" rowspan="2" valign="top" align="right" nowrap="nowrap"><fmt:formatDate value="${message.sentDate}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
            </tr>
            <tr>
              <td valign="top" align="left" style="white-space: normal;" nowrap="nowrap"><c:out value="${message.summary}" /></td>
            </tr>
          </c:forEach>
        </table>
        </form>
        </td>
      </tr>
    </table>
    </center>
  </view:frame>
</view:window>
</body>
</html>