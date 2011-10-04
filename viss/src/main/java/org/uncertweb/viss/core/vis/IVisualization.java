package org.uncertweb.viss.core.vis;

import java.util.Set;
import java.util.UUID;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.codehaus.jettison.json.JSONObject;
import org.opengis.coverage.grid.GridCoverage;

public interface IVisualization {

	/**
	 * @return the uuid
	 */
	public UUID getUuid();

	/**
	 * @param uuid
	 *          the uuid to set
	 */
	public void setUuid(UUID uuid);

	/**
	 * @return the creator
	 */
	public IVisualizer getCreator();

	/**
	 * @param creator
	 *          the creator to set
	 */
	public void setCreator(IVisualizer creator);

	/**
	 * @return the parameters
	 */
	public JSONObject getParameters();

	/**
	 * @param parameters
	 *          the parameters to set
	 */
	public void setParameters(JSONObject parameters);

	/**
	 * @return the reference
	 */
	public IVisualizationReference getReference();

	/**
	 * @param reference
	 *          the reference to set
	 */
	public void setReference(IVisualizationReference reference);

	/**
	 * @return the visId
	 */
	public String getVisId();

	/**
	 * @param visId
	 *          the visId to set
	 */
	public void setVisId(String visId);

	/**
	 * @return the minValue
	 */
	public Double getMinValue();

	/**
	 * @param minValue
	 *          the minValue to set
	 */
	public void setMinValue(Double minValue);

	/**
	 * @return the maxValue
	 */
	public Double getMaxValue();

	/**
	 * @param maxValue
	 *          the maxValue to set
	 */
	public void setMaxValue(Double maxValue);

	/**
	 * @return the uom
	 */
	public String getUom();

	/**
	 * @param uom
	 *          the uom to set
	 */
	public void setUom(String uom);

	/**
	 * @return the sld
	 */
	public StyledLayerDescriptorDocument getSld();

	/**
	 * @param sld
	 *          the sld to set
	 */
	public void setSld(StyledLayerDescriptorDocument sld);

	/**
	 * @return the coverages
	 */
	public Set<GridCoverage> getCoverages();

	/**
	 * @param coverages
	 *          the coverages to set
	 */
	public void setCoverages(Set<GridCoverage> coverages);

}