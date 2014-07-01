package org.silverpeas.components.quickinfo;

import com.stratelia.webactiv.util.ResourceLocator;

public class QuickInfoComponentSettings {
  
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

}