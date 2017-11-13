/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/

package com.hcl.appscan.go.task;

import com.hcl.appscan.sdk.app.CloudApplicationProvider;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.error.InvalidTargetException;
import com.hcl.appscan.sdk.error.ScannerException;

import com.hcl.appscan.sdk.results.IResultsProvider;
import com.hcl.appscan.sdk.scan.IScan;
import com.hcl.appscan.sdk.scanners.sast.SASTScanFactory;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

public class AppScanTaskExecutor {

    private Map config = null;
    private Map context = null;
    private AppScanLogger jobLogger = null;

    public AppScanTaskExecutor(Map config, Map context, AppScanLogger jobLogger) {
        this.config = config;
        this.context = context;
        this.jobLogger = jobLogger;
    }

    public Result execute() {

		String apikeyid          = AppScanConfig.getValue(config,AppScanConfig.APIKEY_ID);
        String apikeysecret      = AppScanConfig.getValue(config,AppScanConfig.APIKEY_SECRET);
		String appName           = AppScanConfig.getValue(config,AppScanConfig.APP_NAME);
		String target            = AppScanConfig.getValue(config,AppScanConfig.TARGET);
        String scanName          = AppScanConfig.getValue(config,AppScanConfig.SCAN_NAME);
    	String emailNotification = AppScanConfig.getValue(config,AppScanConfig.EMAIL_NOTIFY);
    	String failConditions    = AppScanConfig.getValue(config,AppScanConfig.FAIL_CONDITIONS);
		String workingDirectory  = (String) context.get("workingDirectory");

		//Log raw user input
		String userInput = "AppScan Task Input : ";
		userInput += "appname=" + appName;
		userInput += ",target=" + target;
		userInput += ",scanName=" + scanName;
		userInput += ",emailNotify=" + emailNotification;
		userInput += ",failCondittions=" + failConditions;
		userInput += ",workingDirectory=" + new File(workingDirectory).getAbsolutePath();
		jobLogger.printLine(userInput);

		//Find app id
		AppScanAuthenticationProvider authProvider = new AppScanAuthenticationProvider(apikeyid,apikeysecret);
		String appID = getApplicationID(appName, authProvider);

	   //Clean up scan name
       if (scanName == null || scanName.trim().equals("")) {
			scanName = appName.replaceAll(" ", "") + ThreadLocalRandom.current().nextInt(0, 10000);
	   }
          	
	   //Validate the target - Handle an empty string, a relative path or an absolute path
		if ((target == null) || (target.trim().length() == 0) ) {
			target = new File(workingDirectory).getAbsolutePath();
		} else {
			File absoluteTarget = new File(target);
			if (!absoluteTarget.exists()) {
				File relativeTarget = new File(workingDirectory, target);
				if (relativeTarget.exists()) {
					target = relativeTarget.getAbsolutePath();
				} else {
					return new Result(false, "Invalid target : " + target);
				}
			}
		}

		File reportLocation = new File(workingDirectory, scanName + ".html");
		   
	    Map<String, String> props = new HashMap<String, String>();
    	props.put(CoreConstants.SCAN_NAME, scanName);
    	props.put(CoreConstants.TARGET, target);
    	props.put(CoreConstants.APP_ID, appID);
		if ( emailNotification != null && Boolean.parseBoolean(emailNotification)) {
 			props.put(CoreConstants.EMAIL_NOTIFICATION, emailNotification);
		}

		//Log settings we're actually using for the scan
		String normalizedSettings = "AppScan Normalized Scan Settings : ";
		normalizedSettings += "scanName=" + scanName;
		normalizedSettings += ",target=" + target;
		normalizedSettings += ",appid=" + appID;
		normalizedSettings += ",emailnotify=" + emailNotification;
		normalizedSettings += ",reportFile=" + reportLocation.getAbsolutePath();
		jobLogger.printLine(normalizedSettings);
    	  	
		IScan scan = new SASTScanFactory().create(props, jobLogger, authProvider);
    	
    	//Run the scan
    	try {
    		scan.run();
 		} catch (InvalidTargetException e) {
    		return new Result(false, "Invalid Target : " + target, e);
    	} catch (ScannerException e) {
    		return new Result(false, "An error occured during scanning : " + e.getMessage(), e);
    	} 		
    	
    	//Wait for Results - No timeout here since there could be long running scans.  Task can be cancelled by the user if they like
    	jobLogger.printLine("Waiting for scan to finish");
    	IResultsProvider resultsProvider = scan.getResultsProvider();
        while (resultsProvider.getStatus().equals(CoreConstants.RUNNING)) {
        	try {
        		TimeUnit.SECONDS.sleep(1);
        	} catch (InterruptedException e ) {
        		//Swallow it
        	}
        }
        
        //Are the results ready?
        if (resultsProvider.getStatus().equals(CoreConstants.READY)) {
        	try {
        		int total = resultsProvider.getFindingsCount();
               	int high  = resultsProvider.getHighCount();
               	int med   = resultsProvider.getMediumCount();
               	int low   = resultsProvider.getLowCount();
                
                //Download the report
               	resultsProvider.getResultsFile(reportLocation, "html");
               	jobLogger.printLine("Report downloaded: " + reportLocation.getAbsolutePath());
               	
               	//Show some metrics in the log:
               	jobLogger.printLine("Security Issues Found : Total=" + total + " , High=" + high + " , Medium=" + med + " , Low=" + low);
                     	
               	//Check failure conditions
				if ( (failConditions!= null) && (failConditions.length() > 4)) { 
					String[] failConditionArray = failConditions.split(",");
                	int maxTotal = Integer.parseInt(failConditionArray[0]);
            		int maxHigh  = Integer.parseInt(failConditionArray[1]);
    	        	int maxMed   = Integer.parseInt(failConditionArray[2]);
    	        	int maxLow   = Integer.parseInt(failConditionArray[3]);
               		if (total>maxTotal) return new Result(false, total + " total security issues found. The maximum allowed is set to " + maxTotal);
               		if (high>maxHigh)   return new Result(false, high  + " high severity issues found. The maximum allowed is set to " + maxHigh);
               		if (med>maxMed)     return new Result(false, med   + " medium severity issues found. The maximum allowed is set to " + maxMed);
               		if (low>maxLow)     return new Result(false, low   + " low severity issues found. The maximum allowed is set to " + maxLow);          	
				}
        	} catch (Exception e) {
        		return new Result(false, "An error occured while retrieving the results", e);
        	}
               	
        } else {
           		// Results status is something other than READY 
           		return new Result(false,"An error occurred while retreiving results");
        }
        return new Result(true, "Scan complete and report downloaded");
	}
	
	public static Map<String, String> getApplications(String apikeyid, String apikeysecret) {
		Map<String,String> applications = null;
		try {
 			IAuthenticationProvider authProvider = new AppScanAuthenticationProvider(apikeyid, apikeysecret);
    		applications = new CloudApplicationProvider(authProvider).getApplications(); 
		} catch (Exception e) {
			//Swallow.  Null will be returned below
		}
        return applications;
    }
     
	private String getApplicationID(String appName, IAuthenticationProvider authProvider) {
    	Map<String, String> apps = new CloudApplicationProvider(authProvider).getApplications();
		for (String appid : apps.keySet()) {
			if (apps.get(appid).equals(appName)) {
				return appid;
			}
		}
		return "0";
	}
}