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
 * FLOSS exception.  You should have received a copy of the text describing
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

/*
 * WebSitesInstanciator.java
 *
 * Created on 2 Mai 2001, 12:19
 */

package com.stratelia.webactiv.webSites;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.node.NodeInstanciator;
import com.stratelia.webactiv.publication.PublicationInstanciator;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class WebSitesInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator()
      .getString("ApplicationURL");

  private static ResourceLocator uploadSettings = new ResourceLocator(
      "com.stratelia.webactiv.webSites.settings.webSiteSettings", "fr");

  /** Creates new WebSiteInstanciator */
  public WebSitesInstanciator() {
    super("com.stratelia.webactiv.webSites");
  }

  /**
   * create
   */
  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("websites", "WebSitesInstanciator.create()",
        "webSites.MSG_CREATE_WITH_SPACE_AND_COMPONENT", "space : " + spaceId
        + "component : " + componentId);

    // create publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.webSites");
    pub.create(con, spaceId, componentId, userId);

    // create node component
    NodeInstanciator node = new NodeInstanciator(
        "com.stratelia.webactiv.webSites");
    node.create(con, spaceId, componentId, userId);

    setInsertQueries();

    insertSpecialNode(con, componentId, userId);

    try {
      // Create the attachments directory on the server disk
      createAttachmentsAndImagesDirectory(spaceId, componentId);
    } catch (Exception e) {
      // Debug.error(100, "WebSitesInstanciator.create()",
      // "createAttachmentsAndImagesDirectory() failed !", e, null);
      throw new InstanciationException("WebSitesInstanciator.create()",
          SilverpeasException.ERROR,
          "webSites.EX_CREATE_ATACHMENTS_AND_IMAGES_DIRECTORY_FAIL",
          "spaceId = " + spaceId + " componentId = " + componentId, e);
    }

    SilverTrace.info("websites", "WebSitesInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * delete
   */
  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("websites", "WebSitesInstanciator.delete()",
        "webSites.MSG_DELETE_WITH_SPACE", "spaceId : " + spaceId);

    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();

    deleteDataOfInstance(con, componentId, "siteIcons");
    deleteDataOfInstance(con, componentId, "site");

    // delete publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.webSites");
    pub.delete(con, spaceId, componentId, userId);

    // delete node component
    NodeInstanciator node = new NodeInstanciator(
        "com.stratelia.webactiv.webSites");
    node.delete(con, spaceId, componentId, userId);

    try {
      // Delete the attachments directory on the server disk
      deleteAttachmentsAndImagesDirectory(spaceId, componentId);
    } catch (Exception e) {
      throw new InstanciationException("WebSitesInstanciator.delete()",
          SilverpeasException.ERROR,
          "webSites.EX_DELETE_ATACHMENTS_AND_IMAGES_DIRECTORY_FAIL",
          "spaceId = " + spaceId + " componentId = " + componentId, e);
    }

    SilverTrace.info("websites", "WebSitesInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Delete all data of one website instance from the website table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component website.
   * @param suffixName (String) the suffixe of a website table
   */
  private void deleteDataOfInstance(Connection con, String componentId,
      String suffixName) throws InstanciationException {

    Statement stmt = null;

    // get the delete query from the external file
    String deleteQuery = getDeleteQuery(componentId, suffixName);

    // execute the delete query
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      stmt.close();
    } catch (SQLException se) {
      InstanciationException ie = new InstanciationException(
          "WebSitesInstanciator.deleteDataOfInstance()",
          SilverpeasException.ERROR, "root.DELETING_DATA_OF_INSTANCE_FAILED",
          "componentId = " + componentId + " deleteQuery = " + deleteQuery, se);
      throw ie;
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        InstanciationException ie = new InstanciationException(
            "WebSitesInstanciator.deleteDataOfInstance()",
            SilverpeasException.ERROR, "root.EX_RESOURCE_CLOSE_FAILED",
            "componentId = " + componentId + " deleteQuery = " + deleteQuery,
            err_closeStatement);
        throw ie;
      }
    }

  }

  private void insertSpecialNode(Connection con, String componentId,
      String userId) throws InstanciationException {
    // Insert the line corresponding to the Root
    String insertQuery = getInsertQuery(componentId, "Accueil");

    String creationDate = DateUtil.today2SQLDate();
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      InstanciationException ie = new InstanciationException(
          "WebSitesInstanciator.insertSpecialNode()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          " insertQuery = " + insertQuery, se);
      throw ie;
    }
  }

  /**
   * createAttachmentsAndImagesDirectory
   */
  private void createAttachmentsAndImagesDirectory(String spaceId,
      String componentId) throws java.lang.Exception {
    SilverTrace.info("websites",
        "WebSitesInstanciator.createAttachmentsAndImagesDirectory()",
        "webSites.MSG_CREATE_ATTACHMENTS_DIRECTORY_WITH_SPACE_AND_COMPONENT",
        "space : " + spaceId + "component : " + componentId);

    File spaceDirectory = new File(uploadSettings.getString("uploadsPath")
        + uploadSettings.getString("Context"));

    if (spaceDirectory.exists()) {
      /* creation du repertoire webSite10 */
      FileFolderManager.createFolder(uploadSettings.getString("uploadsPath")
          + uploadSettings.getString("Context") + File.separator + componentId);
    }

    else {
      /* creation du repertoire WebSiteUploads */
      FileFolderManager.createFolder(uploadSettings.getString("uploadsPath")
          + uploadSettings.getString("Context"));
      /* creation du repertoire webSite10 */
      FileFolderManager.createFolder(uploadSettings.getString("uploadsPath")
          + uploadSettings.getString("Context") + File.separator + componentId);
    }
  }

  /**
   * deleteAttachmentsAndImagesDirectory
   */
  private void deleteAttachmentsAndImagesDirectory(String spaceId,
      String componentId) throws java.lang.Exception {
    SilverTrace.info("websites",
        "WebSitesInstanciator.deleteAttachmentsAndImagesDirectory()",
        "webSites.MSG_DELETE_ATTACHMENTS_DIRECTORY_WITH_SPACE_AND_COMPONENT",
        "space : " + spaceId + "component : " + componentId);

    FileFolderManager.deleteFolder(uploadSettings.getString("uploadsPath")
        + uploadSettings.getString("Context") + File.separator + componentId);
  }
}