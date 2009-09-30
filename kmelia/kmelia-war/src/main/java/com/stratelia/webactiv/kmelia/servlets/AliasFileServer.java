package com.stratelia.webactiv.kmelia.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class AliasFileServer extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("kmelia", "AliasFileServer.doPost",
        "root.MSG_GEN_ENTER_METHOD");

    String userId = "undefined";
    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController");
    if (mainSessionCtrl != null)
      userId = mainSessionCtrl.getUserId();

    String mimeType = "undefined";
    String sourceFile = "undefined";
    String directory = "undefined";
    String fileComponentId = "undefined";
    String filePublicationId = "undefined";
    String contextComponentId = req.getParameter("ComponentId");
    WAPrimaryKey foreignKey = null;

    String attachmentId = req.getParameter("AttachmentId");
    String language = req.getParameter("lang");
    AttachmentDetail attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      // Check first if attachment exists
      attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
          attachmentId));
      if (attachment != null) {
        mimeType = attachment.getType(language);
        sourceFile = attachment.getPhysicalName(language);
        foreignKey = attachment.getForeignKey();
        directory = FileRepositoryManager.getRelativePath(FileRepositoryManager
            .getAttachmentContext(attachment.getContext()));
      }
    }

    String documentId = req.getParameter("DocumentId");
    if (StringUtil.isDefined(documentId)) {
      String versionId = req.getParameter("VersionId");
      VersioningBm versioning = getVersioningBm();
      DocumentVersionPK versionPK = new DocumentVersionPK(Integer
          .parseInt(versionId), "useless", "useless");
      DocumentVersion version = versioning.getDocumentVersion(versionPK);

      if (version != null) {
        mimeType = version.getMimeType();
        sourceFile = version.getPhysicalName();
        fileComponentId = version.getInstanceId();

        String[] path = new String[1];
        path[0] = "Versioning";
        directory = FileRepositoryManager.getRelativePath(path);

        Document document = versioning.getDocument(version.getDocumentPK());
        foreignKey = document.getForeignKey();
      }
    }

    if (foreignKey != null) {
      fileComponentId = foreignKey.getInstanceId();
      filePublicationId = foreignKey.getId();

      PublicationPK pubPK = new PublicationPK(filePublicationId,
          fileComponentId);

      List aliases = (List) getPublicationBm().getAlias(pubPK);

      // check if user have rights to see alias files
      Alias alias;
      boolean rightsOK = false;
      KmeliaSecurity security = new KmeliaSecurity();
      for (int a = 0; !rightsOK && a < aliases.size(); a++) {
        alias = (Alias) aliases.get(a);
        if (!contextComponentId.equals(alias.getInstanceId())) {
          // it's an alias
          // Check if user is allowed to see topic's content
          rightsOK = security.isAccessAuthorized(alias.getInstanceId(), userId,
              alias.getId(), "Node");
        }
      }

      if (rightsOK) {
        String filePath = FileRepositoryManager
            .getAbsolutePath(fileComponentId)
            + directory + File.separator + sourceFile;

        res.setContentType(mimeType);

        display(res, filePath);
      }
    }
  }

  /**
   * This method writes the result of the preview action.
   * 
   * @param res
   *          - The HttpServletResponse where the html code is write
   * @param htmlFilePath
   *          - the canonical path of the html document generated by the parser
   *          tools. if this String is null that an exception had been catched
   *          the html document generated is empty !! also, we display a warning
   *          html page
   */
  private void display(HttpServletResponse res, String htmlFilePath)
      throws IOException {
    BufferedInputStream input = null;
    OutputStream out2 = res.getOutputStream();
    SilverTrace.info("kmelia", "AliasFileServer.display()",
        "root.MSG_GEN_ENTER_METHOD", " htmlFilePath " + htmlFilePath);
    try {
      input = new BufferedInputStream(new FileInputStream(htmlFilePath));
      int read = input.read();
      if (read == -1) {
        displayWarningHtmlCode(res);
      } else {
        while (read != -1) {
          out2.write(read); // writes bytes into the response
          read = input.read();
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "AliasFileServer.doPost",
          "root.EX_CANT_READ_FILE", "file name=" + htmlFilePath);
      displayWarningHtmlCode(res);
    } finally {
      // we must close the in and out streams
      try {
        if (input != null)
          input.close();
        out2.close();
      } catch (Exception e) {
        SilverTrace.warn("kmelia", "AliasFileServer.display",
            "root.EX_CANT_READ_FILE", "close failed");
      }
    }
  }

  // Add By Mohammed Hguig

  private void displayWarningHtmlCode(HttpServletResponse res)
      throws IOException {
    OutputStream out2 = res.getOutputStream();
    ResourceLocator resourceLocator = new ResourceLocator(
        "com.stratelia.webactiv.util.peasUtil.multiLang.fileServerBundle", "");

    StringReader sr = new StringReader(resourceLocator.getString("warning"));
    try {
      int read = sr.read();
      while (read != -1) {
        out2.write(read); // writes bytes into the response
        read = sr.read();
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "AliasFileServer.displayWarningHtmlCode",
          "root.EX_CANT_READ_FILE", "warning properties");
    } finally {
      try {
        if (sr != null)
          sr.close();
        out2.close();
      } catch (Exception e) {
        SilverTrace.warn("kmelia", "AliasFileServer.displayHtmlCode",
            "root.EX_CANT_READ_FILE", "close failed");
      }
    }
  }

  private VersioningBm getVersioningBm() {
    try {
      VersioningBmHome vscEjbHome = (VersioningBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
      return vscEjbHome.create();
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "AliasFileServer.getVersioningBm",
          "root.EX_CANT_GET_EJB");
      return null;
    }
  }

  private PublicationBm getPublicationBm() {
    try {
      PublicationBmHome ejbHome = (PublicationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
              PublicationBmHome.class);
      return ejbHome.create();
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "AliasFileServer.getPublicationBm",
          "root.EX_CANT_GET_EJB");
      return null;
    }
  }

}
