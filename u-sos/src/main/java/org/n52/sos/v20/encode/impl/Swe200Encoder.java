/***************************************************************
 Copyright (C) 2011
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under
 the terms of the GNU General Public License version 2 as published by the
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/
package org.n52.sos.v20.encode.impl;

import net.opengis.swe.x20.AbstractDataComponentType;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.VectorType.Coordinate;

import org.n52.sos.SosXmlUtilities;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.simpleType.ISosSweSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;

/**
 * Encoder class for SWE Common 2.0.0
 *
 * @author Carsten Hollmann
 * @version 1.0.0
 */
public class Swe200Encoder {

    /**
     * @param element
     * @return
     * @throws OwsExceptionReport
     */
    public static AbstractDataComponentType addSweSimpleTypToField(ISosSweSimpleType element) throws OwsExceptionReport {
        if (element instanceof SosSweQuantity) {
           QuantityType xbQuantity = QuantityType.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe200());
           addValuesToSimpleTypeQuantity(xbQuantity, (SosSweQuantity) element);
           return xbQuantity;
       } else if (element instanceof SosSweText) {
           TextType xbText = TextType.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe200());
           addValuesToSimpleTypeText(xbText, (SosSweText)element);
           return xbText;
       } else if (element instanceof SosSweTime) {
           TimeType xbTime = TimeType.Factory.newInstance(SosXmlUtilities.getInstance().getXmlOptions4Sos2Swe200());
           addValuesToSimpleTypeTime(xbTime, (SosSweTime)element);
           return xbTime;
       } else {
           // TODO: NOT SUPPORTED EXCEPTION
           throw new OwsExceptionReport();
       }
   }

    /**
     * Adds values to SWE quantity
     *
     * @param xbQuantity
     *            SWE Quantity
     * @param quantity
     *            SOS internal representation
     */
    public static void addValuesToSimpleTypeQuantity(QuantityType xbQuantity, SosSweQuantity quantity) {
        if (quantity.getDefinition() != null && !quantity.getDefinition().isEmpty()) {
            xbQuantity.setDefinition(quantity.getDefinition());
        }
        if (quantity.getDescription() != null && !quantity.getDescription().isEmpty()) {
            xbQuantity.setDescription(quantity.getDescription());
        }
        if (quantity.getAxisID() != null && !quantity.getAxisID().isEmpty()) {
            xbQuantity.setAxisID(quantity.getDescription());
        }
        if (quantity.getValue() != null && !quantity.getValue().isEmpty()) {
            xbQuantity.setValue(Double.valueOf(quantity.getValue()));
        }
        if (quantity.getUom() != null && !quantity.getUom().isEmpty()) {
            xbQuantity.addNewUom().setCode(quantity.getUom());
        }
        if (quantity.getQuality() != null) {
            // TODO
        }
    }

    /**
     * Adds values to SWE text
     *
     * @param xbText
     *            SWE text
     * @param text
     *            SOS internal representation
     */
    public static void addValuesToSimpleTypeText(TextType xbText, SosSweText text) {
        if (text.getDefinition() != null && !text.getDefinition().isEmpty()) {
            xbText.setDefinition(text.getDefinition());
        }
        if (text.getDescription() != null && !text.getDescription().isEmpty()) {
            xbText.setDescription(text.getDescription());
        }
        if (text.getValue() != null && !text.getValue().isEmpty()) {
            xbText.setValue(text.getValue());
        }
    }

    public static void addValuesToSimpleTypeTime(TimeType xbTime, SosSweTime time) {
        if (time.getDefinition() != null && !time.getDefinition().isEmpty()) {
            xbTime.setDefinition(time.getDefinition());
        }
        if (time.getDescription() != null && !time.getDescription().isEmpty()) {
            xbTime.setDescription(time.getDescription());
        }
        if (time.getValue() != null && !time.getValue().isEmpty()) {
            xbTime.setValue(time.getValue());
        }
        if (time.getUom() != null && !time.getUom().isEmpty()) {
            xbTime.addNewUom().setHref(time.getUom());
        }
        if (time.getQuality() != null) {
            // TODO
        }

    }

    /**
     * Adds values to SWE coordinates
     *
     * @param xbCoordinate
     *            SWE coordinate
     * @param coordinate
     *            SOS internal representation
     */
    public static void addValuesToCoordinate(Coordinate xbCoordinate, SosSweCoordinate coordinate) {
        xbCoordinate.setName(coordinate.getName().name());
        addValuesToSimpleTypeQuantity(xbCoordinate.addNewQuantity(), (SosSweQuantity) coordinate.getValue());
    }
}
