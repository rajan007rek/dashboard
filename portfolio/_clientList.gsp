
<div id="container" class=" portfolio clearfix height420px">
	<g:each in="${categoryList}" var="category" status="i">
		<category:getProjectLists categoryid="${category?.id}">
			<g:each in="${projects}" var="p">
				<category:getProjectDetails projectId="${p?.id}">
					<div class="element ${i}" data-category="${i}">
						<div class="isotope-block">
							<a class="group1"
								href="${'/'+'portfolio'+category.uri}/${projectDetails?.projectUrl}">
								<span class="user_img_cont"> <img
									src="${projectDetails.logoUrl}" alt="clientlogo" />
							</span>
								<div class="mask">
									<div>
										<span>Technologies</span>
										<g:each in="${projectDetails.technologies}" var="techName">
											${techName+" "}
										</g:each>
									</div>
								</div>
							</a>
						</div>
					</div>
				</category:getProjectDetails>
			</g:each>
		</category:getProjectLists>
	</g:each>
</div>
<div class="black-block-bg">
	<div class="container">
		<div class="row">
			<div class="span12">
				<h3>What Our Clients Say</h3>
			</div>
		</div>
		<div class="row">
			<div class="blockquote-block span12">
				<div class="blockquote_carousel responsive clearfix">
					<ul id="foo" class="clearfix">
						<li>
							<blockquote class="clearfix">
								<span class="blockquote-author"><strong>Michael
										Beddows</strong>Founder , Trendr LLC (USA)</span> <span class="extra-wrap">It's
									been a real pleasure working with Oodles. Great problem solvers
									and very responsive, I couldn't have wished for a better team
									to work with.</span>
							</blockquote>
						</li>
						<li>
							<blockquote class="clearfix">
								<span class="blockquote-author"><strong>Jeff
										Bonnes</strong>Gameshape Pty Ltd (Australia)</span> <span class="extra-wrap">Oodles
									are Grails experts and easy to work with. I use them all the
									time and am never disappointed. Highly recommended.</span>
							</blockquote>
						</li>
						<li>
							<blockquote class="clearfix">
								<span class="blockquote-author"><strong>Fabio
										Cardenas</strong>The Sundown Group (USA)</span> <span class="extra-wrap">“Very
									knowledgeable and trustworthy. Completed project on time and on
									budget. Will do business with Oodles team again.</span>
							</blockquote>
						</li>
						<li>
							<blockquote class="clearfix">
								<span class="blockquote-author"><strong>Derin
										Adeyokunnu</strong>VAGE TV (USA)</span> <span class="extra-wrap">“Great
									team. Thorough understanding of the project and it's
									requirements. Will work with Oodles again.</span>
							</blockquote>
						</li>
						<li>
							<blockquote class="clearfix">
								<span class="blockquote-author"> <strong>
										LyfeLine Co </strong>LyfeLine Co USA
								</span><span class="extra-wrap">"I have worked with Oodles
									Technologies for over two years. During this time we have
									released several apps, including Lyfeline Milestones
									that we made available for both Android and iOS users." </span>
							</blockquote>
						</li>
					</ul>
					<a id="next" class="prev" href="#"></a> <a id="prev" class="next"
						href="#"></a>
				</div>
			</div>
		</div>
	</div>
</div>
<div class="white-block-bg client-manage">
	<div class="container">
		<div class="row">
			<div class="span12">
				<h3>Our Clients</h3>
			</div>
		</div>
		<div class="row">
			<div class="span12">
				<div class="client_carousel responsive clearfix">
					<ul id="foo2" class="clearfix">

						<g:each in="${clients}" var="clnt">
							<li><a target="_blank" title="${clnt.name}"
								href="${clnt.website}"><image src="${clnt.logoUrl}"
										alt="${clnt.name}" class="img-responsive" /> </a></li>
						</g:each>
					</ul>
					<a id="prev2" class="prev2 icon-left-thin" href="#"></a> 
					<a id="next2" class="next2 icon-right-thin" href="#"></a>
				</div>
			</div>
		</div>
	</div>
</div>
<div class="fix-menu"></div>
</div>
<script>
	//<![CDATA[
	$(window).load(function() {
		$("body").addClass("blue");
		"use strict";
		$('#contact-form').forms({})
	})
	//]]>
	$(document).ready(function() {
		$('.tooltipster').tooltipster({
			interactive : true
		});
		$(".workHighlighted").addClass("current");
	});
</script>
<script>
	//<![CDATA[
	$(function() {

		$('#foo').carouFredSel({

			auto : true,
			responsive : false,
			width : '100%',
			prev : {
				button : '#prev',
				key : null,
				items : null, //  scroll.items
				fx : 'crossfade', //  scroll.fx
				easing : 'swing', //  scroll.easing
				duration : 500, //  scroll.duration
				pauseOnHover : null, //  scroll.pauseOnHover
				queue : null, //  scroll.queue
				event : 'click', //  scroll.event
				conditions : null, //  scroll.conditions
				onBefore : null, //  scroll.onBefore
				onAfter : null, //  scroll.onAfter
				onEnd : null
			//  scroll.onEnd

			},
			next : {
				button : '#next',
				key : null,
				items : null, //  scroll.items
				fx : 'crossfade', //  scroll.fx
				easing : 'swing', //  scroll.easing
				duration : 500, //  scroll.duration
				pauseOnHover : null, //  scroll.pauseOnHover
				queue : null, //  scroll.queue
				event : 'click', //  scroll.event
				conditions : null, //  scroll.conditions
				onBefore : null, //  scroll.onBefore
				onAfter : null, //  scroll.onAfter
				onEnd : null
			//  scroll.onEnd

			},
			scroll : 1,
			transition : true,
			items : {
				height : 'auto',
				width : 'auto',
				visible : {
					min : 1,
					max : 1
				}

			},
			mousewheel : true,
			swipe : {
				onMouse : true,
				onTouch : true
			}
		});

		$('#foo2').carouFredSel({
			auto : true,
			responsive : false,
			width : '100%',
			circular : true,
			infinite : true,
			prev : '#prev2',
			next : '#next2',
			scroll : 1,
			items : {
				height : 'auto',
				width : 'auto',
				visible : {
					min : 2,
					max : 6
				}
			},

			mousewheel : true,
			swipe : {
				onMouse : true,
				onTouch : true
			}
		});
	});
	//]]>
</script>