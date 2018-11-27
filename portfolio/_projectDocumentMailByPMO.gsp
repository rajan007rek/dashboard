<html>
<body>
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
                <td style="color: #333; font-size:13px; padding: 21px 10px 12px 10px;"> Hi ${managerName},</td>
            </tr>
            <tr>
                <td style="padding:10px;">
                	<table style="width:100%;">
                        <tbody>
                            <tr>
                                <td style="color:#333;font-size:13px;padding: 6px 10px;"> 
                                	<table>
				                        <tbody>
				                            <tr>
				                                <td style="padding: 25px 11px;"> 
				                                	 Following remark has been made by team PMO on <strong>${documentType}</strong> document of project <strong>${projectName}</strong>  
                    									
				                                </td>
				                            </tr>
				                             <tr>
								            	<td style="padding:10px;">
								            		<strong style="font-weight: 800;"> Comment</strong> : ${comment}
								            	</td>
								            </tr>
								           
								            <tr>
								            	<td style="padding:0px 10px 0px 10px;">
								            		<div style="dispaly:block; line-height: 36px;text-align:left;margin-top:10px">
														<a  href="${Aurl}" class="btn-primary" style="background:#5cb85c;color:#ffffff;text-decoration:none;border-radius:0;font-family:Roboto,sans-serif;font-weight:600;padding:5px 12px;font-size:13px" target="_blank">Add Remark</a>
												</div>	
								            	</td>
								            </tr> 
				                        </tbody>
				                    </table>
                                </td>
                            </tr>
                        </tbody>
                    </table>		
				</td>
            </tr>
           <tr>
				<td style="padding-top: 20px;padding-left: 20px;">Thanks</td>
		  </tr>
			<tr>
				<td style="padding:6px 20px 10px 19px;">Team PMO</td>
			</tr>
			
            <tr>
                <td style="background:#ddd;border-top:5px solid #0099cc;padding:15px 0;text-align:center">
                   <p>Copyright 2009-<g:formatDate format="yyyy" date="${new Date()}"/> OodlesTechnologies. All rights reserved.</p>
                </td>
            </tr>
        </tbody>
    </table>
</body>
</html>