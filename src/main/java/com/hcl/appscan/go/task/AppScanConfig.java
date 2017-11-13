/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/
package com.hcl.appscan.go.task;

import java.util.HashMap;
import java.util.Map;

import com.hcl.appscan.go.task.messages.Messages;

public class AppScanConfig {
   
    public static final String APIKEY_ID        = "apikeyid";
    public static final String APIKEY_SECRET    = "apikeysecret";
    public static final String APP_NAME         = "appname";
    public static final String SCAN_NAME        = "scanname";
    public static final String SCAN_TYPE        = "scantype";
    public static final String TARGET           = "target";
    public static final String DISABLE_MAVEN    = "disablemaven";
    public static final String EMAIL_NOTIFY     = "emailnotify";
    public static final String FAIL_CONDITIONS  = "failconditions";

    public static String getValue(Map config, String property) {
        Map propertyObj = (Map)config.get(property);
        return (String)propertyObj.get("value");
    }

    public static Map getDefaultConfig() {
        Map config = new HashMap();
        
        Map apikeyID = new HashMap();
        apikeyID.put("display-name", "keyid");
        apikeyID.put("required", false);
        config.put(APIKEY_ID, apikeyID);

        Map apikeySecret = new HashMap();
        apikeySecret.put("display-name", "keypass");
        apikeySecret.put("required", false);
        apikeySecret.put("secure", true);
        config.put(APIKEY_SECRET, apikeySecret);

        Map appName = new HashMap();
        appName.put("display-name", "appname");
        appName.put("required",  false);
        config.put(APP_NAME, appName);

        Map scanName = new HashMap();
        scanName.put("display-name", "scanname");
        scanName.put("required",  false);
        config.put(SCAN_NAME, scanName);

        Map scanType = new HashMap();
        scanType.put("display-name", "scantype");
        scanType.put("required", false);
        scanType.put("default-value", "sa");
        config.put(SCAN_TYPE, scanType);
    
        Map target = new HashMap();
        target.put("display-name", "target");
        target.put("required",  false);
        config.put(TARGET, target);

        Map disableMaven = new HashMap();
        disableMaven.put("display-name", "disablemaven");
        disableMaven.put("required",  false);
        config.put(DISABLE_MAVEN, disableMaven);
    
        Map emailNotify = new HashMap();
        emailNotify.put("display-name", "email");
        emailNotify.put("required",  false);
        config.put(EMAIL_NOTIFY, emailNotify);
        
        Map failConditions = new HashMap();
        failConditions.put("display-name", "failrules");
        failConditions.put("required",  false);
        config.put(FAIL_CONDITIONS, failConditions);

        return config;
    }

    public static Map validateConfig(Map configMap) {
        
        //Grab key fields:
        String apikeyid       = getValue(configMap, APIKEY_ID);
        String apikeysecret   = getValue(configMap, APIKEY_SECRET);
        String appname        = getValue(configMap, APP_NAME);
        String failconditions = getValue(configMap, FAIL_CONDITIONS);
        
        Map errorMap = new HashMap();
        errorMap.putAll(validateApplication(apikeyid,apikeysecret,appname));
        errorMap.putAll(validateFailConditions(failconditions));
        return errorMap;
    }

    private static Map validateApplication(String apikeyid, String apikeysecret, String appname) {
        Map errorMap = new HashMap(); 
        
        if ( apikeyid == null || apikeyid.trim().length() == 0) {
            errorMap.put(APIKEY_ID, Messages.getMessage(Messages.NO_CREDENTIALS));
        } else if ( apikeysecret == null || apikeysecret.trim().length() == 0) {
            errorMap.put(APIKEY_SECRET, Messages.getMessage(Messages.NO_CREDENTIALS));
        } else {
            Map<String, String> applications = AppScanTaskExecutor.getApplications(apikeyid, apikeysecret);         
            if (applications == null) {
                 errorMap.put(APIKEY_SECRET, Messages.getMessage(Messages.AUTH_FAILED));
            } else if (applications.isEmpty()) {
                 errorMap.put(APP_NAME, Messages.getMessage(Messages.NO_APPS));
            } else if (appname == null || appname.trim().length()==0) {
                 errorMap.put(APP_NAME, Messages.getMessage(Messages.APP_NOT_SET));  
            } else if (!applications.values().contains(appname)) {
                 errorMap.put(APP_NAME, Messages.getMessage(Messages.APP_NOT_FOUND,appname));
            }
        }

        return errorMap;
    }

    private static Map validateFailConditions(String failconditions) {
        Map errorMap = new HashMap(); 

        if (failconditions != null && failconditions.length() > 1) {
            try {
                String[] failCounts = failconditions.split(",");
                if (failCounts.length != 4) {
                    errorMap.put(FAIL_CONDITIONS, Messages.getMessage(Messages.INVALID_FAIL_CONDITIONS));
                } else {
                    Integer.parseInt(failCounts[0]);
                    Integer.parseInt(failCounts[1]);
                    Integer.parseInt(failCounts[2]);
                    Integer.parseInt(failCounts[3]);
                }
            } catch (NumberFormatException e) {
                errorMap.put(FAIL_CONDITIONS, Messages.getMessage(Messages.INVALID_FAIL_CONDITIONS));
            }
        }

        return errorMap;
    }              
}