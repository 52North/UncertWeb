package org.uncertweb.viss.core.resource.mongo;

import static org.uncertweb.viss.core.util.Constants.GEOTIFF_TYPE;
import static org.uncertweb.viss.core.util.Constants.NETCDF_TYPE;
import static org.uncertweb.viss.core.util.Constants.OM_2_TYPE;
import static org.uncertweb.viss.core.util.Constants.X_NETCDF_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import net.opengis.sld.StyledLayerDescriptorDocument;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.uncertweb.viss.core.VissError;
import org.uncertweb.viss.core.mongo.MongoDB;
import org.uncertweb.viss.core.resource.Resource;
import org.uncertweb.viss.core.resource.ResourceStore;
import org.uncertweb.viss.core.util.Constants;
import org.uncertweb.viss.core.util.Utils;
import org.uncertweb.viss.core.visualizer.Visualization;
import org.uncertweb.viss.core.visualizer.VisualizationReference;
import org.uncertweb.viss.visualizer.MeanVisualizer;

import com.google.code.morphia.dao.BasicDAO;
import com.mongodb.DBCursor;

public class MongoResourceStore implements ResourceStore {

	private class ResourceDAO extends BasicDAO<AbstractMongoResource, UUID> {
		protected ResourceDAO() {
			super(MongoDB.getInstance().getDatastore());
		}
	}

	private static final String RESOURCE_PATH = Constants.RESOURCE_PATH != null ? Constants.RESOURCE_PATH
			: Utils.join(File.separator, System.getProperty("java.io.tmpdir"),
					"VISS_TEMP");

	private ResourceDAO dao = new ResourceDAO();
	private File resourceDir;

	protected ResourceDAO getDao() {
		return this.dao;
	}

	@Override
	public Resource get(UUID uuid) {
		AbstractMongoResource r = getDao().get(uuid);
		if (r == null) {
			throw VissError.noSuchResource();
		}
		return r;
	}

	@Override
	public void rm(Resource resource) {
		getDao().delete((AbstractMongoResource) resource);
		Utils.deleteRecursively(getResourceDir(resource.getUUID()));
	}

	@Override
	public AbstractMongoResource add(InputStream is, MediaType mt) {
		UUID uuid = UUID.randomUUID();
		try {
			AbstractMongoResource r = getResourceForMediaType(mt);
			File f = createResourceFile(uuid, mt);
			Utils.saveToFile(f, is);
			r.setFile(f);
			r.setUUID(UUID.randomUUID());
			getDao().save(r);
			return r;
		} catch (Exception e) {
			throw VissError.internal(e);
		}
	}

	@Override
	public Set<Resource> getAll() {
		return Utils.<Resource> asSet(getDao().find().asList());
	}

	@Override
	public Set<Resource> getResourcesUsedBefore(DateTime dt) {
		return Utils.<Resource> asSet(getDao().createQuery()
				.field(AbstractMongoResource.TIME_PROPERTY).lessThanOrEq(dt)
				.asList());
	}

	@Override
	public void saveVisualizationForResource(Resource r, Visualization v) {
		AbstractMongoResource amr = (AbstractMongoResource) r;
		amr.addVisualization(v);
		getDao().save(amr);
	}

	@Override
	public void deleteVisualizationForResource(Resource r, Visualization v) {
		AbstractMongoResource amr = (AbstractMongoResource) r;
		amr.removeVisualization(v);
		getDao().save(amr);
	}

	public File createResourceFile(UUID uuid, MediaType mt) throws IOException {
		File dir = getResourceDir(uuid);
		if (!dir.exists())
			dir.mkdirs();
		File f = File.createTempFile("RES-", "", dir);
		return f;
	}

	public File getResourceDir(UUID uuid) {
		if (resourceDir == null) {
			resourceDir = new File(RESOURCE_PATH);
			if (!resourceDir.exists())
				resourceDir.mkdirs();
		}
		return Utils.to(resourceDir, uuid.toString());
	}

	public AbstractMongoResource getResourceForMediaType(MediaType mt)
			throws IOException {
		if (mt.equals(GEOTIFF_TYPE)) {
			return new GeoTIFFResource();
		} else if (mt.equals(NETCDF_TYPE) || mt.equals(X_NETCDF_TYPE)) {
			return new NetCDFResource();
		} else if (mt.equals(OM_2_TYPE)) {
			return new OMResource();
		}
		throw VissError.internal("Can not create resource for '" + mt + "'");
	}

	public static void main(String[] args) throws FileNotFoundException,
			JSONException, MalformedURLException {
		MongoResourceStore mrs = new MongoResourceStore();

		AbstractMongoResource r = mrs.add(new FileInputStream(
				"/home/auti/doc/src/uw/viss/src/test/resources/biotemp.nc"),
				Constants.NETCDF_TYPE);
		Visualization v = new Visualization();
		v.setCreator(new MeanVisualizer());
		v.setParameters(new JSONObject().put("test", "testtest").put("test2",
				new JSONObject().put("hallo", "welt")));
		v.setReference(new VisualizationReference("http://localhost:8080/wcs",
				"testLayer", "testLayer2"));
		StyledLayerDescriptorDocument sld = StyledLayerDescriptorDocument.Factory
				.newInstance();
		sld.addNewStyledLayerDescriptor().addNewUserLayer().addNewUserStyle()
				.addNewFeatureTypeStyle().addNewRule().addNewFilter()
				.addNewLogicOps();
		v.setSld(sld);
		mrs.saveVisualizationForResource(r, v);

		DBCursor c = mrs.dao.getCollection().find();
		System.out.println(mrs.getResourcesUsedBefore(new DateTime()).size());
		while (c.hasNext())
			System.out.println(c.next());
		c.close();

	}
}