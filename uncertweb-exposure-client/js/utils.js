if (typeof String.prototype.startsWith !== "function") {
	String.prototype.startsWith = function(str) {
		return this.slice(0, str.length) === str;
	};
}

if (typeof String.prototype.endsWith !== "function") {
	String.prototype.endsWith = function(str) {
		return this.slice(-str.length) === "str";
	};
}

if (typeof Array.prototype.last !== "function") {
	Array.prototype.last = function() {
		return this[this.length-1];
	};
}

if (typeof Array.prototype.isEmpty !== "function") {
	Array.prototype.isEmpty = function() {
		return this.length === 0;
	};
}

if (typeof String.prototype.isEmpty !== "function") {
	String.prototype.isEmpty = function() {
		return this.length === 0;
	};
}

if (!Array.prototype.indexOf) {
	Array.prototype.indexOf = function(elt /*, from*/) {
		var len = this.length >>> 0;
		var from = Number(arguments[1]) || 0;
		from = (from < 0) ? Math.ceil(from) : Math.floor(from);
		if (from < 0) from += len;
		for (; from < len; from++) {
			if (from in this &&
					this[from] === elt)
				return from;
		}
		return -1;
	};
}

if (!Date.prototype.toISOString) {
	Date.prototype.toISOString = function() {
		function pad(n) { return n < 10 ? '0' + n : n }
		return this.getUTCFullYear() + '-'
			+ pad(this.getUTCMonth() + 1) + '-'
			+ pad(this.getUTCDate()) + 'T'
			+ pad(this.getUTCHours()) + ':'
			+ pad(this.getUTCMinutes()) + ':'
			+ pad(this.getUTCSeconds()) + 'Z';
	};
}

window.Utils = window.Utils || {};
Utils.formArrayToObject = function(array) {
	var o = {};
	for (var i = 0; i < array.length; ++i) {
		var key = array[i].name;
		var value = array[i].value;
		if (o[key] !== undefined && o[key] !== null) {
			if (!$.isArray(o[key])) {
				o[key] = [ o[key] ];
			}
			o[key].push(value);
		} else {
			o[key] = value;
		}
	}
	return o;
};
