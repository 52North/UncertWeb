package org.uncertweb.viss.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.uncertweb.viss.core.resource.IDataSet;
import org.uncertweb.viss.core.util.MediaTypes;
import org.uncertweb.viss.mongo.resource.AbstractMongoResource;
import org.uncertweb.viss.mongo.resource.MongoResourceStore;
import org.uncertweb.viss.vis.sample.SampleVisualizer;

public class OsloTest {
	public static void main(String[] args) throws IOException,
			URISyntaxException, JSONException {

		final AbstractMongoResource<?> r = new MongoResourceStore()
				.getResourceForMediaType(
						MediaTypes.NETCDF_TYPE,
						new File(OsloTest.class.getResource(
								"/data/oslo_conc_20110103_new2.nc").toURI()),
						new ObjectId(), 0);

		final IDataSet ds = r.getDataSets().iterator().next();
		final SampleVisualizer v = new SampleVisualizer();

		System.out.println(new JSONObject(v.getOptionsForDataSet(ds)).toString(4));
		
		v.visualize(ds, new JSONObject()
				.put("realisation", 0)
				.put("sample", 3)
				.put("time", "2011-01-03T02:00:00.000+01:00"));
	}
}
