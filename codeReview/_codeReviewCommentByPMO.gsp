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
                <td style="color: #333; font-size:13px; padding: 21px 10px 12px 16px;line-height: 18px;">  Hi ${userName},</td>
            </tr>
            <tr>
				<td style="padding: 25px 9px 11px 30px; display:block;line-height: 18px;"> 
				  Following comment has been made by the PMO for the code review meeting for <strong>"${projectName}"</strong> scheduled on <strong>${scheduleDate}</strong> at <strong>${startTime}</strong>.
				</td>
			</tr>
			 <tr>
				<td style="padding: 25px 9px 11px 30px; display:block;line-height: 18px;"> 
				  <strong>Comment :-</strong> ${comment}
				</td>
			</tr>
            <tr>
				<td style="padding: 25px 9px 11px 30px; display:block;line-height: 18px;"> 
				  
				</td>
			</tr>
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

