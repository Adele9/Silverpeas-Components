<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<view:setConstant var="mediaType" constant="com.silverpeas.gallery.constant.MediaType.Photo"/>
<view:setConstant var="supportedMediaMimeTypes" constant="com.silverpeas.gallery.constant.MediaMimeType.PHOTOS"/>

<%-- Set resource bundle --%>
<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="mandatoryIcon"><fmt:message key='gallery.mandatory' bundle='${icons}'/></c:set>
<c:set var="photo" value="${requestScope.Media}"/>
<jsp:useBean id="photo" type="com.silverpeas.gallery.model.Photo"/>
<c:set var="isNewMediaCase" value="${empty photo.id}"/>
<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>

<c:set var="action" value="CreateMedia"/>
<c:set var="bodyCss" value="createMedia"/>
<c:if test="${not isNewMediaCase}">
  <c:set var="action" value="UpdateInformation"/>
  <c:set var="bodyCss" value="editMedia"/>
</c:if>
<c:set var="albumPath" value="${requestScope.Path}"/>
<jsp:useBean id="albumPath" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>
<c:set var="albumId" value="${albumPath[fn:length(albumPath)-1].id}"/>

<%
  // récupération des paramètres :
  Photo curPhoto = (Photo) request.getAttribute("Media");

  boolean viewMetadata = ((Boolean) request.getAttribute("IsViewMetadata")).booleanValue();

  // paramètres pour le formulaire
  Form formUpdate = (Form) request.getAttribute("Form");
  DataRecord data = (DataRecord) request.getAttribute("Data");

  // déclaration des variables :
  String vignette_url = null;
  String preview_url = null;
  Collection<String> metaDataKeys = null;

  PagesContext context =
      new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, null);
  context.setBorderPrinted(false);
  context.setCurrentFieldIndex("11");
  context.setIgnoreDefaultValues(true);

  if (curPhoto != null) {
    vignette_url = curPhoto.getApplicationThumbnailUrl(MediaResolution.MEDIUM);
    preview_url = curPhoto.getApplicationThumbnailUrl(MediaResolution.PREVIEW);

    if (viewMetadata) {
      metaDataKeys = curPhoto.getMetaDataProperties();
    }
  }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <view:looknfeel/>
  <link type="text/css" href="<c:url value="/util/styleSheets/fieldset.css"/>" rel="stylesheet"/>
  <%
    if (formUpdate != null) {
      formUpdate.displayScripts(out, context);
    }
  %>
  <view:includePlugin name="datepicker"/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>

  <script type="text/javascript">

    /***********************************************
     * Image w/ description tooltip- By Dynamic Web Coding (www.dyn-web.com)
     * Copyright 2002-2007 by Sharon Paine
     * Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
     ***********************************************/

    /* IMPORTANT: Put script after tooltip div or
     put tooltip div just before </BODY>. */

    var dom = (document.getElementById) ? true : false;
    var ns5 = (!document.all && dom || window.opera) ? true : false;
    var ie5 = ((navigator.userAgent.indexOf("MSIE") > -1) && dom) ? true : false;
    var ie4 = (document.all && !dom) ? true : false;
    var nodyn = (!ns5 && !ie4 && !ie5 && !dom) ? true : false;

    var origWidth, origHeight;

    // avoid error of passing event object in older browsers
    if (nodyn) {
      event = "nope"
    }

    ///////////////////////  CUSTOMIZE HERE   ////////////////////
    // settings for tooltip
    // Do you want tip to move when mouse moves over link?
    var tipFollowMouse = false;
    // Be sure to set tipWidth wide enough for widest image
    var tipWidth = 400;
    var offX = 20;	// how far from mouse to show tip
    var offY = 12;
    var tipFontFamily = "Verdana, arial, helvetica, sans-serif";
    var tipFontSize = "8pt";
    // set default text color and background color for tooltip here
    // individual tooltips can have their own (set in messages arrays)
    // but don't have to
    var tipFontColor = "#000000";
    var tipBgColor = "#DDECFF";
    var tipBorderColor = "#000000";
    var tipBorderWidth = 1;
    var tipBorderStyle = "solid";
    var tipPadding = 0;

    // tooltip content goes here (image, description, optional bgColor, optional textcolor)
    var messages = new Array();
    // multi-dimensional arrays containing:
    // image and text for tooltip
    // optional: bgColor and color to be sent to tooltip
    <%
    if (curPhoto != null) {
    %>
    messages[0] = new Array('<%=preview_url%>', '<%=EncodeHelper.javaStringToJsString(curPhoto.
    getName())%>', "#FFFFFF");

    <% } %>

    ////////////////////  END OF CUSTOMIZATION AREA  ///////////////////

    // preload images that are to appear in tooltip
    // from arrays above
    if (document.images) {
      var theImgs = new Array();
      for (var i = 0; i < messages.length; i++) {
        theImgs[i] = new Image();
        theImgs[i].src = messages[i][0];
      }
    }

    // to layout image and text, 2-row table, image centered in top cell
    // these go in var tip in doTooltip function
    // startStr goes before image, midStr goes between image and text
    var startStr = '<table><tr><td align="center" width="100%"><img src="';
    var midStr = '" border="0"></td></tr><tr><td valign="top" align="center">';
    var endStr = '</td></tr></table>';

    ////////////////////////////////////////////////////////////
    //  initTip	- initialization for tooltip.
    //		Global variables for tooltip.
    //		Set styles
    //		Set up mousemove capture if tipFollowMouse set true.
    ////////////////////////////////////////////////////////////
    var tooltip, tipcss;
    function initTip() {
      if (nodyn) return;
      tooltip =
          (ie4) ? document.all['tipDiv'] : (ie5 || ns5) ? document.getElementById('tipDiv') : null;
      tipcss = tooltip.style;
      if (ie4 || ie5 || ns5) {	// ns4 would lose all this on rewrites
        //tipcss.width = tipWidth+"px";
        tipcss.fontFamily = tipFontFamily;
        tipcss.fontSize = tipFontSize;
        tipcss.color = tipFontColor;
        tipcss.backgroundColor = tipBgColor;
        tipcss.borderColor = tipBorderColor;
        tipcss.borderWidth = tipBorderWidth + "px";
        tipcss.padding = tipPadding + "px";
        tipcss.borderStyle = tipBorderStyle;
      }
      if (tooltip && tipFollowMouse) {
        document.onmousemove = trackMouse;
      }
    }

    window.onload = initTip;

    /////////////////////////////////////////////////
    //  doTooltip function
    //			Assembles content for tooltip and writes
    //			it to tipDiv
    /////////////////////////////////////////////////
    var t1, t2;	// for setTimeouts
    var tipOn = false;	// check if over tooltip link
    function doTooltip(evt, num) {
      if (!tooltip) return;
      if (t1) clearTimeout(t1);
      if (t2) clearTimeout(t2);
      tipOn = true;
      // set colors if included in messages array
      if (messages[num][2])  var curBgColor = messages[num][2]; else curBgColor = tipBgColor;
      if (messages[num][3])  var curFontColor = messages[num][3]; else curFontColor = tipFontColor;
      if (ie4 || ie5 || ns5) {
        var tip = startStr + messages[num][0] + midStr + '<span style="font-family:' +
            tipFontFamily + '; font-size:' + tipFontSize + '; color:' + curFontColor + ';">' +
            messages[num][1] + '</span>' + endStr;
        tipcss.backgroundColor = curBgColor;
        tooltip.innerHTML = tip;
      }
      if (!tipFollowMouse) positionTip(evt); else t1 =
          setTimeout("tipcss.visibility='visible'", 100);
    }

    var mouseX, mouseY;
    function trackMouse(evt) {
      standardbody =
          (document.compatMode == "CSS1Compat") ? document.documentElement : document.body //create reference to common "body" across doctypes
      mouseX = (ns5) ? evt.pageX : window.event.clientX + standardbody.scrollLeft;
      mouseY = (ns5) ? evt.pageY : window.event.clientY + standardbody.scrollTop;
      if (tipOn) positionTip(evt);
    }

    /////////////////////////////////////////////////////////////
    //  positionTip function
    //		If tipFollowMouse set false, so trackMouse function
    //		not being used, get position of mouseover event.
    //		Calculations use mouseover event position,
    //		offset amounts and tooltip width to position
    //		tooltip within window.
    /////////////////////////////////////////////////////////////
    function positionTip(evt) {
      if (!tipFollowMouse) {
        standardbody =
            (document.compatMode == "CSS1Compat") ? document.documentElement : document.body
        mouseX = (ns5) ? evt.pageX : window.event.clientX + standardbody.scrollLeft;
        mouseY = (ns5) ? evt.pageY : window.event.clientY + standardbody.scrollTop;
      }
      // tooltip width and height
      var tpWd = (ie4 || ie5) ? tooltip.clientWidth : tooltip.offsetWidth;
      var tpHt = (ie4 || ie5) ? tooltip.clientHeight : tooltip.offsetHeight;
      // document area in view (subtract scrollbar width for ns)
      var winWd = (ns5) ? window.innerWidth - 20 + window.pageXOffset :
          standardbody.clientWidth + standardbody.scrollLeft;
      var winHt = (ns5) ? window.innerHeight - 20 + window.pageYOffset :
          standardbody.clientHeight + standardbody.scrollTop;
      // check mouse position against tip and window dimensions
      // and position the tooltip
      if ((mouseX + offX + tpWd) > winWd) {
        tipcss.left = mouseX - (tpWd + offX) + "px";
      } else {
        tipcss.left = mouseX + offX + "px";
      }
      if ((mouseY + offY + tpHt) > winHt) {
        tipcss.top = winHt - (tpHt + offY) + "px";
      } else {
        tipcss.top = mouseY + offY + "px";
      }
      if (!tipFollowMouse) {
        t1 = setTimeout("tipcss.visibility='visible'", 100);
      }
    }

    function hideTip() {
      if (!tooltip) return;
      t2 = setTimeout("tipcss.visibility='hidden'", 100);
      tipOn = false;
    }
  </script>

</head>

<body class="gallery ${bodyCss} yui-skin-sam" id="${instanceId}">
<gallery:browseBar albumPath="${albumPath}"/>

<view:window>
  <c:if test="${not isNewMediaCase}">
    <view:tabs>
      <fmt:message key="gallery.media" var="mediaViewLabel"/>
      <view:tab label="${mediaViewLabel}" action="MediaView?MediaId=${photo.id}" selected="false"/>
      <fmt:message key="gallery.info" var="mediaEditLabel"/>
      <view:tab label="${mediaEditLabel}" action="#" selected="true"/>
      <fmt:message key="gallery.accessPath" var="accessLabel"/>
      <view:tab label="${accessLabel}" action="AccessPath?MediaId=${photo.id}" selected="false"/>
    </view:tabs>
  </c:if>

  <view:frame>
    <form name="mediaForm" action="${action}" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
      <input type="hidden" name="MediaId" value="${photo.id}"/>
      <input type="hidden" name="Positions"/>
      <input type="hidden" name="type" value="Photo"/>

      <table cellpadding="5" width="100%">
        <tr>
          <td valign="top">
            <%if (curPhoto != null) { %>
            <%
              if (vignette_url != null) {
                if (!curPhoto.isPreviewable()) {
                  vignette_url =
                      m_context + "/gallery/jsp/icons/notAvailable_" + resource.getLanguage() +
                          "_266x150.jpg";
                }
            %>
            <center>
              <a href="#" onmouseover="doTooltip(event,0)" onmouseout="hideTip()"><img src="<%=vignette_url%>" border="0"/></a>
            </center>
            <%
              // AFFICHAGE des métadonnées
              if (metaDataKeys != null && !metaDataKeys.isEmpty()) {
            %>
            <div class="metadata bgDegradeGris" id="metadata">
              <div class="header bgDegradeGris">
                <h4 class="clean"><fmt:message key="GML.metadata"/></h4>
              </div>
              <div id="metadata_list">
                <%
                  MetaData metaData;
                  for (final String propertyLong : metaDataKeys) {
                    metaData = curPhoto.getMetaData(propertyLong);
                    String mdLabel = metaData.getLabel();
                    String mdValue = metaData.getValue();
                    if (metaData.isDate()) {
                      mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
                    }
                %>
                <p id="metadata_<%=mdLabel%>"><%=mdLabel%> <b><%=mdValue%>
                </b></p>
                <%
                  }
                %>
              </div>
            </div>
            <%
                  }
                }
              } %>
          </td>
          <td>

            <gallery:editMedia media="${photo}"
                               mediaType="${mediaType}"
                               supportedMediaMimeTypes="${supportedMediaMimeTypes}"
                               formUpdate="<%=formUpdate%>"
                               isUsePdc="${requestScope.IsUsePdc}"/>

            <c:if test="${requestScope.IsUsePdc}">
              <%-- Display PDC form --%>
              <c:choose>
                <c:when test="${not isNewMediaCase}">
                  <view:pdcClassification componentId="${instanceId}" contentId="${photo.id}" editable="true"/>
                </c:when>
                <c:otherwise>
                  <view:pdcNewContentClassification componentId="${instanceId}"/>
                </c:otherwise>
              </c:choose>
            </c:if>

            <br/>
            <% if (formUpdate != null) { %>
              <%-- Display XML form --%>
            <fieldset id="formInfo" class="skinFieldset">
              <legend><fmt:message key="GML.bloc.further.information"/></legend>
              <%
                formUpdate.display(out, context, data);
              %>
            </fieldset>
            <% } %>
          </td>
        </tr>
      </table>
    </form>
    <fmt:message key="GML.validate" var="validateLabel"/>
    <fmt:message key="GML.cancel" var="cancelLabel"/>
    <view:buttonPane>
      <view:button action="javascript:onClick=sendData();" label="${validateLabel}"/>
      <c:choose>
        <c:when test="${not isNewMediaCase}">
          <view:button action="MediaView?MediaId=${photo.id}" label="${cancelLabel}"/>
        </c:when>
        <c:otherwise>
          <view:button action="GoToCurrentAlbum" label="${cancelLabel}"/>
        </c:otherwise>
      </c:choose>
    </view:buttonPane>

  </view:frame>
</view:window>
<div id="tipDiv" style="position:absolute; visibility:hidden; z-index:100000"></div>
</body>
</html>