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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:setConstant var="MediaTypePhoto" constant="com.silverpeas.gallery.constant.MediaType.Photo"/>
<view:setConstant var="MediaTypeVideo" constant="com.silverpeas.gallery.constant.MediaType.Video"/>

<c:set var="media" value="${requestScope.Media}"/>


<c:choose>
  <c:when test="${media.type eq MediaTypePhoto}">
    <jsp:include page="mediaPhotoEdit.jsp" />
  </c:when>
  <c:when test="${media.type eq MediaTypeVideo}">
    <jsp:include page="mediaVideoEdit.jsp" />
  </c:when>
  <c:otherwise>
    <%-- Nothing --%>
  </c:otherwise>
</c:choose>