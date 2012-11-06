function generateOptions(options, container, tabbed) {
	function required() {
		var valid = true;
		$(this).parents(".control-group").find(".required").each(function(){ 
			var val = $(this).val();
			return valid = (val !== null && val !== undefined && val !== "");
		});
		if (valid) {
			$(this).parents(".control-group").removeClass("error");
		} else {
			$(this).parents(".control-group").addClass("error");
		}
	}

	function generateOption(option) {
		function createInput() {
			var $input = null;
			switch (option.type) {
			case "integer":
				// TODO slider
			case "string":
				$input = $("<input>").attr("type", "text").attr("name", option.id).addClass("span12");
				break;
			case "password":
				$input = $("<input>").attr("type", "password").attr("name", option.id).addClass("span12");
				break;
			case "text":
				$input = $("<textarea>").attr("rows", 5) // TODO make this a option
				.attr("name", option.id).addClass("span12");
				break;
			}
			if (option["default"]) {
				$input.val(option["default"]);
			}
			if (option.required) {
				$input.addClass("required").bind("keyup input", required);
			}
			return $input;
		}

		var $option = $("<div>").addClass("control-group");
		switch (option.type) {
		case "integer":
		case "password":
		case "text":
		case "string":
			var $label = $("<label>").addClass("control-label").attr("for", option.id).html(option.title);
			var $controls = $("<div>").addClass("controls");
			var $input = null;
			if (option.multiple) {
				function plus(e) {
					e.preventDefault();
					var $newInput = createInput().val(""); 

					var $div = $("<div>").addClass("multiple span12")
							.append($newInput.removeClass("span12").addClass("span10"))
							.append($("<div>").addClass("span2")
								.append($("<a>").attr("href","#").addClass("icon-plus").click(plus))
								.append($("<a>").attr("href","#").addClass("icon-minus").click(minus)))
					$div.hide();
					$(this).parents(".controls div:last").after($div);
					$newInput.trigger("input");
					$div.fadeIn();
				}
				function minus(e) {
					e.preventDefault();
					var $prev = $(this).parents("div.multiple").prev().find("input,textarea");
					$(this).parents("div.multiple").andSelf().fadeOut("fast",function() { 
						$(this).remove(); 
						$prev.trigger("input"); 
					});
				}
				$input = $("<div>").addClass("multiple span12")
					.append(createInput().removeClass("span12").addClass("span10"))
					.append($("<div>").addClass("span2")
						.append($("<a>").attr("href","#").addClass("icon-plus")).click(plus));
			} else {
				$input = createInput();
			}
			
			var $description = $("<span>").addClass("help-block").html(option.description);
			if (option.required) {
				var $required = $("<span>").addClass("label label-warning").text("required");
				$description.prepend(" ").prepend($required);
				
			} else {
				var $optional = $("<span>").addClass("label label-info").text("optional");
				$description.prepend(" ").prepend($optional);
			}
			$option.append($label).append($controls.append($input).append($description));
			break;
		case "choice":
			var $controls = $("<div>").addClass("controls");
			var $label = $("<label>").attr("for", option.id).addClass("control-label").text(option.title);
			var $input = $("<select>").attr("name", option.id).addClass("span12");
			$.each(option.options, function(val, desc) {
				$("<option>").attr("value", val).text(desc).appendTo($input);
			});
			if (!option["default"]) {
				var $o = $("<option>").attr("value", "").attr("selected", true);
				$input.prepend($o);
				if (option.required) {
					$input.addClass("required");
					$o.attr("disabled", true).css("display", "none");
				} else {
					var $optional = $("<span>").addClass("label label-info").text("optional");
					$description.prepend(" ").prepend($optional);
				}
			} else {
				$input.val(option["default"]);
			}
			var $description = $("<span>").addClass("help-block").html(option.description);
			if (option.required) {
				var $required = $("<span>").addClass("label label-warning").text("required");
				$description.prepend(" ").prepend($required);
				$input.bind("change", required);
			}
			$controls.append($input)
			$controls.append($description)
			$option.append($label)
			$option.append($controls);
			break;
		case "boolean":
			var $controls = $("<div>").addClass("controls");
			var $input = $("<input>").attr("type", "checkbox").attr("name", option.id);
			var $label = $("<label>").attr("for", option.id).addClass("checkbox").text(option.title);
			var $description = $("<span>").addClass("help-block").html(option.description);
			$option.append($label).append($controls.append($label.prepend($input)).append($description));
			if ((typeof option["default"]) === "boolean") {
				$input.attr("checked", option["default"]);
			}
			break;
		}
		return $option;
	}

	function generateTabbedSection(section, $tabTitles, $tabs) {
		if (!section.title) {
			return;
		} /* generate the tab title */
		section.id = section.title.toLowerCase().replace(/\W/g, "_");
		var $tabHead = $("<li>").append($("<a>").text(section.title).attr("href", "#" + section.id).attr("data-toggle", "tab")); /* generate the tab pane */
		var $tabPane = $("<div>").addClass("tab-pane").attr("id", section.id);
		if (section.description) {
			$("<p>").html(section.description).appendTo($tabPane);
		}
		$.each(section.options, function(id, option) {
			option.id = id;
			$tabPane.append(generateOption(option));
		});
		console.log($tabPane);
		console.log($tabs);
		$tabs.append($tabPane);
		console.log($tabHead);
		console.log($tabTitles);
		$tabTitles.append($tabHead);

	}

	function generateSection(section, $container) {
		if (section.title) {
			$("<legend>").text(section.title).appendTo($container);
		}
		if (section.description) {
			$("<p>").html(section.description).appendTo($container);
		}
		$.each(section.options, function(id, option) {
			option.id = id;
			$container.append(generateOption(option));
		});
	}
	var $container = $(container);
	if (tabbed) {
		var $tabTitles = $("<ul>").addClass("nav nav-tabs");
		var $tabs = $("<div>").addClass("tab-content");
		$.each(options.sections, function(_, section) {
			generateTabbedSection(section, $tabTitles, $tabs);
		});
		$tabs.children(":first").addClass("active");
		$tabTitles.children(":first").addClass("active");
		$container.append($tabTitles).append($tabs);
	} else {
		$.each(options.sections, function(_, section) {
			generateSection(section, $container);
		});
	}
	$container.find("input[type=text],input[type=password],textarea").trigger("input");
	$container.find("select").trigger("change");
}
