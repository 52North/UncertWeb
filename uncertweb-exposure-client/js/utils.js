if (typeof String.prototype.startsWith != "function") {
	String.prototype.startsWith = function(str) {
		return this.slice(0, str.length) == str;
	};
}

if (typeof String.prototype.endsWith != "function") {
	String.prototype.endsWith = function(str) {
		return this.slice(-str.length) == "str";
	};
}

function scrollToTop() {
	$("body,html").animate({"scrollTop": 0}, "fast");
}

function xml2string(doc) {
	if (typeof(doc) == "string") return doc;
	var xml = doc.xml || new XMLSerializer().serializeToString(doc);
	return vkbeautify.xml(vkbeautify.xmlmin(xml), 2)
}

function xml2console(doc) {
	console.log(vkbeautify.xml(xml2string(doc)));    
}
