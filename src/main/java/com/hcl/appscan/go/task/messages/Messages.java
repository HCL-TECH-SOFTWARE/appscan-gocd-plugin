/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/

package com.hcl.appscan.go.task.messages;

public class Messages {

	public static final String NO_CREDENTIALS          = "config.validation.nocredentials";
	public static final String AUTH_FAILED             = "config.validation.authfailed";
	public static final String NO_APPS                 = "config.validation.noapps";
	public static final String APP_NOT_FOUND           = "config.validation.appnotfound";
	public static final String INVALID_FAIL_CONDITIONS = "config.validation.invalidfailconditions";
	public static final String APP_NOT_SET             = "config.validation.appnotset";
	
	private static final MessagesBundle BUNDLE = 
			new MessagesBundle("com.hcl.appscan.go.task.messages.messages", Messages.class.getClassLoader()); //$NON-NLS-1$
	
	/**
	 * Get a message.
	 * 
	 * @param key The key for the message.
	 * @param args Optional list of objects to be inserted into the message.
	 * @return The message.
	 */
	public static String getMessage(String key, Object... args) {
		return BUNDLE.getMessage(key, args);
	}
}