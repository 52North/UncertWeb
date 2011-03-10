package org.n52.wps.io.datahandler.xml;

import java.io.InputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.XMLParser;

public class UncertMLParser extends AbstractXMLParser {

	private XMLParser parser = new XMLParser();
	
	@Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class<?>[] supportedClasses = {UncertWebDataBinding.class};
		return supportedClasses;
	}

	@Override
	public IData parse(InputStream primaryFile, String mimeType) {
		UncertWebData uData = new UncertWebData(primaryFile, mimeType);
		return new UncertWebDataBinding(uData);
	}

	@Override
	public IData parseXML(String uncertML) {
		
		try {
			IUncertainty uncertaintyType = parser.parse(uncertML);
			
			UncertWebData uwData = new UncertWebData(uncertaintyType);
			
			UncertWebDataBinding result = new UncertWebDataBinding(uwData);
			
			return result;
		} catch (UncertaintyParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
//		InputStream is = new ByteArrayInputStream(uncertML.getBytes());
//				
////		int i = 0;
////		
////		try {
////			while((i=is.read())!= -1){
////				
////			System.out.println((char)i);	
////				
////			}
////		} catch (IOException e1) {
////			// TODO Auto-generated catch block
////			e1.printStackTrace();
////		}
//		
//		
//		try{
//		
//		DocumentBuilderFactory dBFac = DocumentBuilderFactory.newInstance();
//		
//		DocumentBuilder dB = dBFac.newDocumentBuilder();
//		
//		Document d = dB.parse(is);
//		
//		Node n = d.getFirstChild().getChildNodes().item(1);
//		
//		String s = n.getAttributes().item(0).getNodeValue();
//		
//		String name = n.getNodeName();
//		
//		System.out.println(d.getLocalName());
//		
//		Node n1 = d.getFirstChild().getChildNodes().item(3);
//		
//		String s1 = n1.getAttributes().item(0).getNodeValue();
//		
//		String name1 = n1.getNodeName();
//		
//		HashMap<String, Object> uncertaintyTypesValuesMap = new HashMap<String, Object>();
//		
//		uncertaintyTypesValuesMap.put(name, s);
//		uncertaintyTypesValuesMap.put(name1, s1);
//		
//		UncertWebData uData = new UncertWebData(uncertaintyTypesValuesMap);
//		
//		return new UncertWebDataBinding(uData);
//		
//		}catch(Exception e){
//			
//			
//		}
////		String url1 = new String("http://localhost:8080/uts/resources/jrcepratio.tif");
////		
////		String url2 = new String("http://localhost:8080/uts/resources/jrcuepratio.tif");
//
//		
//		return null;
	}

	@Override
	public IData parseXML(InputStream arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
