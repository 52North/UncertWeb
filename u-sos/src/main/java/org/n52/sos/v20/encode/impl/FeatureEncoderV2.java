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

 Author: Christoph Stasch, Stephan Kuenster
 Created: <CREATION DATE>
 Modified: 08/11/2008
 ***************************************************************/
package org.n52.sos.v20.encode.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.sampling.x20.SFSamplingFeatureCollectionDocument;
import net.opengis.sampling.x20.SFSamplingFeatureCollectionType;
import net.opengis.sampling.x20.SFSamplingFeaturePropertyType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;
import net.opengis.sos.x20.GetFeatureOfInterestResponseDocument;
import net.opengis.sos.x20.GetFeatureOfInterestResponseType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.Sos2Constants;
import org.n52.sos.SosConfigurator;
import org.n52.sos.SosXmlUtilities;
import org.n52.sos.encode.IFeatureEncoder;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosAbstractSamplingFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingPoint;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingSurface;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.utilities.SosUtilities;

/**
 * class encapsulates encoding methods for OM sampling features
 * 
 * @author Carsten Hollmann
 * 
 */
public class FeatureEncoderV2 implements IFeatureEncoder {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.encode.IFeatureEncoder#createGetFeatureOfInterestResponse
     * (org.n52.sos.ogc.om.features.SosAbstractFeature)
     */
    public XmlObject createGetFeatureOfInterestResponse(SosAbstractFeature sosAbstractFeatures)
            throws OwsExceptionReport {
        int sfIdCounter = 1;
        HashMap<String, String> gmlID4sfIdentifier = new HashMap<String, String>();

        GetFeatureOfInterestResponseDocument xbGetFoiResponseDoc =
                GetFeatureOfInterestResponseDocument.Factory.newInstance(SosXmlUtilities.getInstance()
                        .getXmlOptions4Sos2Swe200());
        GetFeatureOfInterestResponseType xbGetFoiResponse = xbGetFoiResponseDoc.addNewGetFeatureOfInterestResponse();
        if (sosAbstractFeatures instanceof SosFeatureCollection) {
            SosFeatureCollection sosFeatCol = (SosFeatureCollection) sosAbstractFeatures;
            for (SosAbstractFeature feature : sosFeatCol.getMembers()) {
                FeaturePropertyType xbFeatMember = xbGetFoiResponse.addNewFeatureMember();
                String identifier = null;
                if (feature.getId() != null && !feature.getId().isEmpty()) {
                    identifier = feature.getId();
                } else if (feature.getName() != null && !feature.getId().isEmpty()) {
                    identifier = feature.getName();
                } else {
                    identifier = Long.toString(new DateTime().getMillis());
                }
                if (gmlID4sfIdentifier.containsKey(identifier)) {
                    xbFeatMember.setHref("#" + gmlID4sfIdentifier.get(identifier));
                } else {
                    String gmlId = "sf_" + sfIdCounter;
                    sfIdCounter++;
                    xbFeatMember.set(createSpatialSamplingFeature(feature, gmlId));
                    gmlID4sfIdentifier.put(identifier, gmlId);
                }
            }
        } else {
            String gmlId = "sf_" + sfIdCounter;
            xbGetFoiResponse.addNewFeatureMember().set(createSpatialSamplingFeature(sosAbstractFeatures, gmlId));
        }

        return xbGetFoiResponseDoc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.encode.IFeatureEncoder#createSpatialSamplingFeature(org.n52
     * .sos.ogc.om.features.SosAbstractFeature)
     */
    public SFSpatialSamplingFeatureDocument createSpatialSamplingFeature(SosAbstractFeature absFeature, String gmlID) {
        SFSpatialSamplingFeatureDocument xbSampFeatDoc =
                SFSpatialSamplingFeatureDocument.Factory.newInstance(SosXmlUtilities.getInstance()
                        .getXmlOptions4Sos2Swe200());
        SFSpatialSamplingFeatureType xbSampFeature = xbSampFeatDoc.addNewSFSpatialSamplingFeature();

        // set gml:id
        xbSampFeature.setId(gmlID);
        if (!gmlID.startsWith("#")) {

            // set identifier
            CodeWithAuthorityType identifier = xbSampFeature.addNewIdentifier();
            identifier.setCodeSpace("");
            identifier.setStringValue(absFeature.getId());

            // set type
            xbSampFeature.addNewType().setHref(getFeatureType(absFeature));

            // set sampledFeatures
            FeaturePropertyType sampFeat = null;
            SosAbstractSamplingFeature sampAbsFoi = (SosAbstractSamplingFeature) absFeature;
            Collection<SosAbstractFeature> dfs = sampAbsFoi.getDomainFeatureIDs();
            if (dfs != null && !dfs.isEmpty()) {
                for (SosAbstractFeature sosAbstractFeature : dfs) {
                    sampFeat = xbSampFeature.addNewSampledFeature();
                    // Set sampledFeature type
                    if (sosAbstractFeature.getFeatureType() != null && !sosAbstractFeature.getFeatureType().equals("")) {
                        sampFeat.setRole(sosAbstractFeature.getFeatureType());
                    }
                    if (sosAbstractFeature.getId().startsWith("http")
                            || sosAbstractFeature.getId().startsWith("https")) {
                        sampFeat.setHref(sosAbstractFeature.getId());
                    } else {
                        sampFeat.setHref(SosUtilities.createFoiGetUrl(sosAbstractFeature.getId(),
                                Sos2Constants.SERVICEVERSION));
                    }
                }
            } else {
                sampFeat = xbSampFeature.addNewSampledFeature();
                sampFeat.setHref(GMLConstants.HREF_NIL);
            }

            // set position
            ShapeType xbShape = xbSampFeature.addNewShape();
            SosConfigurator.getInstance().getGml321Encoder()
                    .createPosition(absFeature.getGeom(), xbShape.addNewAbstractGeometry(), gmlID);

            // set schemLocation
            XmlCursor cursor = xbSampFeatDoc.newCursor();
            if (cursor.toFirstChild()) {
                cursor.setAttributeText(OMConstants.SCHEMA_LOCATION_QNAME, OMConstants.ATTS_4_SCHEMALOC_SAMS);
            }
            cursor.dispose();
        }
        return xbSampFeatDoc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.encode.IFeatureEncoder#createSamplingFeatureCollection(java
     * .util.List)
     */
    @Override
    public XmlObject createSamplingFeatureCollection(Map<SosAbstractFeature, String> foiGmlIds) {

        if (foiGmlIds.size() == 1) {
            for (SosAbstractFeature sosAbstractFeature : foiGmlIds.keySet()) {
                return createSpatialSamplingFeature(sosAbstractFeature, foiGmlIds.get(sosAbstractFeature));
            }
        }
        SFSamplingFeatureCollectionDocument xbSampFeatCollDoc =
                SFSamplingFeatureCollectionDocument.Factory.newInstance(SosXmlUtilities.getInstance()
                        .getXmlOptions4Sos2Swe200());
        SFSamplingFeatureCollectionType xbSampFeatColl = xbSampFeatCollDoc.addNewSFSamplingFeatureCollection();
        xbSampFeatColl.setId("sfc_" + Long.toString(new DateTime().getMillis()));
        for (SosAbstractFeature sosAbstractFeature : foiGmlIds.keySet()) {
            SFSamplingFeaturePropertyType xbFeatMember = xbSampFeatColl.addNewMember();
            if (foiGmlIds.get(sosAbstractFeature).startsWith("#")) {
                xbFeatMember.setHref("#" + foiGmlIds.get(sosAbstractFeature));
            } else {
                xbFeatMember.set(createSpatialSamplingFeature(sosAbstractFeature, foiGmlIds.get(sosAbstractFeature)));
            }
        }
        return xbSampFeatCollDoc;
    }

    /**
     * Get the OM 2.0 feature type definition for a SOS feature.
     * 
     * @param absFeature
     *            SOS feature
     * @return Feature type definition.
     */
    private String getFeatureType(SosAbstractFeature absFeature) {
        if (absFeature instanceof SosSamplingPoint) {
            return "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";
        }

        else if (absFeature instanceof SosSamplingSurface) {
            return "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingSurface";
        } else {
            return "http://www.opengis.net/def/samplingFeatureType/unknown";
        }
    }

}
