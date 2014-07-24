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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="gallery" tagdir="/WEB-INF/tags/silverpeas/gallery" %>

<c:set var="userLanguage" value="${requestScope.resources.language}" scope="request"/>
<jsp:useBean id="userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Constants --%>
<c:set var="TABULATION" value="&#160;&#160;&#160;&#160;&#160;"/>

<%-- Request attributes --%>
<c:set var="media" value="${requestScope.Media}" scope="request"/>
<jsp:useBean id="media" type="com.silverpeas.gallery.model.Media" scope="request"/>

<c:set var="albumPath" value="${requestScope.Path}"/>
<jsp:useBean id="albumPath" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>

<c:set var="albumListIds" value="${requestScope.PathList}"/>
<jsp:useBean id="albumListIds" type="java.util.List<java.lang.String>"/>
<c:set var="allAlbums" value="${requestScope.Albums}"/>
<jsp:useBean id="allAlbums" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>

<%-- Actions --%>
<c:set var="viewMediaAction" value="MediaView?MediaId=${media.id}"/>
<c:set var="editMediaAction" value="MediaEdit?MediaId=${media.id}"/>

<%-- Labels --%>
<fmt:message key="gallery.media" var="mediaViewLabel"/>
<fmt:message key="gallery.info" var="mediaEditLabel"/>
<fmt:message key="gallery.accessPath" var="accessLabel"/>
<fmt:message key="GML.validate" var="validateLabel"/>
<fmt:message key="GML.cancel" var="cancelLabel"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <view:looknfeel/>
  <script type="text/javascript">
    function sendData() {
      document.paths.submit();
    }
  </script>
</head>
<body>
<c:set var="additionalBrowseBarElements" value="${silfn:truncate(media.title, 50)}@${viewMediaAction}"/>
<c:set var="additionalBrowseBarElements" value="${additionalBrowseBarElements}|${accessLabel}@#"/>
<gallery:browseBar albumPath="${albumPath}" additionalElements="${additionalBrowseBarElements}"/>

<view:window>
  <view:tabs>
    <view:tab label="${mediaViewLabel}" action="${viewMediaAction}" selected="false"/>
    <view:tab label="${mediaEditLabel}" action="${editMediaAction}" selected="false"/>
    <view:tab label="${accessLabel}" action="#" selected="true"/>
  </view:tabs>
  <view:frame>
    <view:board>
      <form name="paths" action="SelectPath" method="POST">
        <input type="hidden" name="MediaId" value="${media.id}">
        <table>
          <c:forEach var="album" items="${allAlbums}">
            <c:if test="${album.level gt 1}">
              <c:set var="albumTabulation" value=""/>
              <c:if test="${album.level gt 2}">
                <c:set var="albumTabulation" value="${silfn:repeat(TABULATION, (album.level - 2))}"/>
              </c:if>
              <c:set var="checked" value="${albumListIds.contains(album.nodePK.id) ? 'checked': ''}"/>
              <tr>
                <td>
                  &#160;&#160;<input type="checkbox" name="albumChoice" value="${album.id}" ${checked}>&#160;
                </td>
                <td>${albumTabulation}${album.name}</td>
              </tr>
            </c:if>
          </c:forEach>
        </table>
      </form>
    </view:board>
    <view:buttonPane>
      <view:button label="${validateLabel}" action="javascript:onClick=sendData();"/>
      <view:button label="${cancelLabel}" action="${viewMediaAction}"/>
    </view:buttonPane>
  </view:frame>
</view:window>
</body>
</html>