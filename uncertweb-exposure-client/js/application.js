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
	/* get the false booleans */

	var o = { 
		id: settings.id, 
		inputs: {},
		outputs: settings.outputs
	};
	for (var i = 0; i < form.length; ++i) {
		o.inputs[form[i].name] = form[i].value;
	}

	for (var i = 0; i < settings.inputs.sections.length; ++i) {
		for (var key in settings.inputs.sections[i].options) {
			if (settings.inputs.sections[i].options[key].type === "boolean" && !o.inputs[key]) {
				o.inputs[key] = false;
			}
		}
	}
	return createExecute(o);
}

$(function() {
	$.getJSON("config.json", function(options) {
		generateOptions(options.processes.nilu.inputs, $("#nilu"), false);
		generateOptions(options.processes.albatross.inputs, $("#albatross"), false);
		generateOptions(options.processes.nilu.inputs, $("#ems"), false);

		$(".processForm")
			.append($("<div>")
				.addClass("form-actions")
				.append($("<button>")
					.addClass("btn send")
					.attr("type","button")
					.text("Send")));

		$(".sidebar-nav a").click(function(e) {
			var $this = $(this);
			e.preventDefault();
			if ($this.parents("li").hasClass("disabled")) return;
			if ($this.parents("li").hasClass("active")) return;
			var $active = $(".sidebar-nav li.active");
			$active.removeClass("active");
			$this.parents("li").addClass("active");
			$active.each(function() {
				$("#" + $(this).find("a").data("toggle")).fadeOut("fast",function() {
					$("#" + $this.data("toggle")).fadeIn()
				});
			});	
		});

		$(".required").bind("keyup input change", function() {
            var valid = true;
            $(this).parents("form").find(".required").each(function(){ 
                var val = $(this).val();
                return valid = (val != null && val != undefined && val != "");
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
			var settings = options.processes[$form.attr("id")];
			var req = createRequest($form, settings);
			appendMessage(req, "Request", false)
			$.ajax({
				"type": "POST",
				"url": settings.url,
				"data": xml2string(req),
				"contentType": "application/xml",
				"dataType": "xml"
			}).done(function(e) {
				if (isException(e)) {
					showError("Request failed!");
					appendMessage(e, "Failure response", true)
				} else {
					appendMessage(e, "Sucess response", false)
					showSuccess("Request succeeded!");
					$(".sidebar-nav li.active")
						.next().removeClass("disabled")
						.children("a").trigger("click");
				}
			}).fail(function(e) {
				scrollToTop();
				showError("Request failed: " + e.statusCode + " " + e.statusText);
			});
		});
	});
});

var logid = 0;
function appendMessage(message, text, failed) {
	var id = ++logid;
	var logrowid = "logrow"+ id;
	var coderowid = "coderow" + id;
	$("#output > tbody").append(
		$("<tr>").attr("id", logrowid)
			.append($("<td>").text(new Date().toLocaleString()))
			.append($("<td>")
				.append(text+" ")
				.append($("<a>")
					.addClass("pull-right")
					.attr("href","#")
					.text("show"))
	.toggle(function() {
		$("#"+logrowid+" a").text("hide");
		var xml = xml2string(message);
		$("#"+logrowid).after(
			$("<tr>")
				.attr("id",coderowid)
				.append($("<td>")
					.attr("colspan", 2)
					.append($("<pre>")
						.addClass("prettyprint")
						.addClass("linenums")
						.text(xml))).hide());
		prettyPrint();
		$("#"+coderowid).fadeIn("fast");
	}, function() {
		$("#"+logrowid+" a").text("show");
		$("#"+coderowid).fadeOut("fast", function(){
			$(this).remove()
		});
	})));
	if (failed) {
		$("#"+logrowid).addClass("text-error");	
	}
}

