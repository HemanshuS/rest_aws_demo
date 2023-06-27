/**
 * 
 */
package org.myorg.demo.lambda;

import org.apache.http.HttpStatus;
import org.myorg.demo.dto.ProductDto;
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
 * Adds new Product.
 * @author Himanshu
 *
 */
public class AddProduct implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static ObjectMapper mapper = new ObjectMapper();
	private static AmazonDynamoDB dbClient = AmazonDynamoDBClientBuilder.defaultClient();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		LambdaLogger logger = context.getLogger();

		try {
			String productDtoStr = input.getBody();
			logger.log("input received form apigw>>>>>" + productDtoStr);

			ProductDto productDto = mapper.readValue(productDtoStr, ProductDto.class);

			logger.log("name >>>>" + productDto.getName());

			if (!(validateInput(productDto))) {

				return ServiceUtil.createAPIResponse("Invalid input, values can't be empty.", HttpStatus.SC_BAD_REQUEST, logger);
			}

			Product product = new Product();
			product.setName(productDto.getName());
			product.setPrice(productDto.getPrice());

			createProduct(product, logger);

			return ServiceUtil.createAPIResponse(product, HttpStatus.SC_CREATED, logger);

		} catch (JsonProcessingException e) {

			e.printStackTrace();
			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Invalid input", HttpStatus.SC_BAD_REQUEST, logger);

		} catch (Exception e) {

			e.printStackTrace();

			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Product service exception occured",
					HttpStatus.SC_INTERNAL_SERVER_ERROR, logger);
		}

	}

	private boolean validateInput(final ProductDto productDto) {

		if (StringUtils.isNullOrEmpty(productDto.getName())
				|| (productDto.getPrice() == null || productDto.getPrice() == 0.0)) {

			return false;
		}

		return true;
	}

	private void createProduct(Product product, LambdaLogger logger) throws Exception {

		if (dbClient == null) { // in case DB client is reclaimed by system

			dbClient = AmazonDynamoDBClientBuilder.defaultClient();
			logger.log("new ProductRepositoryImpl created ***");
		}

		DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dbClient);

		dynamoDBMapper.save(product);

	}

}
