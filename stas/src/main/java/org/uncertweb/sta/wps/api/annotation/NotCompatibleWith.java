package org.uncertweb.sta.wps.api.annotation;

import org.uncertweb.sta.wps.method.grouping.GroupingMethod;

public @interface NotCompatibleWith {
	public Class<? extends GroupingMethod<?>>[] value();
}
