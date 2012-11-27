function App(options, sendCallbacks) {
	this.logid = 1;
	this.options = options;
	for (var p in this.options.processes) {
		sendCallbacks[p] = $.isFunction(sendCallbacks[p]) ? sendCallbacks[p] : $.noop;
	}
	this.sendCallbacks = sendCallbacks;
	/* init */
	this.generateInterface();
	this.addListeners();
}

App.prototype.scrollToTop = function() {
	$("body,html").animate({"scrollTop": 0}, "fast");
}

App.prototype.generateInterface = function() {
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
};

App.prototype.addListeners = function() {
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
			self.sendCallbacks[$(e.target).parents(".processContainer").attr("id")](self);
		});
	})
};

App.prototype.showVisualizationLink = function(process, output) {
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
};

App.prototype.showDownloadLink = function(process, output) {
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
};

App.prototype.buildModalDialog = function(title, text, onclick) {
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
}

App.prototype.onSidebarClick = function(e, element) {
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
};

App.prototype.onRequiredChange = function(event, element) {
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
};

App.prototype.setSettings = function(id, val, options) {
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
};

App.prototype.createRequest = function(id, form) {
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
			if (!settings.inputs.sections[i].options[key].required  && o.inputs[key].length === 1 
				&& (o.inputs[key][0] === "" || o.inputs[key][0] === undefined)) {
				delete o.inputs[key];
			}
		}
	}
	return o;
};

App.prototype.showXML = function(message, text, failed) {
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
};

App.prototype.setResponse = function(id, response) {
	this.options.outputs[id] = response;
};

App.prototype.getResponse = function(id) {
	return this.options.outputs[id];
};

App.prototype.showRequest = function(req) {
	return this.showXML(req, "Request", false);
};

App.prototype.showResponse = function(res, fail) {
	return this.showXML(res, (fail) ? "Failure response" : "Sucess response", fail);
};

App.prototype.showFailureResponse = function(res) {
	return this.showResponse(res, true);
};

App.prototype.showSuccessResponse = function(res) {
	return this.showResponse(res, false);
};

App.prototype.showMessage = function(content, type, autoclose) {
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
};

App.prototype.showError = function(error) {
	this.showMessage($("<strong>Error! </strong> ").after(error), "error");
};

App.prototype.showSuccess = function(message) {
	this.showMessage($("<strong>Success! </strong> ").after(message), "success", true);
};

App.prototype.showWarning = function(message) {
	this.showMessage($("<strong>Warning! </strong> ").after(message), "warning", true);
};

App.prototype.sendRequest = function(e, element, callback) {
	var $form = $(element).parents("form");
	var process = $form.data("process");
	var settings = this.options.processes[process];
	var req = this.createRequest(process, $form.serializeArray());

	for (var p in this.options.mappings[process]) {
		for (var o in this.options.mappings[process][p]) {
			if (!req.inputs[this.options.mappings[process][p][o]] ) {
				req.inputs[this.options.mappings[process][p][o]] = [];
			}
			req.inputs[this.options.mappings[process][p][o]].push(
				XmlUtils.getOutput(this.getResponse(p), o));
		}
	}
	var reqXml = XmlUtils.createExecute(req);
	this.showRequest(reqXml);
	var self = this;
	$.ajax({
		"type": "POST",
		"url": settings.url,
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
};

App.prototype.createShowResponseLink = function(id) {
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
};

App.prototype.onRequestFailure = function(request, e, message, exception) {
	if (message === "parsererror") {
		this.showError("<code>" + request.type + " " + request.url + "</code> failed: response is no valid XML.");	
	} else {
		this.showError("<code>" + request.type + " " + request.url + "</code> failed: <code><b>" 
			+ e.status + "</b> " + e.statusText + "</code>");	
	}
};

App.prototype.onRequestSuccess = function(e, process) {
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
};

App.prototype.toFormattedTime = function(t) {
	return "D" + Math.floor(t[0]/60) + "h" + (t[0] - 60 * Math.floor(t[0]/60)) + "m" +
		  "/D" + Math.floor(t[1]/60) + "h" + (t[1] - 60 * Math.floor(t[1]/60)) + "m";
};

App.prototype.toObservation = function(trackElement) {
	return {
		"phenomenonTime": toFormattedTime([track[i].begin, track[i].end]),
		"resultTime": "2012-01-01T00:00:00Z",
		"observedProperty": "http://www.uncertweb.org/variables/albatross/actionNumber",
		"featureOfInterest": track[i].location
	};
};

App.prototype.toObservationCollection = function(track) {
	var observations = [];
	for (var i = 0; i < track.length; ++i) {
		observations.push(this.toObservation(track[i]));
	}
	return observations;
};
