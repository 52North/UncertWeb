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
package org.uncertweb.sta.wps.api;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.opengis.ows.x11.AllowedValuesDocument.AllowedValues;
import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.wps.x100.ComplexDataCombinationType;
import net.opengis.wps.x100.ComplexDataCombinationsType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.LiteralInputType;
import net.opengis.wps.x100.LiteralOutputType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType.DataInputs;
import net.opengis.wps.x100.ProcessDescriptionType.ProcessOutputs;
import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x100.ProcessDescriptionsDocument.ProcessDescriptions;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.SupportedComplexDataType;

import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;

/**
 * Extended version of {@link AbstractSelfDescribingAlgorithm}. This class
 * supports object-encoded inputs and outputs:
 * <nl>
 * <li>default values</li>
 * <li>minimal/maximal occurrences</li>
 * <li>descriptions</li>
 * <li>titles</li>
 * <li>Id's</li>
 * <li>multivariate inputs</li>
 * </nl>
 * 
 * @see AbstractProcessInput
 * @see ProcessOutput
 * 
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public abstract class ExtendedSelfDescribingAlgorithm implements ISubject,
		IAlgorithm {
	
	/**
	 * List of observers.
	 * 
	 * @see ISubject
	 */
	private List<IObserver> observers = new ArrayList<IObserver>();

	/**
	 * The current state of this algorithm.
	 * 
	 * @see ISubject
	 */
	private Object state = null;

	/**
	 * The process description of this process.
	 */
	private ProcessDescriptionType pdt = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends IData> getInputDataType(String identifier) {
		for (AbstractProcessInput<?> inputs : getInputs()) {
			for (SingleProcessInput<?> input : inputs.getProcessInputs()) {
				if (input.getId().equals(identifier)) {
					return input.getBindingClass();
				}
			}
		}
		throw new RuntimeException("Invalid input identifier: " + identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends IData> getOutputDataType(String identifier) {
		for (ProcessOutput output : getOutputs()) {
			if (output.getId().equals(identifier)) {
				return output.getBindingClass();
			}
		}
		throw new RuntimeException("Invalid output identifier: " + identifier);
	}

	/**
	 * @return the abstract of this process (<code>null</code>; should be
	 *         overwritten)
	 */
	protected String getAbstract() {
		return null;
	}

	/**
	 * @return the title of this process (the class name; should be overwritten)
	 */
	protected String getTitle() {
		return this.getIdentifier();
	}

	/**
	 * @return the identifier of this process (the class name; should be
	 *         overwritten)
	 */
	protected String getIdentifier() {
		return this.getClass().getName();
	}

	/**
	 * @return the inputs for this process
	 */
	protected abstract Set<AbstractProcessInput<?>> getInputs();

	/**
	 * @return the outputs for this process
	 */
	protected abstract Set<ProcessOutput> getOutputs();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getState() {
		return state;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Object state) {
		for (IObserver o : observers) {
			o.update(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObserver(IObserver o) {
		observers.add(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeObserver(IObserver o) {
		observers.remove(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getErrors() {
		return new ArrayList<String>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWellKnownName() {
		return this.getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean processDescriptionIsValid() {
		return getDescription().validate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProcessDescriptionType getDescription() {
		//@formatter off
		if (pdt == null) {
			ProcessDescriptionsDocument document = ProcessDescriptionsDocument.Factory.newInstance();
			ProcessDescriptions processDescriptions = document.addNewProcessDescriptions();
			ProcessDescriptionType processDescription = processDescriptions.addNewProcessDescription();
			processDescription.setStatusSupported(false);
			processDescription.setStoreSupported(false);
			processDescription.setProcessVersion("1.0.0");
	
			// 1. Identifer
			processDescription.addNewIdentifier().setStringValue(this.getIdentifier());
			processDescription.addNewTitle().setStringValue(this.getTitle());
			if (getAbstract() != null) {
				processDescription.addNewAbstract().setStringValue(this.getAbstract());
			}
	
			// 2. Inputs
			Set<AbstractProcessInput<?>> allInputs = this.getInputs();
			DataInputs dataInputs = null;
			if (!allInputs.isEmpty()) {
				dataInputs = processDescription.addNewDataInputs();
			}
			for (AbstractProcessInput<?> inputs : allInputs) {
				for (SingleProcessInput<?> input : inputs.getProcessInputs()) {
					InputDescriptionType dataInput = dataInputs.addNewInput();
					dataInput.setMinOccurs(input.getMinOccurs());
					dataInput.setMaxOccurs(input.getMaxOccurs());
					dataInput.addNewIdentifier().setStringValue(input.getId());
					dataInput.addNewTitle().setStringValue(input.getTitle());
					if (input.getDescription() != null) {
						dataInput.addNewAbstract().setStringValue(input.getDescription());
					}
					Class<? extends IData> inputDataTypeClass = input.getBindingClass();
					Class<?>[] interfaces = inputDataTypeClass.getInterfaces();
					for (Class<?> implementedInterface : interfaces) {
						if (implementedInterface.equals(ILiteralData.class)) {
							LiteralInputType literalData = dataInput.addNewLiteralData();
							String inputClassType = "";
							Constructor<?>[] constructors = inputDataTypeClass.getConstructors();
							for (Constructor<?> constructor : constructors) {
								Class<?>[] parameters = constructor.getParameterTypes();
								if (parameters.length == 1) {
									inputClassType = parameters[0].getSimpleName();
								}
							}
							if (inputClassType.length() > 0) {
								DomainMetadataType datatype = literalData.addNewDataType();
								datatype.setReference("xs:" + inputClassType.toLowerCase());
								AllowedValues vals = input.getAllowedValues();
								if (vals == null) {
									if (inputDataTypeClass.equals(LiteralBooleanBinding.class)) {
										AllowedValues av = literalData.addNewAllowedValues();
										av.addNewValue().setStringValue(String.valueOf(true));
										av.addNewValue().setStringValue(String.valueOf(false));
									} else {
										literalData.addNewAnyValue();
									}
								} else {
									literalData.setAllowedValues(vals);
								}
								if (input.getDefaultValue() != null) {
									literalData.setDefaultValue(String.valueOf(input.getDefaultValue()));
								}
							}
						} else if (implementedInterface.equals(IComplexData.class)) {
							SupportedComplexDataInputType complexData = dataInput.addNewComplexData();
							ComplexDataCombinationType defaultInputFormat = complexData.addNewDefault();
							ComplexDataCombinationsType supportedtInputFormat = complexData.addNewSupported();
							List<IParser> parsers = ParserFactory.getInstance().getAllParsers();
							List<IParser> foundParsers = new ArrayList<IParser>();
							for (IParser parser : parsers) {
								Class<?>[] supportedClasses = parser.getSupportedInternalOutputDataType();
								for (Class<?> clazz : supportedClasses) {
									if (clazz.equals(inputDataTypeClass)) {
										foundParsers.add(parser);
									}
								}
							}
							for (int i = 0; i < foundParsers.size(); i++) {
								IParser parser = foundParsers.get(i);
								String[] supportedFormats = parser.getSupportedFormats();
								String[] supportedSchemas = parser.getSupportedSchemas();
								if (supportedSchemas == null) {
									supportedSchemas = new String[0];
								}
								String[] supportedEncodings = parser.getSupportedEncodings();
								for (int j = 0; j < supportedFormats.length; j++) {
									for (int k = 0; k < supportedEncodings.length; k++) {
										if (j == 0 && k == 0 && i == 0) {
											String supportedFormat = supportedFormats[j];
											ComplexDataDescriptionType defaultFormat = defaultInputFormat.addNewFormat();
											defaultFormat.setMimeType(supportedFormat);
											defaultFormat.setEncoding(supportedEncodings[k]);
											for (int t = 0; t < supportedSchemas.length; t++) {
												if (t == 0) {
													defaultFormat.setSchema(supportedSchemas[t]);
												} else {
													ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtInputFormat.addNewFormat();
													supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
													supportedCreatedFormatAdditional.setMimeType(supportedFormat);
													supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
												}
											}
										} else {
											String supportedFormat = supportedFormats[j];
											ComplexDataDescriptionType supportedCreatedFormat = supportedtInputFormat.addNewFormat();
											supportedCreatedFormat.setMimeType(supportedFormat);
											supportedCreatedFormat.setEncoding(supportedEncodings[k]);
											for (int t = 0; t < supportedSchemas.length; t++) {
												if (t == 0) {
													supportedCreatedFormat.setSchema(supportedSchemas[t]);
												}
												if (t > 0) {
													ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtInputFormat.addNewFormat();
													supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
													supportedCreatedFormatAdditional.setMimeType(supportedFormat);
													supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
												}
											}
										}
										if (supportedFormats.length == 1 && supportedEncodings.length == 1) {
											String supportedFormat = supportedFormats[j];
											ComplexDataDescriptionType supportedCreatedFormat = supportedtInputFormat.addNewFormat();
											supportedCreatedFormat.setMimeType(supportedFormat);
											supportedCreatedFormat.setEncoding(supportedEncodings[k]);
											for (int t = 0; t < supportedSchemas.length; t++) {
												if (t == 0) {
													supportedCreatedFormat.setSchema(supportedSchemas[t]);
												}
												if (t > 0) {
													ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtInputFormat.addNewFormat();
													supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
													supportedCreatedFormatAdditional.setMimeType(supportedFormat);
													supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
	
			// 3. Outputs
			Set<ProcessOutput> outputs = this.getOutputs();
			ProcessOutputs dataOutputs = null;
			if (!outputs.isEmpty()) {
				dataOutputs = processDescription.addNewProcessOutputs();
			}
			for (ProcessOutput output : outputs) {
				OutputDescriptionType dataOutput = dataOutputs.addNewOutput();
	
				dataOutput.addNewIdentifier().setStringValue(output.getId());
				dataOutput.addNewTitle().setStringValue(output.getTitle());
				if (output.getDescription() != null) {
					dataOutput.addNewAbstract().setStringValue(output.getDescription());
				}
				Class<? extends IData> outputDataTypeClass = output.getBindingClass();
				Class<?>[] interfaces = outputDataTypeClass.getInterfaces();
				for (Class<?> implementedInterface : interfaces) {
					if (implementedInterface.equals(ILiteralData.class)) {
						LiteralOutputType literalData = dataOutput.addNewLiteralOutput();
						String outputClassType = "";
						Constructor<?>[] constructors = outputDataTypeClass.getConstructors();
						for (Constructor<?> constructor : constructors) {
							Class<?>[] parameters = constructor.getParameterTypes();
							if (parameters.length == 1) {
								outputClassType = parameters[0].getSimpleName();
							}
						}
						if (outputClassType.length() > 0) {
							literalData.addNewDataType().setReference("xs:" + outputClassType.toLowerCase());
						}
					} else if (implementedInterface.equals(IComplexData.class)) {
						SupportedComplexDataType complexData = dataOutput.addNewComplexOutput();
						ComplexDataCombinationType defaultInputFormat = complexData.addNewDefault();
						ComplexDataCombinationsType supportedtOutputFormat = complexData.addNewSupported();
						List<IGenerator> generators = GeneratorFactory.getInstance().getAllGenerators();
						List<IGenerator> foundGenerators = new ArrayList<IGenerator>();
						for (IGenerator generator : generators) {
							Class<?>[] supportedClasses = generator.getSupportedInternalInputDataType();
							for (Class<?> clazz : supportedClasses) {
								if (clazz.equals(outputDataTypeClass)) {
									foundGenerators.add(generator);
								}
							}
						}
						for (int i = 0; i < foundGenerators.size(); i++) {
							IGenerator generator = foundGenerators.get(i);
							String[] supportedFormats = generator.getSupportedFormats();
							String[] supportedSchemas = generator.getSupportedSchemas();
							if (supportedSchemas == null) {
								supportedSchemas = new String[0];
							}
							String[] supportedEncodings = generator.getSupportedEncodings();
							for (int j = 0; j < supportedFormats.length; j++) {
								for (int k = 0; k < supportedEncodings.length; k++) {
									if (j == 0 && k == 0 && i == 0) {
										String supportedFormat = supportedFormats[j];
										ComplexDataDescriptionType defaultFormat = defaultInputFormat
												.addNewFormat();
										defaultFormat.setMimeType(supportedFormat);
										defaultFormat.setEncoding(supportedEncodings[k]);
										for (int t = 0; t < supportedSchemas.length; t++) {
											if (t == 0) {
												defaultFormat.setSchema(supportedSchemas[t]);
											} else {
												ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtOutputFormat.addNewFormat();
												supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
												supportedCreatedFormatAdditional.setMimeType(supportedFormat);
												supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
											}
										}
									} else {
										String supportedFormat = supportedFormats[j];
										ComplexDataDescriptionType supportedCreatedFormat = supportedtOutputFormat.addNewFormat();
										supportedCreatedFormat.setMimeType(supportedFormat);
										supportedCreatedFormat.setEncoding(supportedEncodings[k]);
										for (int t = 0; t < supportedSchemas.length; t++) {
											if (t == 0) {
												supportedCreatedFormat.setSchema(supportedSchemas[t]);
											}
											if (t > 0) {
												ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtOutputFormat.addNewFormat();
												supportedCreatedFormatAdditional.setMimeType(supportedFormat);
												supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
												supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
											}
										}
									}
									if (supportedFormats.length == 1 && supportedEncodings.length == 1) {
										String supportedFormat = supportedFormats[j];
										ComplexDataDescriptionType supportedCreatedFormat = supportedtOutputFormat.addNewFormat();
										supportedCreatedFormat.setMimeType(supportedFormat);
										supportedCreatedFormat.setEncoding(supportedEncodings[k]);
										for (int t = 0; t < supportedSchemas.length; t++) {
											if (t == 0) {
												supportedCreatedFormat.setSchema(supportedSchemas[t]);
											}
											if (t > 0) {
												ComplexDataDescriptionType supportedCreatedFormatAdditional = supportedtOutputFormat.addNewFormat();
												supportedCreatedFormatAdditional.setEncoding(supportedEncodings[k]);
												supportedCreatedFormatAdditional.setMimeType(supportedFormat);
												supportedCreatedFormatAdditional.setSchema(supportedSchemas[t]);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			pdt = document.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		return pdt;
	}
}
