function showMessage(text, type, autoclose) {
	function closeAlert(a) {
		a.fadeTo(500, 0).slideUp(500, function() {
			a.remove();
		});
	}
	var $alert = $("<div>");
	$alert.addClass("alert alert-" + type).append(text);
	$("<button>").attr("type", "button").addClass("close").click(function() {
		closeAlert($alert);
	}).html("&times;").prependTo($alert);
	$alert.hide().prependTo($("#content")).css("opacity", 0).slideDown(500).animate({
		opacity: 1
	}, {
		queue: false,
		duration: 1000
	});
	if (autoclose) {
		window.setTimeout(function() {
			closeAlert($alert);
		}, 5000);
	}
}

function showError(error) {
	showMessage("<strong>Error!</strong> " + error, "error");
}

function showSuccess(message) {
	showMessage("<strong>Success!</strong> " + message, "success", true);
}

function showWarning(message) {
	showMessage("<strong>Warning!</strong> " + message, "warning", true);
}
