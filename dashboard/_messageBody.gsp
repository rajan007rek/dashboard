<%--<html>

<body>
    <div style="width: 542px; height: 388px; background: #fff; border: 10px solid #c6c1c1;">
        <div style="width: 100%; height: 69px; background: #f2f2f2; margin: 0 auto;">
            <img width="284" height="54" src="http://www.oodlestechnologies.com/assets/dashboard/front/logo.png" style="float: left; margin: 7px 0 0 10px;" />
        </div>
        <div style="width: 476px; height: 225px; margin: 18px auto 0 auto; background: #f2f2f2;">
            <div style="width: 77px; height: 128px; float: left;">
                <img width="48" height="48" src="" style="float: left; margin: 18px 0 0 10px;" alt="user" />
            </div>
            <p style="font-size: 14px; color: #000; float: left; display: block; width: 390px; line-height: 22px;">
                Hi ${email},
                <br />
                <br /> ${message}
                <br /> <a href="${url}/dashboard" style="display: inline-block; margin-top: 18px; border-radius: 4px; padding: 10px 15px; background: #3c91d1; text-decoration: none; float: left; font-size: 18px; color: #fff; margin-left: 10px;">Login</a>
                <br>
                <br />
                <br />
                <br /> Thanks
                <br /> Team Oodles
                <br />
            </p>
        </div>
        <div style="font-size: 11px; color: #5d5d5d; text-align: center; padding-top: 20px; background: #f2f2f2;">
            If you need assistance or have questions,please contact at info@oodlestechnologies.com
            <p>Copyright 2009-2016 OodlesTechnologies. All rights reserved.</p>
        </div>
    </div>
</body>

</html>

--%><html>
<body>
   <div>
   <table style="width:70%;border:1px solid #ddd">
        <tbody>
            <tr>
                <td>
                    <table style="width:100%;background:#ddd">
                        <tbody>
                            <tr>
                                <td style="padding:10px"><img src="http://www.oodlestechnologies.com/assets/dashboard/front/logo.png">
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
             <tr>
                <td style="color:#333;font-size:13px;padding:6px 10px"> Hi ${email}, </td>
            </tr>
            <tr>
                <td style="padding:10px">
                	<table style="width:100%">
                        <tbody>
                            <tr>
				                 <td style="padding:25px 9px 11px 10px"> 
				                      ${message}
				                 </td>
				             </tr>
							<tr>            	
							<td style="padding:0px 10px 0px 10px">      		
									<a href="${url}/dashboard" style="background:#3c91d1;color:#ffffff;text-decoration:none;border-radius:3px;font-family:Roboto,sans-serif;font-weight:600;padding:5px 12px;font-size:13px">
									Login</a>
													
								</td>
							</tr>  
				              
                        </tbody>
                    </table>		
				</td>
            </tr>
            <tr>
				<td style="line-height:0px;padding:10px">Thanks</td>
			</tr>
			<tr>
				<td style="line-height:0px;padding:10px">Team Oodles</td>
			</tr>
			
            <tr>
                <td style="background:#ddd;border-top:5px solid #0099cc;padding:15px 0;text-align:center">
                   If you need assistance or have questions,please contact at <a href="mailto:hr@oodlestechnologies.com" target="_blank">hr@oodlestechnologies.com</a>
				   <p>Copyright 2009-<g:formatDate format="yyyy" date="${new Date()}"/> OodlesTechnologies. All rights reserved.</p>
                </td>
            </tr>
        </tbody>
    </table>
</div>
</body>
</html>