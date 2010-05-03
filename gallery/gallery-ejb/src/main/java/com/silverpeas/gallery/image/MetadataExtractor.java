/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.image;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDescriptor;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ehugonnet
 */
public class MetadataExtractor {

  private ResourceLocator settings;
  private Map<String, ResourceLocator> metaDataBundles;
  private List<ExifProperty> imageProperties;
  private List<IptcProperty> imageIptcProperties;

  public MetadataExtractor() {
    this.settings = new ResourceLocator("com.silverpeas.gallery.settings.metadataSettings",
        I18NHelper.defaultLanguage);
    this.metaDataBundles = new HashMap<String, ResourceLocator>(I18NHelper.allLanguages.size());
    for (String lang : I18NHelper.allLanguages.keySet()) {
      metaDataBundles.put(lang, new ResourceLocator(
          "com.silverpeas.gallery.multilang.metadataBundle",
          lang));
    }
    this.imageProperties = defineImageProperties();
    this.imageIptcProperties = defineImageIptcProperties();

  }

  public final List<ExifProperty> defineImageProperties() {
    List<ExifProperty> properties = new ArrayList<ExifProperty>();
    int indice = 1;
    boolean hasMore = true;
    while (hasMore) {
      String property = settings.getString("METADATA_" + indice + "_TAG");
      String labelKey = settings.getString("METADATA_" + indice + "_LABEL");
      hasMore = StringUtil.isInteger(property);
      if (hasMore) {
        ExifProperty exifProperty = new ExifProperty(Integer.valueOf(property));
        for (Map.Entry<String, ResourceLocator> labels : metaDataBundles.entrySet()) {
          String label = labels.getValue().getString(labelKey);
          exifProperty.setLabel(labels.getKey(), label);
        }
        properties.add(exifProperty);
      }
      indice++;
    }
    return properties;
  }

  public final List<IptcProperty> defineImageIptcProperties() {
    List<IptcProperty> properties = new ArrayList<IptcProperty>();
    int indice = 1 + imageProperties.size();
    boolean hasMore = true;
    while (hasMore) {
      String property = settings.getString("IPTC_" + indice + "_TAG");
      String labelKey = settings.getString("IPTC_" + indice + "_LABEL");
      boolean isDate = settings.getBoolean("IPTC_" + indice + "_DATE", false);
      hasMore = StringUtil.isInteger(property);
      if (hasMore) {
        IptcProperty iptcProperty = new IptcProperty(Integer.valueOf(property));
        for (Map.Entry<String, ResourceLocator> labels : metaDataBundles.entrySet()) {
          String label = labels.getValue().getString(labelKey);
          iptcProperty.setLabel(labels.getKey(), label);
        }
        iptcProperty.setDate(isDate);
        properties.add(iptcProperty);
      }
      indice++;
    }
    return properties;
  }

  public List<MetaData> extractImageExifMetaData(File image) throws JpegProcessingException,
      UnsupportedEncodingException, MetadataException {
    return extractImageExifMetaData(image, I18NHelper.defaultLanguage);
  }

  public List<MetaData> extractImageExifMetaData(File image, String lang) throws
      JpegProcessingException, UnsupportedEncodingException, MetadataException {
    List<MetaData> result = new ArrayList<MetaData>();
    // lire le fichier des properties
    // 1. Traitement des metadata EXIF
    Metadata metadata = JpegMetadataReader.readMetadata(image);
    Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
    ExifDescriptor descriptor = new ExifDescriptor(exifDirectory);
    String value = null;
    for (ExifProperty property : imageProperties) {
      // rechercher la valeur de la metadata "label"
      int currentMetadata = property.getProperty();
      switch (currentMetadata) {
        case ExifDirectory.TAG_WIN_AUTHOR:
          value = descriptor.getWindowsAuthorDescription();
          break;
        case ExifDirectory.TAG_WIN_COMMENT:
          value = descriptor.getWindowsCommentDescription();
          break;

        case ExifDirectory.TAG_WIN_KEYWORDS:
          value = descriptor.getWindowsKeywordsDescription();
          break;

        case ExifDirectory.TAG_WIN_SUBJECT:
          value = descriptor.getWindowsSubjectDescription();
          break;

        case ExifDirectory.TAG_WIN_TITLE:
          value = descriptor.getWindowsTitleDescription();
          break;
        default:
          value = exifDirectory.getString(currentMetadata);
      }
      if (value != null) {
        // ajout de cette metadata à la photo
        MetaData metaData = new MetaData();
        metaData.setLabel(property.getLabel(lang));
        metaData.setProperty(property.getProperty() + "");
        metaData.setValue(value);
        SilverTrace.debug("gallery", "GallerySessionController.addMetaData()",
            "root.MSG_GEN_ENTER_METHOD", "METADATA EXIF label = " + property.getLabel()
            + " value = " + value);
        result.add(metaData);
      }
    }
    return result;
  }

  public List<MetaData> extractImageIptcMetaData(File image) throws JpegProcessingException,
      UnsupportedEncodingException,
      MetadataException {
    return extractImageIptcMetaData(image, I18NHelper.defaultLanguage);




  }

  public List<MetaData> extractImageIptcMetaData(File image, String lang) throws
      JpegProcessingException,
      UnsupportedEncodingException,
      MetadataException {
    List<MetaData> result = new ArrayList<MetaData>();
    // lire le fichier des properties
    // 1. Traitement des metadata EXIF
    Metadata metadata = JpegMetadataReader.readMetadata(image);
    Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);
    for (IptcProperty iptcProperty : imageIptcProperties) {
      // rechercher la valeur de la metadata "label"
      String value = iptcDirectory.getString(iptcProperty.getProperty());
      if (value != null) {
        MetaData metaData = new MetaData();
        metaData.setLabel(iptcProperty.getLabel(lang));
        metaData.setProperty(iptcProperty.getProperty() + "");
        metaData.setValue(value);
        if (iptcProperty.isDate()) {
          metaData.setDate(true);
          metaData.setDateValue(iptcDirectory.getDate(iptcProperty.getProperty()));
        }
        result.add(metaData);
        SilverTrace.debug("gallery",
            "GallerySessionController.addMetaData()",
            "root.MSG_GEN_ENTER_METHOD", "METADATA IPTC label = " + iptcProperty.getLabel()
            + " value = " + value);
      }
    }
    return result;
  }
}
