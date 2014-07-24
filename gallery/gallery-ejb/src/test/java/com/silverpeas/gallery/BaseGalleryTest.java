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
package com.silverpeas.gallery;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import com.stratelia.webactiv.util.DBUtil;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.dao.DAOBasedTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.gallery.dao.PhotoDaoTest;

/**
 * Base class for tests in the gallery component.
 * It prepares the database to use in tests.
 */
public abstract class BaseGalleryTest extends DAOBasedTest {

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"/spring-gallery-embbed-datasource.xml"};
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DBUtil.getInstanceForTest(getDataSource().getConnection());
  }

  @Override
  public void tearDown() throws Exception {
    try {
      super.tearDown();
    } finally {
      DBUtil.clearTestInstance();
    }
  }
}
