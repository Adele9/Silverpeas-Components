package com.silverpeas.silvercrawler.util;

import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 *
 * @author  NEY
 * @version
 */

public class FileServerUtils extends Object {

    private static String replaceSpecialChars(String toParse) {
    	
    	
		String newLogicalName = toParse.replace(' ', '_');
		newLogicalName = newLogicalName.replace('\'', '_');   //added on 06/09/2001
		newLogicalName = newLogicalName.replace('#', '_');
		newLogicalName = newLogicalName.replace('%', '_');
		FileServerUtils.replaceAccentChars(newLogicalName);
		
		return newLogicalName;
   }
    
    public static String replaceAccentChars(String toParse) {

    	String newLogicalName = toParse.replace('�', 'e');
		newLogicalName = newLogicalName.replace('�', 'e');
		newLogicalName = newLogicalName.replace('�', 'e');
		newLogicalName = newLogicalName.replace('�', 'e');   
		newLogicalName = newLogicalName.replace('�', 'o');
		newLogicalName = newLogicalName.replace('�', 'o');   
		newLogicalName = newLogicalName.replace('�', 'o');   
		newLogicalName = newLogicalName.replace('�', 'o');   
		newLogicalName = newLogicalName.replace('�', 'i');  
		newLogicalName = newLogicalName.replace('�', 'i');   
		newLogicalName = newLogicalName.replace('�', 'i');   
		newLogicalName = newLogicalName.replace('�', 'n');   
		newLogicalName = newLogicalName.replace('�', 'u');
		newLogicalName = newLogicalName.replace('�', 'u');
		newLogicalName = newLogicalName.replace('�', 'u');
		newLogicalName = newLogicalName.replace('�', 'c');
		newLogicalName = newLogicalName.replace('�', 'a');
		newLogicalName = newLogicalName.replace('�', 'a');
		newLogicalName = newLogicalName.replace('�', 'a');   
		newLogicalName = newLogicalName.replace('�', 'a');   

		return newLogicalName;
   }
   
    public static String getUrl(String logicalName, String physicalName, String mimeType, String userId, String componentId) {
		StringBuffer url = new StringBuffer();

		String m_context		= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
		String newLogicalName	= replaceSpecialChars(logicalName);

		url.append(m_context).append("/SilverCrawlerFileServer/").append(newLogicalName).append("?SourceFile=").append(physicalName).append("&TypeUpload=link&MimeType=").append(mimeType).append("&UserId=").append(userId).append("&ComponentId=").append(componentId);

		return url.toString();
	}
    
    public static String getUrlToTempDir(String logicalName, String physicalName, String mimeType, String userId, String componentId, String path) {
		StringBuffer	url			= new StringBuffer();
		String			m_context	= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

		String newLogicalName = replaceSpecialChars(logicalName);

		url.append(m_context).append("/SilverCrawlerFileServer/").append(newLogicalName).append("?SourceFile=").append(physicalName).append("&TypeUpload=zip&MimeType=").append(mimeType).append("&UserId=").append(userId).append("&ComponentId=").append(componentId).append("&Path=").append(path);
		return url.toString();
	}
   
}