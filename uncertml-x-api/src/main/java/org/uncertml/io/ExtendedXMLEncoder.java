package org.uncertml.io;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyEncoderException;
import org.uncertml.exception.UnsupportedUncertaintyTypeException;
import org.uncertml.io.IUncertaintyEncoder;

public class ExtendedXMLEncoder implements IUncertaintyEncoder{

	@Override
	public String encode(IUncertainty element)
			throws UnsupportedUncertaintyTypeException,
			UncertaintyEncoderException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void encode(IUncertainty element, File file)
			throws UnsupportedUncertaintyTypeException,
			UncertaintyEncoderException {
		// TODO Auto-generated method stub

	}

	@Override
	public void encode(IUncertainty element, OutputStream stream)
			throws UnsupportedUncertaintyTypeException,
			UncertaintyEncoderException {
		// TODO Auto-generated method stub

	}

	@Override
	public void encode(IUncertainty element, Writer writer)
			throws UnsupportedUncertaintyTypeException,
			UncertaintyEncoderException {
		// TODO Auto-generated method stub

	}

}
