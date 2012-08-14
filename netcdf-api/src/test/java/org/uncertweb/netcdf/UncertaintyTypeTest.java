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
package org.uncertweb.netcdf;

import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.INTERFACE;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PROTECTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Set;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.uncertml.IUncertainty;
import org.uncertml.UncertML;
import org.uncertml.distribution.IDistribution;
import org.uncertml.sample.ISample;
import org.uncertml.statistic.IStatistic;
import org.uncertweb.utils.UwCollectionUtils;
import org.uncertweb.utils.UwCollectionUtils.Filter;

public class UncertaintyTypeTest {

	private static final int NOT_INSTANTIABLE = INTERFACE | ABSTRACT | PRIVATE | PROTECTED;

	protected static class InstantiableFilter implements Filter<Class<?>> {
		public boolean test(Class<?> t) {
			return (t.getModifiers() & NOT_INSTANTIABLE) == 0;
		}
	}
	
	protected static class NotParameterFilter implements Filter<Class<?>> {
		public boolean test(Class<?> t) {
			return !IDistribution.IParameter.class.isAssignableFrom(t);
		}
	}

	private static final Reflections r = new Reflections("org.uncertml",	new SubTypesScanner());

	@SuppressWarnings("unchecked")
	public static <T> Set<Class<? extends T>> getSubclasses(Class<T> t) {
		return UwCollectionUtils.filter(r.getSubTypesOf(t), 
				new InstantiableFilter(), new NotParameterFilter());
	}

	@Test
	public void testCompleteness() {
		final Set<Class<? extends IUncertainty>> classes = getSubclasses(IUncertainty.class);
		for (final Class<? extends IUncertainty> uc : classes) {
			final String uri = UncertML.getURI(uc);
			assertNotNull(uc + "could not be used to generate a URI", uri);
			final NcUwUncertaintyType type = NcUwUncertaintyType.fromUri(URI.create(uri));
			final NcUwUncertaintyType fromClass = NcUwUncertaintyType.fromClass(uc);
			assertNotNull(uri + " not recognized (" + uc + ").", type);
			assertNotNull(uc + " not recognized (" + uri + ").", fromClass);
			assertEquals(type, fromClass);
			
			if (IStatistic.class.isAssignableFrom(uc)) {
				assertTrue(type + " should be a statistic", type.isStatistic());
			}
			if (IDistribution.class.isAssignableFrom(uc)) {
				assertTrue(type + " should be a distribution", type.isDistribtution());
			}
			if (ISample.class.isAssignableFrom(uc)) {
				assertTrue(type + " should be a sample", type.isSample());
			}
			if (type.isStatistic()) {
				assertTrue(type + " should not be a statistic", IStatistic.class.isAssignableFrom(type.getImplementationClass()));
			}
			if (type.isDistribtution()) {
				assertTrue(type + " should not be a distribution", IDistribution.class.isAssignableFrom(type.getImplementationClass()));
			}
			if (type.isSample()) {
				assertTrue(type + " should not be a sample", ISample.class.isAssignableFrom(type.getImplementationClass()));
			}
			
		}
		for (final NcUwUncertaintyType type : NcUwUncertaintyType.values()) {
			System.out.println(type.getImplementationClass().getCanonicalName() +" " + type.getUri());
			final Class<? extends IUncertainty> c = type.getImplementationClass();
			if (!classes.contains(c) && !c.isInterface()) {
				fail(type + "(" + c.getName() + ") does not correspond to a proper class");
			}
		}
	}
}
