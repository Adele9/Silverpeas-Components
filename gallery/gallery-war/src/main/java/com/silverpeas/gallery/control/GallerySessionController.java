/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.control;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.DataRecord;
import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.MediaServiceFactory;
import com.silverpeas.gallery.delegate.GalleryPasteDelegate;
import com.silverpeas.gallery.delegate.MediaDataCreateDelegate;
import com.silverpeas.gallery.delegate.MediaDataUpdateDelegate;
import com.silverpeas.gallery.model.*;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardException;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeSelection;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.search.indexEngine.model.FieldDescription;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.silverpeas.gallery.model.MediaCriteria.QUERY_ORDER_BY.*;

public final class GallerySessionController extends AbstractComponentSessionController {
  // déclaration des variables

  private String currentAlbumId = "0";
  private AlbumDetail currentAlbum = getAlbum(currentAlbumId);
  private int rang = 0;
  private MediaResolution displayedMediaResolution = MediaResolution.SMALL;
  private String tri = "CreationDateAsc";
  private String currentOrderId = "0";
  private List<String> listSelected = new ArrayList<String>();
  private boolean isViewNotVisible = false;
  // gestion de la recherche par mot clé
  private String searchKeyWord = "";
  private List<Media> searchResultListMedia = new ArrayList<Media>();
  private List<Media> restrictedListMedia = new ArrayList<Media>();
  private boolean isSearchResult = false;
  private QueryDescription query = new QueryDescription();
  private SearchContext pdcSearchContext;
  private DataRecord xmlSearchContext;
  // pour tout selectionner / déselectionner
  private boolean select = false;
  private ResourceLocator metadataResources = null;
  private CommentService commentService = null;
  // pagination de la liste des résultats (PDC via DomainsBar)
  private int indexOfCurrentPage = 0;
  // panier en cours
  private List<String> basket = new ArrayList<String>(); // liste des photos mise dans le panier
  static final Properties defaultSettings = new Properties();

  static {
    try {
      FileUtil.loadProperties(defaultSettings,
          "org/silverpeas/gallery/settings/metadataSettings.properties");
    } catch (IOException e) {
      SilverTrace.fatal("gallery", "GallerySessionController()", "root.EX_CANT_GET_REMOTE_OBJECT",
          e);
    }
  }
  Properties settings = new Properties(defaultSettings);

  /**
   * Gets the business service of operations on comments
   *
   * @return a DefaultCommentService instance.
   */
  private CommentService getCommentService() {
    if (commentService == null) {
      commentService = CommentServiceFactory.getFactory().getCommentService();
    }
    return commentService;
  }

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public GallerySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "com.silverpeas.gallery.multilang.galleryBundle",
        "com.silverpeas.gallery.settings.galleryIcons",
        "com.silverpeas.gallery.settings.gallerySettings");

    // affectation du formulaire à la médiathèque
    String xmlFormName = getXMLFormName();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1,
          xmlFormName.indexOf('.'));
      try {
        getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ':'
            + xmlFormShortName, xmlFormName);
      } catch (PublicationTemplateException e) {
        SilverTrace.info("gallery", "GallerySessionController()",
            "root.EX_CANT_GET_REMOTE_OBJECT", "xmlFormName = " + getXMLFormName(), e);
      }
    }

    // affectation du formulaire associé aux demandes de médias
    String xmlOrderFormName = getOrderForm();
    if (StringUtil.isDefined(xmlOrderFormName)) {
      String xmlOrderFormShortName = xmlOrderFormName.substring(xmlOrderFormName.indexOf('/') + 1,
          xmlOrderFormName.indexOf('.'));
      try {
        getPublicationTemplateManager().addDynamicPublicationTemplate(getComponentId() + ':'
            + xmlOrderFormShortName, xmlOrderFormName);
      } catch (PublicationTemplateException e) {
        throw new GalleryRuntimeException("GallerySessionController.super()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }

  public List<Media> getLastRegisteredMedia() {
    try {
      return getMediaService().getLastRegisteredMedia(getComponentId());
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getLastRegistredMedia()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public List<Comment> getAllComments(Media media) {
    return getCommentService()
        .getAllCommentsOnPublication(media.getContributionType(), media.getMediaPK());
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      silverObjectId = getMediaService()
          .getSilverObjectId(new MediaPK(objectId, getSpaceId(), getComponentId()));
    } catch (Exception e) {
      SilverTrace.error("gallery", "GallerySessionController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }

  public AlbumDetail goToAlbum() {
    return currentAlbum;
  }

  public AlbumDetail goToAlbum(String albumId) {
    setCurrentAlbumId(albumId);
    return currentAlbum;
  }

  public AlbumDetail loadCurrentAlbum() {
    return goToAlbum(currentAlbumId);
  }

  public AlbumDetail getAlbum(String albumId) {
    NodePK nodePK = new NodePK(albumId, getComponentId());
    AlbumDetail album;
    try {
      album = getMediaService().getAlbum(nodePK);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return album;
  }

  public AlbumDetail getAlbumLight(String albumId) {
    NodePK nodePK = new NodePK(albumId, getComponentId());
    AlbumDetail album;
    try {
      album = getMediaService().getAlbum(nodePK);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbumLight()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return album;
  }

  public Collection<Media> getNotVisible() {
    Collection<Media> media;
    try {
      media = getMediaService().getNotVisible(getComponentId());
      setRestrictedListMedia(media);
      return media;
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getNotVisible()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public Collection<AlbumDetail> getAllAlbums() {
    try {
      return getMediaService().getAllAlbums(getComponentId());
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.getAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public void setMediaToAlbums(String mediaId, String[] albums) {
    if (albums != null) {
      Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      if (media != null) {
        SilverTrace.debug("gallery", "GallerySessionController.setMediaToAlbums()",
            "root.MSG_GEN_PARAM_VALUE", "mediaId = " + mediaId);
        media.setToAlbums(albums);
      }
    }
  }

  public void addMediaToAlbums(String mediaId, String[] albums) {
    if (albums != null) {
      Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
      if (media != null) {
        SilverTrace.debug("gallery", "GallerySessionController.addMediaToAlbums()",
            "root.MSG_GEN_PARAM_VALUE", "mediaId = " + mediaId);
        media.addToAlbums(albums);
      }
    }
  }

  public void createAlbum(AlbumDetail album) {
    album.setCreationDate(DateUtil.date2SQLDate(new Date()));
    album.setCreatorId(getUserId());
    album.getNodePK().setComponentName(getComponentId());

    getMediaService().createAlbum(album, new NodePK(currentAlbumId, getComponentId()));

    // Reloading the album.
    loadCurrentAlbum();
  }

  public void updateAlbum(AlbumDetail album) {
    getMediaService().updateAlbum(album);

    // Reloading the album.
    loadCurrentAlbum();
  }

  public void deleteAlbum(String albumId) {
    try {
      final String parentId = getAlbumLight(albumId).getFatherPK().getId();
      getMediaService()
          .deleteAlbum(getUserDetail(), getComponentId(), getAlbum(albumId).getNodePK());
      goToAlbum(parentId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GGallerySessionController.deleteAlbum()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public ResourceLocator getMetadataResources() {
    if (metadataResources == null) {
      metadataResources = new ResourceLocator("com.silverpeas.gallery.multilang.metadataBundle",
          getLanguage());
    }
    return metadataResources;
  }

  public Media getMedia(String mediaId) {
    Media media = getMediaById(mediaId);

    // Getting current album identifiers with which the media is linked.
    Collection<String> albumIds = getMediaService().getAlbumIdsOf(media);

    SilverTrace.info("gallery", "GallerySessionController.getMedia", "root.MSG_GEN_PARAM_VALUE",
        "albumIds = " + albumIds + ", currentAlbumId =  " + getCurrentAlbumId());

    // regarder si l'album courant est dans la liste des albums
    boolean inAlbum = albumIds.contains(currentAlbumId);
    String firstAlbumId = albumIds.isEmpty() ? "" : albumIds.iterator().next();
    if (!inAlbum) {
      setCurrentAlbumId(firstAlbumId);
    }

    SilverTrace.info("gallery", "GallerySessionController.getMedia", "root.MSG_GEN_PARAM_VALUE",
        "currentAlbumId fin = " + currentAlbumId);

    // Updating the rank of the media.
    List<Media> mediaOfCurrentAlbum = currentAlbum.getMedia();
    rang = mediaOfCurrentAlbum.indexOf(media);

    return media;
  }

  public Media getPrevious() {
    return getPreviousOrNext(true);
  }

  public Media getNext() {
    return getPreviousOrNext(false);
  }

  private Media getPreviousOrNext(boolean isPreviousRequired) {
    String type = isPreviousRequired ? "Previous" : "Next";
    int offset = isPreviousRequired ? -1 : 1;
    Media media;
    try {

      // Finding the rank of the media
      int newRang = rang + offset;
      media = currentAlbum.getMedia().get(newRang);

      // Updating the rank of the current media.
      rang = newRang;

    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.get" + type + "()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return media;
  }

  /**
   * Creating one media (just only one)
   *
   * @param delegate
   * @return
   */
  public synchronized String createMedia(final MediaDataCreateDelegate delegate) {
    try {

      // Persisting data
      Media createdMedia = getMediaService()
          .createMedia(getUserDetail(), getComponentId(), isMakeWatermark(), getWatermarkHD(),
              getWatermarkOther(), delegate);

      // recharger l'album courant pour prendre en comptes le nouveau média
      goToAlbum(currentAlbumId);

      return createdMedia.getId();
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.createMedia()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Updating one media (just only one)
   *
   * @param mediaId
   * @param delegate
   * @throws Exception
   */
  public void updateMediaByUser(final String mediaId, final MediaDataUpdateDelegate delegate)
  throws Exception {
    // Persisting data
    getMediaService()
        .updateMedia(getUserDetail(), getComponentId(), getMedia(mediaId), isMakeWatermark(),
            getWatermarkHD(), getWatermarkOther(), delegate);
  }

  /**
   * Updating several media (no file have to be handled)
   *
   * @param mediaIds
   * @param delegate
   * @throws Exception
   */
  public void updateMediaByUser(final Collection<String> mediaIds,
      final MediaDataUpdateDelegate delegate) throws Exception {
    if (mediaIds != null) {
      // Persisting data
      getMediaService()
          .updateMedia(getUserDetail(), getComponentId(), mediaIds, getCurrentAlbumId(), delegate);
    }
  }

  public void updateMedia(final Media media) {
    try {
      getMediaService()
          .updateMedia(getUserDetail(), getComponentId(), media, isMakeWatermark(), getWatermarkHD(), getWatermarkOther(), null);

      // Reloading the current album.
      loadCurrentAlbum();

    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.updateMedia()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ADD_METADATA", e);
    }
  }

  public void deleteMedia(String mediaId) {
    deleteMedia(Collections.singletonList(mediaId));
  }

  public void deleteMedia(final Collection<String> mediaIds) {
    try {

      // Deleting the media.
      getMediaService().deleteMedia(getUserDetail(), getComponentId(), mediaIds);

    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.deleteMedia()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    // Reloading the album to take into account the deletion.
    loadCurrentAlbum();
  }

  private void sortMedia() {
    sort(currentAlbum.getMedia());
  }

  private void sortMediaSearch() {
    sort(getSearchResultListMedia());
    sort(getRestrictedListMedia());
  }

  private void sort(final List<Media> media) {
    final Comparator<Media> comparator;
    if (tri.equals("CreationDateAsc")) {
      comparator = MediaLogicalComparator.on(CREATE_DATE_ASC, IDENTIFIER_ASC);
    } else if (tri.equals("CreationDateDesc")) {
      comparator = MediaLogicalComparator.on(CREATE_DATE_DESC, IDENTIFIER_ASC);
    } else if (tri.equals("Size")) {
      comparator = MediaLogicalComparator.on(SIZE_ASC, CREATE_DATE_ASC, IDENTIFIER_ASC);
    } else if (tri.equals("Resolution")) {
      comparator = MediaLogicalComparator.on(DIMENSION_ASC, CREATE_DATE_ASC, IDENTIFIER_ASC);
    } else if (tri.equals("Title")) {
      comparator = MediaLogicalComparator.on(TITLE_ASC, CREATE_DATE_ASC, IDENTIFIER_ASC);
    } else if (tri.equals("Author")) {
      comparator = MediaLogicalComparator.on(AUTHOR_ASC_EMPTY_END, CREATE_DATE_ASC, IDENTIFIER_ASC);
    } else {
      comparator = null;
    }
    if (comparator != null) {
      Collections.sort(media, comparator);
    }
  }

  private boolean isDefined(String param) {
    return (param != null && param.length() > 0 && !"".equals(param));
  }

  private static GalleryBm getMediaService() {
    return MediaServiceFactory.getMediaService();
  }

  public Collection<NodeDetail> getPath(NodePK nodePK) {
    List<NodeDetail> path = (List<NodeDetail>) getMediaService().getPath(nodePK);
    Collections.reverse(path);
    return path;
  }

  public Collection<NodeDetail> getPath() {
    return getPath(new NodePK(getCurrentAlbumId(), getComponentId()));
  }

  public synchronized Collection<String> getAlbumIdsOf(String mediaId) {
    return getMediaService().getAlbumIdsOf(getMediaById(mediaId));
  }

  public int getNbMediaPerPage() {
    return GalleryComponentSettings
        .getNbMediaDisplayedPerPageByResolution(displayedMediaResolution);
  }

  public MediaResolution getDisplayedMediaResolution() {
    return displayedMediaResolution;
  }

  public void setDisplayedMediaResolution(MediaResolution displayedMediaResolution) {
    this.displayedMediaResolution = displayedMediaResolution;
  }

  public String getTri() {
    return tri;
  }

  public void setTri(String tri) {
    this.tri = tri;
    sortMedia();
  }

  public void setTriSearch(String tri) {
    this.tri = tri;
    sortMediaSearch();
  }

  public void setIndexOfCurrentPage(String index) {
    this.indexOfCurrentPage = Integer.parseInt(index);
  }

  public int getIndexOfCurrentPage() {
    return indexOfCurrentPage;
  }

  public String getCurrentAlbumId() {
    return currentAlbumId;
  }

  public void setCurrentAlbumId(String currentAlbumId) {
    SilverTrace.info("gallery", "GallerySessionController.setCurrentAlbumId",
        "root.MSG_GEN_ENTER_METHOD", "currentAlbumId =  " + currentAlbumId);
    this.currentAlbumId = currentAlbumId;
    this.currentAlbum = getAlbum(this.currentAlbumId);
  }

  public int getRang() {
    return rang;
  }

  public Boolean isDragAndDropEnabled() {
    return GalleryComponentSettings.isDragAndDropEnabled(getComponentId());
  }

  public Boolean isUsePdc() {
    return GalleryComponentSettings.isPdcEnabled(getComponentId());
  }

  public Boolean isViewMetadata() {
    return GalleryComponentSettings.isViewMetadataEnabled(getComponentId());
  }

  public Boolean isMakeWatermark() {
    return GalleryComponentSettings.isMakeWatermarkEnabled(getComponentId());
  }

  public String getWatermarkHD() {
    String watermarkHD = getComponentParameterValue("WatermarkHD");
    if (StringUtil.isInteger(watermarkHD)) {
      return watermarkHD;
    }
    return StringUtil.EMPTY;
  }

  public String getWatermarkOther() {
    String watermarkOther = getComponentParameterValue("WatermarkOther");
    if (StringUtil.isInteger(watermarkOther)) {
      return watermarkOther;
    }
    return StringUtil.EMPTY;
  }

  public Boolean isViewList() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("viewList"));
  }

  public Boolean areCommentsEnabled() {
    return !"no".equalsIgnoreCase(getComponentParameterValue("comments"));
  }

  public Integer getSlideshowWait() {
    String wait = getComponentParameterValue("slideshow");
    if (wait == null || "null".equalsIgnoreCase(wait) || wait.length() == 0) {
      wait = "5";
    }
    Integer iWait = new Integer(wait);
    if (iWait <= 0) {
      iWait = 5;
    }
    return iWait;
  }

  public String getXMLFormName() {
    String formName = getComponentParameterValue("XMLFormName");
    // contrôle du formulaire et retour du nom si convenable
    if (isDefined(formName)) {
      try {
        String xmlFormShortName = formName.substring(formName.indexOf("/") + 1, formName
            .indexOf("."));
        getPublicationTemplateManager().getPublicationTemplate(getComponentId()
            + ":" + xmlFormShortName, formName);
      } catch (PublicationTemplateException e) {
        formName = null;
      }
    }
    return formName;
  }

  public void initIndex() {
    indexOfCurrentPage = 0;
  }

  public String initAlertUser(String mediaId) {
    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
    sel.setHostComponentId(getComponentId()); // set id du composant pour appel
    // selectionPeas (extra param permettant de filtrer les users
    // ayant acces au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set
    // nom du composant pour browsebar (PairObject(nom_composant, lien_vers_composant))
    // NB : seul le 1er element est actuellement utilisé (alertUserPeas est toujours
    // présenté en popup => pas de lien sur nom du composant)
    sel.setHostComponentName(hostComponentName);
    SilverTrace.debug("gallery", "GallerySessionController.initAlertUser()",
        "root.MSG_GEN_PARAM_VALUE", "name = " + hostComponentName + " componentId="
        + getComponentId());
    sel.setNotificationMetaData(getAlertNotificationMetaData(mediaId)); // set
    // NotificationMetaData contenant les informations à notifier fin initialisation de
    // AlertUser l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  public Collection<Media> search(QueryDescription query) {
    query.setSearchingUser(getUserId());
    query.addComponent(getComponentId());
    Collection<Media> result = getMediaService().search(query);
    // mise à jour de la liste
    isSearchResult = true;
    // sauvegarde de la recherche
    setQuery(query);
    return result;
  }

  public void sendAskMedia(String order) {
    // envoyer une notification au gestionnaire de la médiathèque concernant la
    // demande de média
    // 1. création du message
    OrganisationController orga = getOrganisationController();
    UserDetail[] admins = orga.getUsers("useless", getComponentId(), "admin");
    String user = getUserDetail().getDisplayedName();

    ResourceLocator message = new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle",
        "fr");
    ResourceLocator message_en = new ResourceLocator(
        "org.silverpeas.gallery.multilang.galleryBundle", "en");

    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("gallery.notifAskSubject");
    messageBody = messageBody.append(user).append(" ").append(
        message.getString("gallery.notifBodyAsk")).append("\n").append(order);

    // english notifications
    String subject_en = message_en.getString("gallery.notifAskSubject");
    messageBody_en = messageBody_en.append(user).append(" ").append(
        message.getString("gallery.notifBodyAsk")).append("\n").append(order);
    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());
    for (UserDetail admin : admins) {
      notifMetaData.addUserRecipient(new UserRecipient(admin));
    }
    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) + "Main");

    // 2. envoie de la notification aux admin
    notifyUsers(notifMetaData);
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String mediaId) {
    MediaPK mediaPK = new MediaPK(mediaId, getSpaceId(), getComponentId());
    NodePK nodePK = currentAlbum.getNodePK();
    String senderName = getUserDetail().getDisplayedName();
    Media media = getMedia(mediaPK.getId());
    String htmlPath = getMediaService().getHTMLNodePath(nodePK);

    ResourceLocator message = new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle",
        "fr");
    ResourceLocator message_en = new ResourceLocator(
        "org.silverpeas.gallery.multilang.galleryBundle", "en");

    String subject = getNotificationSubject(message);
    String body = getNotificationBody(media, htmlPath, message, senderName);

    // english notifications
    String subject_en = getNotificationSubject(message_en);
    String body_en = getNotificationBody(media, htmlPath, message_en, senderName);

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, body);
    notifMetaData.addLanguage("en", subject_en, body_en);

    notifMetaData.setLink(getMediaUrl(media));
    notifMetaData.setComponentId(mediaPK.getInstanceId());
    notifMetaData.setSender(getUserId());

    return notifMetaData;
  }

  private String getNotificationSubject(ResourceLocator message) {
    return message.getString("gallery.notifSubject");
  }

  private String getNotificationBody(Media media, String htmlPath, ResourceLocator message,
      String senderName) {
    StringBuilder messageText = new StringBuilder();
    messageText.append(senderName).append(" ");
    messageText.append(message.getString("gallery.notifInfo")).append("\n\n");
    messageText.append(message.getString("gallery.notifName")).append(" : ").append(media.getName())
        .append("\n");
    if (isDefined(media.getDescription())) {
      messageText.append(message.getString("gallery.notifDesc")).append(" : ").append(
          media.getDescription()).append("\n");
    }
    messageText.append(message.getString("gallery.path")).append(" : ").append(htmlPath);
    return messageText.toString();
  }

  private String getMediaUrl(Media media) {
    return URLManager.getURL(null, getComponentId()) + media.getURL();
  }

  public Collection<String> getListSelected() {
    // restitution de la collection des médias selectionnés
    SilverTrace.info("gallery", "GallerySessionControler.getListSelected()", "",
        "listSelected (taille) = (" + listSelected.size() + ") " + listSelected.toString());
    return listSelected;
  }

  public void clearListSelected() {
    listSelected.clear();
  }

  public List<Media> getRestrictedListMedia() {
    return restrictedListMedia;
  }

  public void setRestrictedListMedia(Collection<Media> restrictedListMedia) {
    this.restrictedListMedia =
        (restrictedListMedia == null ? null : new ArrayList<Media>(restrictedListMedia));
  }

  public List<Media> getSearchResultListMedia() {
    return searchResultListMedia;
  }

  public void setSearchResultListMedia(Collection<Media> searchResultListMedia) {
    this.searchResultListMedia =
        (searchResultListMedia == null ? null : new ArrayList<Media>(searchResultListMedia));
  }

  public String getSearchKeyWord() {
    return searchKeyWord;
  }

  public void setSearchKeyWord(String searchKeyWord) {
    this.searchKeyWord = searchKeyWord;
  }

  public boolean getSelect() {
    return select;
  }

  public void setSelect(boolean select) {
    this.select = select;
  }

  public Boolean isPrivateSearch() {
    // retourne true si on utilise le moteur de recherche dédié
    return "yes".equalsIgnoreCase(getComponentParameterValue("privateSearch"));
  }

  public boolean isAlbumAdmin(SilverpeasRole userRole, String albumId, String userId) {
    if (albumId == null) {
      return isAdminOrPublisher(userRole);
    }
    // rechercher le créateur de l'album
    AlbumDetail album = getAlbum(albumId);
    return (SilverpeasRole.admin == userRole ||
        (SilverpeasRole.publisher == userRole && album.getCreatorId().equals(userId)));
  }

  public boolean isMediaAdmin(SilverpeasRole userRole, String mediaId, String userId) {
    if (mediaId == null) {
      return (isAdminOrPublisher(userRole) || userRole == SilverpeasRole.writer);
    }
    // rechercher le créateur du média
    Media media = getMedia(mediaId);
    return (isAdminOrPublisher(userRole) ||
        (userRole == SilverpeasRole.writer && media.getCreatorId().equals(userId)));
  }

  public void copySelectedMedia(Collection<String> mediaIds) throws ClipboardException {
    for (String mediaId : mediaIds) {
      copyMedia(mediaId);
    }
  }

  public void cutSelectedMedia(Collection<String> mediaIds) throws ClipboardException {
    for (String mediaId : mediaIds) {
      cutMedia(mediaId);
    }
  }

  public void copyMedia(String mediaId) throws ClipboardException {
    Media media = getMedia(mediaId);
    MediaSelection mediaSelect = new MediaSelection(media);
    SilverTrace.info("gallery", "GallerySessionController.copyMedia()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(mediaSelect);
  }

  public void cutMedia(String mediaId) throws ClipboardException {
    Media media = getMedia(mediaId);
    MediaSelection mediaSelect = new MediaSelection(media);
    mediaSelect.setCutted(true);

    SilverTrace.info("gallery", "GallerySessionController.cutMedia()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(mediaSelect);
  }

  public void copyAlbum(String albumId) throws ClipboardException {
    AlbumDetail album = getAlbum(albumId);
    NodeSelection nodeSelect = new NodeSelection(album);
    SilverTrace.info("gallery", "GallerySessionController.copyAlbum()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    SilverTrace.info("gallery", "GallerySessionController.copyAlbum()", "root.MSG_GEN_PARAM_VALUE",
        "nodeSelect = " + nodeSelect.toString() + "' albumId =" + album.getId());
    addClipboardSelection(nodeSelect);
  }

  public void cutAlbum(String albumId) throws ClipboardException {
    NodeSelection nodeSelect = new NodeSelection(getAlbum(albumId));
    nodeSelect.setCutted(true);

    SilverTrace.info("gallery", "GallerySessionController.cutAlbum()", "root.MSG_GEN_PARAM_VALUE",
        "clipboard = " + getClipboardName() + "' count=" + getClipboardCount());
    addClipboardSelection(nodeSelect);
  }

  public void paste() throws ClipboardException {
    try {
      GalleryPasteDelegate delegate = new GalleryPasteDelegate(currentAlbum);
      SilverTrace.info("gallery", "GalleryRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
          "clipboard = " + getClipboardName() + " count=" + getClipboardCount());

      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      Map<Object, ClipboardSelection> clipObjectPerformed
          = new HashMap<Object, ClipboardSelection>();
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(MediaSelection.MediaFlavor)) {
            Media media = (Media) clipObject.getTransferData(MediaSelection.MediaFlavor);

            delegate.addMedia(media, clipObject.isCutted());
            clipObjectPerformed.put(media.getMediaPK(), clipObject);
          }
          if (clipObject.isDataFlavorSupported(NodeSelection.NodeDetailFlavor)) {
            AlbumDetail album = (AlbumDetail) clipObject.getTransferData(
                NodeSelection.NodeDetailFlavor);
            SilverTrace.info("gallery", "GalleryRequestRooter.paste()", "root.MSG_GEN_PARAM_VALUE",
                "albumId = " + album.getId());

            delegate.addAlbum(album, clipObject.isCutted());
            clipObjectPerformed.put(album.getNodePK(), clipObject);
          }
        }
      }

      // Persisting the paste operation
      getMediaService().paste(getUserDetail(), getComponentId(), delegate);

      // End of treatment
      CallBackManager callBackManager = CallBackManager.get();
      for (Map.Entry<Object, ClipboardSelection> entry : clipObjectPerformed.entrySet()) {
        if (entry.getValue().isCutted()) {
          callBackManager.invoke(CallBackManager.ACTION_CUTANDPASTE, Integer.parseInt(
              getUserId()), getComponentId(), entry.getKey());
        }
      }

    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.paste()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_PASTE_ERROR", e);
    }
    clipboardPasteDone();
  }

  public Collection<Media> getAllMedia(AlbumDetail album) {
    try {
      return getMediaService().getAllMedia(album.getNodePK());
    } catch (Exception e) {
      return null;
    }
  }

  public void sendAskOrder(String orderId) {
    // envoyer une notification au gestionnaire pour le prévenir de la demande
    // de l'utilisateur
    // 1. création du message

    OrganisationController orga = getOrganisationController();
    UserDetail[] admins = orga.getUsers("useless", getComponentId(), "admin");
    String user = getUserDetail().getDisplayedName();

    ResourceLocator message = new ResourceLocator("com.silverpeas.gallery.multilang.galleryBundle",
        "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.silverpeas.gallery.multilang.galleryBundle", "en");

    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("gallery.orderNotifAskSubject");
    messageBody = messageBody.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAsk")).append("\n");

    // english notifications
    String subject_en = message_en.getString("gallery.orderNotifAskSubject");
    messageBody_en = messageBody_en.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAsk")).append("\n");

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    for (UserDetail admin : admins) {
      notifMetaData.addUserRecipient(new UserRecipient(admin));
    }
    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) + "OrderView?OrderId="
        + orderId);

    // 2. envoie de la notification aux admin
    notifyUsers(notifMetaData);
  }

  public void sendAskOrderUser(String orderId) {
    // envoyer une notification au lecteur pour le prévenir du traitement de sa
    // demande
    // 1. création du message
    Order order = getOrder(orderId);
    String user = getOrganisationController().getUserDetail(order.
        getProcessUserId()).getDisplayedName();

    ResourceLocator message = new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle",
        "fr");
    ResourceLocator message_en = new ResourceLocator(
        "org.silverpeas.gallery.multilang.galleryBundle", "en");

    StringBuffer messageBody = new StringBuffer();
    StringBuffer messageBody_en = new StringBuffer();

    // french notifications
    String subject = message.getString("gallery.orderNotifAskSubject");
    messageBody = messageBody.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAskOk")).append("\n");

    // english notifications
    String subject_en = message_en.getString("gallery.orderNotifAskSubject");
    messageBody_en = messageBody_en.append(user).append(" ").append(
        message.getString("gallery.orderNotifBodyAskOk")).append("\n");

    NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
        subject, messageBody.toString());
    notifMetaData.addLanguage("en", subject_en, messageBody_en.toString());

    notifMetaData.addUserRecipient(new UserRecipient(String.valueOf(order.getUserId())));
    notifMetaData.setLink(URLManager.getURL(null, getComponentId()) + "OrderView?OrderId="
        + orderId);

    // 2. envoie de la notification aux admin
    notifyUsers(notifMetaData);
  }

  public void addToBasket() {
    // ajout dans le panier toutes les médias sélectionnés
    basket.addAll(listSelected);
    // remettre à blanc la liste des médias sélectionnés
    SilverTrace.debug("gallery", "GallerySessionController.addToBasket()",
        "root.MSG_GEN_PARAM_VALUE", "listSelected = " + listSelected.toString() + " basket = "
        + basket.toString());
    clearListSelected();
  }

  public void addMediaToBasket(String mediaId) {
    // ajout dans le panier le média
    if (!basket.contains(mediaId)) {
      basket.add(mediaId);
    }
  }

  public void deleteToBasket() {
    // suppression dans le panier
    for (String mediaId : listSelected) {
      // rechercher le média dans le panier et la supprimer
      basket.remove(mediaId);

    }
    clearListSelected();
  }

  public void deleteToBasket(String mediaId) {
    basket.remove(mediaId);
  }

  public void deleteBasket() {
    // suppression de tous les médias du panier
    basket.clear();
  }

  public List<String> getBasketMediaIdList() {
    return basket;
  }

  public List<Media> getBasketMedias() {
    List<Media> medias = new ArrayList<Media>();
    if (!basket.isEmpty()) {
      for (String mediaId : basket) {
        medias.add(getMediaById(mediaId));
      }
    }
    return medias;
  }

  public Order getOrder(String orderId) {
    return getMediaService().getOrder(orderId, getComponentId());
  }

  public String getCurrentOrderId() {
    return currentOrderId;
  }

  public void setCurrentOrderId(String orderId) {
    currentOrderId = orderId;
  }

  public String addOrder() {
    // transformer le panier en demande
    String orderId = getMediaService().createOrder(basket, getUserId(), getComponentId());
    // vider le panier
    basket.clear();
    return orderId;
  }

  public void updateOrderRow(String orderId, String mediaId) {
    Order order = getOrder(orderId);
    List<OrderRow> rows = order.getRows();
    for (OrderRow row : rows) {
      if (row.getMediaId().equals(mediaId)) {
        // on est sur la bonne ligne, mettre à jour
        row.setDownloadDecision("T");
        getMediaService().updateOrderRow(row);
      }
    }
  }

  public String getUrl(String orderId, String mediaId) {
    String url = "";
    Order order = getOrder(orderId);
    List<OrderRow> rows = order.getRows();
    for (OrderRow row : rows) {
      if (row.getMediaId().equals(mediaId)) {
        // on est sur la bonne ligne
        InternalMedia media = getInternalMediaById(row.getMediaId());
        String nomRep = media.getWorkspaceSubFolderName();
        String download = row.getDownloadDecision();

        if (!download.equals("T")) {
          // le média n'a pas déjà été téléchargé
          // par defaut média sans watermark
          String title = EncodeHelper.javaStringToHtmlString(media.getFileName());
          if (download.equals("DW")) {
            // demande avec Watermark
            title = mediaId + "_watermark.jpg";
            // regarder si le média existe, sinon prendre celui sans watermark
            String pathFile =
                FileRepositoryManager.getAbsolutePath(getComponentId()) + nomRep + File.separator;
            String watermarkFile = pathFile + title;
            File file = new File(watermarkFile);
            if (!file.exists()) {
              title = EncodeHelper.javaStringToHtmlString(media.getFileName());
            }
          }
          url = FileServerUtils
              .getUrl(getComponentId(), getUrlEncodedParameter(title), media.getFileMimeType(),
                  nomRep);
        }
      }
    }
    return url;
  }

  public boolean isAccessAuthorized(String orderId) {
    return getOrder(orderId).getUserId().equals(getUserId());
  }

  public List<MetaData> getMetaDataKeys() {
    List<MetaData> metaDatas = new ArrayList<MetaData>();
    try {
      FileUtil.loadProperties(settings, "org/silverpeas/gallery/settings/metadataSettings_"
          + getComponentId() + ".properties");
    } catch (Exception e) {
      settings = defaultSettings;
    }
    String display = settings.getProperty("display");
    Iterable<String> propertyNames = StringUtil.splitString(display, ',');

    for (String value : propertyNames) {
      if (value.startsWith("IPTC_")) {
        String property = settings.getProperty(value + "_TAG");
        String label = settings.getProperty(value + "_LABEL");
        label = getMetadataResources().getString(label);
        boolean isSearch = StringUtil.getBooleanValue(settings.getProperty(value + "_SEARCH"));
        if (isSearch) {
          boolean isDate = StringUtil.getBooleanValue(settings.getProperty(value + "_DATE"));
          MetaData metaData = new MetaData();
          metaData.setProperty(property);
          metaData.setDate(isDate);
          metaData.setLabel(label);
          // rechercher sa valeur dans la query (résultat de la recherche)
          List<FieldDescription> metadataValue = query.getMultiFieldQuery();
          if (metadataValue != null) {
            for (FieldDescription field : metadataValue) {
              if (field.getFieldName().equals("IPTC_" + metaData.getProperty())) {
                metaData.setValue(field.getContent());
              }

            }
          }
          // ajout de cette metadata à la liste
          metaDatas.add(metaData);
        }
      }
    }
    return metaDatas;
  }

  public void updateOrder(Order order) {
    getMediaService().updateOrder(order);
  }

  public List<Order> getAllOrders() {
    return getMediaService().getAllOrders("-1", getComponentId());
  }

  public List<Order> getOrdersByUser() {
    return getMediaService().getAllOrders(getUserId(), getComponentId());
  }

  public String getOrderForm() {
    return getComponentParameterValue("XMLOrderFormName");
  }

  public String getCharteUrl() {
    String url = getComponentParameterValue("UrlCharte");
    if (!StringUtil.isDefined(url)) {
      url = "";
    }
    return url;
  }

  public Boolean isBasket() {
    return !isGuest() && StringUtil.getBooleanValue(getComponentParameterValue("basket"));
  }

  public Boolean isGuest() {
    return getUserDetail().isAccessGuest();
  }

  public Boolean isOrder() {
    return !isGuest() && StringUtil.getBooleanValue(getComponentParameterValue("order"));
  }

  public boolean isViewNotVisible() {
    return isViewNotVisible;
  }

  public void setViewNotVisible(boolean isViewNotVisible) {
    this.isViewNotVisible = isViewNotVisible;
  }

  public boolean isSearchResult() {
    return isSearchResult;
  }

  public void setSearchResult(boolean isSearchResult) {
    this.isSearchResult = isSearchResult;
  }

  public QueryDescription getQuery() {
    return query;
  }

  public void setQuery(QueryDescription query) {
    this.query = query;
  }

  public void setPDCSearchContext(SearchContext context) {
    pdcSearchContext = context;
  }

  public SearchContext getPDCSearchContext() {
    return pdcSearchContext;
  }

  public void setXMLSearchContext(DataRecord data) {
    xmlSearchContext = data;
  }

  public DataRecord getXMLSearchContext() {
    return xmlSearchContext;
  }

  public void clearSearchContext() {
    setQuery(new QueryDescription());
    setXMLSearchContext(null);
    setPDCSearchContext(null);
  }

  protected boolean isAdminOrPublisher(SilverpeasRole userRole) {
    return userRole.isGreaterThanOrEquals(SilverpeasRole.publisher);
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }

  public static void sortAlbums(List<NodePK> albumPKs) {
    try {
      getMediaService().sortAlbums(albumPKs);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GallerySessionController.sortAlbums()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  public Collection<AlbumDetail> addNbMedia(Collection<AlbumDetail> albums) {
    // retourne la liste des albums avec leurs nombre de médias
    for (AlbumDetail album : albums) {
      // pour chaque sous album, rechercher le nombre de médias
      int nbMedia;
      Collection<Media> allMedia = getAllMedia(album);
      nbMedia = allMedia.size();
      // parcourir ses sous albums pour comptabiliser aussi ses médias
      AlbumDetail thisAlbum = getAlbumLight(Integer.toString(album.getId()));
      Collection<AlbumDetail> subAlbums = addNbMedia(thisAlbum.getChildrenAlbumsDetails());
      for (AlbumDetail oneSubAlbum : subAlbums) {
        nbMedia = nbMedia + oneSubAlbum.getNbMedia();
      }
      album.setNbMedia(nbMedia);
    }
    return albums;
  }

  public void notifyUsers(NotificationMetaData notifMetaData) {
    try {
      notifMetaData.setSender(getUserId());
      notifMetaData.setComponentId(getComponentId());
      NotificationSender notifSender = new NotificationSender(getComponentId());
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new GalleryRuntimeException("GallerySessionController.notifyUsers()",
          SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST", e);
    }
  }

  /**
   * Gets a media from the specified identifier.
   * @param mediaId
   * @return the instance of the media behinf the specified identifier, null if it does not exist.
   */
  private Media getMediaById(String mediaId) {
    Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
    if (media == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return media;
  }

  /**
   * Gets a, internal media from the specified identifier.
   * @param mediaId
   * @return the instance of the media behinf the specified identifier, null if it does not exist.
   */
  private InternalMedia getInternalMediaById(String mediaId) {
    Media media = getMediaService().getMedia(new MediaPK(mediaId, getComponentId()));
    if (media instanceof InternalMedia) {
      return (InternalMedia) media;
    }
    throw new WebApplicationException(Response.Status.NOT_FOUND);
  }
}
