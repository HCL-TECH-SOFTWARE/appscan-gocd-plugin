/********************************************************************** 
* Licensed Materials - Property of HCL 
* (c) Copyright HCL Technologies Ltd. 2017.  All Rights Reserved. 
***********************************************************************/

package com.hcl.appscan.go.task;

import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;

import java.util.HashMap;
import java.util.Map;

public class Result {
    private boolean success;
    private String message;
    private Exception exception;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }


    public Result(boolean success, String message, Exception exception) {
        this(success, message);
        this.exception = exception;
    }

    public Map<String, String> toMap() {
        final Map<String,String> result = new HashMap<String, String>();
        result.put("success", Boolean.toString(success));
        result.put("message", message);
        result.put("exception", exception.getMessage());
        return result;
    }

    public int responseCode() {
       //return success ? DefaultGoApiResponse.SUCCESS_RESPONSE_CODE : DefaultGoApiResponse.INTERNAL_ERROR;
       return DefaultGoApiResponse.SUCCESS_RESPONSE_CODE; 
    }
}