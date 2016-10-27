/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package com.silverpeas.gallery;

import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.constant.MediaResolution;
import com.silverpeas.gallery.media.DrewMediaMetadataExtractor;
import com.silverpeas.gallery.media.MediaMetadataException;
import com.silverpeas.gallery.media.MediaMetadataExtractor;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.InternalMedia;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.MetaData;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.model.Sound;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.gallery.processing.ImageResizer;
import com.silverpeas.gallery.processing.Watermarker;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.image.option.AbstractImageToolOption;
import org.silverpeas.image.option.DimensionOption;
import org.silverpeas.image.option.TransparencyColorOption;
import org.silverpeas.image.option.WatermarkTextOption;
import org.silverpeas.media.Definition;
import org.silverpeas.media.video.VideoThumbnailExtractor;
import org.silverpeas.media.video.VideoThumbnailExtractorFactory;
import org.silverpeas.notification.message.MessageManager;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.io.file.HandledFile;
import org.silverpeas.util.ImageLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.silverpeas.gallery.constant.MediaResolution.*;
import static com.silverpeas.util.StringUtil.defaultStringIfNotDefined;
import static com.silverpeas.util.StringUtil.isDefined;
import static org.silverpeas.image.ImageToolDirective.GEOMETRY_SHRINK;
import static org.silverpeas.image.ImageToolDirective.PREVIEW_WORK;
import static org.silverpeas.image.ImageToolFactory.getImageTool;

public class MediaHelper {

  /**
   * Saves uploaded sound file on file system
   * @param fileHandler
   * @param sound the current sound media
   * @param fileItem the current uploaded sound
   * @throws Exception
   */
  public synchronized static void processSound(final FileHandler fileHandler, Sound sound,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getName();
      if (name != null) {
        try {
          sound.setFileName(FileUtil.getFilename(name));
          final HandledFile handledSoundFile = getHandledFile(fileHandler, sound);
          handledSoundFile.copyInputStreamToFile(fileItem.getInputStream());
          new SoundProcess(handledSoundFile, sound).process();
        } finally {
          fileItem.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded sound file on file system (In case of drag And Drop upload)
   * @param fileHandler
   * @param sound the current sound media
   * @param uploadedFile the current uploaded sound
   * @throws Exception
   */
  public synchronized static void processSound(final FileHandler fileHandler, Sound sound,
      final File uploadedFile) throws Exception {
    if (uploadedFile != null) {
      try {
        sound.setFileName(uploadedFile.getName());
        final HandledFile handledSoundFile = getHandledFile(fileHandler, sound);
        fileHandler.copyFile(uploadedFile, handledSoundFile);
        new SoundProcess(handledSoundFile, sound).process();
      } finally {
        FileUtils.deleteQuietly(uploadedFile);
      }
    }
  }

  /**
   * Saves uploaded video file on file system
   * @param fileHandler
   * @param video the current video media
   * @param fileItem the current uploaded video
   * @throws Exception
   */
  public synchronized static void processVideo(final FileHandler fileHandler, Video video,
      final FileItem fileItem) throws Exception {
    if (fileItem != null) {
      String name = fileItem.getName();
      if (name != null) {
        try {
          video.setFileName(FileUtil.getFilename(name));
          final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
          handledVideoFile.copyInputStreamToFile(fileItem.getInputStream());
          new VideoProcess(handledVideoFile, video).process();
        } finally {
          fileItem.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded video file on file system (In case of drag And Drop upload)
   * @param fileHandler
   * @param video the current video media
   * @param uploadedFile the current uploaded video
   * @throws Exception
   */
  public synchronized static void processVideo(final FileHandler fileHandler, Video video,
      final File uploadedFile) throws Exception {
    if (uploadedFile != null) {
      try {
        video.setFileName(uploadedFile.getName());
        final HandledFile handledVideoFile = getHandledFile(fileHandler, video);
        fileHandler.copyFile(uploadedFile, handledVideoFile);
        new VideoProcess(handledVideoFile, video).process();
      } finally {
        FileUtils.deleteQuietly(uploadedFile);
      }
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks.
   * @param fileHandler
   * @param photo
   * @param image
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public synchronized static void processPhoto(final FileHandler fileHandler, final Photo photo,
      final FileItem image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    if (image != null) {
      String name = image.getName();
      if (name != null) {
        try {
          photo.setFileName(FileUtil.getFilename(name));
          final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
          handledImageFile.copyInputStreamToFile(image.getInputStream());
          new PhotoProcess(handledImageFile, photo, watermark, watermarkHD, watermarkOther)
              .process();
        } finally {
          image.delete();
        }
      }
    }
  }

  /**
   * Saves uploaded photo file on file system with associated thumbnails and watermarks. (In case of
   * drag And Drop upload)
   * @param fileHandler
   * @param photo
   * @param image
   * @param watermark
   * @param watermarkHD
   * @param watermarkOther
   * @throws Exception
   */
  public synchronized static void processPhoto(final FileHandler fileHandler, final Photo photo,
      final File image, final boolean watermark, final String watermarkHD,
      final String watermarkOther) throws Exception {
    if (image != null) {
      try {
        photo.setFileName(image.getName());
        final HandledFile handledImageFile = getHandledFile(fileHandler, photo);
        fileHandler.copyFile(image, handledImageFile);
        new PhotoProcess(handledImageFile, photo, watermark, watermarkHD, watermarkOther).process();
      } finally {
        FileUtils.deleteQuietly(image);
      }
    }
  }

  /**
   * Pastes media from a source to a destination.
   * @param fileHandler the file handler (space quota management).
   * @param fromPK the source.
   * @param media the destination.
   * @param cut true if it is a cut operation, false if it is a copy one.
   */
  public synchronized static void pasteInternalMedia(final FileHandler fileHandler,
      final MediaPK fromPK, final InternalMedia media, final boolean cut) {
    InternalMedia fromMedia = media.getType().newInstance();
    fromMedia.setMediaPK(fromPK);
    fromMedia.setFileName(media.getFileName());
    final HandledFile fromDir = getHandledFile(fileHandler, fromMedia).getParentHandledFile();
    final HandledFile toDir = getHandledFile(fileHandler, media).getParentHandledFile();

    // Copy and rename all media that exist into source folder
    if (fromDir.exists()) {

      // Copy thumbnails & watermark (only if it does exist)
      final String originalFileExt = "." + FilenameUtils.getExtension(media.getFileName());
      for (final MediaResolution mediaResolution : new MediaResolution[]{MEDIUM, SMALL, TINY,
          PREVIEW, LARGE, WATERMARK}) {
        String[] thumbnailSuffixes = {mediaResolution.getThumbnailSuffix() + originalFileExt,
            mediaResolution.getThumbnailSuffix() + ".jpg"};
        for (String thumbnailSuffix : thumbnailSuffixes) {
          final HandledFile source = fromDir.getHandledFile(fromPK.getId() + thumbnailSuffix);
          final HandledFile destination = toDir.getHandledFile(media.getId() + thumbnailSuffix);
          if (source.exists()) {
            pasteFile(source, destination, cut);
            break;
          }
        }
      }
      // Copy original image
      pasteFile(fromDir.getHandledFile(media.getFileName()), toDir.getHandledFile(media.
          getFileName()), cut);

      // On cut operation, deleting the source repo
      if (cut && !fromPK.getInstanceId().equals(media.getInstanceId())) {
        try {
          fromDir.delete();
        } catch (Exception e) {
          SilverTrace.error("gallery", "MediaHelper.pasteInternalMedia", "root.MSG_GEN_PARAM_VALUE",
              "Unable to delete source folder : folder path = " + fromDir.getFile().getPath(), e);
        }
      }
    }
  }

  private static void pasteFile(final HandledFile fromFile, final HandledFile toFile,
      final boolean cut) {
    if (fromFile.exists()) {
      try {
        if (cut) {
          fromFile.moveFile(toFile);
        } else {
          fromFile.copyFile(toFile);
        }
      } catch (final Exception e) {
        SilverTrace.error("gallery", "MediaHelper.pasteFile", "root.MSG_GEN_PARAM_VALUE",
            "Unable to copy file : fromImage = " + fromFile.getFile().getPath() + ", toImage = " +
                toFile.getFile().getPath(), e);
      }
    }
  }

  /**
   * Gets a handled file.
   * @param fileHandler
   * @param media
   * @return
   */
  private static HandledFile getHandledFile(FileHandler fileHandler, InternalMedia media) {
    if (StringUtil.isNotDefined(media.getFileName())) {
      throw new IllegalArgumentException("media.getFilename() must return a defined name");
    }
    return fileHandler.getHandledFile(Media.BASE_PATH, media.getComponentInstanceId(),
        media.getWorkspaceSubFolderName(), media.getFileName());
  }

  /**
   * Sets metadata to given instance which represents a photo in memory.
   * @param fileHandler the file handler (quota space management).
   * @param photo the photo to set.
   * @throws IOException
   * @throws MediaMetadataException
   */
  public static void setMetaData(final FileHandler fileHandler, final Photo photo)
      throws IOException, MediaMetadataException {
    setMetaData(fileHandler, photo, MessageManager.getLanguage());
  }

  private static void setMetaData(final FileHandler fileHandler, final Photo photo,
      final String lang) throws MediaMetadataException, IOException {
    if (MediaMimeType.JPG == photo.getFileMimeType()) {
      final HandledFile handledFile = fileHandler
          .getHandledFile(Media.BASE_PATH, photo.getInstanceId(), photo.getWorkspaceSubFolderName(),
              photo.getFileName());
      if (handledFile.exists()) {
        try {
          final MediaMetadataExtractor extractor = new DrewMediaMetadataExtractor(photo.
              getInstanceId());
          for (final MetaData meta : extractor
              .extractImageExifMetaData(handledFile.getFile(), lang)) {
            photo.addMetaData(meta);
          }
          for (final MetaData meta : extractor
              .extractImageIptcMetaData(handledFile.getFile(), lang)) {
            photo.addMetaData(meta);
          }
        } catch (UnsupportedEncodingException e) {
          SilverTrace.error("gallery", "MediaHelper.computeWatermarkText", "root.MSG_BAD_ENCODING",
              "Bad metadata encoding in image " + photo.getTitle() + ": " + e.getMessage());
        }
      }
    }
  }

  private MediaHelper() {
  }

  /**
   * In charge of processing an internal media.
   * @param <M>
   */
  private static abstract class MediaProcess<M extends InternalMedia> {

    private final HandledFile handledFile;
    private final M media;
    private final Set<MediaMimeType> supportedMimeTypes;

    private MediaMimeType physicalFileMimeType = null;
    private com.silverpeas.util.MetaData physicalFileMetaData = null;

    private MediaProcess(final HandledFile handledFile, final M media) {
      this.handledFile = handledFile;
      this.media = media;
      this.supportedMimeTypes = MediaMimeType.getSupportedMimeTypes(media.getType());
    }

    /**
     * Processes the media files.
     * @throws Exception
     */
    public void process() throws Exception {
      try {
        setInternalMetadata();
        generateFiles();
      } finally {
        close();
      }
    }

    /**
     * Generates specific media files.
     * @throws Exception
     */
    protected abstract void generateFiles() throws Exception;

    /**
     * Sets the internal metadata. If metadata
     * @return true if internal data have been set, false otherwise.
     * @throws GalleryRuntimeException if no supported mime type.
     */
    private void setInternalMetadata() throws Exception {
      File fileForData = getHandledFile().getFile();
      MediaMimeType mediaMimeType = getPhysicalFileMimeType();
      if (supportedMimeTypes.contains(mediaMimeType)) {
        getMedia().setFileName(fileForData.getName());
        getMedia().setFileMimeType(mediaMimeType);
        getMedia().setFileSize(fileForData.length());
        switch (getMedia().getType()) {
          case Photo:
            getMedia().getPhoto().setDefinition(getPhysicalFileMetaData().getDefinition());
            break;
          case Video:
            getMedia().getVideo().setDefinition(getPhysicalFileMetaData().getDefinition());
            break;
        }
        if (getPhysicalFileMetaData().getDuration() != null) {
          switch (getMedia().getType()) {
            case Video:
              getMedia().getVideo()
                  .setDuration(getPhysicalFileMetaData().getDuration().getTimeAsLong());
              break;
            case Sound:
              getMedia().getSound()
                  .setDuration(getPhysicalFileMetaData().getDuration().getTimeAsLong());
              break;
          }
        }
        if (StringUtil.isNotDefined(getMedia().getTitle()) &&
            StringUtil.isDefined(getPhysicalFileMetaData().getTitle())) {
          getMedia().setTitle(getPhysicalFileMetaData().getTitle());
        }
      } else {
        getMedia().setFileName(null);
        try {
          throw new GalleryRuntimeException("MediaHelper.setInternalMetadata",
              SilverpeasRuntimeException.ERROR,
              "Mime-Type of " + fileForData.getName() + " is not supported (" +
                  FileUtil.getMimeType(fileForData.getPath()) + ")");
        } finally {
          getHandledFile().delete();
        }
      }
    }

    /**
     * Gets the meta data of the physical file.
     * @return meta data.
     */
    com.silverpeas.util.MetaData getPhysicalFileMetaData() {
      if (physicalFileMetaData == null) {
        physicalFileMetaData =
            MetadataExtractor.getInstance().extractMetadata(getHandledFile().getFile());
      }
      return physicalFileMetaData;
    }

    /**
     * Gets lazily the mime type from the physical file which represents the media file.
     * @return the mime type of the physical file.
     */
    private MediaMimeType getPhysicalFileMimeType() {
      if (physicalFileMimeType == null) {
        physicalFileMimeType = MediaMimeType.fromFile(getHandledFile().getFile());
      }
      return physicalFileMimeType;
    }

    /**
     * Gets the handled physical file.
     * @return
     */
    HandledFile getHandledFile() {
      return handledFile;
    }

    /**
     * Gets the representation of the handled media.
     * @return the media instance.
     */
    public M getMedia() {
      return media;
    }

    /**
     * Closes all streams if any
     */
    protected void close() {
    }
  }

  private static class SoundProcess extends MediaProcess<Sound> {
    private SoundProcess(final HandledFile handledFile, final Sound media) {
      super(handledFile, media);
    }

    @Override
    protected void generateFiles() throws Exception {
      // No generation.
    }
  }

  private static class VideoProcess extends MediaProcess<Video> {
    private VideoProcess(final HandledFile handledFile, final Video media) {
      super(handledFile, media);
    }

    @Override
    protected void generateFiles() throws Exception {
      VideoThumbnailExtractor vte =
          VideoThumbnailExtractorFactory.getInstance().getVideoThumbnailExtractor();
      if (vte.isActivated()) {
        vte.generateThumbnailsFrom(getPhysicalFileMetaData(), getHandledFile().getFile());
      }
    }
  }

  private static class PhotoProcess extends MediaProcess<Photo> {
    private final boolean watermark;
    private final String watermarkHD;
    private final String watermarkOther;

    private BufferedImage bufferedImage = null;
    private List<MetaData> iptcMetadata = null;

    private PhotoProcess(final HandledFile handledFile, final Photo photo, final boolean watermark,
        final String watermarkHD, final String watermarkOther) {
      super(handledFile, photo);
      this.watermark = watermark;
      this.watermarkHD = watermarkHD;
      this.watermarkOther = watermarkOther;
    }

    @Override
    protected void generateFiles() throws Exception {

      final Photo photo = getMedia();
      if (photo.isPreviewable()) {

        // Registering the size of the image
        registerResolutionData();

        // Computing watermark data and retrieving the name of the author
        final String nameForWatermark = computeWatermarkText();

        // Creating preview and thumbnails
        try {
          createThumbnails(nameForWatermark);
        } catch (final Exception e) {
          SilverTrace
              .error("gallery", "MediaHelper.createImage", "gallery.ERR_CANT_CREATE_THUMBNAILS",
                  "image = " + photo.getTitle() + " (#" + photo.getId() + ")");
        }
      }
    }

    /**
     * Gets lazily the buffered image instance of photo.
     * @return the buffered instance.
     * @throws Exception
     */
    private BufferedImage getBufferedImage() throws Exception {
      if (bufferedImage == null) {
        bufferedImage = ImageLoader.loadImage(getHandledFile().getFile());
      }
      return bufferedImage;
    }

    /**
     * Registers the resolution of a photo.
     */
    private void registerResolutionData() throws Exception {
      if (getMedia().getDefinition().getWidth() != 0 &&
          getMedia().getDefinition().getHeight() != 0) {
        // definition already set.
        return;
      }
      final BufferedImage image = getBufferedImage();
      if (image == null) {
        getMedia().setDefinition(Definition.fromZero());
      } else {
        getMedia().setDefinition(Definition.of(image.getWidth(), image.getHeight()));
      }
    }

    /**
     * Creates all the thumbnails around a photo.
     * @param nameWatermark
     * @throws Exception
     */
    private void createThumbnails(final String nameWatermark) throws Exception {
      Photo photo = getMedia();

      // File name
      final String photoId = photo.getId();

      // Processing order :
      // Large (preview without watermark)
      // Preview
      // Medium
      // Small
      // Tiny
      final MediaResolution[] mediaResolutions =
          new MediaResolution[]{LARGE, PREVIEW, MEDIUM, SMALL, TINY};
      final HandledFile originalFile = getHandledFile();
      HandledFile source = originalFile;
      final String originalFileExt = "." + FilenameUtils.getExtension(photo.getFileName());
      for (MediaResolution mediaResolution : mediaResolutions) {
        HandledFile currentThumbnail = originalFile.getParentHandledFile()
            .getHandledFile(photoId + mediaResolution.getThumbnailSuffix() + originalFileExt);
        generateThumbnail(source, currentThumbnail, mediaResolution, nameWatermark);
        // The first thumbnail that has to be created must be the larger one and without watermark.
        // This first thumbnail is cached and reused for the following thumbnail creation.
        if (source == originalFile) {
          source = currentThumbnail;
        }
      }
    }

    /**
     * Return the written file
     * @param sourceFile
     * @param outputFile
     * @param mediaResolution
     * @param watermarkAuthorName
     * @throws Exception
     */
    private void generateThumbnail(final HandledFile sourceFile, final HandledFile outputFile,
        MediaResolution mediaResolution, final String watermarkAuthorName) throws Exception {
      final boolean watermarkToApply =
          mediaResolution.isWatermarkApplicable() && isDefined(watermarkAuthorName);
      final Definition definition = getMedia().getDefinition();
      final boolean resizeToPerform = definition.getWidth() > mediaResolution.getWidth() ||
          definition.getHeight() > mediaResolution.getHeight();
      if (!resizeToPerform && !watermarkToApply) {
        // Simple copy
        sourceFile.copyFile(outputFile);
        return;
      }
      if (getImageTool().isActivated()) {

        // Optimized media processing
        Set<AbstractImageToolOption> options = new HashSet<AbstractImageToolOption>();
        if (resizeToPerform) {
          options.add(DimensionOption
              .widthAndHeight(mediaResolution.getWidth(), mediaResolution.getHeight()));
        }
        if (watermarkToApply) {
          options.add(WatermarkTextOption.text(watermarkAuthorName).withFont("Arial"));
        }
        getImageTool().convert(sourceFile.getFile(), outputFile.getFile(), options, PREVIEW_WORK,
            GEOMETRY_SHRINK);
        // No other treatments is needed
        return;
      }

      OutputStream os = null;
      BufferedImage image = null;
      try {
        image = ImageLoader.loadImage(sourceFile.getFile());
        os = outputFile.openOutputStream();
        final int originalMaxSize = Math.max(definition.getWidth(), definition.getHeight());
        final int resizeWidth = Math.min(originalMaxSize, mediaResolution.getWidth());
        final ImageResizer resizer = new ImageResizer(image, resizeWidth);
        if (watermarkToApply) {
          final int watermarkSize =
              mediaResolution.getWatermarkSize() != null ? mediaResolution.getWatermarkSize() : 0;
          resizer.resizeImageWithWatermark(os, watermarkAuthorName, watermarkSize);
        } else {
          resizer.resizeImage(os);
        }
      } finally {
        if (os != null) {
          IOUtils.closeQuietly(os);
        }
        if (image != null) {
          image.flush();
        }
      }
    }

    private void createWatermark(final OutputStream watermarkedTargetStream,
        final String watermarkLabel) throws Exception {

      final BufferedImage image = getBufferedImage();
      final int imageWidth = image.getWidth();
      final int imageHeight = image.getHeight();

      // création du buffer a la même taille
      final BufferedImage outputBuf =
          new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

      try {
        // recherche de la taille du watermark en fonction de la taille de la photo
        int size = getWatermarkSizeSize(Math.max(imageWidth, imageHeight));
        final Watermarker watermarker = new Watermarker(imageWidth, imageHeight);
        watermarker
            .addWatermark(image, outputBuf, new Font("Arial", Font.BOLD, size), watermarkLabel,
                size);
        ImageIO.write(outputBuf, "JPEG", watermarkedTargetStream);
      } finally {
        outputBuf.flush();
      }
    }

    private int getWatermarkSizeSize(final int max) {
      int size = 8;
      if (max < 600) {
        size = 8;
      } else if (max >= 3000) {
        int percentSizeWatermark = GalleryComponentSettings.getWatermarkPercentSize();
        size = (int) Math.rint(max * percentSizeWatermark / 100);
      } else {
        final int offset = 250;
        int inf = 500;
        int sup = inf + 250;
        do {
          size += 2;
          inf = sup;
          sup += offset;
        } while (inf <= max && max < sup);
      }
      return size;
    }

    private String computeWatermarkText() throws Exception {
      String nameAuthor = "";
      String nameForWatermark = "";
      Photo photo = getMedia();
      if (watermark && photo.getFileMimeType().isIPTCCompliant()) {
        try {
          if (StringUtil.isDefined(watermarkHD)) {
            // Photo duplication that is stamped with a Watermark.
            nameAuthor = defaultStringIfNotDefined(getWatermarkValue(watermarkHD), nameAuthor);
            if (!nameAuthor.isEmpty()) {
              final HandledFile watermarkFile = getHandledFile().getParentHandledFile()
                  .getHandledFile(photo.getId() + "_watermark.jpg");
              if (getImageTool().isActivated()) {
                AbstractImageToolOption option = WatermarkTextOption.text(nameAuthor);
                getImageTool().convert(getHandledFile().getFile(), watermarkFile.getFile(), option);
              } else {
                OutputStream watermarkStream = null;
                try {
                  watermarkStream = watermarkFile.openOutputStream();
                  createWatermark(watermarkStream, nameAuthor);
                } finally {
                  IOUtils.closeQuietly(watermarkStream);
                }
              }
            }
          }
          if (StringUtil.isDefined(watermarkOther)) {
            nameAuthor = defaultStringIfNotDefined(getWatermarkValue(watermarkOther), nameAuthor);
            if (!nameAuthor.isEmpty()) {
              nameForWatermark = nameAuthor;
            }
          }
        } catch (MediaMetadataException e) {
          SilverTrace
              .error("gallery", "MediaHelper.computeWatermarkText", "root.MSG_BAD_FILE_FORMAT",
                  "Bad image file format " + getHandledFile().getFile().getPath() + ": " +
                      e.getMessage());
        } catch (UnsupportedEncodingException e) {
          SilverTrace.error("gallery", "MediaHelper.computeWatermarkText", "root.MSG_BAD_ENCODING",
              "Bad metadata encoding in image " + getHandledFile().getFile().getPath() + ": " +
                  e.getMessage());
        }
      }
      return nameForWatermark;
    }

    /**
     * Gets lazily the IPTC data from a photo.
     * @return
     * @throws MediaMetadataException
     * @throws IOException
     */
    private List<MetaData> getIptcMetaData() throws MediaMetadataException, IOException {
      if (iptcMetadata == null) {
        final MediaMetadataExtractor extractor =
            new DrewMediaMetadataExtractor(getMedia().getInstanceId());
        iptcMetadata = extractor.extractImageIptcMetaData(getHandledFile().getFile());
      }
      return iptcMetadata;
    }

    private String getWatermarkValue(final String property) throws Exception {
      String value = null;
      final List<MetaData> iptcMetadata = getIptcMetaData();
      for (final MetaData metadata : iptcMetadata) {
        if (property.equalsIgnoreCase(metadata.getProperty())) {
          value = metadata.getValue();
        }
      }
      return value;
    }

    @Override
    protected void close() {
      super.close();
      if (bufferedImage != null) {
        bufferedImage.flush();
      }
    }
  }
}
