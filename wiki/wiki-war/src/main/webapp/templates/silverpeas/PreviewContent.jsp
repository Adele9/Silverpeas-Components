<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.ui.EditorManager" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<%-- Inserts page content for preview. --%>
<wiki:TabbedSection>
<wiki:Tab id="previewcontent" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "preview.tab")%>'>

  <div class="information">
    <fmt:message key="preview.info"/>
    <wiki:Editor/>
  </div>

  <div class="previewcontent">
    <wiki:Translate><%=EditorManager.getEditedText(pageContext)%></wiki:Translate>
  </div>

  <div class="information">
    <fmt:message key="preview.info"/>
  </div>

</wiki:Tab>
</wiki:TabbedSection>