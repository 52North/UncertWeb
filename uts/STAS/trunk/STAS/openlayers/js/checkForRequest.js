function checkForRequest() {
	var parameters = {};
	var str = document.location.search.substr(1, document.location.search.length);
	if (str != '') {
		params = str.split('&');
		for (var i = 0; i < params.length; i++) {
			v = '';
			kvPair = params[i].split('=');
			if (kvPair.length > 1) { 
				v = kvPair[1]; 
			}
			parameters[unescape(kvPair[0])] = unescape(v);
		}
	}
	if (parameters['url']) {
		OpenLayers.Request.GET({
			url: parameters['request'],
			success: function (r) { 
				ctrl.addLayer({
					url: parameters['url'], 
					request: r.responseText 
				}); 
			},
			failure: error
		});
	}
	if (parameters['oc']) {
		OpenLayers.Request.GET({
			url: parameters['oc'],
			success: function (r) { 
				ctrl.addLayer({ 
					oc: (r.responseXML)? r.responseXML : r.responseText 
				}); 
			},
			failure: error
		});
	}
	if (parameters['json']) {
		OpenLayers.Request.GET({
			url: parameters['json'],
			success: function(r) { 
				ctrl.addLayer({ 
					json: r.responseText 
				}); 
			},
			failure: error
		});
	}
}
