/**
 * 
 */
package org.myorg.demo.lambda;

import org.apache.http.HttpStatus;
import org.myorg.demo.entity.Product;
import org.myorg.demo.util.ServiceUtil;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Deletes Product by taking Id as input.
 * @author Himanshu
 *
 */
public class DeleteProduct
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static AmazonDynamoDB dbClient = AmazonDynamoDBClientBuilder.defaultClient();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		LambdaLogger logger = context.getLogger();

		try {
			String inputId = input.getPathParameters().get("id");
			logger.log("input received form apigw>>>>>" + inputId);

			boolean isdeleted = deleteProduct(inputId, logger);

			if (!isdeleted) {

				return ServiceUtil.createAPIResponse("No record found for the id", HttpStatus.SC_BAD_REQUEST,logger);
			}

			return ServiceUtil.createAPIResponse("Record deletd for id:" + inputId, HttpStatus.SC_OK,logger);

		} catch (JsonProcessingException e) {

			e.printStackTrace();
			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Invalid input", HttpStatus.SC_BAD_REQUEST,logger);

		} catch (Exception e) {

			e.printStackTrace();

			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Internal server error", HttpStatus.SC_INTERNAL_SERVER_ERROR,logger);
		}

	}

	private boolean deleteProduct(String id, LambdaLogger logger)  throws Exception {

		
		if (dbClient == null) { // in case DB connected is reclaimed by system 

			dbClient = AmazonDynamoDBClientBuilder.defaultClient();
			logger.log("new ProductRepositoryImpl created ***");
		}

		DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dbClient);

		Product product = dynamoDBMapper.load(Product.class, id);

		if (product == null) {

			return false;

		}

		dynamoDBMapper.delete(product);

		return true;

	}

}
