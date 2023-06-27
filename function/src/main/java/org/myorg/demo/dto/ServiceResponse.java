/**
 * 
 */
package org.myorg.demo.dto;

/**
 * @author Himanshu
 *
 */
public class ServiceResponse<T> {
	
	private T message;
	

	public T getMessage() {
		return message;
	}

	public void setMessage(T message) {
		this.message = message;
	}
	
	
	

}
