/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/
package com.hcl.appscan.go.task;

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import com.hcl.appscan.sdk.logging.IProgress;
import com.hcl.appscan.sdk.logging.Message;
public class AppScanLogger implements IProgress {
        
    JobConsoleLogger jobLogger;

    public AppScanLogger(JobConsoleLogger jobLogger) {
        this.jobLogger = jobLogger;
    }
        @Override
        public void setStatus(Message status) {
            jobLogger.printLine(status.getText());
        }
	
        @Override
	    public void setStatus(Throwable e) {
            jobLogger.printLine(e.getMessage());
        };
	
        @Override
	    public void setStatus(Message status, Throwable e) {
            jobLogger.printLine(status.getText() + " , " + e.getMessage());
        };

        public void printLine(String s) {
            jobLogger.printLine(s);
        }

        public JobConsoleLogger getConsoleLogger() {
            return JobConsoleLogger.getConsoleLogger();
        }
      
    }
