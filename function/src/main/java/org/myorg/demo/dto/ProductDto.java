/**
 * 
 */
package org.myorg.demo.dto;

/**
 * This DTO is to receive input for creation of new Product
 * @author Himanshu
 *
 */
public class ProductDto {
	
	
	private String name;
	private Double price;
	
	
	public ProductDto(String name, Double price) {
		super();
		this.name = name;
		this.price = price;
	}
	public ProductDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	
	
	

}
