function UIBuilder() {}

UIBuilder.prototype.createInput = function(option) {
	var $input = null;
	switch (option.type) {
	case "integer":
	case "number":
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
		$input.addClass("required").bind("keyup input", this.required);
	}
	return $input;
}

UIBuilder.prototype.generateSection = function(section, $container) {
	if (section.title) {
		$("<legend>").text(section.title).appendTo($container);
	}
	if (section.description) {
		$("<p>").html(section.description).appendTo($container);
	}
	var self = this;
	$.each(section.options, function(id, option) {
		option.id = id;
		$container.append(self.generateOption(option));
	});
}

UIBuilder.prototype.generateTabbedSection = function(section, $tabTitles, $tabs) {
	if (!section.title) {
		return;
	} /* generate the tab title */
	section.id = section.title.toLowerCase().replace(/\W/g, "_");
	var $tabHead = $("<li>").append($("<a>").text(section.title)
		.attr("href", "#" + section.id).attr("data-toggle", "tab"));
	var $tabPane = $("<div>").addClass("tab-pane").attr("id", section.id);
	if (section.description) {
		$("<p>").html(section.description).appendTo($tabPane);
	}
	var self = this;
	$.each(section.options, function(id, option) {
		option.id = id;
		$tabPane.append(self.generateOption(option));
	});
	$tabs.append($tabPane);
	$tabTitles.append($tabHead);
}

UIBuilder.prototype.generateOption = function(option) {
	var $option = $("<div>").addClass("control-group");
	var self = this;
	switch (option.type) {
	case "complex": 
		function create() {
			var $group = $("<div>").addClass("control-group");
			$group.append($("<label>").addClass("control-label").text(option.title));
			var $controls = $("<div>").addClass("controls well").appendTo($group);
			for (var key in option.properties) {
				option.properties[key].id = key;
				$controls.append(self.generateOption(option.properties[key]));
			}
			var $help = $("<span>").addClass("help-block");
			var $label = $("<span>").addClass("label");
			if (option.required) {
				$label.addClass("label-warning").append("required");
			} else {
				$label.addClass("label-info").append("optional");
			}
			$help.append($label).append(" " + option.description).appendTo($controls);
			var $button = $("<button>").css({
					"margin-top": "-22px", 
					"margin-right": "-15px"
				}).attr("type", "button").addClass("btn")
				  .append($("<i>").addClass("icon-minus"));
			$button.on("click", function() {
				$group.slideUp(function() {
					$group.remove();
				});
			});
			$controls.append($("<div>").addClass("pull-right").append($button));
			return $group;
		}

		var $div = $("<div>").addClass("control-group");
		$div.append($("<label>").addClass("control-label").text(option.title));
		var $controls = $("<div>").addClass("controls").appendTo($div);
		var $add = $("<button>")
					.attr("type", "button")
					.addClass("btn")
					.css("margin-bottom", "6px")
					.append($("<i>").addClass("icon-plus"))
					.appendTo($controls).on("click", function() { 
						var $a = create();
						$a.find(".required").trigger("input");
						$a.hide().appendTo($div).slideDown(); 
					});
		return $div;
	case "integer":
	case "number":
	case "password":
	case "text":
	case "string":
		var $label = $("<label>").addClass("control-label").attr("for", option.id).html(option.title);
		var $controls = $("<div>").addClass("controls");
		var $input = null;
		if (option.multiple) {
			function plus(e) {
				e.preventDefault();
				var $newInput = self.createInput(option).val(""); 

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
				.append(this.createInput(option).removeClass("span12").addClass("span10"))
				.append($("<div>").addClass("span2")
					.append($("<a>").attr("href","#").addClass("icon-plus")).click(plus));
		} else {
			$input = this.createInput(option);
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
			$input.bind("change", this.required);
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

UIBuilder.prototype.required = function () {
	var valid = true;
	$(this).parents(".control-group").find(".required").each(function(){ 
		var val = $(this).val();
		return valid = (val !== null && val !== undefined && val !== "");
	});
	if (valid) {
		$(this).parents(".control-group").find(".control-group").andSelf().removeClass("error");
	} else {
		$(this).parents(".control-group").find(".control-group").andSelf().addClass("error");
	}
}


UIBuilder.prototype.generateOptions = function(options, container, tabbed) {
	var $container = $(container);
	var self = this;
	if (tabbed) {
		var $tabTitles = $("<ul>").addClass("nav nav-tabs");
		var $tabs = $("<div>").addClass("tab-content");
		$.each(options.sections, function(_, section) {
			self.generateTabbedSection(section, $tabTitles, $tabs);
		});
		$tabs.children(":first").addClass("active");
		$tabTitles.children(":first").addClass("active");
		$container.append($tabTitles).append($tabs);
	} else {
		$.each(options.sections, function(_, section) {
			self.generateSection(section, $container);
		});
	}
	$container.find("input[type=text],input[type=password],textarea").trigger("input");
	$container.find("select").trigger("change");
}
