<!DOCTYPE html>
<html lang="en">

<head>
	<!-- Start Alexa Certify Javascript -->
	<script type="text/javascript">
	_atrk_opts = { atrk_acct:"oqENn1QolK10fn", domain:"oodlestechnologies.com",dynamic: true};
	(function() { var as = document.createElement('script'); as.type = 'text/javascript'; as.async = true; as.src = "https://d31qbv1cthcecs.cloudfront.net/atrk.js"; var s = document.getElementsByTagName('script')[0];s.parentNode.insertBefore(as, s); })();
	</script>
	<noscript><img src="https://d5nxst8fruw4z.cloudfront.net/atrk.gif?account=oqENn1QolK10fn" style="display:none" height="1" width="1" alt="" /></noscript>
	<!-- End Alexa Certify Javascript -->
	
    <meta name="layout" content="main">
    <title>Oodles Technologies | SaaS Applications</title>
    <meta http-equiv="Content-Type" content="text/html" charset="utf-8" />
    <meta name="robots" content="index/follow" />
    <meta name="googlebot" content="noodp" />
    <meta http-equiv=" content-language" content="EN">
    <meta name="description" content="We are pioneers in executing SaaS Development Projects with deft expertise in groundbreaking technologies like AngularJs, NodeJS, HTML5 and jQuery.">
    <meta name="keywords" content="Web development portfolio, mobile apps development, android application development, Grails, SaaS Services, Bigdata Services and Video Streaming.">
    <!--[if lt IE 8]>
  		<div style='text-align:center'><a href="http://www.microsoft.com/windows/internet-explorer/default.aspx?ocid=ie6_countdown_bannercode"><img src="http://www.theie6countdown.com/img/upgrade.jpg"border="0"alt=""/></a></div>  
 	<![endif]-->
    <!--[if lt IE 9]>
		<link rel="stylesheet" href="common/ie.css" type="text/css" media="screen">
		<script src="dashboard/html5shiv.js"></script>
	<![endif]-->
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0">
	
</head>

<body class="blue">
    <div id="work">
        <div class="title-bg">
            <div class="container">
                <div class="row">
                    <div class="span12">
                        <h1>Portfolio - SaaS Applications</h1>
                    </div>
                </div>
            </div>
        </div>
        <div class="container">
            <div class="row">
                <div class="span12">
                    <div id="options">
                        <ul id="filters" class="option-set clearfix" >
                            <g:each in="${categoryList}" var="category">
                            	 <li><a href="${url+'/portfolio'+category.uri}" class="${category?.uri.equals('/'+categorySelected)?'selected':'' }">${category?.name}</a>
								 </li>
							</g:each>
						</ul>
                    </div>
                </div>
            </div>
        </div>
        <!-- Portfolio List-->
        <g:render template="/portfolio/clientList" />

        <script>
            //<![CDATA[
            $(window).load(function() {

                    /*Flexslider setup*/
                    $('.flexslider').flexslider({
                        animation: "slide",
                        animationLoop: false,
                        slideshow: false,
                        itemWidth: 635,
                        itemMargin: 0,
                        move: 1,
                        minItems: 1,
                        maxItems: 3,
                        start: function() {}
                    });
                    $('.flexslider1').flexslider({
                        animation: "fade",
                        controlNav: false,
                        slideshowSpeed: 3000
                    });

                    /*Izotope*/
                    var jQuerycontainer = jQuery('.portfolio');
                    //Run to initialise column sizes
                    updateSize();
                    //Load masonry when images all loaded
                    jQuerycontainer.imagesLoaded(function() {
                        jQuerycontainer.isotope({
                            // options
                            itemSelector: '.element',
                            filter: '.0',
                            layoutMode: 'masonry',
                            transformsEnabled: false,
                            columnWidth: function(containerWidth) {
                                containerWidth = jQuerybrowserWidth;
                                return Math.floor(containerWidth / jQuerycols);
                            }
                        });
                    });
                    // update columnWidth on window resize
                    jQuery(window).smartresize(function() {
                        updateSize();
                        jQuerycontainer.isotope('reLayout');
                    });
                    //Set item size
                    function updateSize() {
                        jQuerybrowserWidth = jQuerycontainer.width();
                        jQuerycols = 6;
                        if (jQuerybrowserWidth >= 1300) {
                            jQuerycols = 6;
                        } else if (jQuerybrowserWidth >= 1080 && jQuerybrowserWidth < 1300) {
                            jQuerycols = 5;
                        } else if (jQuerybrowserWidth >= 870 && jQuerybrowserWidth < 1080) {
                            jQuerycols = 4;
                        } else if (jQuerybrowserWidth >= 660 && jQuerybrowserWidth < 870) {
                            jQuerycols = 3;
                        } else if (jQuerybrowserWidth >= 450 && jQuerybrowserWidth < 660) {
                            jQuerycols = 2;
                        } else if (jQuerybrowserWidth < 450) {
                            jQuerycols = 1;
                        }
                        //console.log("Browser width is:" + jQuerybrowserWidth);
                        //console.log("Cols is:" + jQuerycols);

                        // jQuerygutterTotal = jQuerycols * 20;
                        jQuerybrowserWidth = jQuerybrowserWidth; // - jQuerygutterTotal;
                        jQueryitemWidth = jQuerybrowserWidth / jQuerycols;
                        jQueryitemWidth = Math.floor(jQueryitemWidth);
                        jQuery(".element").each(function(index) {
                            jQuery(this).css({
                                "width": jQueryitemWidth + "px"
                            });
                        });
                        
                    };
                })
                //]]>
        </script>
        <script>
            $("#navigation").addClass("position-fix");
        </script>
</body>

</html>