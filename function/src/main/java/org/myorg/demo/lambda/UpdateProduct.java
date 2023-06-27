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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Updates an existing Product record.
 * @author Himanshu
 *
 */
public class UpdateProduct implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static ObjectMapper mapper = new ObjectMapper();
	private static AmazonDynamoDB dbClient = AmazonDynamoDBClientBuilder.defaultClient();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		LambdaLogger logger = context.getLogger();
		try {

			String productDtoStr = input.getBody();
			logger.log("input received form apigw>>>>>" + productDtoStr);
			
			

			Product product = mapper.readValue(productDtoStr, Product.class);

			if (!(validateInput(product))) {
				
				return ServiceUtil.createAPIResponse("Invalid input; missing required value.",
						HttpStatus.SC_BAD_REQUEST, logger);

				
			}

			UUID.fromString(product.getId()); // throws IllegalArgumentException if not a valid UUID format
			
			
			boolean responseStr = updateProduct(product, logger);

			if (!responseStr) {

				return ServiceUtil.createAPIResponse("No record found to update for id:" + product.getId(),
						HttpStatus.SC_BAD_REQUEST, logger);
			}

			return ServiceUtil.createAPIResponse(product, HttpStatus.SC_OK, logger);

		} catch (JsonProcessingException e) {

			e.printStackTrace();
			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Invalid input exception occured", HttpStatus.SC_BAD_REQUEST, logger);

		}catch (IllegalArgumentException e) {

			e.printStackTrace();
			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Invalid input.Id is not valid UUID", HttpStatus.SC_BAD_REQUEST,logger);

		} catch (Exception e) {

			e.printStackTrace();

			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Product service exception occured",
					HttpStatus.SC_INTERNAL_SERVER_ERROR, logger);
		}

	}

	/**
	 * @param product
	 */
	private boolean validateInput(final Product product) {
		if (StringUtils.isNullOrEmpty(product.getName())
				|| (product.getPrice() == null || product.getPrice() == 0.0)
				|| StringUtils.isNullOrEmpty(product.getId())) {

			return false;
		}
		
		return true;
	}

	private boolean updateProduct(Product product, LambdaLogger logger) {

		if (dbClient == null) { // in case DB client is reclaimed by system 

			dbClient = AmazonDynamoDBClientBuilder.defaultClient();
			logger.log("new ProductRepositoryImpl created ***");
		}

		DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dbClient);

		Product productfromDB = dynamoDBMapper.load(Product.class, product.getId());

		if (productfromDB == null) {

			return false;
		}

		productfromDB.setName(product.getName());
		productfromDB.setPrice(product.getPrice());

		dynamoDBMapper.save(productfromDB);

		return true;

	}

}
