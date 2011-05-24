<%@ include file="checkCrm.jsp" %>

<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function openEditDelivery(c) {
    document.editDelivery.deliveryId.value = c;
    document.editDelivery.submit();
}

function deliveryDeleteConfirm(deliveryId, name) {
    if(window.confirm("<%=resources.getString("crm.confirmDelete")%> '" + name + "' ?")){
          document.deleteDeliverys.action.value = "DeleteDeliverys";
          document.deleteDeliverys.deliveryId.value = deliveryId;
          document.deleteDeliverys.submit();
    }
}


</script>
</head>


<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
<%

    //Les op�rations
	operationPane.addOperation(resources.getIcon("crm.newDelivery"), resources.getString("crm.newDelivery"), "NewDelivery");	

    //Les onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resources.getString("crm.projet"), myURL+"ViewProject",false);
	tabbedPane.addTab(resources.getString("crm.client"), myURL+"ViewClient",false);
	tabbedPane.addTab(resources.getString("crm.delivrable"), myURL+"ViewDelivrable",true);
	tabbedPane.addTab(resources.getString("crm.journal"), myURL+"ViewJournal",false);			

        out.println(window.printBefore());
  		out.println(tabbedPane.print());
        out.println(frame.printBefore());
%>
<center>

<%
//Recuperation des parametres
Vector deliverys = (Vector) request.getAttribute("listDeliverys");
String s1;

		  // Tableau

          ArrayPane arrayPane = gef.getArrayPane("List", "", request,session);
          arrayPane.setVisibleLineNumber(50);

          ArrayColumn arrayColumn0 = arrayPane.addArrayColumn(resources.getString("crm.attachment"));
		  arrayColumn0.setSortable(false);

          ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("crm.deliveryDate"));
		  arrayColumn1.setSortable(true);

          ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("crm.deliveryElement"));
		  arrayColumn2.setSortable(true);

          ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("crm.deliveryVersion"));
		  arrayColumn3.setSortable(true);

          ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("crm.deliveryIntervenant"));
		  arrayColumn4.setSortable(true);
		  
          ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("crm.deliveryContactName"));
		  arrayColumn5.setSortable(true);

          ArrayColumn arrayColumn6 = arrayPane.addArrayColumn(resources.getString("crm.deliveryMedia"));
		  arrayColumn6.setSortable(true);

    	  ArrayColumn arrayColumn7 = arrayPane.addArrayColumn(resources.getString("GML.operation"));
		  arrayColumn7.setSortable(false);

	if (deliverys.size()>0) 
	{
		for (int i = 0; i < deliverys.size(); i++) 
		{
			CrmDelivery crmDelivery = (CrmDelivery) deliverys.elementAt(i);
			ArrayLine arrayLine = arrayPane.addArrayLine();
        	
            Vector     attachments = crmDelivery.returnAttachments();
            StringBuffer sb = new StringBuffer();

			if (attachments.size() > 0) 
            {
                for (int j = 0; j < attachments.size(); j++)
                {
                    AttachmentDetail ad = (AttachmentDetail)(attachments.get(j));
                    sb.append("<A href=\"" + ad.getAttachmentURL() + "\" target=\"CRMWindow\">" + "<img src=\"" + resources.getIcon("crm.attachedFiles") + "\" alt=\"" + ad.getLogicalName() + "\">" + "</A>&nbsp");
                }
            }
            arrayLine.addArrayCellText(sb.toString());

            // formatage date BD -> Client		    
    		try
    		{
        		s1 = DateUtil.dateToString(DateUtil.parse(crmDelivery.getDeliveryDate()), resources.getLanguage());
    		}
    		catch (java.text.ParseException e)
    		{
        		s1 = "";
    		}
			arrayLine.addArrayCellText(s1);
			arrayLine.addArrayCellText(crmDelivery.getElement());
			arrayLine.addArrayCellText(crmDelivery.getVersion());			
			arrayLine.addArrayCellText(crmDelivery.getDeliveryName());
			arrayLine.addArrayCellText(crmDelivery.getContactName());
			arrayLine.addArrayCellText(crmDelivery.getMedia());
			
            IconPane iconPane3 = gef.getIconPane();
            Icon     updateIcon = iconPane3.addIcon();
           	updateIcon.setProperties(resources.getIcon("crm.update"), resources.getString("crm.update") + " '" + Encode.javaStringToHtmlString(crmDelivery.getDeliveryName()) + "'", "javascript:onClick=openEditDelivery('" + crmDelivery.getPK().getId() + "')");
            Icon deleteIcon = iconPane3.addIcon();
            deleteIcon.setProperties(resources.getIcon("crm.delete"), resources.getString("crm.delete") + " '" + Encode.javaStringToHtmlString(crmDelivery.getDeliveryName()) + "'", "javascript:onClick=deliveryDeleteConfirm('" + crmDelivery.getPK().getId() + "', '" + Encode.javaStringToHtmlString(Encode.javaStringToJsString(crmDelivery.getElement())) + "')");
            iconPane3.setSpacing("30px");
            arrayLine.addArrayCellIconPane(iconPane3);
		}
	}

    out.println(arrayPane.print());

%>

</center>

<form name="deleteDeliverys" action="DeleteDeliverys" method="post">
	<input type="hidden" name="deliveryId" value="">
</form>

<form name="editDelivery" action="NewDelivery" method="post">
	<input type="hidden" name="deliveryId" value="">
</form>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>
