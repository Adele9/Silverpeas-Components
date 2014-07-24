<%@ tag import="com.silverpeas.form.DataRecord" %>
<%@ tag import="com.silverpeas.form.Form" %>
<%@ tag import="com.silverpeas.form.PagesContext" %>
<%--
  Copyright (C) 2000 - 2014 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib prefix="gallery" tagdir="/WEB-INF/tags/silverpeas/gallery" %>

<c:set var="_userLanguage" value="${requestScope.resources.language}" scope="request"/>
<jsp:useBean id="_userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${_userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:set var="mandatoryIcon"><fmt:message key='gallery.mandatory' bundle='${icons}'/></c:set>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<jsp:useBean id="componentId" type="java.lang.String"/>
<c:set var="userId" value="${sessionScope.SilverSessionController.userId}"/>
<jsp:useBean id="userId" type="java.lang.String"/>

<view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>
<view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<view:setConstant var="userRole" constant="com.stratelia.webactiv.SilverpeasRole.user"/>

<fmt:message var="permalinkIcon" key='gallery.link' bundle='${icons}'/>
<c:url var="permalinkIconUrl" value="${permalinkIcon}"/>
<fmt:message var="previousIcon" key='gallery.previous' bundle='${icons}'/>
<c:url var="previousIconUrl" value="${previousIcon}"/>
<fmt:message var="nextIcon" key='gallery.next' bundle='${icons}'/>
<c:url var="nextIconUrl" value="${nextIcon}"/>
<fmt:message var="downloadIcon" key='gallery.image.download' bundle='${icons}'/>
<c:url var="downloadIconUrl" value="${downloadIcon}"/>
<fmt:message var="downloadWatermarkIcon" key='gallery.image.dowloadWatermark' bundle='${icons}'/>
<c:url var="downloadWatermarkIconUrl" value="${downloadWatermarkIcon}"/>
<fmt:message var="downloadForbiddenIcon" key='gallery.image.download.forbidden' bundle='${icons}'/>
<c:url var="downloadForbiddenIconUrl" value="${downloadForbiddenIcon}"/>

<fmt:message var="commentTab" key="gallery.comments"/>

<%-- Fragments --%>
<%@ attribute name="headerBloc" fragment="true"
              description="Fragment to put additional things into HTML HEAD tag" %>
<%@ attribute name="additionalDownloadBloc" fragment="true"
              description="Fragment to put additional things into bloc of downloads" %>
<%@ attribute name="mediaPreviewBloc" required="true" fragment="true"
              description="Fragment to put the display of the media" %>
<%@ attribute name="specificSpecificationBloc" fragment="true"
              description="Fragment to put additional things into specifications of the media" %>
<%@ attribute name="metadataBloc" fragment="true"
              description="Fragment to put additional bloc of metadata" %>

<%-- Request attributes --%>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<c:set var="media" value="${requestScope.Media}" scope="request"/>
<jsp:useBean id="media" type="com.silverpeas.gallery.model.Media" scope="request"/>
<c:set var="internalMedia" value="${media.internalMedia}"/>
<c:set var="isNewMediaCase" value="${empty media.id}" scope="request"/>
<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="mediaResourceType" value="${media.contributionType}"/>
<c:set var="mediaId" value="${media.id}"/>
<jsp:useBean id="mediaId" type="java.lang.String"/>
<c:set var="mediaUrl" value="${media.applicationOriginalUrl}" scope="request"/>

<c:set var="albumPath" value="${requestScope.Path}"/>
<jsp:useBean id="albumPath" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>
<c:set var="albumId" value="${albumPath[fn:length(albumPath)-1].nodePK.id}"/>
<jsp:useBean id="albumId" type="java.lang.String"/>

<c:set var="callback">function( event ) { if (event.type === 'listing') { commentCount = event.comments.length; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + event.comments.length + ')'); } else if (event.type === 'deletion') { commentCount--; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } else if (event.type === 'addition') { commentCount++; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } }</c:set>

<c:set var="mediaSrcValue" value="${not empty internalMedia ? internalMedia.fileName : media.streaming.homepageUrl}"/>
<c:set var="mediaTitle" value="${(not empty media.title and media.title != mediaSrcValue) ? media.title : mediaSrcValue}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <view:looknfeel/>
  <view:includePlugin name="popup"/>
  <view:includePlugin name="preview"/>
  <view:includePlugin name="wysiwyg"/>
  <view:includePlugin name="messageme"/>
  <view:includePlugin name="invitme"/>
  <view:includePlugin name="userZoom"/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>
  <script language="javascript">

    var notifyWindow = window;

    function goToNotify(url) {
      windowName = "notifyWindow";
      larg = "740";
      haut = "600";
      windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
      if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
        notifyWindow.close();
      notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
    }

    <c:if test="${requestScope.UpdateMediaAllowed}">
    function deleteConfirm(id, name) {
      if (window.confirm("<fmt:message key="gallery.confirmDeleteMedia"/> '" + name + "' ?")) {
        document.mediaForm.action = "DeleteMedia?MediaId=" + id;
        document.mediaForm.submit();
      }
    }
    </c:if>

    <c:if test="${greaterUserRole eq adminRole}">
    function clipboardCopy() {
      top.IdleFrame.location.href =
          '<c:url value="${silfn:componentURL(componentId)}"/>copy?Object=Image&Id=${mediaId}';
    }

    function clipboardCut() {
      top.IdleFrame.location.href =
          '<c:url value="${silfn:componentURL(componentId)}"/>cut?Object=Image&Id=${mediaId}';
    }
    </c:if>

    $(window).keydown(function(e) {
      var keyCode = eval(e.keyCode);
      if (37 == keyCode || keyCode == 39) {
        e.preventDefault();
        var button;
        if (37 == keyCode) {
          // Previous
          button = $('#previousButton').get(0);
        } else if (39 == keyCode) {
          // Next
          button = $('#nextButton').get(0);
        }
        if (button) {
          button.click();
        }
        return true;
      }
    });

  </script>
  <c:if test="${media.type.photo}">
    <gallery:diaporama/>
  </c:if>
  <jsp:invoke fragment="headerBloc"/>
</head>
<body class="gallery gallery-fiche-media yui-skin-sam" id="${instanceId}">

<gallery:browseBar albumPath="${albumPath}" additionalElements="${silfn:truncate(mediaTitle, 50)}@#" />

<view:operationPane>
  <fmt:message key="GML.notify" var="notifLabel"/>
  <fmt:message key="gallery.alert" var="notifIcon" bundle="${icons}"/>
  <c:url value="${notifIcon}" var="notifIcon"/>
  <view:operation altText="${notifLabel}" action="ToAlertUser?MediaId=${mediaId}" icon="${notifIcon}"/>
  <view:operationSeparator/>
  <c:if test="${requestScope.UpdateMediaAllowed}">
    <fmt:message key="GML.modify" var="modifyLabel"/>
    <fmt:message key="GML.modify" var="modifyIcon" bundle="${icons}"/>
    <c:url value="${modifyIcon}" var="modifyIcon"/>
    <fmt:message key="GML.delete" var="deleteLabel"/>
    <fmt:message key="GML.delete" var="deleteIcon" bundle="${icons}"/>
    <c:url value="${deleteIcon}" var="deleteIcon"/>
    <c:set var="tmpLabel"><c:out value="${mediaTitle}"/></c:set>
    <c:set var="deleteAction" value="javaScript:deleteConfirm('${mediaId}', '${silfn:escapeJs(tmpLabel)}')"/>
    <view:operation altText="${modifyLabel}" action="EditInformation?MediaId=${mediaId}" icon="${modifyIcon}"/>
    <view:operation altText="${deleteLabel}" action="${deleteAction}" icon="${deleteIcon}"/>
  </c:if>
  <c:if test="${greaterUserRole eq adminRole}">
    <fmt:message key="GML.copy" var="copyLabel"/>
    <fmt:message key="gallery.copy" var="copyIcon" bundle="${icons}"/>
    <c:url var="copyIcon" value="${copyIcon}"/>
    <fmt:message key="GML.cut" var="cutLabel"/>
    <fmt:message key="gallery.cut" var="cutIcon" bundle="${icons}"/>
    <c:url var="cutIcon" value="${cutIcon}"/>
    <view:operation action="javascript:onClick=clipboardCopy()" altText="${copyLabel}" icon="${copyIcon}"/>
    <view:operation action="javascript:onClick=clipboardCut()" altText="${cutLabel}" icon="${cutIcon}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${media.type.photo and requestScope.NbMedia gt 1}">
    <view:operationSeparator/>
    <fmt:message var="diapoLabel" key="gallery.diaporama"/>
    <fmt:message var="diapoIcon" key="gallery.startDiaporama" bundle="${icons}"/>
    <c:url var="diapoIcon" value="${diapoIcon}"/>
    <view:operation altText="${diapoLabel}" action="javascript:startSlideshow('${mediaId}')" icon="${diapoIcon}"/>
  </c:if>
  <c:if test="${media.type.photo and greaterUserRole eq userRole and requestScope.IsBasket}">
    <view:operationSeparator/>
    <fmt:message var="addBasketLabel" key="gallery.addMediaToBasket"/>
    <fmt:message var="addBasketIcon" key="gallery.addMediaToBasket" bundle="${icons}"/>
    <c:url var="addBasketIcon" value="${addBasketIcon}"/>
    <view:operation altText="${addBasketLabel}" action="BasketAddMedia?MediaId=${mediaId}" icon="${addBasketIcon}"/>
  </c:if>
  <c:if test="${requestScope.IsPrivateSearch}">
    <view:operationSeparator/>
    <fmt:message var="lastResultLabel" key="gallery.lastResult"/>
    <fmt:message var="lastResultIcon" key="gallery.lastResult" bundle="${icons}"/>
    <c:url var="lastResultIcon" value="${lastResultIcon}"/>
    <view:operation altText="${lastResultLabel}" action="LastResult" icon="${lastResultIcon}"/>
  </c:if>
</view:operationPane>

<view:window>

  <view:tabs>
    <fmt:message key="gallery.media" var="mediaViewLabel"/>
    <view:tab label="${mediaViewLabel}" action="#" selected="true"/>
    <c:if test="${requestScope.UpdateMediaAllowed}">
      <fmt:message key="gallery.info" var="mediaEditLabel"/>
      <view:tab label="${mediaEditLabel}" action="EditInformation?MediaId=${mediaId}" selected="false"/>
      <fmt:message key="gallery.accessPath" var="accessLabel"/>
      <view:tab label="${accessLabel}" action="AccessPath?MediaId=${mediaId}" selected="false"/>
    </c:if>
  </view:tabs>

  <view:frame>
    <form name="mediaForm" method="post" accept-charset="UTF-8" action="#">

      <div class="rightContent">
        <div class="fileName">
          <c:if test="${not empty internalMedia}">
            <c:choose>
              <c:when test="${requestScope.ViewLinkDownload or media.downloadable}">
                <a href="${mediaUrl}" target="_blank"><c:out value="${media.name}"/>
                  <img src="${downloadIconUrl}" alt="<fmt:message key='gallery.download.media'/>" title="<fmt:message key='gallery.original'/>"/>
                </a>
                <jsp:invoke fragment="additionalDownloadBloc"/>
              </c:when>
              <c:otherwise>
                <c:out value="${internalMedia.fileName}"/>
                <img src="${downloadForbiddenIconUrl}" alt="<fmt:message key='gallery.download.forbidden'/>" title="<fmt:message key='gallery.download.forbidden'/>" class="forbidden-download-file"/>
              </c:otherwise>
            </c:choose>
          </c:if>
        </div>
        <div class="fileCharacteristic bgDegradeGris">
          <p>
            <c:if test="${not empty internalMedia}">
              <span class="fileCharacteristicWeight"><fmt:message key="gallery.weight"/> <b>${silfn:formatMemSize(internalMedia.fileSize)}</b></span>
            </c:if>
            <jsp:invoke fragment="specificSpecificationBloc"/>
            <br class="clear"/>
          </p>
        </div>

        <jsp:invoke fragment="metadataBloc"/>

        <c:if test="${media.visibilityPeriod.defined or (not empty internalMedia and internalMedia.downloadable and internalMedia.downloadPeriod.defined)}">
          <div class="periode bgDegradeGris" id="periode">
            <div class="header bgDegradeGris">
              <h4 class="clean"><fmt:message key="GML.period"/></h4>
            </div>
            <c:if test="${media.visibilityPeriod.defined}">
              <div class="periode_visibility paragraphe">
                <c:if test="${media.visibilityPeriod.beginDatable.defined}">
                  <fmt:message key="gallery.beginDate">
                    <fmt:param value="${media.visibilityPeriod.endDatable.defined ? 1 : 2}"/>
                  </fmt:message>
                  <b><view:formatDate value="${media.visibilityPeriod.beginDate}" language="${_userLanguage}"/></b>
                </c:if>
                <c:if test="${media.visibilityPeriod.endDatable.defined}">
                  <fmt:message key="gallery.endDate">
                    <fmt:param value="${media.visibilityPeriod.beginDatable.defined ? 1 : 2}"/>
                  </fmt:message>
                  <b><view:formatDate value="${media.visibilityPeriod.endDate}" language="${_userLanguage}"/></b>
                </c:if>
              </div>
            </c:if>
            <c:if test="${not empty internalMedia and internalMedia.downloadPeriod.defined}">
              <div class="periode_download paragraphe">
                <c:if test="${internalMedia.downloadPeriod.beginDatable.defined}">
                  <fmt:message key="gallery.beginDownloadDate">
                    <fmt:param value="${internalMedia.downloadPeriod.endDatable.defined ? 1 : 2}"/>
                  </fmt:message>
                  <b><view:formatDate value="${internalMedia.downloadPeriod.beginDate}" language="${_userLanguage}"/></b>
                </c:if>
                <c:if test="${internalMedia.downloadPeriod.endDatable.defined}">
                  <fmt:message key="gallery.endDate">
                    <fmt:param value="${internalMedia.downloadPeriod.beginDatable.defined ? 1 : 2}"/>
                  </fmt:message>
                  <b><view:formatDate value="${internalMedia.downloadPeriod.endDate}" language="${_userLanguage}"/></b>
                </c:if>

              </div>
            </c:if>
            <br class="clear"/>
          </div>
        </c:if>

        <fmt:message key="gallery.CopyMediaLink" var="cpMediaLinkAlt"/>
        <viewTags:displayLastUserCRUD createDate="${media.creationDate}"
                                      createdBy="${media.creator}"
                                      updateDate="${media.lastUpdateDate}"
                                      updatedBy="${media.lastUpdater}"
                                      permalink="${media.permalink}"
                                      permalinkHelp="${cpMediaLinkAlt}"
                                      permalinkIconUrl="${permalinkIconUrl}">
          <jsp:attribute name="beforeCommonContentBloc">
          <c:if test="${not empty media.author}">
            <p id="authorInfo"><b><fmt:message key="GML.author"/></b> ${media.author}</p>
          </c:if>
          </jsp:attribute>
        </viewTags:displayLastUserCRUD>

        <c:if test="${requestScope.IsUsePdc}">
          <view:pdcClassificationPreview componentId="${instanceId}" contentId="${mediaId}"/>
        </c:if>

      </div>
      <div class="principalContent">
        <div id="pagination">
          <c:if test="${requestScope.Rang ne 0}">
            <fmt:message var="previousMedia" key="gallery.previous"/>
            <a id="previousButton" href="PreviousMedia">
              <img alt="${previousMedia}" title="${previousMedia}" src="${previousIconUrl}"/>
            </a>
          </c:if>
          <span class="txtnav"><span class="currentPage">${requestScope.Rang + 1}</span> / ${requestScope.NbMedia}</span>
          <c:if test="${requestScope.Rang ne (requestScope.NbMedia - 1)}">
            <fmt:message var="nextMedia" key="gallery.next"/>
            <a id="nextButton" href="NextMedia"><img alt="${nextMedia}" title="${nextMedia}" src="${nextIconUrl}"/></a>
          </c:if>
        </div>
        <div class="contentMedia">
          <div class="${media.visible ? 'fondPhoto' : 'fondPhotoNotVisible'}">
            <div class="cadrePhoto">
              <jsp:invoke fragment="mediaPreviewBloc"/>
            </div>
            <c:if test="${not empty mediaTitle}">
            <h2 class="mediaTitle">${mediaTitle}</h2>
            </c:if>
            <c:if test="${not empty media.keyWord}">
              <div class="motsClefs">
                <c:set var="listKeys" value="${fn:split(media.keyWord,' ')}"/>
                <c:forEach items="${listKeys}" var="keyword">
                  <span><a href="SearchKeyWord?SearchKeyWord=${keyword}">${keyword}</a></span>
                </c:forEach>
              </div>
            </c:if>
            <c:if test="${not empty media.description}">
              <p class="description">${media.description}</p>
            </c:if>
          </div>
        </div>

        <%
          Form xmlForm = (Form) request.getAttribute("XMLForm");
          DataRecord xmlData = (DataRecord) request.getAttribute("XMLData");
          if (xmlForm != null) {
        %>
        <br/>
        <%
          PagesContext xmlContext =
              new PagesContext("myForm", "0", _userLanguage, false, componentId, userId, albumId);
          xmlContext.setObjectId(mediaId);
          xmlContext.setBorderPrinted(false);
          xmlContext.setIgnoreDefaultValues(true);

          xmlForm.display(out, xmlContext, xmlData);
        %>
        <% } %>
      </div>

    </form>
    <c:if test="${requestScope.ShowCommentsTab}">
      <div class="principalContent">
        <view:comments userId="${userId}" componentId="${instanceId}"
                       resourceType="${mediaResourceType}" resourceId="${mediaId}" indexed="${callback}"/>
      </div>
    </c:if>
  </view:frame>
</view:window>
</body>
</html>