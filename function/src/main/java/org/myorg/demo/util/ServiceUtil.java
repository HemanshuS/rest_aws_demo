/**
 * 
 */
package org.myorg.demo.util;

import org.myorg.demo.dto.ServiceResponse;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Himanshu
 *
 */
public class ServiceUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	public static APIGatewayProxyResponseEvent createAPIResponse(Object body, Integer statusCode, LambdaLogger logger) {

		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

		String responseStr = "";
		

		try {
			if (body instanceof String) {
				ServiceResponse<String> serviceResponse = new ServiceResponse<>();

				serviceResponse.setMessage((String) body);
				logger.log("Response Status code::" + statusCode);
				logger.log("Response message ::" + body);
				
					responseStr = mapper.writeValueAsString(serviceResponse);
					
					responseEvent.setBody(responseStr);
					
					logger.log("responseStr ::" + responseStr);
				
			}else {
				
				responseStr = mapper.writeValueAsString(body);
				responseEvent.setBody(responseStr);
				logger.log("responseStr ::" + responseStr);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.log(" unexpected error ::" + e.getMessage());
		}
		// responseEvent.setHeaders(headers);
		responseEvent.setStatusCode(statusCode);
		return responseEvent;
	}

}
