package org.uncertweb.sta.wps.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.uncertweb.sta.wps.method.aggregation.AggregationMethod;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IsOnlyCompatibleWith {
	public Class<? extends AggregationMethod>[] value();
}
