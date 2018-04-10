/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.jdbcconnector.control;

import org.silverpeas.components.jdbcconnector.model.DataSourceDefinition;
import org.silverpeas.components.jdbcconnector.service.JdbcConnectorException;
import org.silverpeas.components.jdbcconnector.service.JdbcConnectorRuntimeException;
import org.silverpeas.components.jdbcconnector.service.JdbcRequester;
import org.silverpeas.components.jdbcconnector.service.TableRow;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternal;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The web controller of the ConnecteurJDBC application. Like all of the web controllers in
 * Silverpeas, it is both session-scoped and spawn per application instance.
 * @author mmoquillon
 */
@WebComponentController("connecteurJDBC")
public class JdbcConnectorWebController extends
    org.silverpeas.core.web.mvc.webcomponent
        .WebComponentController<JdbcConnectorWebRequestContext> {

  private static final String TRANSLATIONS_PATH =
      "org.silverpeas.jdbcConnector.multilang.jdbcConnector";

  public static final String RESULT_SET = "resultSet";
  public static final String COMPARING_COLUMN = "comparingColumn";
  public static final String COMPARING_OPERATOR = "currentComparator";
  public static final String COMPARING_VALUE = "columnValue";
  public static final String COMPARING_OPERATORS = "comparators";

  private JdbcRequester requester;
  private List<TableRow> result = Collections.emptyList();
  private TableRowsFilter filter = new TableRowsFilter();

  /**
   * Constructs a new Web controller for the specified context and with the
   * {@link MainSessionController} instance that is specific to the user behind the access to the
   * underlying application instance.
   * @param controller the main session controller for the current user.
   * @param context the context identifying among others the targeted application instance.
   */
  public JdbcConnectorWebController(final MainSessionController controller,
      final ComponentContext context) {
    super(controller, context, TRANSLATIONS_PATH);
  }

  @Override
  protected void onInstantiation(final JdbcConnectorWebRequestContext context) {
    requester = new JdbcRequester(context.getComponentInstanceId());
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("jdbcConnector.jsp")
  @LowestRoleAccess(value = SilverpeasRole.reader)
  public void home(final JdbcConnectorWebRequestContext context) {
    if (requester.isDataSourceDefined()) {
      String reload = context.getRequest().getParameter("reload");
      if (StringUtil.getBooleanValue(reload)) {
        clearQueryResult();
      }
      executeSQLQuery();
    }
    setQueryResult(context.getRequest());
  }

  @GET
  @Path("portlet")
  @RedirectToInternalJsp("portlet.jsp")
  public void portlet(final JdbcConnectorWebRequestContext context) {
    home(context);
  }

  @GET
  @Path("ParameterRequest")
  @RedirectToInternalJsp("requestParameters.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher, onError = @RedirectTo("Main"))
  public void editSQLRequest(final JdbcConnectorWebRequestContext context) {
    HttpRequest request = context.getRequest();
    request.setAttribute("sqlRequest", requester.getCurrentConnectionInfo().getSqlRequest());
    request.setAttribute("editorUrl",
        request.getContextPath() + getComponentUrl() + "/RequestEditor");
  }

  @POST
  @Path("DoRequest")
  @RedirectToInternalJsp("jdbcConnector.jsp")
  @LowestRoleAccess(value = SilverpeasRole.reader)
  public void performSQLRequest(final JdbcConnectorWebRequestContext context) {
    if (requester.isDataSourceDefined()) {
      readRequestParameters(context.getRequest());
      executeSQLQuery();
    }
    setQueryResult(context.getRequest());
  }

  @GET
  @Path("ParameterConnection")
  @RedirectToInternalJsp("connectionParameters.jsp")
  @LowestRoleAccess(value = SilverpeasRole.admin, onError = @RedirectTo("Main"))
  public void editConnection(final JdbcConnectorWebRequestContext context) {
    context.getRequest()
        .setAttribute("currentConnectionInfo", requester.getCurrentConnectionInfo());
    context.getRequest().setAttribute("availableDataSources", DataSourceDefinition.getAll());
  }

  @POST
  @Path("UpdateConnection")
  @RedirectToInternal("{nextView}")
  @LowestRoleAccess(value = SilverpeasRole.publisher, onError = @RedirectTo("Main"))
  public void saveConnection(final JdbcConnectorWebRequestContext context) {
    String nextView = "ParameterRequest";
    String dataSource = context.getRequest().getParameter("DataSource");
    String login = context.getRequest().getParameter("Login");
    String password = context.getRequest().getParameter("Password");
    int rowLimit = 0;
    if (context.getRequest().isParameterDefined("RowLimit")) {
      rowLimit = context.getRequest().getParameterAsInteger("RowLimit");
    }
    requester.getCurrentConnectionInfo()
        .withDataSourceName(dataSource)
        .withLoginAndPassword(login, password)
        .withDataMaxNumber(rowLimit);
    // we check the connection: it is saved only if the connection with the data source can be
    // established. In that case, any previous SQL query and its result are cleared.
    try {
      requester.checkConnection();
      requester.getCurrentConnectionInfo().setSqlRequest("");
      requester.getCurrentConnectionInfo().save();
      clearQueryResult();
    } catch (JdbcConnectorException e) {
      SilverLogger.getLogger(this).error(e);
      MessageNotifier.addError(getString("erreurParametresConnectionIncorrects"));
      nextView = "ParameterConnection";
    }
    context.addRedirectVariable("nextView", nextView);
  }

  @POST
  @Path("SetSQLRequest")
  @RedirectToInternal("{nextView}")
  @LowestRoleAccess(value = SilverpeasRole.publisher, onError = @RedirectTo("Main"))
  public void saveSQLRequest(final JdbcConnectorWebRequestContext context) {
    String nextView = "Main";
    String sqlRequest = context.getRequest().getParameter("SQLReq");
    Optional<String> validationFailure = validateSQLRequest(sqlRequest);
    if (validationFailure.isPresent()) {
      MessageNotifier.addError(
          getString("erreurRequeteIncorrect") + ": " + validationFailure.get());
      nextView = "ParameterRequest";
    } else {
      requester.getCurrentConnectionInfo().withSqlRequest(sqlRequest.trim()).save();
      clearQueryResult();
    }
    context.addRedirectVariable("nextView", nextView);
  }

  @GET
  @Path("RequestEditor")
  @RedirectToInternalJsp("requestEditor.jsp")
  @LowestRoleAccess(value = SilverpeasRole.publisher, onError = @RedirectTo("Main"))
  public void openRequestEditor(final JdbcConnectorWebRequestContext context) {
    HttpRequest request = context.getRequest();
    try {
      List<String> tableNames = requester.getTableNames();
      Map<String, String> tables = new LinkedHashMap<>(tableNames.size());
      tableNames.forEach(t -> tables.put(t,
          requester.getColumnNames(t).stream().collect(Collectors.joining(","))));
      request.setAttribute("tables", tables);
      request.setAttribute(COMPARING_OPERATORS, TableRowsFilter.getAllComparators());
    } catch (JdbcConnectorRuntimeException e) {
      SilverLogger.getLogger(this).error(e);
      MessageNotifier.addError(getString("sqlRequestExecutionFailure"));
    }
  }

  private Optional<String> validateSQLRequest(String request) {
    Optional<String> validationFailure = Optional.empty();
    String sqlQuery = request.trim();
    if (!requester.isDataSourceDefined()) {
      validationFailure = Optional.of(getString("erreurParametresConnectionIncorrects"));
    } else if (StringUtil.isNotDefined(request)) {
      validationFailure = Optional.of(getString("erreurRequeteVide"));
    } else if (!sqlQuery.toLowerCase().startsWith("select")) {
      validationFailure = Optional.of(getString("erreurModifTable"));
    } else {
      try {
        result = requester.request(sqlQuery);
      } catch (JdbcConnectorException e) {
        SilverLogger.getLogger(this).error("Error while validating SQL request: " + request, e);
        validationFailure = Optional.of(e.getLocalizedMessage());
      }
    }
    return validationFailure;
  }

  private void readRequestParameters(final HttpRequest request) {
    if (!result.isEmpty()) {
      if (request.isParameterDefined(COMPARING_COLUMN)) {
        final String fieldName = request.getParameter(COMPARING_COLUMN);
        final Object value = result.get(0).getFieldValue(fieldName);
        if (value != null) {
          filter.setFieldName(fieldName, value.getClass());
        } else {
          filter.setFieldName(TableRowsFilter.FIELD_NONE, String.class);
        }
      }
      if (request.isParameterDefined(COMPARING_OPERATOR)) {
        filter.setComparator(request.getParameter(COMPARING_OPERATOR));
      }
      if (request.isParameterDefined(COMPARING_VALUE)) {
        filter.setFieldValue(request.getParameter(COMPARING_VALUE));
      }
    }
  }

  private void executeSQLQuery() {
    if (result.isEmpty()) {
      // the request is executed only once, when no query hasn't be yet performed.
      // after that, the query result is cached in this web controller.
      try {
        result = requester.request();
      } catch (JdbcConnectorException e) {
        SilverLogger.getLogger(this).error(e);
        MessageNotifier.addError(getString("sqlRequestExecutionFailure"));
      }
    }
  }

  private void clearQueryResult() {
    result.clear();
    filter.clear();
  }

  private void setQueryResult(final HttpRequest request) {
    request.setAttribute(RESULT_SET, filter.filter(result));
    request.setAttribute(COMPARING_COLUMN, filter.getFieldName());
    request.setAttribute(COMPARING_OPERATOR, filter.getComparator());
    request.setAttribute(COMPARING_VALUE, filter.getFieldValue());
    request.setAttribute(COMPARING_OPERATORS, TableRowsFilter.getAllComparators());
  }

}
  