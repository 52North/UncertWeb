package org.uncertweb.api.om.result;

/**
 * result of an observation which is a category defined in a certain code space.
 * 
 * @author staschc
 *
 */
public class CategoryResult implements IResult{
	
	/**category value of the observation*/
	private String value;
	
	/**codeSpace of the category value*/
	private String codeSpace;
	
	/**
	 * constructor
	 * 
	 * @param value
	 * 			category value of the observation
	 * @param codeSpace
	 * 			codeSpace of the category value
	 */
	public CategoryResult(String value, String codeSpace){
		this.value = value;
		this.codeSpace = codeSpace;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object v) {
		this.value = (String)v;
	}
	
	/**
	 * returns category value
	 * 
	 * @return
	 */
	public String getCategoryValue(){
		return value;
	}

	/**
	 * returns codeSpace of the category value
	 * 
	 * @return codeSpace of the category value
	 */
	public String getCodeSpace(){
		return codeSpace;
	}
}
