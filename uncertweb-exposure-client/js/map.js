function Map(options) {
	this.formid = 1;
	this.$div = $("#" + options.div);
	this.map = L.map(options.div, {
		attributionControl: false,
		doubleClickZoom: false,
		closePopupOnClick: false,
		layers: [ new L.TileLayer.MapQuestOpen.OSM() ]
	}).setView(options.pos, 13);
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

timeValue = {
	"weekday": 0,
	"begin": 16,
	"duration": 200
}

Map.prototype.getPopupContent = function(marker) {
	marker.formid = marker.formid || this.formid++;

	var begin = marker.time ? marker.time.begin : 0;
	var length = marker.time ? marker.time.length : 0;
	var weekday = marker.time ? marker.time.weekday : 0;

	var $bubble = $("<div>").addClass("bubble-content");
	var $form = $("<form>").attr("id", "trajectory" + marker.formid).appendTo($bubble);
	var $fieldset = $("<fieldset>").appendTo($form);
	$fieldset.append($("<legend>").html("Trajectory").hide());

	$("<label>").attr("for", "weekday")
		.html("<h5>Day of the Week</h5>").appendTo($fieldset);
	$select = $("<select>").attr("name", "weekday").addClass("span12")
		.append($("<option>").attr("value", 0).text("Monday"))
		.append($("<option>").attr("value", 1).text("Tuesday"))
		.append($("<option>").attr("value", 2).text("Wednesday"))
		.append($("<option>").attr("value", 3).text("Thursday"))
		.append($("<option>").attr("value", 4).text("Friday"))
		.append($("<option>").attr("value", 5).text("Saturday"))
		.append($("<option>").attr("value", 6).text("Sunday"))
		.appendTo($fieldset);

	console.group("Select Value")
	if (!marker.time) {
		$select.prepend($("<option>").attr("value", "").attr("selected", true).attr("disabled", true).css("display", "none"));
	} else {
		console.log($select.val());
		console.log(marker.time.weekday);
		$select.val(marker.time.weekday);
	}
	console.log($select.val());
	console.groupEnd();

	$("<label>").attr("for", "begin")
		.html("<h5>Begin Time</h5>").appendTo($fieldset);
	$("<input>").attr({
		"name": "begin",
		"type": "slider",
		"value": begin
	}).appendTo($fieldset);

	$("<label>").attr("for", "length")
		.html("<h5>Duration</h5>").appendTo($fieldset);
	$("<input>").attr({
		"name": "length",
		"type": "slider",
		"value": length
	}).appendTo($fieldset);

	$fieldset.append($("<div>").addClass("controls pull-right")
		.append($("<div>").addClass("btn-group")
			.append($("<button>")
				.attr("type", "button")
				.addClass("btn btn-small form-save")
				.append($("<i>").addClass("icon-ok")))
			.append($("<button>")
				.attr("type", "button")
				.addClass("btn btn-small form-abort")
				.append($("<i>").addClass("icon-remove")))));

	return $bubble.get(0).outerHTML;
};

Map.prototype.addMarker = function(coords, time) {
	var m = L.marker(coords, {
		icon: new this.icon(),
		draggable: true
	})
	.on("drag", this.onMarkerDrag, this);
	if (time !== undefined) {
		m.time = time;
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
	var lengthScale = [], beginScale = [];
	var self = this;
	var $form = $("form#trajectory" + marker.formid);
	var $weekday = $form.find(":input[name=weekday]")
	var $begin = $form.find("input[name=begin]");
	var $length = $form.find("input[name=length]");
	var $save = $form.find("button.form-save").attr("disabled", true);
	var $abort = $form.find("button.form-abort");
	
	for (var i = 0; i <= 24; i +=3 ) {
		beginScale.push((i % 6) != 0 ? "|" : i + ":00");
		lengthScale.push((i % 6) != 0 ? "|" : i + "h");
	}

	$begin.slider({
		from: 0, to: 1439, step: 1, skin: "round_plastic",
		dimension: '', scale: beginScale, limits: false,
		calculate: function( value ) {
			value = parseInt(value);
			var hours = Math.floor(value/60);
			var mins = value - hours * 60;
			return (hours < 10 ? "0" + hours : hours) + ":" 
					+ ( mins < 10 ? "0" + mins : mins );
		}
	});

	$length.slider({
		from: 0, to: 1439, step: 1, skin: "round_plastic",
		dimension: '', scale: lengthScale, limits: false,
		calculate: function(value) {
			value = parseInt(value);
			var hours = Math.floor(value/60);
			var mins = value - hours * 60;
			return (hours < 10 ? "0" + hours : hours) + "h " 
					+ (mins < 10 ? "0" + mins : mins) + "m";
		},
		callback: function(value) {
			value = parseInt(value);
			if (value > 0 && $weekday.val() !== "") {
				$save.removeAttr("disabled");
			} else {
				$save.attr("disabled", true);
			}
		}
	});

	$save.click(function() {
		marker.time = $form.serializeToArray();
		marker.closePopup();
	});

	$abort.click(function() {
		/* close; if !saved delete */
		if (!marker.time) {
			self.removeMarker(marker);
		}
		marker.closePopup();
	});

	$weekday.on("change", function() {
		if (parseInt($length.slider("value")) > 0 
				&& $weekday.val() !== "") {
			$save.removeAttr("disabled");
		} else {
			$save.attr("disabled", true);
		}
	}).trigger("change");
};

Map.prototype.removeMarker = function(marker) {
	this.map.removeLayer(marker);
	var i = this.markers.indexOf(marker);
	if (i >= 0) {
		this.markers.splice(i, 1);	
	}
	this.onMarkerDrag();
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
		var val = this.markers[i].time;
		var latlng = this.markers[i].getLatLng();
		track.push({
			location: [ latlng.lat, latlng.lng ],
			time: this.markers[i].time
		});
	}
	return track;
};

Map.prototype.loadTrack = function(track) {
	for (var i = 0; i < track.length; ++i) {
		this.addMarker(track[i].location, track[i].time);
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
