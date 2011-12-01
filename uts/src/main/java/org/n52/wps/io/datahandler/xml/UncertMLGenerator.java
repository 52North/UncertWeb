package org.n52.wps.io.datahandler.xml;

import static org.n52.wps.io.data.UncertWebDataConstants.ENCODING_UTF_8;
import static org.n52.wps.io.data.UncertWebDataConstants.MIME_TYPE_UNCERTML_XML;
import static org.n52.wps.io.data.UncertWebDataConstants.SCHEMA_UNCERTML;
import static org.uncertweb.utils.UwCollectionUtils.set;

import java.io.OutputStream;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.UncertWebData;
import org.n52.wps.io.data.binding.complex.UncertWebDataBinding;
import org.n52.wps.io.datahandler.AbstractUwGenerator;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.XMLEncoder;
import org.uncertweb.utils.UwCollectionUtils;

public class UncertMLGenerator extends AbstractUwGenerator {

	public UncertMLGenerator() {
		super(set(SCHEMA_UNCERTML), set(ENCODING_UTF_8),
				set(MIME_TYPE_UNCERTML_XML), UwCollectionUtils
						.<Class<?>> set(UncertWebDataBinding.class));
	}

	@Override
	protected void writeToStream(IData data, OutputStream out) {
		XMLEncoder encoder = new XMLEncoder();
		if (data instanceof UncertWebDataBinding) {

			UncertWebData theData = ((UncertWebDataBinding) data).payload;

			if (theData.getUncertaintyType() != null) {

				try {
					encoder.encode(theData.getUncertaintyType(), out);

				} catch (UnsupportedUncertaintyTypeException e) {
					e.printStackTrace();
				} catch (UncertaintyEncoderException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
