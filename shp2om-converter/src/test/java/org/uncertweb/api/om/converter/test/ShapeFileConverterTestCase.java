package org.uncertweb.api.om.converter.test;

import java.io.IOException;

import org.uncertweb.api.om.converter.ShapeFileConverter;

import junit.framework.TestCase;

public class ShapeFileConverterTestCase extends TestCase {

	private static final String FILE_PATH="file://D:/IfGI/Projekte/UncertWeb/Implementations/uw_workspace/shape-om-converter/src/main/resources";

	public void setUp() {
	}

//	public void testConverter() throws Exception{
//		ShapeFileConverter converter = new ShapeFileConverter();
//		converter.convertShp2OM(FILE_PATH+"/shp/lqMS.shp", "");
//	}

	public void testUncertaintyConverter() throws Exception{
		ShapeFileConverter converter = new ShapeFileConverter();
		converter.run();
	}

	public void tearDown() {
	}
}
