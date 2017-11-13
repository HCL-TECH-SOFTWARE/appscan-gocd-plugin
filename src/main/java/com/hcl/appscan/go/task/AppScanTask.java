/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/

package com.hcl.appscan.go.task;

import com.thoughtworks.go.plugin.api.AbstractGoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

import org.apache.commons.io.IOUtils;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Extension
public class AppScanTask extends AbstractGoPlugin {

    private Logger logger = Logger.getLoggerFor(AppScanTask.class);

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("task", Arrays.asList("1.0"));
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        switch(request.requestName()) {
            case "view"          : return handleTaskView();
            case "configuration" : return handleGetConfigRequest();
            case "validate"      : return handleValidation(request);
            case "execute"       : return handleTaskExecution(request);
            default              : throw new UnhandledRequestTypeException(request.requestName());
        }
    }

    private GoPluginApiResponse handleTaskView() {
        int responseCode = DefaultGoApiResponse.SUCCESS_RESPONSE_CODE;
        Map view = new HashMap();
        view.put("displayValue", "AppScan Security Test");
        try {
            view.put("template", IOUtils.toString(getClass().getResourceAsStream("/views/task.template.html"), "UTF-8"));
        } catch (Exception e) {
            responseCode = DefaultGoApiResponse.INTERNAL_ERROR;
            String errorMessage = "Failed to find template: " + e.getMessage();
            view.put("exception", errorMessage);
            logger.error(errorMessage, e);
        }
        return createResponse(responseCode, view);
    }

    private GoPluginApiResponse handleGetConfigRequest() {
        return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, AppScanConfig.getDefaultConfig());
    }

    private GoPluginApiResponse handleValidation(GoPluginApiRequest request) {
        Map configMap = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        Map errorMap = AppScanConfig.validateConfig(configMap);
        Map validationResult = new HashMap();
        validationResult.put("errors", errorMap);
        return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, validationResult);
     }

    private GoPluginApiResponse handleTaskExecution(GoPluginApiRequest request) {
        Map executionRequest = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        Map config = (Map)executionRequest.get("config");
        Map context = (Map)executionRequest.get("context");
        AppScanTaskExecutor executor = new AppScanTaskExecutor(config, context, new AppScanLogger(JobConsoleLogger.getConsoleLogger()));
        Result result = executor.execute();
        return createResponse(result.responseCode(), result.toMap());
    }

    private GoPluginApiResponse createResponse(int responseCode, Map body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
        return response;
    }

   
}
