var gulp = require('gulp');
var autoprefixer = require('gulp-autoprefixer');
var jshint = require('gulp-jshint');
var uglify = require('gulp-uglify');
var imagemin = require('gulp-imagemin');
var rename = require('gulp-rename');
var concat = require('gulp-concat');
var cache = require('gulp-cache');
var connect = require('gulp-connect');
var del = require('del');
var sass = require("gulp-sass");
var merge = require('merge-stream');
var sourcemaps = require('gulp-sourcemaps');




gulp.task('libstyles', function() {
  return gulp.src([
	    'lib/ExtJs/resources/css/ext-all.css',
	    'lib/ExtJs/resources/css/xtheme-gray.css',
	    'lib/ExtUx/MultiSelect.css',
	    'images/style.css'
	])
  	.pipe(sourcemaps.init())
	.pipe(concat('dependencies.css'))
	.pipe(gulp.dest('dist/styles'));
});

gulp.task('libscripts', function() {
	return gulp.src([
			'lib/OpenLayers/OpenLayers.js',
		    'lib/OpenLayers/OpenStreetMap.js',

		    'lib/flot/jquery.js',
		    'lib/flot/jquery.flot.js',
		    'lib/flot/jquery.flot.pie.js',
		    'lib/flot/jquery.flot.stack.min.js',
		    'lib/flot/jquery.flot.selection.min.js',
		    'lib/flot/jquery.flot.navigate.min.js',

		    //'lib/ExtJs/ext-base.js',
		    //'lib/ExtJs/ext-all.js',
		    'lib/ExtJs/ext-base-debug.js',
		    'lib/ExtJs/ext-all-debug.js',
		    'lib/ExtUx/TabCloseMenu.js',
		    'lib/ExtUx/ItemSelector.js',
		    'lib/ExtUx/MultiSelect.js',
		    'lib/ExtUx/FitToParent.js',

		    'lib/proj4/proj4js-compressed.js',
		    'lib/proj4/defs.js',

		    'lib/GeoExt/GeoExt.js',

		    'lib/jstat/jstat.js',

		    'lib/jstat/jstat.additions.js',
		    'lib/flot/jquery.greenland.noconflict.js',
		])
		.pipe(sourcemaps.init())
		.pipe(concat('dependencies.js'))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest('dist/scripts'))
		.pipe(rename('dependencies.min.js'))
		.pipe(uglify())
		.pipe(sourcemaps.write())
		.pipe(gulp.dest('dist/scripts'));
});

gulp.task('html', function() {
	return gulp.src('*.html')
		.pipe(gulp.dest('dist'))
		.pipe(connect.reload());
});

gulp.task('styles', function() {
  return gulp.src(['styles/**/*.sass', 'styles/**/*.scss', 'styles/**/*.css'])
	  	.pipe(sourcemaps.init())
	  	.pipe(sass({expand: true}))
		.pipe(autoprefixer('last 2 version', 'safari 5', 'ie 8', 'ie 9', 'opera 12.1', 'ios 6', 'android 4'))
		.pipe(concat('greenland.css'))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest('dist/styles'))
		.pipe(connect.reload());
});

gulp.task('scripts', function() {
	// FIXME: currently the order is important, so we can't use a glob.....
	return gulp.src([
			'js/ext/flotpanel.js',
			'js/ext/featurearrow.js',
			'js/ext/slider.js',
			'js/ext/resourcetree.js',
			'js/ext/legendscalebar.js',
			'js/ext/legend.js',
			'js/ext/resourcewindow.js',
			'js/ext/layersettingswindow.js',
			'js/ext/featurewindow.js',
			'js/settings.js',
			'js/parser/jsom.js',
			'js/parser/om.js',
			'js/parser/om2.js',
			'js/parser/statisticsvalue.js',
			'js/layer/raster.js',
			'js/layer/vector.js',
			'js/layer/multivector.js',
			'js/layer/observation.js',
			'js/styler/styler.js',
			'js/styler/color.js',
			'js/styler/shape.js',
			'js/styler/size.js',
			'js/styler/width.js',
			'js/styler/opacity.js',
			'js/styler/continuousBounds.js',
			'js/styler/equalIntervalBounds.js',
			'js/styler/exceedanceIntervalBounds.js',
			'js/styler/irregularIntervalBounds.js',
			'js/styler/label.js',
			'js/styler/chooser.js',
			'js/symbology/symbology.js',
			'js/symbology/vector.js',
			'js/symbology/numericvector.js',
			'js/symbology/categoricalvector.js',
			'js/parser/v1_3_0_ncWMS.js',
			'js/layer/wmsq/wmsq.js',
			'js/layer/wmsq/visualization.js',
			'js/layer/wmsq/vector.js',
			'js/layer/wmsq/whitening.js',
			'js/layer/wmsq/contour.js',
			'js/layer/wmsq/glyphs.js',
			'js/layer/wmsq/colorrange.js',
			'js/layer/wmsq/exceedance.js',
			'js/layer/wmsq/confidenceinterval.js',
			'js/layer/wms/wms.js',
			'js/resultvalue/resultvalue.js',
			'js/resultvalue/mean.js',
			'js/resultvalue/mode.js',
			'js/resultvalue/modeprobability.js',
			'js/resultvalue/exceedanceprobability.js',
			'js/resultvalue/statistics.js',
			'js/resultvalue/custom.js',
			'js/color.js',
			'js/map.js',
			'js/sld.js',
			'js/ui.js',
			'js/swipe.js',
			'js/help.js',
			'js/resources/settingsparcel.js',
			'js/resources/resourceloader.js',
			'js/resources/resource.js',
			'js/main.js'
		])
		.pipe(sourcemaps.init())
		.pipe(concat('greenland.js'))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest('dist/scripts'))
		.pipe(rename('greenland.min.js'))
		.pipe(uglify())
		.pipe(sourcemaps.write())
		.pipe(gulp.dest('dist/scripts'))
		.pipe(connect.reload());
});

gulp.task('clean', function() {
	return del('dist');
});

gulp.task('serve', ['build'], function() {
  	connect.server({
  		root: 'dist',
  		port: 8080,
  		host: 'localhost',
  		https: false,
  		livereload: true
  	});
});

gulp.task('copy:extjs', function() {
	return gulp.src([
			'lib/ExtJs/resources/images/**',
			'lib/ExtUx/images/**'
		])
		.pipe(gulp.dest('dist/images'));
});

gulp.task('copy:openlayer', function() {
	return gulp.src('lib/OpenLayers/theme/**')
		.pipe(gulp.dest('dist/theme'));
});

gulp.task('copy:data', function() {
	return gulp.src('data/**')
		.pipe(gulp.dest('dist/data'));
});


gulp.task('images', function() {
  return gulp.src('img/**/*')
    .pipe(cache(imagemin({
    	optimizationLevel: 3,
    	progressive: true,
    	interlaced: true
    })))
    .pipe(gulp.dest('dist/images'))
	.pipe(connect.reload());
});

gulp.task('watch', ['serve'], function() {
  gulp.watch(['styles/**'], ['styles']);
  gulp.watch(['img/**'], ['images']);
  gulp.watch(['js/**'], ['scripts']);
  gulp.watch(['*.html'], ['html']);
  gulp.watch(['data/**'], ['copy:data']);
});

gulp.task('build', [
	'html',
	'copy:extjs',
	'copy:openlayer',
	'copy:data',
	'images',
	'libstyles',
	'styles',
	'libscripts',
	'scripts'
]);

gulp.task('default', ['build']);

