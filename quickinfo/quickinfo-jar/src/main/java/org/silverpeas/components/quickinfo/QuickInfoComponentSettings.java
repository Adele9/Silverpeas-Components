package org.silverpeas.components.quickinfo;

import com.stratelia.webactiv.util.ResourceLocator;

public class QuickInfoComponentSettings {
  
  private String description;
  private boolean commentsEnabled = false;
  private boolean taxonomyEnabled = false;
  private boolean notificationAllowed = false;
  private boolean broadcastTicker = false;
  private boolean broadcastBlocking = false;
  
  public static final String PARAM_COMMENTS = "comments";
  public static final String PARAM_TAXONOMY = "usePdc";
  
  public static final String PARAM_BROADCAST = "broadcasting";
  public static final String VALUE_BROADCAST_TICKER = "ticker";
  public static final String VALUE_BROADCAST_BLOCKING = "blocking";
  public static final String VALUE_BROADCAST_BOTH = "both";
  
  /**
   * The name of the Quickinfo component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "quickinfo";

  /**
   * The relative path of the properties file containing the settings of the component.
   */
  public static final String SETTINGS_PATH
      = "org.silverpeas.quickinfo.settings.quickInfoSettings";

  /**
   * The relative path of the i18n bundle of the component.
   */
  public static final String MESSAGES_PATH
      = "org.silverpeas.quickinfo.multilang.quickinfo";

  /**
   * The relative path of the properties file containing the references of the icons dedicated to
   * the component.
   */
  public static final String ICONS_PATH
      = "org.silverpeas.quickinfo.settings.quickinfoIcons";
  
  public QuickInfoComponentSettings(String desc) {
    this.description = desc;
  }
  
  public boolean isCommentsEnabled() {
    return commentsEnabled;
  }

  public void setCommentsEnabled(boolean commentsEnabled) {
    this.commentsEnabled = commentsEnabled;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Gets all the messages for the Suggestion Box component and translated in the specified
   * language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static ResourceLocator getMessagesIn(String language) {
    return new ResourceLocator(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the Suggestion Box component.
   * @return the resource with the different component settings.
   */
  public static ResourceLocator getSettings() {
    return new ResourceLocator(SETTINGS_PATH, "");
  }

  /**
   * Gets all the icons definitions particular to the Suggestion Box component.
   * @return the resource with icons definition.
   */
  public static ResourceLocator getIcons() {
    return new ResourceLocator(ICONS_PATH, "");
  }

  public void setTaxonomyEnabled(boolean taxonomyEnabled) {
    this.taxonomyEnabled = taxonomyEnabled;
  }

  public boolean isTaxonomyEnabled() {
    return taxonomyEnabled;
  }

  public void setNotificationAllowed(boolean notificationAllowed) {
    this.notificationAllowed = notificationAllowed;
  }

  public boolean isNotificationAllowed() {
    return notificationAllowed;
  }
  
  public void setBroadcastModes(String paramValue) {
    this.broadcastTicker =
        VALUE_BROADCAST_BOTH.equals(paramValue) || VALUE_BROADCAST_TICKER.equals(paramValue);
    this.broadcastBlocking =
        VALUE_BROADCAST_BOTH.equals(paramValue) || VALUE_BROADCAST_BLOCKING.equals(paramValue);
  }

  public boolean isBroadcastingByTicker() {
    return broadcastTicker;
  }

  public boolean isBroadcastingByBlockingNews() {
    return broadcastBlocking;
  }

}