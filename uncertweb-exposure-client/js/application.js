function setSetting(id, val, options) {
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
}

function createRequest($form, settings) {
	var form = $form.serializeArray();
	var url = settings.url;
	
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
		}
	}
	return createExecute(o);
}

$(function() {
	$.getJSON("config.json", function(options) {
		generateOptions(options.processes.nilu.inputs, $("#nilu form"), false);
		generateOptions(options.processes.albatross.inputs, $("#albatross form"), false);
		generateOptions(options.processes.ems.inputs, $("#ems form"), false);

		$(".processForm")
			.append($("<div>")
				.addClass("form-actions")
				.append($("<button>")
					.addClass("btn send btn-info")
					.attr("type","button")
					.text("Send")));

		$("#start").click(function() {
			$(".sidebar-nav li.active").next()
				.next().removeClass("disabled")
				.find("a").trigger("click");
			//$(this).attr("disabled", true);
			$(this).parent().fadeOut(function() {
				$(this).remove();
			}) 
		});

		$(".sidebar-nav a").click(function(e) {
			var $this = $(this);
			e.preventDefault();
			if ($this.parents("li").hasClass("disabled")) return;
			if ($this.parents("li").hasClass("active")) return;
			var $active = $(".sidebar-nav li.active");
			$active.removeClass("active");
			$this.parents("li").addClass("active");
			$($active.find("a").attr("href")).fadeOut("fast", function() {
				$($this.attr("href")).fadeIn();
			});
		});

		$(document).on("keyup input change", ".required", function() {
            var valid = true;
            $(this).parents("form").find(".required").each(function(){ 
                var val = $(this).val();
                return valid = (val !== null && val !== undefined && val !== "");
            });
            var $button = $(this).parents("form").find("button.send");
            if (valid) {
                $button.removeAttr("disabled");
            } else {
                $button.attr("disabled", true);
            }
        });

		$("form").find(".required:first").trigger("change");
		
		$("form button.send").click(function(){
			var $form = $(this).parents("form");
			var settings = options.processes[$form.data("process")];
			var req = createRequest($form, settings);
			appendMessage(req, "Request", false)
			$.ajax({
				"type": "POST",
				"url": settings.url,
				"data": xml2string(req),
				"contentType": "application/xml",
				"dataType": "xml"
			}).done(function(e) {
				var fail = isException(e);
				var id = appendMessage(e, (fail) ? "Failure response" : "Sucess response", fail);

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
				if (fail) {
					showError($a.before("Request failed! "));
				} else {
					showSuccess($a.before("Request succeeded! "));
					$(".sidebar-nav li.active")
						.next().removeClass("disabled")
						.children("a").trigger("click");
				}
			}).fail(function(e,message,exception) {
				if (message === "parsererror") {
					showError("<code>" + this.type + " " + this.url + "</code> failed: response is no valid XML.");	
				} else {
					showError("<code>" + this.type + " " + this.url + "</code> failed: <code><b>" 
						+ e.status + "</b> " + e.statusText + "</code>");	
				}
				
			});
		});
	});
});

var logid = 0;
function appendMessage(message, text, failed) {
	var id = ++logid;
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
					.text(xml2string(message)))));
	prettyPrint();
	if (failed) {
		$("#"+logrowid).addClass("text-error");	
	}
	return id;
}

