package org.n52.sos.uncertainty.decode.impl;

import java.util.ArrayList;
import java.util.Collection;

import net.opengis.gml.MetaDataPropertyType;
import net.opengis.gml.StringOrRefType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.PositionsDocument.Positions;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.swe.x101.AbstractDataRecordType;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.BooleanDocument.Boolean;
import net.opengis.swe.x101.CategoryDocument.Category;
import net.opengis.swe.x101.CountDocument.Count;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;

import org.apache.xmlbeans.XmlCursor;
import org.n52.sos.Sos1Constants;
import org.n52.sos.SosConstants;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.om.SosPhenomenon;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorSystem;

import com.vividsolutions.jts.geom.Point;

/**
 * class encapsulates method for mapping XmlBeans representation of SensorML
 * elements to SOS objects. Currently this class includes only one method to
 * parse a System, as it is contained in the System template for registering
 * mobile sensorSystems to the SOSmobile
 *
 * @author Christoph Stasch, Martin Kiesow
 *
 */
public class SensorMLDecoder extends org.n52.sos.decode.impl.SensorMLDecoder {

	// TODO comments

	/**
	 * Hide utility constructor
	 */
	protected SensorMLDecoder() {
		super();
	}

    /**
     * parses System, which should accords to SensorML System template for
     * registerSensor requests to SOSmobile
     *
     * @param xb_system
     *            XMLBeans representation of SensorML System, whcih should be
     *            mapped to SOS's SensorSystem
     * @return Returns SensorSystem, which represents system contained in
     *         registerSensor request
     * @throws OwsExceptionReport
     *             if parsing of system failed
     */
    public static SensorSystem parseSystem(SystemType xb_system, String smlFile, boolean uncertObs) throws OwsExceptionReport {
        SensorSystem system = null;
        String id = null;
        String descriptionURL = SosConstants.PROCEDURE_STANDARD_DESC_URL;
        String descriptionType = Sos1Constants.SENSORML_OUTPUT_FORMAT;

        boolean active = true;
        boolean mobile = false;

        // Get ID for Feature of Interest
        Identification[] xb_identifications = xb_system.getIdentificationArray();

        for (Identification identification : xb_identifications) {
            Identifier[] identifiers = identification.getIdentifierList().getIdentifierArray();
            for (Identifier identifier : identifiers) {
                if (checkUniqueIDDefinition(identifier.getTerm().getDefinition())) {
                    id = identifier.getTerm().getValue();
                }
            }

        }
        if (id == null) {
            String message =
                    "Error reading sensorML description: Please make sure that the identifier of the sml:System is contained! Have a look at the RegisterSensor example request or ask the admin of this SOS!";
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, "SensorDescription", message);
            throw se;
        }
        Capabilities[] xb_capsArray = xb_system.getCapabilitiesArray();
        if (xb_capsArray.length >= 1) {
            Capabilities xb_caps = xb_capsArray[0];
            AbstractDataRecordType xb_absDataRec = xb_caps.getAbstractDataRecord();
            if (xb_absDataRec instanceof SimpleDataRecordType) {
                SimpleDataRecordType xb_sdr = (SimpleDataRecordType) xb_absDataRec;
                AnyScalarPropertyType[] xb_fieldArray = xb_sdr.getFieldArray();
                for (AnyScalarPropertyType xb_propType : xb_fieldArray) {
                    String name = xb_propType.getName();
                    if (name.equals(getMobileName())) {
                        Boolean xb_bool = xb_propType.getBoolean();
                        if (xb_bool != null) {
                            mobile = xb_bool.getValue();
                        } else {
                            String message =
                                    "Error reading sensorML description: Please make sure that the mobile field of the sml:System is contained in an sml:capabilities element! Have a look at the RegisterSensor example request or ask the admin of this SOS!";
                            OwsExceptionReport se = new OwsExceptionReport();
                            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                                    "SensorDescription", message);
                            throw se;
                        }
                    } else if (name.equals(getStatusName())) {
                        Boolean xb_bool = xb_propType.getBoolean();
                        if (xb_bool != null) {
                            active = xb_bool.getValue();
                        } else {
                            String message =
                                    "Error reading sensorML description: Please make sure that the status field of the sml:System is contained in an sml:capabilities element! Have a look at the RegisterSensor example request or ask the admin of this SOS!";
                            OwsExceptionReport se = new OwsExceptionReport();
                            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
                                    "SensorDescription", message);
                            throw se;
                        }
                    }
                }
            }
        } else {
            String message =
                    "Error reading sensorML description: exactly one 'sml:capabilities' element has to be contained in the sml:System of the SensorDescription parameter!";
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, "SensorDescription", message);
            throw se;
        }

        Position xb_position = xb_system.getPosition();

        // If none Position is given try first element of PositionList
        // TODO: make this work like it should
        if (xb_position == null) {
            try {
                Positions xb_positions = xb_system.getPositions();
                xb_position = xb_positions.getPositionList().getPositionArray(0);
            } catch (Exception e) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, null,
                        "Error reading position, please ensure that the position is included");
                throw se;
            }
        }
        PositionType xb_posType = xb_position.getPosition();

        Point actualPosition = parsePointPosition(xb_posType);

        Outputs xb_outputs = xb_system.getOutputs();
        if (xb_outputs == null) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, null,
                    "No outputs are set in the registerSensor system parameter!!");
            throw se;
        }

        Collection<SosPhenomenon> phenomena = parseOutputs(xb_outputs, uncertObs);

        if (!id.equals("")) {
            system =
                    new SensorSystem(id, descriptionURL, descriptionType, smlFile, actualPosition, active, mobile,
                            phenomena);
        }
        return system;
    }

	/**
	 * parses the outputs of a SensorML system and returns SosPhenomena
	 * representing the observed outputs of the sensor system; ATTENTION:
	 * currently only implemented for Quantities, Texts and Positions!
	 *
	 * @param xb_outputs
	 *            XMLBeans representation of SensorML System's output element
	 * @return Returns collection containing SosPhenomena representing the
	 *         outputs
	 * @throws OwsExceptionReport
	 */
	private static Collection<SosPhenomenon> parseOutputs(Outputs xb_outputs, boolean uncertObs)
			throws OwsExceptionReport {
		OutputList xb_outputList = xb_outputs.getOutputList();
		IoComponentPropertyType[] xb_outputArray = xb_outputList
				.getOutputArray();
		ArrayList<SosPhenomenon> phenomena = new ArrayList<SosPhenomenon>(
				xb_outputArray.length);
		for (int i = 0; i < xb_outputArray.length; i++) {
			ArrayList<SosOffering> offerings = null;
			IoComponentPropertyType xb_comPropType = xb_outputArray[i];

			Boolean xb_boolean = null;
			Count xb_count = null;
			Quantity xb_quantity = null;
			Time xb_time = null;
			Category xb_category = null;
			PositionType xb_positionType = null;
			Text xb_text = null;
			String uom = null;

			// determine if quantities or positions will be observed
			if (xb_comPropType.getBoolean() != null) {
				xb_boolean = xb_comPropType.getBoolean();
			} else if (xb_comPropType.getCount() != null) {
				xb_count = xb_comPropType.getCount();
			} else if (xb_comPropType.getQuantity() != null) {
				xb_quantity = xb_comPropType.getQuantity();
			} else if (xb_comPropType.getTime() != null) {
				xb_time = xb_comPropType.getTime();
			} else if (xb_comPropType.getCategory() != null) {
				xb_category = xb_comPropType.getCategory();
			} else if (xb_comPropType.getText() != null) {
				xb_text = xb_comPropType.getText();
			} else if (xb_comPropType.getAbstractDataRecord() != null) {
				if (xb_comPropType.getAbstractDataRecord() instanceof PositionType) {
					xb_positionType = (PositionType) xb_comPropType
							.getAbstractDataRecord();
				}

				// TODO create composite phenomenon from fields of DataRecords
			}

			if (xb_boolean != null || xb_count != null || xb_quantity != null || xb_time != null
					|| xb_category != null || xb_positionType != null
					|| xb_text != null) {

				MetaDataPropertyType[] metaDataArray = null;
				String phenID = null;
				String phenDesc = null;
				StringOrRefType phenGmlDesc = null;
				String valueType = null;

				if (xb_boolean != null) {
					metaDataArray = xb_boolean.getMetaDataPropertyArray();
					phenID = xb_boolean.getDefinition();
					phenGmlDesc = xb_boolean.getDescription();
					valueType = SosConstants.ValueTypes.booleanType.name();
				} else if (xb_count != null) {
					metaDataArray = xb_count.getMetaDataPropertyArray();
					phenID = xb_count.getDefinition();
					phenGmlDesc = xb_count.getDescription();
					valueType = SosConstants.ValueTypes.countType.name();
				} else if (xb_quantity != null) {
					metaDataArray = xb_quantity.getMetaDataPropertyArray();

					if (metaDataArray.length == 0) {
						String offeringID = null;
						String offeringName = null;

						XmlCursor qCursor = xb_quantity.newCursor();
						qCursor.toFirstChild();

						qCursor.toFirstContentToken();
						qCursor.toFirstChild();

						// read id and name of offering
						if (qCursor.getName().getLocalPart().equals("id")) {
							offeringID = qCursor.getTextValue();
						} else if (qCursor.getName().getLocalPart().equals("name")) {
							offeringName = qCursor.getTextValue();
						}
						qCursor.toNextSibling();
						if (qCursor.getName().getLocalPart().equals("id")) {
							offeringID = qCursor.getTextValue();
						} else if (qCursor.getName().getLocalPart().equals("name")) {
							offeringName = qCursor.getTextValue();
						}

						if (offeringID == null) {
							OwsExceptionReport se = new OwsExceptionReport();
							se.addCodedException(
									OwsExceptionReport.ExceptionCode.InvalidParameterValue,
									"Offering",
									"Error Reading Offering of the Sensor, please check if Output Property has an Child similar to:\n"
											+ "<gml:metaDataProperty>"
											+ "/n        <offering>"
											+ "/n               <id>OfferingID1</id>"
											+ "/n               <name>OfferingName1</name>"
											+ "/n        </offering>"
											+ "</gml:metaDataProperty>");
							throw se;
						}

						else {
							offerings = new ArrayList<SosOffering>(1);
							offerings.add(new SosOffering(offeringID, offeringName,
									null, null));
						}
					}

					phenID = xb_quantity.getDefinition();
					phenGmlDesc = xb_quantity.getDescription();
					valueType = SosConstants.ValueTypes.numericType.name();
				} else if (xb_time != null) {
					metaDataArray = xb_time.getMetaDataPropertyArray();
					phenID = xb_time.getDefinition();
					phenGmlDesc = xb_time.getDescription();
					valueType = SosConstants.ValueTypes.isoTimeType.name();
				} else if (xb_category != null) {
					metaDataArray = xb_category.getMetaDataPropertyArray();
					phenID = xb_category.getDefinition();
					phenGmlDesc = xb_category.getDescription();
					valueType = SosConstants.ValueTypes.categoryType.name();
				}
				// position default
				else if (xb_positionType != null) {
					metaDataArray = xb_positionType.getMetaDataPropertyArray();
					phenID = xb_positionType.getDefinition();
					phenGmlDesc = xb_positionType.getDescription();
					valueType = SosConstants.ValueTypes.spatialType.name();
				}
				// text or WKT position
				else if (xb_text != null) {
					metaDataArray = xb_text.getMetaDataPropertyArray();
					phenID = xb_text.getDefinition();
					phenGmlDesc = xb_text.getDescription();
					if (phenID.toLowerCase().matches(getPositionFieldRegex())
							|| xb_comPropType.getName().toLowerCase()
									.matches(getPositionFieldRegex())) {
						valueType = SosConstants.ValueTypes.spatialType.name();
					} else {
						valueType = SosConstants.ValueTypes.textType.name();
					}
				}

				checkPhenID(phenID);
				if (phenGmlDesc != null) {
					phenDesc = phenGmlDesc.getStringValue();
				}

				// parse offering of phenomenon
				for (MetaDataPropertyType metaData : metaDataArray) {
					String offeringID = null;
					String offeringName = null;
					// try {
					XmlCursor mdCursor = metaData.newCursor();
					mdCursor.toFirstContentToken();
					mdCursor.toFirstChild();

					// read id and name of offering
					if (mdCursor.getName().getLocalPart().equals("id")) {
						offeringID = mdCursor.getTextValue();
					} else if (mdCursor.getName().getLocalPart().equals("name")) {
						offeringName = mdCursor.getTextValue();
					}
					mdCursor.toNextSibling();
					if (mdCursor.getName().getLocalPart().equals("id")) {
						offeringID = mdCursor.getTextValue();
					} else if (mdCursor.getName().getLocalPart().equals("name")) {
						offeringName = mdCursor.getTextValue();
					}
					// FIXME: delete commentted code
					// } catch (Exception e) {
					// OwsExceptionReport se = new OwsExceptionReport();
					// se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
					// "Offering",
					// "Error Reading Offering of the Sensor, please check if Output Property has an Child similar to:\n"
					// + "<gml:metaDataProperty>" + "/n        <offering>"
					// + "/n               <id>OfferingID1</id>"
					// + "/n           <name>OfferingName1</name>" +
					// "/n        </offering>"
					// + "</gml:metaDataProperty>");
					// throw se;
					// }

					if (offeringID == null) {
						OwsExceptionReport se = new OwsExceptionReport();
						se.addCodedException(
								OwsExceptionReport.ExceptionCode.InvalidParameterValue,
								"Offering",
								"Error Reading Offering of the Sensor, please check if Output Property has an Child similar to:\n"
										+ "<gml:metaDataProperty>"
										+ "/n        <offering>"
										+ "/n               <id>OfferingID1</id>"
										+ "/n               <name>OfferingName1</name>"
										+ "/n        </offering>"
										+ "</gml:metaDataProperty>");
						throw se;
					}

					else {
						offerings = new ArrayList<SosOffering>(1);
						offerings.add(new SosOffering(offeringID, offeringName,
								null, null));
					}

				}

				// set uom only for quantities and times
				String code = null;
				if (xb_quantity != null) {
					if (xb_quantity.isSetUom()) {
						code = xb_quantity.getUom().getCode();
					}
				} else if (xb_time != null) {
					if (xb_time.isSetUom()) {
						code = xb_time.getUom().getCode();
					}
				}
				if (code != null && !code.equals("")) {
					uom = code;
				}

				// set valueType to 'uncertaintyType' for uncertaintyObservations
				if (uncertObs) {
					valueType = SosConstants.ValueTypes.uncertaintyType.name();
				}

				SosPhenomenon phen = new SosPhenomenon(phenID, phenDesc, uom,
						null, valueType, null, offerings);
				phenomena.add(phen);
			} else {
				OwsExceptionReport se = new OwsExceptionReport();
				se.addCodedException(
						OwsExceptionReport.ExceptionCode.InvalidParameterValue,
						"sml:outputs",
						"Currently, only booleans, quantities, ISO times, categories, texts and positions are supported for output phenomena of sensors!");
				throw se;
			}
		}
		return phenomena;
	}

    /**
     * Checks the URN of uniqueID definition.
     *
     * @param definition
     *            URN of uniqueID.
     * @return Boolean.
     */
    private static boolean checkUniqueIDDefinition(String definition) {
        if (definition != null
                && (definition.equals("urn:ogc:def:identifier:OGC:uniqueID")
                        || definition.equals("urn:ogc:def:identifier:OGC::uniqueID") || (definition
                        .startsWith("urn:ogc:def:identifier:OGC:") && definition.contains("uniqueID")))) {
            return true;
        }
        return false;
    }

    /**
     * throws service exception, if passed phenID is null or empty string
     *
     * @param phenID
     *            phenID, which should be checked
     * @throws OwsExceptionReport
     *             throws service exception, if passed phenID is null or empty
     *             string
     */
    private static void checkPhenID(String phenID) throws OwsExceptionReport {
        if (phenID == null || phenID.equals("")) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue, null,
                    "def attribute has to be set in quantities of outputList in sensor system, which should be registered !!");
            throw se;
        }
    }
}
