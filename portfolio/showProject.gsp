<!DOCTYPE html>
<html lang="en">

<head>
    <meta name="layout" content="main">
    <g:if test="${categoryId=='1'}">
    <title>
        ${projectList?.name} | Mobile Applications Development Project
    </title>
    </g:if>
    <g:if test="${categoryId=='2'}">
    <title>
        ${projectList?.name} | Video Streaming Project
    </title>
    </g:if>
    <g:if test="${categoryId=='3'}">
    <title>
        ${projectList?.name} | SaaS Applications Development Project
    </title>
    </g:if>
    <g:if test="${categoryId=='4'}">
    <title>
        ${projectList?.name} | BigData and NoSQL Project
    </title>
    </g:if>
    <g:if test="${categoryId=='5'}">
    <title>
        ${projectList?.name} | Blockchain Development Project
    </title>
    </g:if>
    <g:if test="${projectList?.canonicalUrl!=null}">
   		<link rel="canonical" href="${projectList?.canonicalUrl}"/>
   	</g:if>
    <meta http-equiv="Content-Type" content="text/html" charset="utf-8" />
    <meta name="robots" content="index/follow" />
    <meta name="googlebot" content="noodp" />
    <meta http-equiv=" content-language" content="EN">
    <meta name="description" content="${projectList?.description }" />
    <meta name="keywords" content="${projectList?.keywords?:"Titanium Appcelerator, Grails Portfolio , Mobile App Development, Titanium App Development"}" />
    <!--[if lt IE 8]>
  		<div style='text-align:center'><a href="http://www.microsoft.com/windows/internet-explorer/default.aspx?ocid=ie6_countdown_bannercode"><img src="http://www.theie6countdown.com/img/upgrade.jpg"border="0"alt=""/></a></div>  
 	<![endif]-->
    <!--[if lt IE 9]>
		<link rel="stylesheet" href="common/ie.css" type="text/css" media="screen">
		<script src="dashboard/html5shiv.js"></script>
	<![endif]-->
    <script>
    $(window).load(function()
		   {
            $(".workHighlighted").addClass("current");
		   });
    </script>
</head>

<body class="blue">

    <div class="sub-page-bg">
        <div class="title-bg">
            <div class="container">
                <div class="row">
                    <div class="span12">
                        <h2>
							${projectList?.name}
						</h2>
                    </div>
                </div>
            </div>
        </div>
        <div class="container">
            <div class="row">
                <div class="span12">
                    <div class="work-carousel none">
                    <div>                    
						<div class="category-list">
							<ul>
					  			<li><a href="${url}" class="btn-projects icon-align-justify">Portfolio</a>
					    		<ul>
					      			<li><a href="${url+'/saasApplications'}">Saas Applications</a></li>
					      			<li><a href="${url+'/videoStreaming'}">Video Streaming Services</a></li>
								    <li><a href="${url+'/mobileApplications'}">Mobile Applications</a></li> 
								    <li><a href="${url+'/bigDataAndNoSQL'}">BigData & NoSql</a></li>
								    <li><a href="${url+'/blockchainDevelopment'}">Blockchain Development</a></li>            
					        	</ul>
					      		</li>
					    	</ul>
						</div>                    
                    </div>
                    <div class="clearfix"></div>
                        <div class="carousel-relative">
                            <g:if test="${projectList?.image}">
                                <div class="carousel-2">
                                    <div class="flexslider2 carousel">
                                        <ul class="slides">
                                            <g:each in="${projectList?.image}" var="image">
                                                <li><img src="${image}" />
                                                </li>
                                            </g:each>
                                        </ul>
                                    </div>
                                </div>
                            </g:if> 
                            <g:if test="${projectList1.size()>1}">
                            <g:if test="${prv}">
				                <a href="${grailsApplication.config.grails.portfolioURL}/${category}/${prv.projectUrl}" class="prev"><span>Prev Project</span></a>
				            </g:if>
				            <g:else>
				                <a href="#" class="prev"><span>Prev Project</span></a>
				            </g:else>
				            <g:if test="${nxt}">
				                 <a href="${grailsApplication.config.grails.portfolioURL}/${category}/${nxt.projectUrl}" class="next"><span>Next Project</span></a>
				            </g:if>
				            <g:else>
				                 <a href="#" class="next"><span>Next Project</span></a>
				            </g:else> 
				       	</g:if>
						</div>
                    </div>
                </div>
            </div>
            
            <div class="row">
                <div class="span9 work-manage">
                    <dl class="dl-horizontal mediacastalign">
                        <dt>
							Our Client Name <small>:</small>
						</dt>
                        <dd>
                            ${projectList?.clientName}
                        </dd>
                        <g:if test="${projectList?.websitesUrl && !projectList?.websitesUrl.isEmpty() && !projectList?.websitesUrl.trim().equalsIgnoreCase("null")}">
                            <dt>
								Website Url <small>:</small>
							</dt>
                            <dd>
                                <a href="${projectList.websitesUrl}" target="_blank">
									${projectList?.websitesUrl}
								</a>
                            </dd>
                        </g:if>
                        <g:if test="${projectList?.googlePlayUrl && !projectList?.googlePlayUrl.isEmpty() && !projectList?.googlePlayUrl.trim().equalsIgnoreCase("null")}">
                            <dt>
								Google Play Url <small>:</small>
							</dt>
                            <dd>
                                <a href="${projectList.googlePlayUrl}" target="_blank">
									${projectList?.googlePlayUrl}
								</a>
                            </dd>
                        </g:if>
                        <g:if test="${projectList?.itunesUrl && !projectList?.itunesUrl.isEmpty() &&  !projectList?.itunesUrl.trim().equalsIgnoreCase("null")}">
                            <dt>
								ITunes Url <small>:</small>
							</dt>
                            <dd>
                                <a href="${projectList.itunesUrl}" target="_blank">
									${projectList?.itunesUrl}
								</a>
                            </dd>
                        </g:if>
                        <dt>
							Description<small>:</small>
						</dt>
                        <dd>
                            ${projectList?.description}
                        </dd>


                         <sec:ifAnyGranted roles="ROLE_ADMIN">

                            <dt>
								Project Manager<small>:</small>
							</dt>
                            <dd>
                                ${projectList?.projectManager}
                            </dd>
                            <dt>
								Project Lead<small>:</small>
							</dt>
                            <dd>
                                ${projectList?.teamLead}
                            </dd>
                            <dt>
								Team Members<small>:</small>
							</dt>
                            <dd>
                                    <g:if test="${projectList?.value=="more"}">
                                        <g:each in="${projectList?.member}" var="team">
                                            <li>
                                                ${team}
                                            </li>
                                        </g:each>
                                    </g:if>
                                    <g:if test="${projectList?.value=="one"}">
                                        <g:each in="${projectList?.member}" var="team">
                                            <dd>
                                                ${team}
                                            </dd>
                                        </g:each>
                                    </g:if>

                            </dd>
                         </sec:ifAnyGranted>
                    </dl>
                </div>
                <div class="span3 padding-bottom">
                    <h4>Technologies</h4>
                    <ul class="list">
                        <g:each in="${projectList?.technologies}" var="tech">
                            <li>
                                ${tech.name}
                            </li>
                        </g:each>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <script>
        //<![CDATA[
        $(window).load(function() {
                $("body").addClass("blue");
                // Flexslider setup
                $('.flexslider2').flexslider({
                    animation: "slide",
                    animationLoop: false,
                    controlNav: true,
                    slideshow: false,
                    itemWidth: '100%',
                    itemMargin: 0,
                    move: 1,
                    minItems: 1,
                    maxItems: 1,
                    start: function() {}
                });
            })
            //]]>
       
    </script>
    <!-- Google map -->
        <script>
        $("#navigation").addClass("position-fix");

    </script>
</body>

</html>