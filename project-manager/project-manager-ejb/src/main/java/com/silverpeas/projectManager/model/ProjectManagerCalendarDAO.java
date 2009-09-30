/*
 * Created on 25 oct. 2004
 *
 */
package com.silverpeas.projectManager.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * @author neysseri
 * 
 */
public class ProjectManagerCalendarDAO {

  // the date format used in database to represent a date
  private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
  private final static String PROJECTMANAGER_CALENDAR_TABLENAME = "SC_ProjectManager_Calendar";

  public static void addHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException, UtilException {
    SilverTrace.info("projectManager",
        "ProjectManagerCalendarDAO.addHolidayDate()",
        "root.MSG_GEN_ENTER_METHOD", holiday.getDate().toString());

    if (!isHolidayDate(con, holiday)) {
      StringBuffer insertStatement = new StringBuffer(128);
      insertStatement.append("insert into ").append(
          PROJECTMANAGER_CALENDAR_TABLENAME);
      insertStatement.append(" values ( ? , ? , ? )");
      PreparedStatement prepStmt = null;

      try {
        prepStmt = con.prepareStatement(insertStatement.toString());

        prepStmt.setString(1, date2DBDate(holiday.getDate()));
        prepStmt.setInt(2, holiday.getFatherId());
        prepStmt.setString(3, holiday.getInstanceId());

        prepStmt.executeUpdate();
      } finally {
        DBUtil.close(prepStmt);
      }
    }
  }

  public static void removeHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuffer deleteStatement = new StringBuffer(128);
    deleteStatement.append("delete from ").append(
        PROJECTMANAGER_CALENDAR_TABLENAME);
    deleteStatement.append(" where holidayDate = ? ");
    deleteStatement.append(" and fatherId = ? ");
    deleteStatement.append(" and instanceId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());

      prepStmt.setString(1, date2DBDate(holiday.getDate()));
      prepStmt.setInt(2, holiday.getFatherId());
      prepStmt.setString(3, holiday.getInstanceId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static boolean isHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" where holidayDate = ? ");
    query.append(" and fatherId = ? ");
    query.append(" and instanceId = ? ");

    SilverTrace.info("projectManager",
        "ProjectManagerCalendarDAO.isHolidayDate()",
        "root.MSG_GEN_PARAM_VALUE", "date = " + holiday.getDate().toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());

      stmt.setString(1, date2DBDate(holiday.getDate()));
      stmt.setInt(2, holiday.getFatherId());
      stmt.setString(3, holiday.getInstanceId());

      rs = stmt.executeQuery();

      return rs.next();
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List getHolidayDates(Connection con, String instanceId)
      throws SQLException {
    List holidayDates = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" where instanceId = ? ");
    query.append("order by holidayDate ASC");

    SilverTrace.info("projectManager",
        "ProjectManagerCalendarDAO.getHolidayDates()",
        "root.MSG_GEN_PARAM_VALUE", "instanceId = " + instanceId);

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates
            .add(dbDate2Date(rs.getString("holidayDate"), "holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static List getHolidayDates(Connection con, String instanceId,
      Date beginDate, Date endDate) throws SQLException {
    List holidayDates = new ArrayList();
    StringBuffer query = new StringBuffer(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" where instanceId = ? ");
    query.append(" and ? <= holidayDate ");
    query.append(" and holidayDate <= ? ");
    query.append("order by holidayDate ASC");

    SilverTrace.info("projectManager",
        "ProjectManagerCalendarDAO.getHolidayDates()",
        "root.MSG_GEN_PARAM_VALUE", "instanceId = " + instanceId
            + ", beginDate=" + beginDate.toString() + ", endDate="
            + endDate.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      stmt.setString(2, date2DBDate(beginDate));
      stmt.setString(3, date2DBDate(endDate));
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates
            .add(dbDate2Date(rs.getString("holidayDate"), "holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static String date2DBDate(Date date) {
    String dbDate = formatter.format(date);
    return dbDate;
  }

  private static Date dbDate2Date(String dbDate, String fieldName)
      throws SQLException {
    Date date = null;
    try {
      date = formatter.parse(dbDate);
    } catch (ParseException e) {
      throw new SQLException("ProjectManagerCalendarDAO : dbDate2Date("
          + fieldName + ") : format unknown " + e.toString());
    }
    return date;
  }
}