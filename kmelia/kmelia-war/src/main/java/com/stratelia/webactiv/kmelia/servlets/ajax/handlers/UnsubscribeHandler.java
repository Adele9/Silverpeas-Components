package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class UnsubscribeHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {   
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);
    
    String topicId = request.getParameter("Id");

    try {
      kmelia.removeSubscription(topicId);
      return "ok";
    } catch (Exception e) {
      SilverTrace.error("kmelia", "UnsubscribeHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}
