/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/

package com.hcl.appscan.go.task;

import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.auth.AuthenticationHandler;
import com.hcl.appscan.sdk.auth.LoginType;
import com.hcl.appscan.sdk.utils.SystemUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import org.apache.wink.json4j.JSONException;

public class AppScanAuthenticationProvider implements IAuthenticationProvider, Serializable {
    private static final long serialVersionUID = 1L;

    String apikeyID;
    String apikeySecret;
    String token = ""; //Set to non-null so that checks for if it's expired don't throw a NPE
   
   	public AppScanAuthenticationProvider (String apikeyID, String apikeySecret) {
    	this.apikeyID = apikeyID;
       	this.apikeySecret = apikeySecret;
   	}

    @Override
	public String getServer() {
		return SystemUtil.getDefaultServer();
    }
   
   	@Override
	public Map<String, String> getAuthorizationHeader(boolean persist) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer "+ token.trim()); //$NON-NLS-1$ //$NON-NLS-2$
		if(persist) {
			headers.put("Connection", "Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return headers;
	}

    @Override
	public boolean isTokenExpired() {
		boolean isExpired = false;
		AuthenticationHandler handler = new AuthenticationHandler(this);
		try {
			isExpired = handler.isTokenExpired() && !handler.login(apikeyID, apikeySecret, true, LoginType.ASoC_Federated);
		} catch (IOException | JSONException e) {
			isExpired = false;
		}
		return isExpired;
	}

    @Override
	public void saveConnection(String token) {
		this.token = token;
	}
}