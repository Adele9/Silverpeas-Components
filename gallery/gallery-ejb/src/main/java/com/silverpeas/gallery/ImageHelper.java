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
package com.silverpeas.gallery;

import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.MetadataException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

import org.apache.commons.fileupload.FileItem;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.iptc.IptcDirectory;
import com.silverpeas.gallery.image.MetadataExtractor;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import java.util.List;

public class ImageHelper {

  static ResourceLocator gallerySettings = new ResourceLocator(
      "com.silverpeas.gallery.settings.gallerySettings", "");
  static ResourceLocator settings = new ResourceLocator(
      "com.silverpeas.gallery.settings.metadataSettings", "");
  static final String thumbnailSuffix_small = "_66x50.jpg";
  static final String thumbnailSuffix_medium = "_133x100.jpg";
  static final String thumbnailSuffix_large = "_266x150.jpg";
  static final String previewSuffix = "_preview.jpg";
  static final String thumbnailSuffix_Xlarge = "_600x400.jpg";
  static final String watermarkSuffix = "_watermark.jpg";
  static final MetadataExtractor extractor = new MetadataExtractor();

  public static boolean isImage(String name) {
    String type = FileRepositoryManager.getFileExtension(name);
    return (isJpeg(type) || type.equalsIgnoreCase("bmp") || type.equalsIgnoreCase("tiff")
        || type.equalsIgnoreCase("jpeg") || type.equalsIgnoreCase("png") || type.equalsIgnoreCase(
        "tif"));
  }

  public static boolean isValidExtension(String name) {
    String type = FileRepositoryManager.getFileExtension(name);
    return (isJpeg(type) || type.equalsIgnoreCase("tiff") || type.equalsIgnoreCase("jpeg") || type.
        equalsIgnoreCase("png"));
  }

  public static boolean isJpeg(String type) {
    return type.equalsIgnoreCase("gif") || type.equalsIgnoreCase("jpg");
  }

  /**
   * In case of unit upload
   * @param photo
   * @param image
   * @param subDirectory
   * @throws Exception
   */
  public static void processImage(PhotoDetail photo, FileItem image,
      String subDirectory, boolean watermark, String watermarkHD,
      String watermarkOther) throws Exception {
    String name = null;
    String mimeType = null;
    long size = 0;
    File dir = null;
    String photoId = photo.getPhotoPK().getId();
    String instanceId = photo.getPhotoPK().getInstanceId();

    if (image != null) {
      name = image.getName();
      if (name != null) {
        boolean runOnUnix = !FileUtil.isWindows();
        if (runOnUnix) {
          name = name.replace('\\', File.separatorChar);
          SilverTrace.info("gallery", "ImageHelper.processImage",
              "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = " + name);
        }

        name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
        // name = replaceSpecialChars(name);

        if (isImage(name)) {
          dir = new File(FileRepositoryManager.getAbsolutePath(instanceId)
              + subDirectory + photoId + File.separator + name);
          mimeType = image.getContentType();
          size = image.getSize();

          // création du répertoire pour mettre la photo
          String nameRep = subDirectory + photoId;
          FileRepositoryManager.createAbsolutePath(instanceId, nameRep);
          image.write(dir);

          photo.setImageName(name);
          photo.setImageMimeType(mimeType);
          photo.setImageSize(size);

          createImage(name, dir, photo, subDirectory, watermark, watermarkHD,
              watermarkOther);
        }
      }
    }
  }

  /**
   * In case of drag And Drop upload
   * @param photo
   * @param image
   * @throws Exception
   */
  public static void processImage(PhotoDetail photo, File image,
      boolean watermark, String watermarkHD, String watermarkOther)
      throws Exception {
    String name = null;
    String mimeType = null;
    long size = 0;
    String dir = null;
    String photoId = photo.getPhotoPK().getId();
    String instanceId = photo.getPhotoPK().getInstanceId();

    String percent = gallerySettings.getString("percentSizeWatermark");
    if (!StringUtil.isDefined(percent)) {
      percent = "1";
    }
    int percentSize = new Integer(percent).intValue();
    if (percentSize <= 0) {
      percentSize = 1;
    }

    if (image != null) {
      name = image.getName();
      if (name != null) {
        name = name.substring(name.lastIndexOf(File.separator) + 1, name.length());
        if (isImage(name)) {
          String subDirectory = gallerySettings.getString("imagesSubDirectory");

          dir = FileRepositoryManager.getAbsolutePath(instanceId)
              + subDirectory + photoId + File.separator + name;

          mimeType = AttachmentController.getMimeType(name);
          size = image.length();

          // création du répertoire pour mettre la photo
          String nameRep = subDirectory + photoId;
          FileRepositoryManager.createAbsolutePath(instanceId, nameRep);

          FileRepositoryManager.copyFile(image.getAbsolutePath(), dir);

          photo.setImageName(name);
          photo.setImageMimeType(mimeType);
          photo.setImageSize(size);

          createImage(name, image, photo, subDirectory, watermark, watermarkHD,
              watermarkOther);
        }
      }
    }
  }

  private static void createImage(String name, File dir, PhotoDetail photo,
      String subDirectory, boolean watermark, String watermarkHD,
      String watermarkOther) throws Exception {
    String type = FileRepositoryManager.getFileExtension(name);
    String photoId = photo.getPhotoPK().getId();
    String instanceId = photo.getPhotoPK().getInstanceId();

    // recherche du paramètre du pourcentage de la taille du watermark
    String percent = gallerySettings.getString("percentSizeWatermark");
    if (!StringUtil.isDefined(percent)) {
      percent = "1";
    }
    int percentSize = Integer.parseInt(percent);
    if (percentSize <= 0) {
      percentSize = 1;
    }

    if (isValidExtension(name)) {
      // recherche de la taille de l'image
      getDimension(dir, photo);
    }

    // faire les preview et vignettes pour les formats possibles
    // NOTE : on ne peut pas redimensionner les images de type "bmp" ni "tif"
    if (isValidExtension(name)) {

      String pathFile = FileRepositoryManager.getAbsolutePath(instanceId)
          + subDirectory + photoId + File.separator;
      // ajout du watermark (si le paramètre est activé) QUE POUR LES IMAGES
      // JPEG
      String nameAuthor = "";
      String nameForWatermark = "";
      if (StringUtil.isDefined(watermarkHD) && watermark && isJpeg(type)) {
        // création d'un duplicata de l'image originale avec intégration du
        // watermark
        String property = watermarkHD;
        Metadata metadata = JpegMetadataReader.readMetadata(dir);
        Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);

        int currentMetadata = new Integer(property).intValue();
        String value = iptcDirectory.getString(currentMetadata);

        if (value != null) {
          nameAuthor = value;
        }
        if (!nameAuthor.equals("")) {
          createWatermark(photo.getId(), nameAuthor, pathFile, dir, percentSize);
        }
      }
      if (StringUtil.isDefined(watermarkOther) && watermark && isJpeg(type)) {
        String property = watermarkOther;
        Metadata metadata = JpegMetadataReader.readMetadata(dir);
        Directory iptcDirectory = metadata.getDirectory(IptcDirectory.class);

        int currentMetadata = new Integer(property).intValue();
        String value = iptcDirectory.getString(currentMetadata);

        if (value != null) {
          nameAuthor = value;
        }
        if (!nameAuthor.equals("")) {
          nameForWatermark = nameAuthor;
        }
      }
      // création de la preview et des vignettes
      createVignettes(photo, pathFile, dir, watermark, nameForWatermark);
    }
  }

  public static void setMetaData(PhotoDetail photo) throws ImageProcessingException,
      UnsupportedEncodingException, MetadataException {
    setMetaData(photo, I18NHelper.defaultLanguage);
  }

  public static void setMetaData(PhotoDetail photo, String lang) throws ImageProcessingException,
      UnsupportedEncodingException, MetadataException {
    String photoId = photo.getPhotoPK().getId();
    String name = photo.getImageName();
    String mimeType = photo.getImageMimeType();
    if ("image/jpeg".equals(mimeType) || "image/pjpeg".equals(mimeType)) {
      File file = new File(FileRepositoryManager.getAbsolutePath(photo.getInstanceId())
          + settings.getString("imagesSubDirectory")
          + photoId
          + File.separator
          + name);
      if (file != null && file.exists()) {
        List<MetaData> metadata = extractor.extractImageExifMetaData(file, lang);
        for (MetaData meta : metadata) {
          photo.addMetaData(meta);
        }
        metadata = extractor.extractImageIptcMetaData(file, lang);
        for (MetaData meta : metadata) {
          photo.addMetaData(meta);
        }
      }
    }
  }
  

  private static void getDimension(File inputFile, PhotoDetail photo)
      throws IOException {
    BufferedImage inputBuf = ImageIO.read(inputFile);
    photo.setSizeL(inputBuf.getWidth());
    photo.setSizeH(inputBuf.getHeight());
  }

  private static void createVignettes(PhotoDetail photo, String path, File dir,
      boolean watermark, String nameWatermark) throws IOException {
    String fileId = photo.getId();

    // création d'une preview sans watermark (pour être utilisée pour créer les
    // vignettes)
    String previewFile = path + fileId + thumbnailSuffix_Xlarge;
    int vignetteFile = 600;
    // on ne redimensionne l'image en preview que si l'image est plus grande que
    // la preview
    int largeWidth = photo.getSizeL();
    if (photo.getSizeH() > photo.getSizeL()) {
      largeWidth = photo.getSizeH();
    }
    if (largeWidth > vignetteFile) {
      redimPhoto(dir, previewFile, vignetteFile, false, nameWatermark, 0);
    } else {
      redimPhoto(dir, previewFile, largeWidth, false, nameWatermark, 0);
    }

    dir = new File(previewFile);

    // 1/ création de la preview
    int sizeWatermarkPreview = Integer.parseInt(gallerySettings.getString("sizeWatermark600x400"));
    String previewFileWatermark = path + fileId + previewSuffix;
    int previewWidth = 600;
    // on ne redimensionne l'image en preview que si l'image est plus grande que
    // la preview
    if (photo.getSizeH() > photo.getSizeL()) {
      largeWidth = photo.getSizeH();
    }
    if (largeWidth > previewWidth) {
      redimPhoto(dir, previewFileWatermark, previewWidth, watermark,
          nameWatermark, sizeWatermarkPreview);
    } else {
      redimPhoto(dir, previewFileWatermark, largeWidth, watermark,
          nameWatermark, sizeWatermarkPreview);
    }

    // 2/ création de la vignette 266x150
    int sizeWatermark266x150 = Integer.parseInt(gallerySettings.getString("sizeWatermark266x150"));
    String vignetteFile1 = path + fileId + thumbnailSuffix_large;
    int vignetteWidth1 = 266;
    if (largeWidth > vignetteWidth1) {
      redimPhoto(dir, vignetteFile1, vignetteWidth1, watermark, nameWatermark,
          sizeWatermark266x150);
    } else {
      redimPhoto(dir, vignetteFile1, largeWidth, watermark, nameWatermark,
          sizeWatermark266x150);
    }

    // création de la vignette 133x100
    int sizeWatermark133x100 = Integer.parseInt(gallerySettings.getString("sizeWatermark133x100"));
    String vignetteFile2 = path + fileId + thumbnailSuffix_medium;
    int vignetteWidth2 = 133;
    if (largeWidth > vignetteWidth2) {
      redimPhoto(dir, vignetteFile2, vignetteWidth2, watermark, nameWatermark,
          sizeWatermark133x100);
    } else {
      redimPhoto(dir, vignetteFile2, largeWidth, watermark, nameWatermark,
          sizeWatermark133x100);
    }

    // création de la vignette 50x66
    int sizeWatermark50x66 = Integer.parseInt(gallerySettings.getString("sizeWatermark66x50"));
    String vignetteFile3 = path + fileId + thumbnailSuffix_small;
    int vignetteWidth3 = 66;
    if (largeWidth > vignetteWidth3) {
      redimPhoto(dir, vignetteFile3, vignetteWidth3, watermark, nameWatermark,
          sizeWatermark50x66);
    } else {
      redimPhoto(dir, vignetteFile3, largeWidth, watermark, nameWatermark,
          sizeWatermark50x66);
    }
  }

  public static String[] getWidthAndHeight(String instanceId, String subDir,
      String imageName, int baseWidth) throws IOException {
    String[] directory = new String[1];
    directory[0] = subDir;

    File image = new File(FileRepositoryManager.getAbsolutePath(instanceId,
        directory)
        + imageName);

    BufferedImage inputBuf = ImageIO.read(image);

    return getWidthAndHeight(inputBuf, baseWidth);
  }

  public static String[] getWidthAndHeight(BufferedImage inputBuf,
      int widthParam) {
    String[] result = new String[2];
    try {
      // calcul de la taille de la sortie
      double inputBufWidth;
      double inputBufHeight;
      double width = widthParam;
      double ratio;
      double height;
      if (inputBuf.getWidth() > inputBuf.getHeight()) {
        inputBufWidth = inputBuf.getWidth();
        inputBufHeight = inputBuf.getHeight();
        width = widthParam;
        ratio = inputBufWidth / width;
        height = inputBufHeight / ratio;
      } else {
        inputBufWidth = inputBuf.getHeight();
        inputBufHeight = inputBuf.getWidth();
        height = widthParam;
        ratio = inputBufWidth / width;
        width = inputBufHeight / ratio;
      }
      String sWidth = Double.toString(width);
      String sHeight = Double.toString(height);

      result[0] = sWidth.substring(0, sWidth.indexOf("."));
      result[1] = sHeight.substring(0, sHeight.indexOf("."));
    } catch (Exception e) {
      result[0] = "60";
      result[1] = "60";
    }
    return result;
  }

  private static void redimPhoto(File inputFile, String outputFile,
      int widthParam, boolean watermark, String nameWatermark, int sizeWatermark)
      throws IOException {
    // création du buffer avec l'image d'origine
    BufferedImage inputBuf = ImageIO.read(inputFile);

    String[] widthAndHeight = getWidthAndHeight(inputBuf, widthParam);
    int width = Integer.parseInt(widthAndHeight[0]);
    int height = Integer.parseInt(widthAndHeight[1]);

    boolean higherQuality = gallerySettings.getBoolean("UseHigherQuality", true);

    BufferedImage scaledImage = getScaledInstance(inputBuf, width, height,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC, higherQuality);

    if (watermark) {
      Graphics2D g = (Graphics2D) scaledImage.getGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);

      // ajout du watermark sur la preview et les vignettes
      AlphaComposite alpha = AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER, 0.5f);
      g.setComposite(alpha);

      // ajout watermark noir
      g.setColor(Color.BLACK);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setFont(new Font("Arial", Font.BOLD, sizeWatermark));
      FontMetrics fontMetrics = g.getFontMetrics();
      Rectangle2D rect = fontMetrics.getStringBounds(nameWatermark, g);
      g.drawString(nameWatermark, ((int) width - (int) rect.getWidth())
          - sizeWatermark, ((int) height - (int) rect.getHeight())
          - sizeWatermark);

      // ajout watermark blanc
      g.setColor(Color.WHITE);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setFont(new Font("Arial", Font.BOLD, sizeWatermark));
      fontMetrics = g.getFontMetrics();
      rect = fontMetrics.getStringBounds(nameWatermark, g);
      g.drawString(nameWatermark, ((int) width - (int) rect.getWidth())
          - sizeWatermark / 2, ((int) height - (int) rect.getHeight())
          - sizeWatermark / 2);

      g.dispose();
    }

    // écriture du buffer sortie dans le fichier "outputFile" sur disque
    ImageIO.write(scaledImage, "JPEG", new File(outputFile));
  }

  public static BufferedImage getScaledInstance(BufferedImage img,
      int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
    // int type = (img.getTransparency() == Transparency.OPAQUE) ?
    // BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
    int type = BufferedImage.TYPE_INT_RGB;
    BufferedImage ret = (BufferedImage) img;
    int w, h;
    if (higherQuality) {
      // Use multi-step technique: start with original size, then
      // scale down in multiple passes with drawImage()
      // until the target size is reached
      w = img.getWidth();
      h = img.getHeight();
    } else {
      // Use one-step technique: scale directly from original
      // size to target size with a single drawImage() call
      w = targetWidth;
      h = targetHeight;
    }

    do {
      if (higherQuality && w > targetWidth) {
        w /= 2;
        if (w < targetWidth) {
          w = targetWidth;
        }
      }

      if (higherQuality && h > targetHeight) {
        h /= 2;
        if (h < targetHeight) {
          h = targetHeight;
        }
      }

      BufferedImage tmp = new BufferedImage(w, h, type);
      Graphics2D g2 = tmp.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
      g2.drawImage(ret, 0, 0, w, h, null);
      g2.dispose();

      ret = tmp;
    } while (w != targetWidth || h != targetHeight);

    return ret;
  }

  private static void createWatermark(String fileId, String name, String path,
      File dir, int percentSizeWatermark) throws IOException {
    String watermarkFile = path + fileId + "_watermark.jpg";

    // création du buffer avec l'image d'origine
    BufferedImage inputBuf = ImageIO.read(dir);
    double inputBufWidth = inputBuf.getWidth();
    double inputBufHeight = inputBuf.getHeight();

    // création du buffer a la même taille
    BufferedImage outputBuf = new BufferedImage((int) inputBufWidth,
        (int) inputBufHeight, BufferedImage.TYPE_INT_RGB);
    // Ajout du watermark (passage par le graphique pour mettre à jour le
    // buffer)
    Graphics2D g = (Graphics2D) outputBuf.getGraphics();
    g.drawImage(inputBuf, 0, 0, (int) inputBufWidth, (int) inputBufHeight,
        null);

    // opacité du texte de 50%
    AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
        0.5f);
    g.setComposite(alpha);

    double max = Math.max(inputBufWidth, inputBufHeight);

    // recherche de la taille du watermark en fonction de la taille de la photo
    int size = 8;
    if (max < 600) {
      size = 8;
    }
    if (max >= 600 && max < 750) {
      size = 10;
    }
    if (max >= 750 && max < 1000) {
      size = 12;
    }
    if (max >= 1000 && max < 1250) {
      size = 14;
    }
    if (max >= 1250 && max < 1500) {
      size = 16;
    }
    if (max >= 1500 && max < 1750) {
      size = 18;
    }
    if (max >= 1750 && max < 2000) {
      size = 20;
    }
    if (max >= 2000 && max < 2250) {
      size = 22;
    }
    if (max >= 2250 && max < 2500) {
      size = 24;
    }
    if (max >= 2500 && max < 2750) {
      size = 26;
    }
    if (max >= 2750 && max < 3000) {
      size = 28;
    }
    if (max >= 3000) {
      size = (int) Math.rint(max * percentSizeWatermark / 100);
    }

    // affichage d'un watermark noir
    g.setColor(Color.BLACK);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setFont(new Font("Arial", Font.BOLD, size));
    FontMetrics fontMetrics = g.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(name, g);

    g.drawString(name, ((int) inputBufWidth - (int) rect.getWidth()) - size,
        ((int) inputBufHeight - (int) rect.getHeight()) - size);

    // affichage d'un watermark blanc en décalé
    g.setColor(Color.WHITE);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setFont(new Font("Arial", Font.BOLD, size));
    fontMetrics = g.getFontMetrics();
    rect = fontMetrics.getStringBounds(name, g);
    // double angle = 3.14159265 / 2;
    // AffineTransform saveAT = g.getTransform();
    // saveAT.rotate(angle);
    // g.setTransform(saveAT);

    g.drawString(name,
        ((int) inputBufWidth - (int) rect.getWidth()) - size / 2,
        ((int) inputBufHeight - (int) rect.getHeight()) - size / 2);

    g.dispose();
    // écriture du buffer sortie dans le fichier "outputFile" sur disque
    File fileWatermark = new File(watermarkFile);
    ImageIO.write(outputBuf, "JPEG", fileWatermark);
  }

  public static void pasteImage(PhotoPK fromPK, PhotoDetail image, boolean cut) {
    PhotoPK toPK = image.getPhotoPK();

    String toAbsolutePath = FileRepositoryManager.getAbsolutePath(toPK.getInstanceId());
    String fromAbsolutePath = FileRepositoryManager.getAbsolutePath(fromPK.getInstanceId());

    String subDirectory = gallerySettings.getString("imagesSubDirectory");

    String fromDir = fromAbsolutePath + subDirectory + fromPK.getId()
        + File.separator;
    String toDir = toAbsolutePath + subDirectory + toPK.getId()
        + File.separator;

    // création du répertoire pour mettre la photo
    String nameRep = subDirectory + toPK.getId();
    try {
      FileRepositoryManager.createAbsolutePath(toPK.getInstanceId(), nameRep);
    } catch (Exception e) {
      SilverTrace.error("gallery", "ImageHelper.pasteImage",
          "root.MSG_GEN_PARAM_VALUE", "Unable to create dir : "
          + toAbsolutePath + nameRep, e);
    }

    // copier et renommer chaque image présente dans le répertoire d'origine
    File dirToCopy = new File(fromDir);
    if (dirToCopy.exists()) {
      // copier vignettes
      String fromImage = fromDir + fromPK.getId() + thumbnailSuffix_large;
      String toImage = toDir + toPK.getId() + thumbnailSuffix_large;
      pasteFile(fromImage, toImage, cut);

      fromImage = fromDir + fromPK.getId() + thumbnailSuffix_medium;
      toImage = toDir + toPK.getId() + thumbnailSuffix_medium;
      pasteFile(fromImage, toImage, cut);

      fromImage = fromDir + fromPK.getId() + thumbnailSuffix_small;
      toImage = toDir + toPK.getId() + thumbnailSuffix_small;
      pasteFile(fromImage, toImage, cut);

      // copier preview
      fromImage = fromDir + fromPK.getId() + previewSuffix;
      toImage = toDir + toPK.getId() + previewSuffix;
      pasteFile(fromImage, toImage, cut);

      // copier originale
      fromImage = fromDir + image.getImageName();
      toImage = toDir + image.getImageName();
      pasteFile(fromImage, toImage, cut);

      // copie original avec Watermark si elle existe
      fromImage = fromDir + fromPK.getId() + watermarkSuffix;
      toImage = toDir + toPK.getId() + watermarkSuffix;
      pasteFile(fromImage, toImage, cut);
    }
  }

  private static void pasteFile(String fromImage, String toImage, boolean cut) {
    File fromFile = null;
    File toFile = null;
    if (cut) {
      fromFile = new File(fromImage);
      toFile = new File(toImage);
      fromFile.renameTo(toFile);

      fromFile = null;
      toFile = null;
    } else {
      try {
        FileRepositoryManager.copyFile(fromImage, toImage);
      } catch (Exception e) {
        SilverTrace.error("gallery", "ImageHelper.pasteFile",
            "root.MSG_GEN_PARAM_VALUE", "Unable to copy file : fromImage = "
            + fromImage + ", toImage = " + toImage, e);
      }
    }
  }
}
