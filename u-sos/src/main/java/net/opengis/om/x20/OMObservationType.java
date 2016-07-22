package net.opengis.om.x20;

import org.apache.xmlbeans.XmlObject;
import org.isotc211.x2005.gmd.DQElementPropertyType;
import org.isotc211.x2005.gmd.MDMetadataPropertyType;

import net.opengis.gml.x32.AbstractFeatureType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimePeriodPropertyType;

/**
 * Dummy interface to handle different O&M implementations 
 * @author Kiesow
 *
 */

public abstract interface OMObservationType extends AbstractFeatureType {

	// Field descriptor #8 Lorg/apache/xmlbeans/SchemaType;
	// public static final org.apache.xmlbeans.SchemaType type;

	// Method descriptor #41 ()Lnet/opengis/gml/x32/ReferenceType;
	public abstract ReferenceType getType();

	// Method descriptor #43 ()Z
	public abstract boolean isSetType();

	// Method descriptor #45 (Lnet/opengis/gml/x32/ReferenceType;)V
	public abstract void setType(ReferenceType arg0);

	// Method descriptor #41 ()Lnet/opengis/gml/x32/ReferenceType;
	public abstract ReferenceType addNewType();

	// Method descriptor #10 ()V
	public abstract void unsetType();

	// Method descriptor #49 ()Lorg/isotc211/x2005/gmd/MDMetadataPropertyType;
	public abstract MDMetadataPropertyType getMetadata();

	// Method descriptor #43 ()Z
	public abstract boolean isSetMetadata();

	// Method descriptor #52 (Lorg/isotc211/x2005/gmd/MDMetadataPropertyType;)V
	public abstract void setMetadata(MDMetadataPropertyType arg0);

	// Method descriptor #49 ()Lorg/isotc211/x2005/gmd/MDMetadataPropertyType;
	public abstract MDMetadataPropertyType addNewMetadata();

	// Method descriptor #10 ()V
	public abstract void unsetMetadata();

	// Method descriptor #56
	// ()[Lnet/opengis/om/x20/ObservationContextPropertyType;
	public abstract ObservationContextPropertyType[] getRelatedObservationArray();

	// Method descriptor #57
	// (I)Lnet/opengis/om/x20/ObservationContextPropertyType;
	public abstract ObservationContextPropertyType getRelatedObservationArray(
			int arg0);

	// Method descriptor #59 ()I
	public abstract int sizeOfRelatedObservationArray();

	// Method descriptor #61
	// ([Lnet/opengis/om/x20/ObservationContextPropertyType;)V
	public abstract void setRelatedObservationArray(
			ObservationContextPropertyType[] arg0);

	// Method descriptor #62
	// (ILnet/opengis/om/x20/ObservationContextPropertyType;)V
	public abstract void setRelatedObservationArray(int arg0,
			ObservationContextPropertyType arg1);

	// Method descriptor #57
	// (I)Lnet/opengis/om/x20/ObservationContextPropertyType;
	public abstract ObservationContextPropertyType insertNewRelatedObservation(
			int arg0);

	// Method descriptor #65
	// ()Lnet/opengis/om/x20/ObservationContextPropertyType;
	public abstract ObservationContextPropertyType addNewRelatedObservation();

	// Method descriptor #67 (I)V
	public abstract void removeRelatedObservation(int arg0);

	// Method descriptor #69 ()Lnet/opengis/om/x20/TimeObjectPropertyType;
	public abstract TimeObjectPropertyType getPhenomenonTime();

	// Method descriptor #71 (Lnet/opengis/om/x20/TimeObjectPropertyType;)V
	public abstract void setPhenomenonTime(TimeObjectPropertyType arg0);

	// Method descriptor #69 ()Lnet/opengis/om/x20/TimeObjectPropertyType;
	public abstract TimeObjectPropertyType addNewPhenomenonTime();

	// Method descriptor #74 ()Lnet/opengis/gml/x32/TimeInstantPropertyType;
	public abstract TimeInstantPropertyType getResultTime();

	// Method descriptor #76 (Lnet/opengis/gml/x32/TimeInstantPropertyType;)V
	public abstract void setResultTime(TimeInstantPropertyType arg0);

	// Method descriptor #74 ()Lnet/opengis/gml/x32/TimeInstantPropertyType;
	public abstract TimeInstantPropertyType addNewResultTime();

	// Method descriptor #79 ()Lnet/opengis/gml/x32/TimePeriodPropertyType;
	public abstract TimePeriodPropertyType getValidTime();

	// Method descriptor #43 ()Z
	public abstract boolean isSetValidTime();

	// Method descriptor #82 (Lnet/opengis/gml/x32/TimePeriodPropertyType;)V
	public abstract void setValidTime(TimePeriodPropertyType arg0);

	// Method descriptor #79 ()Lnet/opengis/gml/x32/TimePeriodPropertyType;
	public abstract TimePeriodPropertyType addNewValidTime();

	// Method descriptor #10 ()V
	public abstract void unsetValidTime();

	// Method descriptor #86 ()Lnet/opengis/om/x20/OMProcessPropertyType;
	public abstract OMProcessPropertyType getProcedure();

	// Method descriptor #43 ()Z
	public abstract boolean isNilProcedure();

	// Method descriptor #89 (Lnet/opengis/om/x20/OMProcessPropertyType;)V
	public abstract void setProcedure(OMProcessPropertyType arg0);

	// Method descriptor #86 ()Lnet/opengis/om/x20/OMProcessPropertyType;
	public abstract OMProcessPropertyType addNewProcedure();

	// Method descriptor #10 ()V
	public abstract void setNilProcedure();

	// Method descriptor #93 ()[Lnet/opengis/om/x20/NamedValuePropertyType;
	public abstract NamedValuePropertyType[] getParameterArray();

	// Method descriptor #94 (I)Lnet/opengis/om/x20/NamedValuePropertyType;
	public abstract NamedValuePropertyType getParameterArray(int arg0);

	// Method descriptor #59 ()I
	public abstract int sizeOfParameterArray();

	// Method descriptor #97 ([Lnet/opengis/om/x20/NamedValuePropertyType;)V
	public abstract void setParameterArray(NamedValuePropertyType[] arg0);

	// Method descriptor #98 (ILnet/opengis/om/x20/NamedValuePropertyType;)V
	public abstract void setParameterArray(int arg0, NamedValuePropertyType arg1);

	// Method descriptor #94 (I)Lnet/opengis/om/x20/NamedValuePropertyType;
	public abstract NamedValuePropertyType insertNewParameter(int arg0);

	// Method descriptor #101 ()Lnet/opengis/om/x20/NamedValuePropertyType;
	public abstract NamedValuePropertyType addNewParameter();

	// Method descriptor #67 (I)V
	public abstract void removeParameter(int arg0);

	// Method descriptor #41 ()Lnet/opengis/gml/x32/ReferenceType;
	public abstract ReferenceType getObservedProperty();

	// Method descriptor #43 ()Z
	public abstract boolean isNilObservedProperty();

	// Method descriptor #45 (Lnet/opengis/gml/x32/ReferenceType;)V
	public abstract void setObservedProperty(ReferenceType arg0);

	// Method descriptor #41 ()Lnet/opengis/gml/x32/ReferenceType;
	public abstract ReferenceType addNewObservedProperty();

	// Method descriptor #10 ()V
	public abstract void setNilObservedProperty();

	// Method descriptor #109 ()Lnet/opengis/gml/x32/FeaturePropertyType;
	public abstract FeaturePropertyType getFeatureOfInterest();

	// Method descriptor #43 ()Z
	public abstract boolean isNilFeatureOfInterest();

	// Method descriptor #112 (Lnet/opengis/gml/x32/FeaturePropertyType;)V
	public abstract void setFeatureOfInterest(FeaturePropertyType arg0);

	// Method descriptor #109 ()Lnet/opengis/gml/x32/FeaturePropertyType;
	public abstract FeaturePropertyType addNewFeatureOfInterest();

	// Method descriptor #10 ()V
	public abstract void setNilFeatureOfInterest();

	// Method descriptor #116 ()[Lorg/isotc211/x2005/gmd/DQElementPropertyType;
	public abstract DQElementPropertyType[] getResultQualityArray();

	// Method descriptor #117 (I)Lorg/isotc211/x2005/gmd/DQElementPropertyType;
	public abstract DQElementPropertyType getResultQualityArray(int arg0);

	// Method descriptor #59 ()I
	public abstract int sizeOfResultQualityArray();

	// Method descriptor #120 ([Lorg/isotc211/x2005/gmd/DQElementPropertyType;)V
	public abstract void setResultQualityArray(DQElementPropertyType[] arg0);

	// Method descriptor #121 (ILorg/isotc211/x2005/gmd/DQElementPropertyType;)V
	public abstract void setResultQualityArray(int arg0,
			DQElementPropertyType arg1);

	// Method descriptor #117 (I)Lorg/isotc211/x2005/gmd/DQElementPropertyType;
	public abstract DQElementPropertyType insertNewResultQuality(int arg0);

	// Method descriptor #124 ()Lorg/isotc211/x2005/gmd/DQElementPropertyType;
	public abstract DQElementPropertyType addNewResultQuality();

	// Method descriptor #67 (I)V
	public abstract void removeResultQuality(int arg0);

	// Method descriptor #127 ()Lorg/apache/xmlbeans/XmlObject;
	public abstract XmlObject getResult();

	// Method descriptor #129 (Lorg/apache/xmlbeans/XmlObject;)V
	public abstract void setResult(XmlObject arg0);

	// Method descriptor #127 ()Lorg/apache/xmlbeans/XmlObject;
	public abstract XmlObject addNewResult();

	// Inner classes:
	// [inner class info: #134 net/opengis/om/x20/OMObservationType$Factory,
	// outer class info: #1 net/opengis/om/x20/OMObservationType
	// inner name: #136 Factory, accessflags: 25 public static final]
}
