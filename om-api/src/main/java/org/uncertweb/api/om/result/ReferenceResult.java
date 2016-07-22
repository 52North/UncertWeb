package org.uncertweb.api.om.result;

import org.apache.xmlbeans.SchemaType;
import org.w3.x1999.xlink.ActuateType;
import org.w3.x1999.xlink.ShowType;

/**
 * Result representing an reference with optional attributes
 *
 * @author Kiesow
 *
 */
public class ReferenceResult implements IResult {

	private SchemaType type;
	private String href;
	private String role;
	private String arcrole;
	private String title;
	private ShowType.Enum show;
	private ActuateType.Enum actuate;
	private Object result;

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
	 * @param type2
	 * @param href
	 * 			reference
	 * @param role
	 * 			role of reference
	 * @param arcrole
	 * @param show2
	 * @param actuate2
	 */
	public ReferenceResult(SchemaType type2, String href, String role, String arcrole, String title, ShowType.Enum show2, ActuateType.Enum actuate2) {
		this.type = type2;
		this.href = href;
		this.role = role;
		this.arcrole = arcrole;
		this.title = title;
		this.show = show2;
		this.actuate = actuate2;
	}

	// specific getters and setters
	public SchemaType getType() {
		return type;
	}

	public void setType(SchemaType type) {
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

	public ShowType.Enum getShow() {
		return show;
	}

	public void setShow(ShowType.Enum show) {
		this.show = show;
	}

	public ActuateType.Enum getActuate() {
		return actuate;
	}

	public void setActuate(ActuateType.Enum actuate) {
		this.actuate = actuate;
	}

	// generic getter and setter
	public Object getValue() {
		return this.result;
	}

	public void setValue(Object v) {
		this.result = v;
	}

}
