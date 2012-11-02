function createExecute(options) {
	var doc = jsxml.fromString(
		'<?xml version="1.0" encoding="UTF-8"?>' 
		+ '<wps:Execute service="WPS" version="1.0.0" '
			+ 'xmlns:wps="http://www.opengis.net/wps/1.0.0" '
			+ 'xmlns:ows="http://www.opengis.net/ows/1.1" '
			+ 'xmlns:xlink="http://www.w3.org/1999/xlink" />');
	var identifier = doc.createElement("ows:Identifier");
	identifier.appendChild(doc.createTextNode(options.id));
	var inputs = doc.createElement("wps:DataInputs");
	function createInput(id, value) {
		var input = doc.createElement("wps:Input");
		var inputId = doc.createElement("ows:Identifier");
		inputId.appendChild(doc.createTextNode(id));
		var data = doc.createElement("wps:Data");
		var literalData = doc.createElement("wps:LiteralData");
		literalData.appendChild(doc.createTextNode(value));
		data.appendChild(literalData);
		input.appendChild(inputId);
		input.appendChild(data);
		inputs.appendChild(input);
	}
	for (var key in options.inputs) {
		if (options.inputs[key] instanceof Array)
			for (var i = options.inputs[key].length - 1; i >= 0; i--)
				createInput(key, options.inputs[key][i]);
		else createInput(key, options.inputs[key]);
	}
	var respForm = doc.createElement("wps:ResponseForm");
	var outputs = doc.createElement("wps:ResponseDocument")
	respForm.appendChild(outputs);
	for (var key in options.outputs) {
		var output = doc.createElement("wps:Output");
		for (var attr in options.outputs[key]) {
			output.setAttribute(attr, options.outputs[key][attr]);
		}
		var outputId = doc.createElement("ows:Identifier");
		outputId.appendChild(doc.createTextNode(key));
		output.appendChild(outputId);
		outputs.appendChild(output);
	}
	respForm.appendChild(outputs);
	doc.documentElement.appendChild(identifier);
	doc.documentElement.appendChild(inputs);
	doc.documentElement.appendChild(respForm);    
	return doc;
};

function isException(e) {
	return false;
}
