jQuery(document).ready(function($) {
	$.getJSON("wp-7-client-config.json", function(options) {

		function transformer(name) {
            return function(options) {
                var stddev = options.inputs[name+"-stddev"],
                    aid = options.inputs[name+"-aid"],
                    sector = options.inputs[name+"-sector"];
                delete options.inputs[name+"-stddev"],
                delete options.inputs[name+"-aid"],
                delete options.inputs[name+"-sector"];

                if (stddev && aid && sector 
                    && stddev.length === aid.length 
                    && stddev.length === sector.length
                    && stddev.length > 0) {
                    var l = stddev.length;
                    var a = [];
                    for (var i = 0; i < l; ++i) {
                        if (aid[i] !== undefined && aid[i] !== ""
                            && sector[i] !== undefined && sector[i] !== ""
                            && stddev[i] !== undefined && stddev[i] !== "")
                        a.push(XmlUtils.uncertainAlbatrossInput(
                            aid[i], sector[i], stddev[i]));
                    }
                    if (a.length > 0) {
                        options.inputs[name] = a;
                    }
                }
            }
        }

        options.complexInputTransformers = [
            transformer("uncert-area"),
            transformer("uncert-link")
        ]

		 var app = new App(options, {
			"albatross": function() {
				this.showDownloadLink("albatross", "export-file");	
			}
		});
	});
});