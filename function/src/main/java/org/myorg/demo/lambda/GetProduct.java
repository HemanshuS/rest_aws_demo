/**
 * 
 */
package org.myorg.demo.lambda;

import java.util.UUID;

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
import com.amazonaws.util.StringUtils;

/**
 * Read operation by using Id as input. 
 * @author Himanshu
 *
 */
public class GetProduct implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static AmazonDynamoDB dbClient = AmazonDynamoDBClientBuilder.defaultClient();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		LambdaLogger logger = context.getLogger();

		try {
			
			if ( StringUtils.isNullOrEmpty(input.getPathParameters().get("id")) 
					||  input.getPathParameters() == null )
			{
				return ServiceUtil.createAPIResponse("Product Id in path param cano be null or empty.", 
						HttpStatus.SC_BAD_REQUEST,logger);

			}
			String inputId = input.getPathParameters().get("id");
			logger.log("input received form apigw>>>>>" + inputId);
			
			UUID.fromString(inputId);

			Product product = getProduct(inputId, logger);

			if (product == null) {

				return ServiceUtil.createAPIResponse("No record found for the id.", HttpStatus.SC_BAD_REQUEST,logger);

			}

			logger.log("fetched product with id" + product.getId());

			//response.setBody(productJSON);
			
			return ServiceUtil.createAPIResponse(product, HttpStatus.SC_OK,logger);

		} catch (IllegalArgumentException e) {

			e.printStackTrace();
			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Invalid input. Id is not valid UUID", HttpStatus.SC_BAD_REQUEST,logger);

		} catch (Exception e) {

			e.printStackTrace();

			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Product service exception occured", HttpStatus.SC_INTERNAL_SERVER_ERROR,logger);
		}

	}

	private Product getProduct(String id, LambdaLogger logger) {

		if (dbClient == null) { // in case DB client is reclaimed by system 

			dbClient = AmazonDynamoDBClientBuilder.defaultClient();
			logger.log("new ProductRepositoryImpl created ***");
		}

		DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dbClient);

		Product product = dynamoDBMapper.load(Product.class, id);

		return product;

	}

	
}
