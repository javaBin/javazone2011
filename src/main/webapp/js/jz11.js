(function($) {

  $.fn.randomize = function(childElem) {
    return this.each(function() {
      var $this = $(this);
      var elems = $this.children(childElem);

      elems.sort(function() { return (Math.round(Math.random())-0.5); });

      $this.remove(childElem);

      for(var i=0; i < elems.length; i++)
      $this.append(elems[i]);

    });
  }
})(jQuery);


/* disqus */
var disqus_developer = 0; // Set to 1 for local debugging

(function() {
  var dsq = document.createElement('script');
  dsq.type = 'text/javascript';
  dsq.async = true;
  dsq.src = 'http://javazone.disqus.com/embed.js';
  (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
})();

(function() {
  var links = document.getElementsByTagName('a');
  var query = '?';
  for (var i = 0; i < links.length; i++) {
    if (links[i].href.indexOf('#disqus_thread') >= 0) {
      query += 'url' + i + '=' + encodeURIComponent(links[i].href) + '&';
    }
  }
  var gnr = document.createElement('script');
  gnr.type = 'text/javascript';
  gnr.async = true;
  gnr.src = 'http://disqus.com/forums/javazone/get_num_replies.js' + query;
  var s = document.getElementsByTagName('script')[0];
  s.parentNode.insertBefore(gnr, s);
})();

/* google analytics */

var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-3676724-4']);
_gaq.push(['_trackPageview']);

(function() {
  var ga = document.createElement('script');
  ga.type = 'text/javascript';
  ga.async = true;
  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
  var s = document.getElementsByTagName('script')[0];
  s.parentNode.insertBefore(ga, s);
})();

/* Dependents on jQuery */

$(document).ready(function() {

  $(".person").tooltip({
    bodyHandler: function() {
      return $(this).find(".popup").html();
    },
    extraClass: "pretty",
    showURL: false
  });

  $("#people").randomize(".person");


  /* jQuery twitter */
  /* jQuery lightbox */

            $("#twitter").getTwitter({
                        userName: "javazone",
                        numTweets: 5,
                        loaderText: "Loading tweets...",
                        slideIn: true,
                        slideDuration: 750,
                        showHeading: false,
                        headingText: "Latest Tweets",
                        showProfileLink: true,
                        showTimestamp: true
                    });
            $(function() {
    $('a.lightbox').lightBox(); // Select all links with lightbox class
  });

  /* Set current page active in menu */
  $('#mainmenu a').each(function () {
    if (window.location.href.indexOf($(this).attr('href')) > 0) {
      $(this).parent().addClass('active');
    }
  });


  /* Sort partner logos */
  var partnerLogos = $("#partner-logos");
  var elems = partnerLogos.children("a");

  if(elems.size() > 0) {
    elems.sort(function() { return (Math.round(Math.random())-0.5); });

    partnerLogos.children().remove();

    for (var i = 0; i < elems.length; i++) {
      partnerLogos.append(elems[i]);
    }
  }

  /* dumb banner carousel*/
  $('#prev').click(function() {
    var slides = $('ol.images');
    var prev = slides.find('li.prev');
    var curr = slides.find('li.curr');
    var next = slides.find('li.next');
    prev.fadeOut().removeClass().addClass('curr').fadeIn();
    curr.fadeOut().removeClass().addClass('next').fadeIn();
    next.fadeOut().removeClass().addClass('prev').fadeIn();
  });

  $('#next').click(function() {
    var slides = $('#banner-content');
    var prev = slides.find('li.prev');
    var curr = slides.find('li.curr');
    var next = slides.find('li.next');
    prev.fadeOut().removeClass().addClass('next').fadeIn();
    curr.fadeOut().removeClass().addClass('prev').fadeIn();
    next.fadeOut().removeClass().addClass('curr').fadeIn();
  });

});

