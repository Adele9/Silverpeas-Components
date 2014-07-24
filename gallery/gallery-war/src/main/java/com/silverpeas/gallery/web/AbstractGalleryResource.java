/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.web;

import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.apache.commons.collections.CollectionUtils;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.EnumSet;

import static com.silverpeas.gallery.web.GalleryResourceURIs.*;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractGalleryResource extends RESTWebService {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  /**
   * Converts the album into its corresponding web entity.
   *
   * @param album the album.
   * @return the corresponding photo entity.
   */
  protected AlbumEntity asWebEntity(AlbumDetail album) {
    checkNotFoundStatus(album);
    AlbumEntity albumEntity = AlbumEntity.createFrom(album, getUserPreferences().getLanguage())
        .withURI(getUriInfo().getRequestUri()).withParentURI(buildAlbumURI(album.getFatherPK()));
    for (Media media : album.getMedia()) {
      if (media.getType().isPhoto() && hasUserMediaAccess(media.getPhoto())) {
        albumEntity.addPhoto(asWebEntity(media.getPhoto(), album));
      }
    }
    return albumEntity;
  }

  /**
   * Converts the photo into its corresponding web entity.
   *
   * @param media the photo to convert.
   * @param album the album of the photo.
   * @return the corresponding photo entity.
   */
  protected PhotoEntity asWebEntity(Media media, AlbumDetail album) {
    checkNotFoundStatus(media);
    checkNotFoundStatus(media.getPhoto());
    checkNotFoundStatus(album);
    verifyPhotoIsInAlbum(media.getPhoto(), album);
    return PhotoEntity.createFrom(media.getPhoto(), getUserPreferences().getLanguage())
        .withURI(buildPhotoURI(media.getMediaPK(), album.getNodePK()))
        .withParentURI(buildAlbumURI(album.getNodePK()));
  }

  /**
   * Converts the photo into an input stream.
   *
   *
   *
   * @param photo
   * @param album the album of the photo.
   * @param isOriginalRequired the original or preview content
   * @return the corresponding photo entity.
   */
  protected InputStream asInputStream(Photo photo, AlbumDetail album, boolean isOriginalRequired) {
    checkNotFoundStatus(photo);
    checkNotFoundStatus(album);
    verifyPhotoIsInAlbum(photo, album);
    return ImageHelper.openInputStream(photo, isOriginalRequired);
  }

  /**
   * Indicates if the current user is a privileged one.
   *
   * @return
   */
  protected boolean isUserPrivileged() {
    return !CollectionUtils.intersection(
        EnumSet.of(SilverpeasRole.admin, SilverpeasRole.publisher, SilverpeasRole.privilegedUser),
        getUserRoles()).isEmpty();
  }

  /**
   * Centralized build of album URI.
   *
   * @param album
   * @return album URI
   */
  protected URI buildAlbumURI(NodePK album) {
    if (album == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(GALLERY_BASE_URI).path(getComponentId())
        .path(GALLERY_ALBUMS_URI_PART).path(album.getId()).build();
  }

  /**
   * Centralized build of album URI.
   *
   * @param photo
   * @param album
   * @return album URI
   */
  protected URI buildPhotoURI(MediaPK photo, NodePK album) {
    if (photo == null || album == null) {
      return null;
    }
    return getUriInfo().getBaseUriBuilder().path(GALLERY_BASE_URI).path(getComponentId())
        .path(GALLERY_ALBUMS_URI_PART).path(album.getId()).path(GALLERY_PHOTOS_PART)
        .path(photo.getId()).build();
  }

  /**
   * Centralization
   *
   * @param object any object
   */
  protected void checkNotFoundStatus(Object object) {
    if (object == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Centralization
   *
   * @param media
   * @return
   */
  protected boolean hasUserMediaAccess(Media media) {
    return media.canBeAccessedBy(getUserDetail());
  }

  /**
   * Verifying that the authenticated user is authorized to view the given photo.
   *
   * @return
   * @param media
   */
  protected void verifyUserMediaAccess(Media media) {
    if (!hasUserMediaAccess(media)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Checking if the authenticated user is authorized to view all photos.
   *
   * @return
   */
  protected boolean isViewAllPhotoAuthorized() {
    return getUserRoles().contains(SilverpeasRole.admin);
  }

  /**
   * Verifying that the given photo is included in the given album.
   *
   * @return
   */
  protected void verifyPhotoIsInAlbum(Photo photo, AlbumDetail album) {
    if (!album.getMedia().contains(photo)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets Gallery EJB.
   *
   * @return
   */
  protected GalleryBm getGalleryBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
    } catch (Exception e) {
      throw new GalleryRuntimeException("AbstractGalleryResource.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}
