
module.exports.byProperty = function byProperty(property, array) {
	return array.reduce(function(object, value) {
		object[value[property]] = value;
		return object;
	}, {});
};

module.exports.toArray = function toArray(object) {
	var array = [];
	for (var key in object) {
		if (object.hasOwnProperty(key)) {
			array.push(object[key]);
		}
	}
	return array;
};

module.exports.constant = function constant(val) {
	return function() { return val; };
};


function parseIntervals(intervals) {
	function parseIsoInterval(isostr) {
		var instances = [];
		var periodRegExp = /^P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?T?(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/;
		var funcMapping = [ 'Year', 'Month', 'Date', 'Hours', 'Minutes', 'Seconds' ];

		intervalParts = isostr.split('/');
		if (intervalParts.length >= 2) {
			// Start & End point
			var start = new Date(intervalParts[0]);
			var end = new Date(intervalParts[1]);

			if (intervalParts.length == 2) {
				// Only two instances, no period
				instances.push(start, end);
			} else {

				// Parse duration notation
				var match = intervalParts[2].match(periodRegExp);
				var current = new Date(start.getTime());
				instances.push(new Date(current.getTime()));

				// Add duration until end time reached, record each instance
				while (current < end) {
					for (var i = 1; i <= funcMapping.length; i++) {
						if (match[i]) {
							current['set' + funcMapping[i-1]](current['get' + funcMapping[i-1]]() + parseInt(match[i]));
						}
					}
					instances.push(new Date(current.getTime()));
				}
			}
		}
		return instances;
	}
	var instances = [], str;
	for ( var i = 0; i < intervals.length; i++) {
		str = intervals[i].trim();
		if (str.indexOf('/') != -1) {
			instances = instances.concat(parseIsoInterval(str));
		} else {
			instances.push(new Date(str));
		}
	}

	return instances;
}