<%@ include file="check.jsp"%>

<%
	// r�cup�ration des param�tres :
	String 		searchKeyWord 		= (String) request.getAttribute("SearchKeyWord");
	List 		photos 				= (List) request.getAttribute("Photos");
	String 		profile 			= (String) request.getAttribute("Profile");
	int 		firstPhotoIndex 	= ((Integer) request.getAttribute("FirstPhotoIndex")).intValue();
	int 		nbPhotosPerPage 	= ((Integer) request.getAttribute("NbPhotosPerPage")).intValue();
	String 		taille 				= (String) request.getAttribute("Taille");
	Boolean 	isViewMetadata 		= (Boolean) request.getAttribute("IsViewMetadata");
	Boolean 	isViewList 			= (Boolean) request.getAttribute("IsViewList");
	Collection 	selectedIds 		= (Collection) request.getAttribute("SelectedIds");
	boolean 	isViewNotVisible 	= ((Boolean) request.getAttribute("ViewVisible")).booleanValue();
	boolean 	isBasket	 		= ((Boolean) request.getAttribute("IsBasket")).booleanValue();

	// d�claration des variables :
	int nbAffiche = 0;
	int nbParLigne = 1;
	int largeurCellule = 0;
	String extension = "";
	boolean viewMetadata = isViewMetadata.booleanValue();
	boolean viewList = isViewList.booleanValue();
	String typeAff = "1";

	// initialisation de la pagination
	Pagination pagination = gef.getPagination(photos.size(), nbPhotosPerPage, firstPhotoIndex);
	List affPhotos = photos.subList(pagination.getFirstItemIndex(), pagination.getLastItemIndex());

	// cr�ation du chemin :
	String chemin = " ";
	if (isViewNotVisible)
		chemin = resource.getString("gallery.viewNotVisible");
	else {
		chemin = "<a href=\"SearchAdvanced\">"
				+ resource.getString("gallery.searchAdvanced") + "</a>";
		chemin = chemin + " > "
				+ resource.getString("gallery.resultSearch");
		if (StringUtil.isDefined(searchKeyWord))
			chemin = chemin + " '" + searchKeyWord + "'";
	}

	// calcul du nombre de photo par ligne en fonction de la taille
	if (taille.equals("66x50")) {
		nbParLigne = 8;
		extension = "_66x50.jpg";
	} else if (taille.equals("133x100")) {
		nbParLigne = 5;
		extension = "_133x100.jpg";
		if (viewList)
			typeAff = "2";
	} else if (taille.equals("266x150")) {
		nbParLigne = 3;
		extension = "_266x150.jpg";
		if (viewList) {
			typeAff = "3";
			nbParLigne = 1;
		}
	}
	largeurCellule = 100 / nbParLigne;
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript"
	src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
	var albumWindow = window;

	function choiceGoTo(selectedIndex) 
	{
		// envoi du choix de la taille des vignettes
		if (selectedIndex != 0 && selectedIndex != 1) 
		{
			document.ChoiceSelectForm.Choice.value = document.photoForm.ChoiceSize[selectedIndex].value;
			document.ChoiceSelectForm.submit();
		}
	}
	
	function sendData() 
	{
		// envoi des photos s�lectionn�es pour la modif par lot
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		
		document.photoForm.submit();
	}
	
	function sendToBasket() 
	{
		// envoi des photos s�lectionn�es dans le panier
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		document.photoForm.action				= "BasketAddPhotos";
		document.photoForm.submit();
	}
	
	function getObjects(selected)
	{
		var  items = "";
		var boxItems = document.photoForm.SelectPhoto;
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == selected) ){
				// il n'y a qu'une checkbox non selectionn�e
				items += boxItems.value+",";
			} else{
				// search not checked boxes 
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == selected){
						items += boxItems[i].value+",";
					}
				}
			}
		}
		return items;
	}
	
	function doPagination(index)
	{
		document.photoForm.SelectedIds.value 	= getObjects(true);
		document.photoForm.NotSelectedIds.value = getObjects(false);
		document.photoForm.Index.value 			= index;
		document.photoForm.action				= "PaginationSearch";
		document.photoForm.submit();
	}
	
	function sortGoTo(selectedIndex) 
	{
		// envoi du choix du tri
		if (selectedIndex != 0 && selectedIndex != 1) 
		{
			document.OrderBySelectForm.Tri.value = document.photoForm.SortBy[selectedIndex].value;
			document.OrderBySelectForm.submit();
		}
	}

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">

<%
	// cr�ation de la barre de navigation
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(chemin);

	Button returnButton;
	if (isViewNotVisible)
		returnButton = (Button) gef.getFormButton(resource.getString("GML.back"), "Main", false);
	else
		returnButton = (Button) gef.getFormButton(resource.getString("GML.back"), "SearchAdvanced", false);

	if ("admin".equals(profile) || "publisher".equals(profile) || "writer".equals(profile)) 
	{
		// possibilit� de modifier les photos par lot
		operationPane.addOperation(resource.getIcon("gallery.updateSelectedPhoto"), resource.getString("gallery.updateSelectedPhoto"),"javascript:onClick=sendData();");
		operationPane.addOperation(resource.getIcon("gallery.allSelect"), resource.getString("gallery.allSelect"), "AllSelected");
	}
	if ("user".equals(profile) && isBasket)
	{
		// ajouter les photos s�lectionn�es au panier
		operationPane.addOperation(resource.getIcon("gallery.addToBasketSelectedPhoto"),resource.getString("gallery.addToBasketSelectedPhoto"),"javascript:onClick=sendToBasket();");
	}

	out.println(window.printBefore());
	out.println(frame.printBefore());

	// afficher les photos
	// -------------------
	// affichage des photos sous forme de vignettes	
	if (photos != null) {
%>
<br>
<%
	String vignette_url = null;
		int nbPhotos = photos.size();
		Board board = gef.getBoard();

		if (photos.size() > 0) {
			out.println(board.printBefore());
			// affichage de l'ent�te
%>
<form name="photoForm" action="EditSelectedPhoto">
<table width="98%" border="0" cellspacing="0" cellpadding="0"
	align=center>
	<input type="hidden" name="SearchKeyWord" value="<%=searchKeyWord%>">
	<input type="hidden" name="Index">
	<input type="hidden" name="SelectedIds">
	<input type="hidden" name="NotSelectedIds">
	<tr>
		<%
			int textColonne = 0;
					if (typeAff.equals("3"))
						textColonne = 1;
		%>
		<td colspan="<%=nbParLigne + textColonne%>" align="center">
		<table border="0" width="100%">
			<tr>
				<td align="center" width="100%" class=ArrayNavigation><%=pagination.printCounter()%>
				<%
					if (photos.size() == 1)
								out.println(resource.getString("gallery.photo"));
							else
								out.println(resource.getString("gallery.photos"));
				%>
				</td>
				<td align="right" nowrap><select name="ChoiceSize"
					onChange="javascript:choiceGoTo(this.selectedIndex);">
					<option selected><%=resource.getString("gallery.choixTaille")%></option>
					<option>-------------------------------</option>
					<%
						String selected = "";
								if (taille.equals("66x50"))
									selected = "selected";
					%>
					<option value="66x50" <%=selected%>><%="66x50"%></option>
					<%
						selected = "";
								if (taille.equals("133x100"))
									selected = "selected";
					%>
					<option value="133x100" <%=selected%>><%="133x100"%></option>
					<%
						selected = "";
								if (taille.equals("266x150"))
									selected = "selected";
					%>
					<option value="266x150" <%=selected%>><%="266x150"%></option>
				</select> <select name="SortBy"
					onChange="javascript:sortGoTo(this.selectedIndex);">
					<option selected><%=resource.getString("gallery.orderBy")%></option>
					<option>-------------------------------</option>
					<option value="CreationDateAsc"><%=resource.getString("gallery.dateCreatAsc")%></option>
					<option value="CreationDateDesc"><%=resource.getString("gallery.dateCreatDesc")%></option>
					<option value="Title"><%=resource.getString("GML.title")%></option>
					<option value="Size"><%=resource.getString("gallery.taille")%></option>
					<option value="Author"><%=resource.getString("GML.author")%></option>
				</select></td>
			</tr>
		</table>
		</td>
	</tr>
</table>
<%
	String photoColor = "";
			PhotoDetail photo;
			String idP;
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date today = calendar.getTime();
			Iterator it = (Iterator) affPhotos.iterator();
			while (it.hasNext()) {
				// affichage de la photo
%>
<table width="98%" border="0" cellspacing="5" cellpadding="0"
	align=center>
	<tr>
		<td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td>
	</tr>
	<tr>
		<%
			while (it.hasNext() && nbAffiche < nbParLigne) {
							photo = (PhotoDetail) it.next();
							if (photo != null) {
								idP = photo.getPhotoPK().getId();
								String nomRep = resource
										.getSetting("imagesSubDirectory")
										+ idP;
								String name = photo.getImageName();
								String type = name.substring(name
										.lastIndexOf(".") + 1, name.length());
								//name = name.substring(0,name.indexOf(".")) + extension;
								name = photo.getId() + extension;
								vignette_url = FileServer.getUrl(spaceId,
										componentId, name, photo
												.getImageMimeType(), nomRep);
								if ("bmp".equalsIgnoreCase(type))
									vignette_url = m_context
											+ "/gallery/jsp/icons/notAvailable_"
											+ resource.getLanguage()
											+ extension;

								photoColor = "fondPhoto";
								if (!photo.isVisible(today))
									photoColor = "fondPhotoNotVisible";

								nbAffiche = nbAffiche + 1;

								String altTitle = Encode
										.javaStringToHtmlString(photo
												.getTitle());
								if (photo.getDescription() != null
										&& photo.getDescription().length() > 0)
									altTitle += " : "
											+ Encode
													.javaStringToHtmlString(photo
															.getDescription());

								// on affiche encore sur la m�me ligne
		%>

		<%
			if (typeAff.equals("2")) {
		%>
		<td valign="top" width="<%=largeurCellule%>%">
		<table border="0" align="center" width="10" cellspacing="1"
			cellpadding="0" class="<%=photoColor%>">
			<tr>
				<td align="center" colspan="2">
				<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto">
					<tr>
						<td bgcolor="#FFFFFF"><a href="PreviewPhoto?PhotoId=<%=idP%>"><IMG
							SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>"
							title="<%=altTitle%>"></a></td>
					</tr>
				</table>
				</td>
			</tr>
			<tr>
				<%
					//traitement de la case � cocher
											String usedCheck = "";
											if (selectedIds != null
													&& selectedIds.contains(idP))
												usedCheck = "checked";
				%>
				<td align="center" width="10"><input type="checkbox"
					name="SelectPhoto" value="<%=idP%>" <%=usedCheck%>></td>
				<td class="txtlibform"><%=photo.getName()%></td>
			</tr>
			<%
				if (photo.getDescription() != null) {
			%>
			<tr>
				<td>&nbsp;</td>
				<td><%=photo.getDescription()%></td>
			</tr>
			<%
				}
			%>
		</table>
		</td>
		<%
			}
								if (typeAff.equals("1")) {
		%>
		<td valign="bottom" width="<%=largeurCellule%>%">
		<table border="0" width="10" align="center" cellspacing="1"
			cellpadding="0" class="<%=photoColor%>">
			<tr>
				<td align="center">
				<table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto">
					<tr>
						<td bgcolor="#FFFFFF"><a href="PreviewPhoto?PhotoId=<%=idP%>"><IMG
							SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>"
							title="<%=altTitle%>"></a></td>
					</tr>
				</table>
				</td>
			</tr>
			<%
				//traitement de la case � cocher
										String usedCheck = "";
										if (selectedIds != null
												&& selectedIds.contains(idP))
											usedCheck = "checked";
			%>
			<tr>
				<td align="center"><input type="checkbox" name="SelectPhoto"
					value="<%=idP%>" <%=usedCheck%>></td>
			</tr>
		</table>
		</td>
		<%
			}
								// affichage du texte � cot� de la photo pour le cas de l'affichage en liste
								if (typeAff.equals("3")) {
									// on affiche les photos en colonne avec les m�taData � droite
		%>
		<td valign="middle" align="center">
		<table border="0" width="10" align="center" cellspacing="1"
			cellpadding="0" class="<%=photoColor%>">
			<tr>
				<td align="center">
				<table cellspacing="1" cellpadding="5" border="0" class="cadrePhoto">
					<tr>
						<td bgcolor="#FFFFFF"><a href="PreviewPhoto?PhotoId=<%=idP%>"><IMG
							SRC="<%=vignette_url%>" border="0" alt="<%=altTitle%>"
							title="<%=altTitle%>"></a></td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
		<td valign="top" width="100%">
		<table border="0" width="100%">
			<tr>
				<td class="txtlibform" nowrap><%=resource.getString("GML.title")%>
				:</td>
				<td><%=photo.getName()%></td>
			</tr>
			<%
				if (photo.getDescription() != null) {
			%>
			<tr>
				<td class="txtlibform" nowrap><%=resource
																.getString("GML.description")%>
				:</td>
				<td><%=photo.getDescription()%></td>
			</tr>
			<%
				}
										if (photo.getAuthor() != null) {
			%>
			<tr>
				<td class="txtlibform" nowrap><%=resource
																.getString("GML.author")%>
				:</td>
				<td><%=photo.getAuthor()%></td>
			</tr>
			<%
				}
										Collection metaDataKeys = photo
												.getMetaDataProperties();
										if (viewMetadata) {
											if (metaDataKeys != null) {
												Iterator itMeta = (Iterator) metaDataKeys
														.iterator();
												while (itMeta.hasNext()) {
													// traitement de la metaData
													String property = (String) itMeta
															.next();

													MetaData metaData = photo
															.getMetaData(property);
													String mdLabel = metaData
															.getLabel();
													String mdValue = metaData
															.getValue();
													if (metaData.isDate())
														mdValue = resource
																.getOutputDateAndHour(metaData
																		.getDateValue());
													// affichage
			%>
			<tr>
				<td class="txtlibform" nowrap><%=mdLabel%> :</td>
				<td><%=mdValue%></td>
			</tr>
			<%
				}
											}
										}
										if (photo.getKeyWord() != null) {
			%>
			<tr>
				<td class="txtlibform" nowrap><%=resource
																.getString("gallery.keyWord")%>
				:</td>
				<td>
				<%
					String keyWord = photo.getKeyWord();
												// d�couper la zone keyWord en mots
												StringTokenizer st = new StringTokenizer(
														keyWord);
												// traitement des mots cl�s
												while (st.hasMoreTokens()) {
													String searchWord = (String) st
															.nextToken();
				%> <a href="SearchKeyWord?SearchKeyWord=<%=searchWord%>">
				<%=searchWord%> </a> <%
 	}
 								out.println("</td></tr>");
 							}
 							//traitement de la case � cocher
 							String usedCheck = "";
 							if (selectedIds != null
 									&& selectedIds.contains(idP))
 								usedCheck = "checked";
 %>
				
			<tr>
				<td align="left" valign="top" colspan="2"><input
					type="checkbox" name="SelectPhoto" value="<%=idP%>" <%=usedCheck%>></td>
			</tr>
		</table>
		</td>
		<%
			}
							}
						}

						// on pr�pare pour la ligne suivante
						nbAffiche = 0;
		%>
	</tr>
	<%
		}

				if (nbPhotos > nbPhotosPerPage) {
	%>
	<tr>
		<td colspan="<%=nbParLigne + textColonne%>">&nbsp;</td>
	</tr>
	<tr class=intfdcolor4>
		<td colspan="<%=nbParLigne + textColonne%>"><%=pagination.printIndex("doPagination")%>
		</td>
	</tr>
	<%
		}
	%>
</table>
</form>
<%
	out.println(board.printAfter());
		} else {
			// pas de photos
			out.println(board.printBefore());
			if (isViewNotVisible)
				out.println("<center>"+ resource.getString("gallery.pasPhoto")+ "</center>");
			else
				out.println("<center>"+ resource.getString("gallery.noPhotoSearchBegin") + searchKeyWord + " " + resource.getString("gallery.noPhotoSearchEnd")+ "</center>");
			out.println("<BR/><center>" + returnButton.print()
					+ "</center><BR/>");
			out.println(board.printAfter());
		}
	}

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="ChoiceSelectForm" action="ChoiceSize" Method="POST">
<input type="hidden" name="Choice"> <input type="hidden" name="SearchKeyWord" value="<%=searchKeyWord%>"></form>
<form name="OrderBySelectForm" action="SortBy" Method="POST">
<input type="hidden" name="Tri"> 
<input type="hidden" name="SearchKeyWord" value="<%=searchKeyWord%>"></form>

</body>
</html>