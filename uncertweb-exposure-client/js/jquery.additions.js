(function($) {
	$.queryParam = (function(a) {
		if (a === "") { return {}; }
		var b = {};
		for (var i = 0; i < a.length; ++i) {
			var p = a[i].split('=');
			if (p.length != 2) continue;
			b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
		}
		return b;
	})(window.location.search.substr(1).split('&'));

	$.fn.disabled = function(opts) {
		if (opts === false) {
			$(this).removeAttr("disabled");
		} else {
			$(this).attr("disabled", "disabled")
		}
		return this;
	};

	$.fn.slideRight = function(speed, callback) {
		this.animate({
			width: "show",
			paddingLeft: "show",
			paddingRight: "show",
			marginLeft: "show",
			marginRight: "show"
		}, speed, callback);
	};

	$.fn.slideLeft = function(speed, callback) {
		this.animate({
			width: "hide",
			paddingLeft: "hide",
			paddingRight: "hide",
			marginLeft: "hide",
			marginRight: "hide"
		}, speed, callback);
	};
})(jQuery);