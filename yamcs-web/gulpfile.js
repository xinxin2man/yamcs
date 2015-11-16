var angularFilesort = require('gulp-angular-filesort'),
    bowerFiles = require('main-bower-files'),
    concat = require('gulp-concat'),
    del = require('del'),
    gulp = require('gulp'),
    gulpFilter = require('gulp-filter'),
    inject = require('gulp-inject'),
    merge = require('gulp-merge'),
    ngAnnotate = require('gulp-ng-annotate'),
    path = require('path'),
    watch = require('gulp-watch');

gulp.task('clean', function () {
    return del(['build']);
});

// Gathers the main bower files into the build dir
// and concats them in the order defined by main-bower-files
gulp.task('bower-main', ['clean'], function () {
    // Exclude JS because angular-bootstrap only needs bootstrap's css
    var jsFilter = gulpFilter(['**/*.js', '!bootstrap.js'], {restore: true});
    var cssFilter = gulpFilter('**/*.css', {restore: true});
    return gulp.src(bowerFiles())
        .pipe(jsFilter)
        .pipe(concat('vendor.js'))
        .pipe(jsFilter.restore)
        .pipe(cssFilter)
        .pipe(concat('vendor.css'))
        .pipe(cssFilter.restore)
        .pipe(gulpFilter(['*', '!bootstrap.js', '!bootstrap.less', '!**glyphicons**']))
        .pipe(gulp.dest('./build/vendor'));
});

// Copies the referred map files to prevent 404s with an open
// Inspector window (generally not included in bower main files)
gulp.task('bower-map', ['clean'], function () {
    return gulp.src([
        './bower_components/bootstrap/dist/css/bootstrap.css.map',
        './bower_components/dygraphs/dygraph-combined.js.map'
    ]).pipe(gulp.dest('./build/vendor'));
});

gulp.task('bower-fonts', ['clean'], function () {
    return gulp.src(bowerFiles()).pipe(gulpFilter('**glyphicons**')).pipe(gulp.dest('./build/fonts'));
});

gulp.task('bower', ['bower-main', 'bower-map', 'bower-fonts']);

gulp.task('css', ['clean'], function () {
    return gulp.src('./src/app/**/*.css')
        .pipe(gulp.dest('./build/app'));
});

gulp.task('js-uss', ['clean'], function () {
    // Order is important for these
    return gulp.src([
            '**/uss.js',
            '**/uss.basic-widgets.js',
            '**/uss.graph.js',
            '**/uss.meters.js',
            '**/jquery.svg.js',
            '**/sprintf-0.7-beta1.js'
            //'**/highcharts.src.js'
        ])
        .pipe(concat('uss.js'))
        .pipe(gulp.dest('./build/app/uss'));
});

gulp.task('js', ['clean', 'js-uss'], function () {
    return gulp.src(['./src/app/**/*.js', '!./src/app/**/uss/*'])
        .pipe(ngAnnotate())
        .pipe(gulp.dest('./build/app'));
});

gulp.task('html', ['clean'], function () {
    return gulp.src('./src/app/**/*.html')
        .pipe(gulp.dest('./build/app'));
});

gulp.task('img', ['clean'], function () {
    return gulp.src('./src/app/**/*.png')
        .pipe(gulp.dest('./build/app'));
});

// Updates the CSS and JS references defined in the root index.html
gulp.task('index', ['clean', 'bower', 'css', 'js', 'html', 'img'], function () {
    return gulp.src('./src/index.html')
        .pipe(inject(gulp.src('./build/vendor/**/*', {read: false}),
            {ignorePath: '/build', addPrefix: '/_static', name: 'bower'}))
        .pipe(inject(gulp.src('./build/app/**/*.js', {}).pipe(angularFilesort()),
            {ignorePath: '/build', addPrefix: '/_static'}))
        .pipe(inject(gulp.src('./build/app/**/*.css',{read: false}),
            {ignorePath: '/build', addPrefix: '/_static'}))
        .pipe(gulp.dest('./build'));
});

/**
 *
 * Default 'gulp' behaviour
 *
 */
gulp.task('default', ['css', 'js', 'index']);

