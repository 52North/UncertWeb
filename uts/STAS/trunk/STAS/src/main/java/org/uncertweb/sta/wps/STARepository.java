/*
 * Copyright (C) 2011 52° North Initiative for Geospatial Open Source Software 
 *                   GmbH, Contact: Andreas Wytzisk, Martin-Luther-King-Weg 24, 
 *                   48155 Muenster, Germany                  info@52north.org
 *
 * Author: Christian Autermann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,51 Franklin
 * Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.uncertweb.sta.wps;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncertweb.intamap.utils.Namespace;
import org.uncertweb.sta.utils.Constants;
import org.uncertweb.sta.wps.api.annotation.Ignore;
import org.uncertweb.sta.wps.api.annotation.IsOnlyCompatibleWith;
import org.uncertweb.sta.wps.api.annotation.SpatialAggregationFunction;
import org.uncertweb.sta.wps.api.annotation.SpatialOnly;
import org.uncertweb.sta.wps.api.annotation.SpatialPartitioningPredicate;
import org.uncertweb.sta.wps.api.annotation.TemporalAggregationFunction;
import org.uncertweb.sta.wps.api.annotation.TemporalOnly;
import org.uncertweb.sta.wps.api.annotation.TemporalPartitioningPredicate;
import org.uncertweb.sta.wps.method.MethodFactory;
import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;
import org.uncertweb.sta.wps.method.grouping.GroupingMethod;
import org.uncertweb.sta.wps.method.grouping.SpatialGrouping;
import org.uncertweb.sta.wps.method.grouping.TemporalGrouping;

/**
 * {@link IAlgorithmRepository} for all instances of
 * {@link GenericObservationAggregationProcess}. All {@link GroupingMethod}s
 * will be loaded at startup and all combinations will be registered as separate
 * processes.
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class STARepository implements IAlgorithmRepository {

	/**
	 * Utility class to encapsulate {@link GroupingMethod}s.
	 */
	protected static class MethodCombination {

		private Class<? extends SpatialGrouping> sg;
		private Class<? extends TemporalGrouping> tg;
		private Class<? extends AggregationMethod> sam;
		private Class<? extends AggregationMethod> tam;

		public MethodCombination(
				Class<? extends SpatialGrouping> sg,
				Class<? extends TemporalGrouping> tg,
				Class<? extends AggregationMethod> sam,
				Class<? extends AggregationMethod> tam) {
			this.tg = tg;
			this.sg = sg;
			this.sam = sam;
			this.tam = tam;
		}
	}

	/**
	 * The Logger.
	 */
	protected static final Logger log = LoggerFactory
			.getLogger(STARepository.class);

	/**
	 * The format of the process id.
	 */
	private static final MessageFormat PROCESS_NAME = new MessageFormat(
			Constants.Process.PROCESS_PREFIX + "{0}:{1}:{2}:{3}");

	/**
	 * Mapping between process identifier and methods.
	 */
	private static final Map<String, MethodCombination> ALGORITHMS = loadProcesses();

	/**
	 * Loads all {@code GenericObservationAggregationProcess}es.
	 */
	protected static HashMap<String, MethodCombination> loadProcesses() {
		HashMap<String, MethodCombination> algos = new HashMap<String, MethodCombination>();
		MethodFactory mf = MethodFactory.getInstance();
		
		for (Class<? extends SpatialGrouping> sg : mf.getSpatialGroupingMethods()) {
			if (sg.getAnnotation(Ignore.class) != null) { continue; }
			SpatialPartitioningPredicate spp = sg.getAnnotation(SpatialPartitioningPredicate.class);
			String sppName = (spp == null) ? sg.getSimpleName() : spp.value();
			IsOnlyCompatibleWith sgIocw = sg.getAnnotation(IsOnlyCompatibleWith.class);
			
			for (Class<? extends AggregationMethod> sm : mf.getAggregationMethods()) {
				if (sm.getAnnotation(Ignore.class) != null) { continue; }
				if (sm.getAnnotation(TemporalOnly.class) != null) { continue; }
				if (!isCompatible(sgIocw, sm)) { continue; }
				SpatialAggregationFunction sam = sm.getAnnotation(SpatialAggregationFunction.class);
				String samName = (sam == null) ? sm.getSimpleName() : sam.value();
				
				for (Class<? extends TemporalGrouping> tg : mf.getTemporalGroupingMethods()) {
					if (tg.getAnnotation(Ignore.class) != null) { continue; }
					TemporalPartitioningPredicate tpp = tg.getAnnotation(TemporalPartitioningPredicate.class);
					String tppName = (tpp == null) ? tg.getSimpleName() : tpp.value();
					IsOnlyCompatibleWith tgIocw = tg.getAnnotation(IsOnlyCompatibleWith.class);
				
					for (Class<? extends AggregationMethod> tm : mf.getAggregationMethods()) {
						if (tm.getAnnotation(Ignore.class) != null) { continue; }
						if (tm.getAnnotation(SpatialOnly.class) != null) { continue; }
						if (!isCompatible(tgIocw, tm)) { continue; }

						TemporalAggregationFunction tam = tm.getAnnotation(TemporalAggregationFunction.class);
						String tamName = (tam == null) ? tm.getSimpleName() : tam.value();

						String id = PROCESS_NAME.format(new Object[] {
								sppName, samName, tppName, tamName
						});
						
						algos.put(id, new MethodCombination(sg, tg, sm, tm));
						log.info("Registered Algorithm: {}", id);
					}
				}
			}
		}
		return algos;
	}
	
	private static boolean isCompatible(IsOnlyCompatibleWith iocw, Class<?> a) {
		if (iocw == null) { 
			return true;
		}
		for (Class<? extends AggregationMethod> c : iocw.value()) {
			if (c.equals(a)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getAlgorithmNames() {
		return ALGORITHMS.keySet();
	}

	/**
	 * Instantiates the {@link IAlgorithm} with the given {@code id} and tests
	 * if the process description is valid.
	 * 
	 * @param id the process id
	 * @return the {@code IAlgorithm} with that {@code Id}. {@code null} if the
	 *         process description is not valid or the {@code IAlgorithm} does
	 *         not exist
	 */
	private IAlgorithm instantiate(String id) {
		MethodCombination methods = STARepository.ALGORITHMS.get(id);
		if (methods == null) {
			String msg = "The requested Algorithm is not available: " + id;
			log.error(msg);
			throw new RuntimeException(msg);
		}
		IAlgorithm a = new GenericObservationAggregationProcess(id,
				id, methods.sg, methods.tg, methods.sam, methods.tam);
		if (!a.processDescriptionIsValid()) {
			String msg = "ProcessDescription is not valid for " + id;
			log.error(msg + ":\n"
					+ a.getDescription().xmlText(Namespace.defaultOptions()));
			throw new RuntimeException(msg);
		}
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<IAlgorithm> getAlgorithms() {
		LinkedList<IAlgorithm> result = new LinkedList<IAlgorithm>();
		for (String id : STARepository.ALGORITHMS.keySet()) {
			IAlgorithm a = instantiate(id);
			if (a != null) {
				result.add(a);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAlgorithm(String processID) {
		return STARepository.ALGORITHMS.containsKey(processID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAlgorithm getAlgorithm(String processID,
			ExecuteRequest executeRequest) {
		return instantiate(processID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProcessDescriptionType getProcessDescription(String id) {
		return instantiate(id).getDescription();
	}

}
