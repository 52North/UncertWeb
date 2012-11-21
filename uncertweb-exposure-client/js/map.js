function Map(options) {
	this.sliderId = 0;
	this.$div = $("#" + options.div);
	this.map = L.map(options.div, {
		attributionControl: false,
		zoomControl: false,
		doubleClickZoom: false,
		closePopupOnClick: false,
		layers: [ new L.TileLayer.MapQuestOpen.OSM() ]
	}).setView(options.pos, 13);
	this.map.addControl(new L.Control.ZoomFS());
	this.markers = [];
	this.map.on("dblclick", function(e) {
		e.originalEvent.preventDefault();
		// mouse event seems to be shifted some pixels...
		this.addMarker(this.map.layerPointToLatLng(e.layerPoint.add([-20, -22])));
	}, this);
	this.map.on("popupopen", function(e) {
		var m = e.popup._source;
		e.popup.setContent(this.getPopupContent(m));
		this.createSlider(m);
	}, this)
}

Map.prototype.getPopupContent = function(marker) {
	marker.sliderId = marker.sliderId || "slider" + this.sliderId++;
	if (marker && marker.timeValue) {
		min = marker.timeValue[0];
		max = marker.timeValue[1];
	} else {
		min = 0;
		if (this.markers.length > 0) {
			min = this.markers.last().timeValue[1];
		}
		if (this.markers.length > 0) {
			min = this.markers.last().timeValue[1];
		}
		var max = (min > 1320) ? 1439 : min + 60;
		if (min == max && min == 1439) min -= 60;
	}
	var $s = $("<div>").addClass("bubble-content")
		.append($("<div>")
			.append($("<input>").attr({
				"id": marker.sliderId, 
				"type": "slider",
				"value": min + ";" + max
			}))
			.append($("<div>").addClass("controls")
				.append($("<div>").addClass("btn-group")
					.append($("<button>")
						.attr("type", "button")
						.addClass("btn btn-small")
						.append($("<i>").addClass("icon-ok")))
					.append($("<button>")
						.attr("type", "button")
						.addClass("btn btn-small")
						.append($("<i>").addClass("icon-remove"))))));
	return $s.get(0).outerHTML;
};

Map.prototype.addMarker = function(coords, min, max) {
	var m = L.marker(coords, {
		icon: new this.icon(),
		draggable: true
	})
	.on("drag", this.onMarkerDrag, this);
	if (min !== undefined && max != undefined) {
		m.timeValue = [min, max];
	}
	m.bindPopup(this.getPopupContent(m), {
		closeButton: false
	}).addTo(this.map);
	m.openPopup();
	this.createSlider(m);
	this.markers.push(m);
	this.onMarkerDrag();
}

Map.prototype.createSlider = function(marker) {
	var scale = [];
	for (var i = 0; i <= 24; i +=3 ) {
		if ((i % 6) != 0) {
			scale.push("|");
		} else {
			scale.push(i + ":00");	
		}
		
	}
	$("#" + marker.sliderId).slider({
		from: 0, to: 1439, step: 1, skin: "round_plastic",
		dimension: '', scale: scale, limits: false,
		calculate: function( value ){
			var hours = Math.floor(value/60);
			var mins = value - hours * 60;
			return (hours < 10 ? "0" + hours : hours) + ":" 
					+ ( mins < 10 ? "0" + mins : mins );
		},
		onstatechange: function(value){
			var val = value.split(/;/)
			if (val.length == 1) {
				val.push(val[0]);
			}
			marker.timeValue = [ 
				parseInt(val[0]), 
				parseInt(val[1])
			];
		}
	});
};

Map.prototype.onMarkerDrag = function() {
	if (!this.line) {
		this.line = L.polyline([], {
			color: '#49AFCD'
		}).addTo(this.map);	
	}
	var latlng = [];
	for (var i = 0; i <this.markers.length; ++i) {
		latlng.push(this.markers[i].getLatLng());
	}
	this.line.setLatLngs(latlng);
}

Map.prototype.getTrack = function() {
	var track = [];
	for (var i = 0; i < this.markers.length; ++i) {
		var val = this.markers[i].timeValue;
		var latlng = this.markers[i].getLatLng();
		track.push({
			location: [ latlng.lat, latlng.lng ],
			begin: val[0],
			end: val[1]
		});
	}
	return track;
};

Map.prototype.loadTrack = function(track) {
	for (var i = 0; i < track.length; ++i) {
		this.addMarker(track[i].location, track[i].begin, track[i].end);
	}
};

Map.prototype.zoomToTrack = function() {
	if (!this.line) {
		this.map.fitWorld();
	} else {
		this.map.fitBounds(this.line.getBounds());	
	}
	
};

Map.prototype.icon = L.Icon.extend({
	options: {
		iconUrl: "img/marker.png",
		shadowUrl: "img/marker_shadow.png",
		iconSize: [32, 37],
		iconAnchor: [16, 37],
		shadowSize: [51, 37],
		shadowAnchor: [21, 37],
		popupAnchor: [0, -26]
	}
});
