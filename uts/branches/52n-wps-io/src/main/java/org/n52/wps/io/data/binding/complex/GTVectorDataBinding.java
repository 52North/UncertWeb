package org.n52.wps.io.data.binding.complex;

import java.io.IOException;
import java.io.StringWriter;

import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.datahandler.xml.SimpleGMLGenerator;
import org.n52.wps.io.datahandler.xml.SimpleGMLParser;

public class GTVectorDataBinding implements IComplexData{
	
	private transient FeatureCollection featureCollection;	
	
	public GTVectorDataBinding(FeatureCollection payload) {
		this.featureCollection = payload;
	}

	public Class<FeatureCollection> getSupportedClass() {
		return FeatureCollection.class;
	}

	public FeatureCollection getPayload() {
			return featureCollection;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		StringWriter buffer = new StringWriter();
		SimpleGMLGenerator generator = new SimpleGMLGenerator(false);
		generator.write(this, buffer);
		oos.writeObject(buffer.toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		SimpleGMLParser parser = new SimpleGMLParser(false);
		this.featureCollection = parser.parseXML((String) oos.readObject()).getPayload();
	}

}
