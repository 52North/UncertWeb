package org.uncertweb.api.om.result;

import org.w3.x1999.xlink.ShowAttribute.Show;
import org.w3.x1999.xlink.ActuateAttribute.Actuate;



/**
 * Result representing an reference with optional attributes
 * 
 * @author Kiesow
 * 
 */
public class ReferenceResult implements IResult {

	private String type;
	private String href;
	private String role;
	private String arcrole;
	private String title;
	private Show.Enum show;
	private Actuate.Enum actuate;

	/**
	 * Generic constructor
	 */
	public ReferenceResult() { }
	
	/**
	 * constructor with reference and role attribute
	 * 
	 * @param href
	 * 			reference
	 * @param role
	 * 			role of reference
	 */
	public ReferenceResult(String href, String role){
		this.href=href;
		this.role=role;
	}
	
	/**
	 * Constructor with all possible attributes
	 * @param type
	 * @param href
	 * 			reference
	 * @param role
	 * 			role of reference
	 * @param arcrole
	 * @param show
	 * @param actuate
	 */
	public ReferenceResult(String type, String href, String role, String arcrole, String title, Show.Enum show, Actuate.Enum actuate) {
		this.type = type;
		this.href = href;
		this.role = role;
		this.arcrole = arcrole;
		this.title = title;
		this.show = show;
		this.actuate = actuate;
	}

	// specific getters and setters
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getArcrole() {
		return arcrole;
	}

	public void setArcrole(String arcrole) {
		this.arcrole = arcrole;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Show.Enum getShow() {
		return show;
	}

	public void setShow(Show.Enum show) {
		this.show = show;
	}

	public Actuate.Enum getActuate() {
		return actuate;
	}

	public void setActuate(Actuate.Enum actuate) {
		this.actuate = actuate;
	}

	// generic getter and setter
	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setValue(Object v) {
		// TODO Auto-generated method stub

	}

}
