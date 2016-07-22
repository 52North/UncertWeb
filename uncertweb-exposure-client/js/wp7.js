jQuery(document).ready(function($) {
	$.getJSON("wp-7-client-config.json", function(options) {

        options.complexInputTransformers = [
            transformer("uncert-area"),
            transformer("uncert-link")
        ]

		 var app = new App(options, {
			"albatross": function() {
				this.showVisualizationLink("albatross", "om_schedules");
			}
		});

	});
});