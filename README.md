# JavaDoc regeneration

Check out the repository into two different directories. One needs to be
on master, one needs to be on gh-pages.

    $ javadoc -stylesheetfile ../amu-web/css/javadoc-base.css -nonavbar -bottom '<script src="//www.google.com/js/gweb/analytics/autotrack.js"></script><script>new gweb.analytics.AutoTrack({profile: 'UA-12846745-19',heatMapper: true});</script>' -top '<a class="back" href="javascript:history.back()">Back</a> <a class="back" href="/android-maps-utils/" target="_top">Back to site</a>' -notimestamp -d ../amu-web/javadoc/ $(find library/src/ -name '*.java')
