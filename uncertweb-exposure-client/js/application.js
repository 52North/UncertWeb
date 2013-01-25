function App(options, sendCallbacks) {
	this.logid = 1;
	this.options = options;
	for (var p in this.options.processes) {
		sendCallbacks[p] = $.isFunction(sendCallbacks[p]) ? sendCallbacks[p] : $.noop;
	}
	this.sendCallbacks = sendCallbacks; 
	this.generateInterface();
	this.addListeners();
}

$.extend(App.prototype, {
	scrollToTop: function() {
		$("body,html").animate({"scrollTop": 0}, "fast");
	},

	generateInterface: function() {
		var uib = new UIBuilder();
		for (var process in this.options.processes) {
			uib.generateOptions(this.options.processes[process].inputs, $("#" + process + " form"), false);
		}
		$(".processForm")
			.append($("<div>")
				.addClass("form-actions")
				.append($("<button>")
					.addClass("btn send btn-info")
					.attr("type","button")
					.text("Send")));
	},

	addListeners: function() {
		$("#start").click(function() {
			$(".sidebar-nav li.active").next()
				.next().removeClass("disabled")
				.find("a").trigger("click");
			$(this).parent().fadeOut(function() {
				$(this).remove();
			}) 
		});
		var self = this;
		$(".sidebar-nav a").click(function(e){
			self.onSidebarClick(e, this);
		});
		$(document).on("keyup input change", ".required", this.onRequiredChange);
		$("form").find(".required:first").trigger("change");
		$(".processForm button.send").click(function(e) {
			self.sendRequest(e, this, function() {
				self.sendCallbacks[$(e.target).parents(".processContainer").attr("id")].call(self);
			});
		})
	},

	showVisualizationLink: function(process, output) {
		var self = this, r = this.getResponse(process);
		if (!XmlUtils.isException(r)) {
			var pid = "<code>" + this.options.processes[process].id + "</code>";
			var text = "Do you want to see the output of the " + pid + " process?";
			this.buildModalDialog("Visualization", text, function(dialog) {
				var ref = XmlUtils.getOutput(r, output);
				if (typeof(ref) === "object") {
					window.open(
						self.options.visualizationUrl 
							+ "?url="  + encodeURIComponent(ref["xlink:href"])
							+ "&mime=" + encodeURIComponent(ref["mimeType"]));	
				}
				dialog.modal("hide");
			});
		}
	},

	showDownloadLink: function(process, output) {
		var self = this, r = this.getResponse(process);
		if (!XmlUtils.isException(r)) {
			var pid = "<code>" + this.options.processes[process].id + "</code>";
			var text = "Do you want to download the output of the " + pid + " process?";
			this.buildModalDialog("Download", text, function(dialog) {
				var ref = XmlUtils.getOutput(r, output);
				if (typeof(ref) === "object") {
					window.open(ref["xlink:href"]);	
				} else if (typeof(ref) === "string") {
					window.open(ref);	
				}
				dialog.modal("hide");
			});
		}
	},

	buildModalDialog: function(title, text, onclick) {
		var $dialog = $("<div>").addClass("modal hide");
		var $header = $("<div>").addClass("modal-header").appendTo($dialog);
		var $body   = $("<div>").addClass("modal-body")  .appendTo($dialog);
		var $footer = $("<div>").addClass("modal-footer").appendTo($dialog);

		$("<button>")
			.attr({
				"type": "button",
				"aria-hidden": true
			})
			.addClass("close")
			.data("dismiss", "modal")
			.html("&times;")
			.appendTo($header);

		$("<h3>").html(title).appendTo($header);
		$("<p>").html(text).appendTo($body);
		$("<button>")
			.attr("type", "button")
			.addClass("btn btn-info")
			.text("Yes")
			.appendTo($footer).on("click", function() {
				onclick($dialog);
			});

		$("<button>")
			.attr({
				"type": "button",
				"aria-hidden": true
			})
			.addClass("btn")
			.data("dismiss", "modal")
			.text("No")
			.appendTo($footer)
			.on("click", function() {
				$dialog.modal("hide");
			});

		$dialog.appendTo($("body")).modal({
			"keyboard": true,
			"show": true
		});
	},

	onSidebarClick: function(e, element) {
		var $this = $(element);
		e.preventDefault();
		if ($this.parents("li").hasClass("disabled")) return;
		if ($this.parents("li").hasClass("active")) return;
		var $active = $(".sidebar-nav li.active");
		$active.removeClass("active");
		$this.parents("li").addClass("active");
		$($active.find("a").attr("href")).fadeOut("fast", function() {
			$($this.attr("href")).fadeIn();
		});
	},

	onRequiredChange: function(event, element) {
		var valid = true;
		$(element).parents("form").find(".required").each(function(){ 
			var val = $(element).val();
			return valid = (val !== null && val !== undefined && val !== "");
		});
		var $button = $(element).parents("form").find("button.send");
		if (valid) {
			$button.removeAttr("disabled");
		} else {
			$button.attr("disabled", true);
		}
	},

	setSettings: function(id, val, options) {
		for (var section in options.sections) {
			for (var option in options.sections[section].options) {
				if (option === id) {
					switch (options.sections[section].options[option].type) {
					case "integer":
					case "string":
					case "password":
						$("input[name=" + option + "]").val(val);
						break;
					case "text":
						$("textarea[name=" + option + "]").val(val);
						break;
					case "choice":
						$("select[name=" + option + "]").val(val);
						break;
					case "boolean":
						$("select[name=" + option + "]").attr("checked", val === "true" || val === true);
						break;
					}
					return;
				}
			}
		}	
	},

	createRequest: function(id, form) {
		var settings = this.options.processes[id];
		var o = { 
			id: settings.id, 
			inputs: {},
			outputs: settings.outputs
		};
		for (var i = 0; i < form.length; ++i) {
			if (!o.inputs[form[i].name]) {
				o.inputs[form[i].name] = [];
			}
			o.inputs[form[i].name].push(form[i].value);
		}
		for (var i = 0; i < settings.inputs.sections.length; ++i) {
			for (var key in settings.inputs.sections[i].options) {
				if (settings.inputs.sections[i].options[key].type === "boolean" && !o.inputs[key]) {
					o.inputs[key] =  [ false ];
				}
				if (!settings.inputs.sections[i].options[key].required 
					&& o.inputs[key] 
					&& o.inputs[key].length === 1 
					&& (o.inputs[key][0] === "" || o.inputs[key][0] === undefined)) {
					delete o.inputs[key];
				}
			}
		}
		for (var i = 0; i < this.options.complexInputTransformers.length; ++i) {
			this.options.complexInputTransformers[i](o);
		}
		return o;
	},

	showXML: function(message, text, failed) {
		var id = this.logid++;
		var logrowid = "logrow"+ id;
		var coderowid = "coderow" + id;

		var $a = $("<a>")
			.addClass("pull-right")
			.attr("href","#")
			.text("show").click(function(e) {
				e.preventDefault();
				if ($(this).text() === "show") {
					$(this).text("hide");
					$("#"+coderowid).fadeIn("fast");	
				} else if ($(this).text() === "hide") {
					$(this).text("show");
					$("#"+coderowid).fadeOut("fast");
				}
			});
		$("#output > tbody")
			.append($("<tr>").attr("id", logrowid)
				.append($("<td>").text(new Date().toLocaleString()))
				.append($("<td>")
					.append(text)
					.append($a)))
			.append($("<tr>").attr("id", coderowid).hide()
				.append($("<td>").attr("colspan", 2)
					.append($("<pre>")
						.addClass("prettyprint")
						.text(XmlUtils.xml2string(message)))));
		prettyPrint();
		if (failed) {
			$("#"+logrowid).addClass("text-error");	
		}
		return id;
	},

	setResponse: function(id, response) {
		this.options.outputs[id] = response;
	},

	getResponse: function(id) {
		return this.options.outputs[id];
	},

	showRequest: function(req) {
		return this.showXML(req, "Request", false);
	},

	showResponse: function(res, fail) {
		return this.showXML(res, (fail) ? "Failure response" : "Sucess response", fail);
	},

	showFailureResponse: function(res) {
		return this.showResponse(res, true);
	},

	showSuccessResponse: function(res) {
		return this.showResponse(res, false);
	},

	showMessage: function(content, type, autoclose) {
		var $alert = $("<div>");
		function closeAlert() {
			$alert.fadeTo(500, 0).slideUp(500, function() {
				$alert.remove();
			});
		}
		$alert.addClass("alert alert-" + type)
			  .append($("<button>")
				  .attr("type", "button")
				  .addClass("close")
				  .html("&times;")
				  .click(closeAlert))
			  .append(content)
			  .hide()
			  .prependTo($("#content"))
			  .css("opacity", 0)
			  .slideDown(500)
			  .animate({ opacity: 1 }, { queue: false, duration: 1000 });
		if (autoclose) {
			window.setTimeout(closeAlert, 5000);
		}
	},

	showError: function(error) {
		this.showMessage($("<strong>Error! </strong> ").after(error), "error");
	},

	showSuccess: function(message) {
		this.showMessage($("<strong>Success! </strong> ").after(message), "success", true);
	},

	showWarning: function(message) {
		this.showMessage($("<strong>Warning! </strong> ").after(message), "warning", true);
	},

	sendRequest: function(e, element, callback) {
		var $form = $(element).parents("form");
		var process = $form.data("process");
		var settings = this.options.processes[process];
		var req = this.createRequest(process, $form.serializeArray());

		for (var p in this.options.mappings[process]) {
			if (typeof this.options.mappings[process][p] === "string") {
				// response is a single input
				if (!req.inputs[this.options.mappings[process][p]]) {
					 req.inputs[this.options.mappings[process][p]] = [];
				}
				var response = this.getResponse(p);
				if (response) {
					req.inputs[this.options.mappings[process][p]].push(response);
				}
			} else {
				for (var o in this.options.mappings[process][p]) {
					if (!req.inputs[this.options.mappings[process][p][o]] ) {
						req.inputs[this.options.mappings[process][p][o]] = [];
					}
					var response = this.getResponse(p);
					if (response) {
						req.inputs[this.options.mappings[process][p][o]].push(
							XmlUtils.getOutput(response, o));
					}
				}
			}
		}
		var reqXml = XmlUtils.createExecute(req);
		this.showRequest(reqXml);
		var self = this;
		$.ajax({
			"type": "POST",
			"url": settings[ this.options.mock ? "mock-url" : "url"],
			"data": XmlUtils.xml2string(reqXml),
			"contentType": "application/xml",
			"dataType": "xml"
		}).done(function(e) {
			self.onRequestSuccess(e, process);
			if (typeof callback === "function") {
				callback();
			}
		}).fail(function(e, message, exception) {
			self.onRequestFailure(this, e, message, exception);
		});
	},

	createShowResponseLink: function(id) {
		var $a = $("<a>").attr("href", "#logrow"+id).click(function(e) {
			e.preventDefault();
			$('html,body').animate({
				"scrollTop": $($(this).attr("href")).offset().top
			}, "fast", function() {
				/* will be triggerd twice ...*/
				if (this.localName === "html") {
					$("#logrow" + id + " a").text("hide");
					$("#coderow" + id).fadeIn("fast");
				}
			});
		}).text("show response");
		return $a;
	},

	onRequestFailure: function(request, e, message, exception) {
		if (message === "parsererror") {
			this.showError("<code>" + request.type + " " + request.url + "</code> failed: response is no valid XML.");	
		} else {
			this.showError("<code>" + request.type + " " + request.url + "</code> failed: <code><b>" 
				+ e.status + "</b> " + e.statusText + "</code>");	
		}
	},

	onRequestSuccess: function(e, process) {
		var fail = XmlUtils.isException(e);
		var id = this.showResponse(e, fail);
		var $a = this.createShowResponseLink(id);

		if (fail) {
			this.showError($a.before("Request failed with ExceptionReport! "));
		} else {
			this.showSuccess($a.before("Request succeeded! "));
			$(".sidebar-nav li.active")
				.next().removeClass("disabled")
				.children("a").trigger("click");
				this.setResponse(process, e);
		}
	}
});

TimePoint = function(day, time) {
	time = parseInt(time, 10);
	this.d = parseInt(day, 10);
	this.h = parseInt(Math.floor(time/60), 10);
	this.m = parseInt(time - this.h * 60, 10);

	if (isNaN(this.d) || this.d < 0 || this.d >= 7) {
		throw new Error("day out of range");
	}
	if (isNaN(this.h) || this.h < 0 || this.h >= 24) {
		throw new Error("hours out of range");
	}
	if (isNaN(this.m) || this.m < 0 || this.m >= 60) {
		throw new Error("minutes out of range");
	}
};

$.extend(TimePoint.prototype, {
	getDay: function() { 
		return this.d;
	},
	getHours: function() { 
		return this.h;
	},
	getMinutes: function() { 
		return this.m;
	},
	getTime: function() { 
		return this.getHours() * 60 + this.getMinutes();
	},
	compareTo: function(that) { 
		return  (this.d === that.d) ? (this.h === that.h) 
			? (this.m === that.m) ? 0 : (this.m < that.m) ? -1 : 1 : 
			(this.h < that.h) ? -1 : 1  : (this.d < that.d) ? -1 : 1;
	},
	toString: function() {
		function format(val) { return (val < 10 ? "0" : "") + val; }
		return "D" + this.getDay()
			 + "h" + format(this.getHours())
			 + "m" + format(this.getMinutes());
	}
});

TimeValue = function() {
	if (arguments.length === 3) {
		this.d = parseInt(arguments[0], 10);
		this.b = parseInt(arguments[1], 10);
		this.l = parseInt(arguments[2], 10);	
	} else if (arguments.length === 1 && $.isPlainObject(arguments[0])) {
		this.d = parseInt(arguments[0].day, 10);
		this.b = parseInt(arguments[0].begin, 10);
		this.l = parseInt(arguments[0].length, 10);	
	} else {
		throw new Error("Invalid arguments");
	}

	this.begin = new TimePoint(this.d, this.b);
	this.end   = new TimePoint((this.d + Math.floor((this.b + this.l) / 1440)) % 6, 
							   (this.b + this.l) % 1440);
	
	if (isNaN(this.l) || this.l <= 0 || this.l >= 7 * 24 * 60) {
		throw new Error("length out of range");
	}
	if (isNaN(this.b) || this.b < 0 || this.b >=     24 * 60) {
		throw new Error("begin out of range");
	}
	if (isNaN(this.d) || this.d < 0 || this.d >= 7          ) {
		throw new Error("day out of range");
	}
};

$.extend(TimeValue.prototype, {
	getLength: function() {
		return this.l;
	},
	getDay: function() {
		return this.d;
	},
	getBegin: function() {
		return this.b;
	},
	conflicts: function(that) {
	return !((this.getBeginPoint().compareTo(that.getEndPoint()) > 0) ||
			 (that.getBeginPoint().compareTo(this.getEndPoint()) > 0));
	},
	getBeginPoint: function() {
		return this.begin;
	},
	getEndPoint: function() {
		return this.end;
	},
	toString: function() {
		return 		this.getBeginPoint().toString()
			+ "/" + this.getEndPoint().toString();
	}
});

Map = function(options) {
	this.formid = 1;
	this.markers = [];
	this.$div = $("#" + options.div);
	this.WEEKDAYS = [ "Monday", 
		"Tuesday",   "Wednesday", 
		"Thursday",  "Friday", 
		"Saturday",  "Sunday" ];

	this.icon = L.icon({
		iconUrl: "img/marker.png",
		shadowUrl: "img/marker_shadow.png",
		iconSize: [32, 37],
		iconAnchor: [16, 37],
		shadowSize: [51, 37],
		shadowAnchor: [21, 37],
		popupAnchor: [0, -26]
	});

	this.onChange = $.isFunction(options.onChange) ? options.onChange : $.noop;

	this.map = L.map(options.div, {
		attributionControl: false,
		doubleClickZoom: false,
		closePopupOnClick: false,
		zoomControl: false,
		layers: [ 
			new L.TileLayer("http://tile.openstreetmap.org/{z}/{x}/{y}.png") 
		]
	});
	this.map.addControl(new L.Control.ZoomFS());
	this.map.on({
		"popupopen": this.onPopupOpen,
		"popupclose": this.onPopupClose,
		"dblclick": this.onDoubleClick
	}, this);
};

$.extend(Map.prototype, {
	getMap: function() {
		return this.map;
	},

	getPopup: function(event) {
		return event.popup._source;
	},

	onPopupOpen: function(e) {
		var m = this.getPopup(e);
		e.popup.setContent(this.getPopupContent(m));
		this.createSlider(m);
		e.popup._adjustPan();
	},

	onPopupClose: function(e) {
		var m = this.getPopup(e);
		if (!m.time) { this.removeMarker(m); }
	},

	onDoubleClick: function(e) {
		e.originalEvent.preventDefault();
		this.map.closePopup();
		// mouse event seems to be shifted some pixels...
		this.addMarker(this.map.layerPointToLatLng(e.layerPoint.add([-20, -22])));
	},

	getPopupContent: function(marker) {
		marker.formid = marker.formid || this.formid++;

		var begin = marker.time ? marker.time.getBegin() : 0;
		var length = marker.time ? marker.time.getLength() : 0;
		var weekday = marker.time ? marker.time.getDay() : 0;

		var $bubble = $("<div>").addClass("bubble-content");
		var $form = $("<form>").attr("id", "trajectory" + marker.formid).appendTo($bubble);
		var $fieldset = $("<fieldset>").appendTo($form);
		$fieldset.append($("<legend>").html("Trajectory").hide());
		$fieldset.append($("<div>").addClass("alert alert-error")
			.text("There is a trajectory conflicting with this one.")
			.prepend($("<strong>").text("Conflict! ")).hide());

		$("<label>").attr("for", "day")
			.html("<h5>Day of the Week</h5>").appendTo($fieldset);
		var $select = $("<select>").attr("name", "day").addClass("span12");
		$.each(this.WEEKDAYS, function(i, e){
			$select.append($("<option>").attr("value", i+1).text(e));
		});
		$select.appendTo($fieldset);

		$("<option>").attr({
			"value": "",
			"selected": true,
			"disabled": true
		}).css("display", "none")
		  .prependTo($select);
			

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

		var $btnGroup = $("<div>").addClass("btn-group")

		$("<button>")
			.attr("type", "button")
			.addClass("btn btn-small form-save")
			.append($("<i>").addClass("icon-ok"))
			.appendTo($btnGroup);

		$("<button>")
			.attr("type", "button")
			.addClass("btn btn-small form-abort")
			.append($("<i>").addClass("icon-remove"))
			.appendTo($btnGroup);


		if (marker.time) {
			$("<button>")
				.attr("type", "button")
				.addClass("btn btn-small form-delete")
				.append($("<i>").addClass("icon-trash"))
				.appendTo($btnGroup);
		}

		$("<div>").addClass("controls pull-right")
			.append($btnGroup).appendTo($fieldset);

		return $bubble.get(0).outerHTML;
	},

	addMarker: function(coords, time) {
		var marker = L.marker(coords, {
			icon: this.icon,
			draggable: true
		});

		if (time !== undefined) {
			marker.time = time;
		}
		marker.bindPopup(this.getPopupContent(marker), 
			{ closeButton: false }).addTo(this.map);
		
		if (marker.time === undefined) {
			marker.openPopup();
			this.createSlider(marker);
		}
		this.markers.push(marker);
		
		marker.on({
			drag: this.onMarkerDrag
		}, this).fire("drag");
	},

	createSlider: function(marker) {
		var lengthScale = (function() { var scale = []; for (var i = 0; i <= 24; i += 3) scale.push((i % 6) != 0 ? "|" : i + "h"  ); return scale; })(), 
			beginScale  = (function() { var scale = []; for (var i = 0; i <= 24; i += 3) scale.push((i % 6) != 0 ? "|" : i + ":00"); return scale; })(),
			self        = this,
			$form       = $("form#trajectory" + marker.formid),
			$weekday    = $form.find(":input[name=day]"),
			$begin      = $form.find("input[name=begin]"),
			$length     = $form.find("input[name=length]"),
			$save       = $form.find("button.form-save").disabled(),
			$abort      = $form.find("button.form-abort"),
			$delete     = $form.find("button.form-delete"),
			$error      = $form.find("div.alert").hide();
		
		function validate() {
			var time, error = false;
			try {
				time = new TimeValue(Utils.formArrayToObject($form.serializeArray()));
			} catch (e) {
				error = true;
			}

			$save.disabled(error);
			if (error) { return; }
			
			for (var i = 0; i < self.markers.length; ++i) {
				if (self.markers[i] === marker) { continue; }
				if (self.markers[i].time) {
					if (self.markers[i].time.conflicts(time)) {
						$save.disabled();
						$error.slideDown(function() {
							marker._popup._adjustPan();	
						});
						return;
					}
				}
			}
			$error.slideUp(function() {
				marker._popup._adjustPan();
			});
			$save.disabled(false);
		}

		$begin.slider({
			from: 0, to: 1439, step: 1, skin: "round_plastic",
			dimension: '', scale: beginScale, limits: false,
			calculate: function(value) {
				value = parseInt(value, 10);
				var hours = Math.floor(value/60);
				var mins = value - hours * 60;
				return (hours < 10 ? "0" + hours : hours) + ":" 
						+ ( mins < 10 ? "0" + mins : mins );
			},
			callback: validate
		});

		$length.slider({
			from: 0, to: 1439, step: 1, skin: "round_plastic",
			dimension: '', scale: lengthScale, limits: false,
			calculate: function(value) {
				value = parseInt(value, 10);
				var hours = Math.floor(value/60);
				var mins = value - hours * 60;
				return (hours < 10 ? "0" + hours : hours) + "h " 
						+ (mins < 10 ? "0" + mins : mins) + "m";
			},
			callback: validate
		});

		$save.on("click", function() {
			marker.time = new TimeValue(Utils.formArrayToObject($form.serializeArray()));
			marker.closePopup();
			self.onChange();			
		});

		$abort.on("click", function() { marker.closePopup(); });

		$delete.on("click", function() {
			marker.time = undefined;
			marker.closePopup();
			self.onChange();
		});
		if (marker.time) {
			$weekday.val(marker.time.getDay());
		}
		
		$weekday.on("change", validate).trigger("change");
	},

	removeMarker: function(marker) {
		this.map.removeLayer(marker);
		var i = this.markers.indexOf(marker);
		if (i >= 0) {
			this.markers.splice(i, 1);	
		}
		this.onMarkerDrag();
	},

	onMarkerDrag: function() {
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
	},

	getTrack: function() {
		var track = [];
		for (var i = 0; i < this.markers.length; ++i) {
			if (this.markers[i].time) {
				var latlng = this.markers[i].getLatLng();
				track.push({
					location: [ latlng.lat, latlng.lng ],
					time: this.markers[i].time
				});
			}
		}
		return track;
	},

	getTrackAsXml: function() {
		return XmlUtils.createObservationCollection(this.toObservationCollection(this.getTrack()));
	},

	loadTrack: function(track) {
		for (var i = 0; i < track.length; ++i) {
			this.addMarker(track[i].location, new TimeValue(track[i].time));
		}
	},

	hasTrack: function() {
		return this.getTrack().length > 0;
	},

	zoomToTrack: function() {
		if (!this.line) {
			this.map.fitWorld();
		} else {
			this.map.fitBounds(this.line.getBounds());	
		}
	},

	toObservationCollection: function(track) {
		var observations = [], base = {
			resultTime: new Date().toISOString(),
			procedure: location.href,
			observedProperty: location.href,
		};
		for (var i = 0; i < track.length; ++i) {
			observations.push($.extend({
				"phenomenonTime": track[i].time.toString(),
				"featureOfInterest": track[i].location
			}, base));
		}
		return observations;
	}
});


function transformer(name) {
	return function(options) {
		var stddev = options.inputs[name+"-stddev"],
			aid = options.inputs[name+"-aid"],
			parameter = options.inputs[name+"-parameter"];
		delete options.inputs[name+"-stddev"],
		delete options.inputs[name+"-aid"],
		delete options.inputs[name+"-parameter"];
		var parameterName = (name === "uncert-link") ? 
			"sector" : "link"; /* FIXME real name? */
		if (stddev && aid && parameter 
			&& stddev.length === aid.length 
			&& stddev.length === parameter.length
			&& stddev.length > 0) {
			var l = stddev.length;
			var a = [];
			for (var i = 0; i < l; ++i) {
				if (aid[i] !== undefined && aid[i] !== ""
					&& parameter[i] !== undefined && parameter[i] !== ""
					&& stddev[i] !== undefined && stddev[i] !== "")
				a.push(XmlUtils.uncertainAlbatrossInput(
					aid[i], parameter[i], stddev[i], parameterName));
			}
			if (a.length > 0) {
				options.inputs[name] = a;
			}
		}
	}
}
