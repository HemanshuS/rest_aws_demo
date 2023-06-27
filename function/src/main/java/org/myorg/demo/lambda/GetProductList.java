/**
 * 
 */
package org.myorg.demo.lambda;

import java.util.List;

import org.apache.http.HttpStatus;
import org.myorg.demo.entity.Product;
import org.myorg.demo.util.ServiceUtil;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Returns a list of all Product in the table.
 * In real world this output should be paginated.
 * @author Himanshu
 *
 */
public class GetProductList implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static AmazonDynamoDB dbClient = AmazonDynamoDBClientBuilder.defaultClient();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		LambdaLogger logger = context.getLogger();

		try {
			
			List<Product> productList = getProductList( logger);

			if (productList == null || productList.size()==0) {

				return ServiceUtil.createAPIResponse("No product found", HttpStatus.SC_NOT_FOUND,logger);
			}

			logger.log("fetched product count " + productList.size());

			return ServiceUtil.createAPIResponse(productList, HttpStatus.SC_OK,logger);

		} catch (Exception e) {

			e.printStackTrace();

			logger.log("Exception:: >>>>" + e.getMessage());

			return ServiceUtil.createAPIResponse("Product service exception occured", HttpStatus.SC_INTERNAL_SERVER_ERROR,logger);
		}

	}

	private List<Product> getProductList(LambdaLogger logger) {

		if (dbClient == null) { // // in case DB client is reclaimed by system 

			dbClient = AmazonDynamoDBClientBuilder.defaultClient();
			logger.log("new ProductRepositoryImpl created ***");
		}

		DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dbClient);

		List<Product> productList = dynamoDBMapper.scan(Product.class,new DynamoDBScanExpression());

		return productList;

	}

	
}
