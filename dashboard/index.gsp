<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" ng-app="oodlesApp" class="no-js">

<head>

    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
    <asset:stylesheet src="dashboard/dashboard.css" />
     <asset:javascript src="dashboard/dashboard.js" />
     <asset:javascript src="common/mordnizr.js" />
     <script type="text/javascript" src='assets/site/tinymce/tinymce.min.js'></script>
     <script type="text/javascript" src='assets/site/tinymce/tinymce.js'></script>
     
    
    <title>Oodles Technologies</title>
</head>

<script type="text/javascript">
_linkedin_data_partner_id = "75197";
</script><script type="text/javascript">
(function(){var s = document.getElementsByTagName("script")[0];
var b = document.createElement("script");
b.type = "text/javascript";b.async = true;
b.src = "https://snap.licdn.com/li.lms-analytics/insight.min.js";
s.parentNode.insertBefore(b, s);})();
</script>
<noscript>
<img height="1" width="1" style="display:none;" alt="" src="https://dc.ads.linkedin.com/collect/?pid=75197&fmt=gif" />
</noscript>

<body>
    <div class="sticky-wrapper">
        <div class="sticky-wrapperIn">
            <div class="content-wrapper">
                <g:render template="/layouts/navigation"></g:render>
                <div ng-view></div>
            </div>
        </div>
    </div>
    <g:render template="/layouts/dashboard-footer" />
</body>

</html>