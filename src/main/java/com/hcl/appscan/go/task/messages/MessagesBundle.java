/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/

package com.hcl.appscan.go.task.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Wrapper class for using message resource bundles.
 */
public final class MessagesBundle {
	
	private ResourceBundle m_bundle;
	
	/**
	 * Constructor.
	 * 
	 * @param name Name of the resource bundle.
	 * @param locale A specific locale.
	 * @param cl The class loader to use.
	 */
	public MessagesBundle(String name, Locale locale, ClassLoader cl) {
		m_bundle = ResourceBundle.getBundle(name, locale, cl);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name Name of the resource bundle.
	 * @param cl The class loader to use.
	 */
	public MessagesBundle(String name, ClassLoader cl) {
		this(name, Locale.getDefault(), cl);
	}
	
	/**
	 * Bind a message with the given arguments.
	 * 
	 * @param message The message.
	 * @param args Optional list of objects to be inserted into the message.
	 * @return The modified message.
	 */
	public static String bind(String message, Object... args) {
		
		if (message.contains("{0}") && args.length > 0) { //$NON-NLS-1$
			try {
				return MessageFormat.format(message, args);
			}
			catch (IllegalArgumentException e) {
				// just return original message
			}
		}
		
		return message;
	}
	
	/**
	 * Get a message.
	 * 
	 * @param key The key for the message.
	 * @param args Optional list of objects to be inserted into the message.
	 * @return The message.
	 */
	public String getMessage(String key, Object... args) {
		return bind(m_bundle.getString(key), args);
	}
	
	/**
	 * Return the keys in this message bundle.
	 * 
	 * @return The keys in this message bundle.
	 */
	public Set<String> keySet() {
		return m_bundle.keySet();
	}
}
