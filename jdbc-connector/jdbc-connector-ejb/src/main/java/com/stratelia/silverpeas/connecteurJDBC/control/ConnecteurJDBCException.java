package com.stratelia.silverpeas.connecteurJDBC.control;

import com.stratelia.webactiv.util.exception.SilverpeasException;
/**
 * Title:        Connecteur JDBC
 * Description:  Ce composant a pour objet de permettre de r�cup�rer rapidement et simplement des donn�es du syst�me d'information de l'entreprise.
 * Copyright:    Copyright (c) 2001
 * Company:      Strat�lia
 * @author Eric BURGEL
 * @version 1.0
 * @modified by Mohammed Hguig
 */

public class ConnecteurJDBCException extends SilverpeasException {

  public ConnecteurJDBCException(String callingClass, int errorLevel, String message) {
    super(callingClass, errorLevel, message);
  }

  public ConnecteurJDBCException(String callingClass, int errorLevel, String message, String extraParams) {
	super(callingClass, errorLevel, message, extraParams);
  }

  public ConnecteurJDBCException(String callingClass, int errorLevel, String message, Exception nested) {
	super(callingClass, errorLevel, message, nested);
  }

  public ConnecteurJDBCException(String callingClass, int errorLevel, String message, String extraParams,
	                             Exception nested) {
	super(callingClass, errorLevel, message, extraParams, nested);
  }

  public String getModule() {
	 return "connecteurJDBC";
  }
  
}
