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
