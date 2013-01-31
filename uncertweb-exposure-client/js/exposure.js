jQuery(document).ready(function($) {
    $.getJSON("config.json", function(options) {

        options.complexInputTransformers = [
            transformer("uncert-area"),
            transformer("uncert-link")
        ]

        var app = new App(options, {
            "ems": function() { 
                this.showVisualizationLink("ems", "result"); 
            }
        });

        var map = new Map({ 
            div: "map", onChange: function() { 
                $("#mapsave").disabled(!this.hasTrack()); 
            }
        });

        $("a[href=#albatross], a[href=#map-form]").on("click", function() {
            setTimeout(function() { 
                map.getMap().invalidateSize(); /* FIXME ????? */ 
            }, 10);
        })
        $("#mapsave").disabled().on("click", function() {
            app.onRequestSuccess(map.getTrackAsXml(), "map");
        });

        $("form[data-process=nilu] :input[name=site]").on("change", function() {
            map.getMap().setView(options.mapCenter[$(this).val()], 13)
        }).trigger("change");
    });
});