
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
                <td style="color: #333; font-size:13px; padding:21px 0px 4px 0px;line-height: 18px;">Hi ${user.firstName},</td>
            </tr>
            <g:if test="${conducted==true}">
            <tr>
                <td style="color: #333;padding:21px 0px 4px 30px;">Since no action items have been added for code review meeting of ${meeting.schedule.project?.name} conducted on <g:formatDate format="dd-MM-yyyy" date="${meeting.scheduleDate}"/> at ${meeting.startTime}, its status 
                has been changed to 'Missed'.</td>
            </tr>
            </g:if>
            
            <g:if test="${conducted==false}">
            <tr>
                <td style="color: #333;padding:21px 0px 4px 30px;">Since code review meeting of ${meeting.schedule.project?.name} has not been conducted as per schedule i.e. on <g:formatDate format="dd-MM-yyyy" date="${meeting.scheduleDate}"/> at ${meeting.startTime}, its status 
                has been changed to 'Missed'.</td>
            </tr>
            </g:if>
            
            <tr>
				<td style="padding-top: 20px;padding-left: 20px;">Thanks</td>
		  </tr>
		   <tr>
				<td style="padding:6px 20px 10px 19px;">Team Oodles</td>
		  </tr>
        
		  <tr>
                <td style="background:#ddd;border-top:5px solid #0099cc;padding:15px 0;text-align:center">
				   Copyright 2009-<g:formatDate format="yyyy" date="${new Date()}"/> OodlesTechnologies. All rights reserved.
                </td>
            </tr>
        </tbody>
    </table>
</body>
</html>

