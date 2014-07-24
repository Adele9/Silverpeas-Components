/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.gallery.constant.StreamingProvider;

/**
 * This class represents a Streaming.
 */
public class Streaming extends InternalMedia {
  private static final long serialVersionUID = 5772513957256327862L;

  private String homepageUrl = "";
  private StreamingProvider provider = StreamingProvider.Unknown;

  @Override
  public MediaType getType() {
    return MediaType.Streaming;
  }

  /**
   * Gets the provider URL that permits to access to the video play.
   * @return the provider URL that permits to access to the video play.
   */
  public String getHomepageUrl() {
    return homepageUrl;
  }

  /**
   * Sets the provider URL that permits to access to the video play.
   * @param url the provider URL that permits to access to the video play.
   */
  public void setHomepageUrl(final String url) {
    this.homepageUrl = url;
  }

  /**
   * Gets the streaming provider.
   * @return the streaming provider.
   */
  public StreamingProvider getProvider() {
    return provider;
  }

  /**
   * Gets the streaming provider.
   * @param provider the streaming provider.
   */
  public void setProvider(final StreamingProvider provider) {
    this.provider = provider;
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return MediaType.Streaming.name();
  }
}