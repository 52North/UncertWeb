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
import org.uncertweb.sta.wps.method.MethodFactory;
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
	protected static class MethodPair {

		private Class<? extends SpatialGrouping> sg;
		private Class<? extends TemporalGrouping> tg;

		public MethodPair(Class<? extends SpatialGrouping> sg,
				Class<? extends TemporalGrouping> tg) {
			this.tg = tg;
			this.sg = sg;
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
			"{0}:{1}");

	/**
	 * Mapping between process identifier and methods.
	 */
	private static final Map<String, MethodPair> ALGORITHMS = loadProcesses();

	/**
	 * Loads all {@code GenericObservationAggregationProcess}es.
	 */
	protected static HashMap<String, MethodPair> loadProcesses() {
		HashMap<String, MethodPair> algos = new HashMap<String, MethodPair>();
		for (Class<? extends SpatialGrouping> sg : MethodFactory.getInstance()
				.getSpatialGroupingMethods()) {
			for (Class<? extends TemporalGrouping> tg : MethodFactory
					.getInstance().getTemporalGroupingMethods()) {
				String id = PROCESS_NAME.format(new Object[] {
						sg.getSimpleName(), tg.getSimpleName() });
				algos.put(id, new MethodPair(sg, tg));
				log.info("Registered Algorithm: {}", id);
			}
		}
		return algos;
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
		MethodPair methods = STARepository.ALGORITHMS.get(id);
		if (methods == null) {
			String msg = "The requested Algorithm is not available: " + id;
			log.error(msg);
			throw new RuntimeException(msg);
		}
		IAlgorithm a = new GenericObservationAggregationProcess(id,
				id.replace(":", " "), methods.sg, methods.tg);
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
