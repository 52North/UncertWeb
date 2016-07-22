package org.uncertweb.aqms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.n52.wps.io.data.binding.complex.UncertWebIODataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.api.om.observation.collections.UncertaintyObservationCollection;
import org.uncertweb.aqms.interpolation.PolygonKriging;
import org.uncertweb.aqms.interpolation.SOSconnection;
import org.uncertweb.aqms.util.Utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class PolygonInterpolationAlgorithm extends AbstractObservableAlgorithm{

	private final String inputIDStartTime = "start-time";
	private final String inputIDEndTime = "end-time";
	private final String inputIDSOSURL = "sos-url";
	private final String inputIDPredictGeom = "prediction-area";
	private final String outputIDResult = "result";

	private DateTime startDate, endDate;
//	private static String resultsPath = "C:\\Temp\\AQMS";

	private List<String> errors = new ArrayList<String>();

	public List<String> getErrors() {
		return errors;
	}

	public Class getInputDataType(String id) {
		if(id.equals(inputIDStartTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDEndTime)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDSOSURL)){
			return LiteralDateTimeBinding.class;
		}else if(id.equals(inputIDPredictGeom)){
			return GTVectorDataBinding.class;
			//return LiteralStringBinding.class;
		}else{
			return GenericFileDataBinding.class;
		}
	}

	public Class getOutputDataType(String arg0) {
		return UncertWebIODataBinding.class;
	}

	public Map<String, IData> run(Map<String, List<IData>> inputMap) {
		// 1) get INPUTS
				// get dates
				//2010-03-01T01:00:00.000+01
				DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
				List<IData> startList = inputMap.get(inputIDStartTime);
				String startTime = "2010-03-01T01:00:00.000+01";
				if(startList!=null){
					startTime = ((IData)startList.get(0)).getPayload().toString();
				}

				List<IData> endList = inputMap.get(inputIDEndTime);
				String endTime = "2010-03-05T00:00:00.000+01";
				if(endList!=null){
					endTime = ((IData)endList.get(0)).getPayload().toString();
				}

				startDate = dateFormat.parseDateTime(startTime);
				endDate = dateFormat.parseDateTime(endTime);

				// get SOS url
				List<IData> sosUrlList = inputMap.get(inputIDSOSURL);
				String sosURL = ((IData)sosUrlList.get(0)).getPayload().toString();

				// get prediction polygon
				List<IData> predictionDataList = inputMap.get(inputIDPredictGeom);
				double maxX=0, minX=0, maxY=0, minY=0;
				int epsg_prediction = 0;

				if(!(predictionDataList == null) && predictionDataList.size() != 0){
					IData predictionData = predictionDataList.get(0);
					if(predictionData instanceof GTVectorDataBinding){
						FeatureCollection<?,?> featColl =  (FeatureCollection<?, ?>)predictionData.getPayload();
						FeatureIterator<?> iterator = featColl.features();

						// get bounding box
						ReferencedEnvelope env = featColl.getBounds();
						maxX = env.getMaxX();
						minX = env.getMinX();
						maxY = env.getMaxY();
						minY = env.getMinY();

						// else get polygon feature
						if(minX==0){
							while (iterator.hasNext()) {
								SimpleFeature feature = (SimpleFeature) iterator.next();
								if (feature.getDefaultGeometry() instanceof com.vividsolutions.jts.geom.Polygon) {
									epsg_prediction =  ((Geometry) feature.getDefaultGeometry()).getSRID();
									Coordinate[] coords = ((Geometry) feature.getDefaultGeometry())
											.getCoordinates();
									minX=coords[0].x;
									minY=coords[0].y;
									for(Coordinate c : coords){
										if(c.x<=minX)
											minX=c.x;
										if(c.x>=maxX)
											maxX=c.x;
										if(c.y<=minY)
											minX=c.y;
										if(c.y>=maxY)
											maxX=c.y;
									}
								}
							}
						}
					}
				}


				// 2) perform INTERPOLATION
				// required objects
				SOSconnection sosConn = new SOSconnection(sosURL);
				PolygonKriging kriging = new PolygonKriging(minX, maxX, minY, maxY, epsg_prediction);
				UncertaintyObservationCollection uColl = null;

				// if time period is too long, split requests
				if(Days.daysBetween(startDate, endDate).getDays()>10){
						ArrayList<UncertaintyObservationCollection> uobsList = new ArrayList<UncertaintyObservationCollection>();
						DateTime startTemp = startDate;
						DateTime endTemp = startDate.plusDays(10);

						// 2.1) SOS request
				    	IObservationCollection iobs = sosConn.getObservationAll(startDate.minusHours(1).toString(dateFormat), endTemp.plusHours(1).toString(dateFormat));
//				        Utils.writeObsColl(iobs, resultsPath + "\\UBASOS_"+startDate.toString("yyyy-MM-dd")+".xml");

				        // 2.2) call R interpolation process
				        uobsList.add(kriging.performKriging(iobs, startDate));

				        // if time difference is still too large
						while(Days.daysBetween(endTemp, endDate).getDays()>10){
							startTemp = endTemp;
							endTemp = endTemp.plusDays(10);

							// 1.1) SOS request
					    	iobs = sosConn.getObservationAll(startTemp.minusHours(1).toString(dateFormat), endTemp.plusHours(1).toString(dateFormat));
//					    	Utils.writeObsColl(iobs, resultsPath + "\\UBASOS_"+startTemp.toString("yyyy-MM-dd")+".xml");

					        // 1.2) call R interpolation process
					        uobsList.add(kriging.performKriging(iobs, startTemp));
						}

						// finally for the last time period
						// 2.1) SOS request
				    	iobs = sosConn.getObservationAll(endTemp.minusHours(1).toString(dateFormat), endDate.plusHours(1).toString(dateFormat));
//				    	Utils.writeObsColl(iobs, resultsPath + "\\UBASOS_"+endTemp.toString("yyyy-MM-dd")+".xml");

				        // 2.2) call R interpolation process
				        uobsList.add(kriging.performKriging(iobs, endTemp));

				        // Merge resulting collections
				        uColl = kriging.mergeUncertaintyObsColl(uobsList);
					}else{
						// 2.1) SOS request
				    	IObservationCollection iobs = sosConn.getObservationAll(startDate.minusHours(1).toString(dateFormat), endDate.plusHours(1).toString(dateFormat));

				        // 2.2) call R interpolation process
				        uColl = kriging.performKriging(iobs, startDate);
				}


				// 3) prepare RESULTS
				Map<String, IData> result = new HashMap<String, IData>();
				OMBinding omd = new OMBinding(uColl);
				result.put(outputIDResult, omd);
				return result;

	}

}
