


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
                <td style="color: #333; font-size:13px; padding:21px 0px 4px 0px;line-height: 18px;">Hi ${user?.firstName},</td>
            </tr>
            <tr>
                <td style="color: #333;padding:21px 10px 4px 30px;">Following are the Action Items for code review meeting conducted on
                    <g:formatDate format="dd-MM-yyyy" date="${meeting.scheduleDate}" /> at ${meeting.startTime}</td>
            </tr>
            <tr>
				<td style="padding: 25px 0px 11px 0px; display:block;"> 
				   <table style="width:100%">
                        <tr>
                            <th>S.no</th>
                            <th>Description</th>
                            <th>Due Date</th>
                        </tr>
                        <tr>
                            <td>${actionItem.actionItemId} </td>
                            <td>${actionItem.description} </td>
                            <td>
                                <g:formatDate format="dd-MM-yyyy" date="${actionItem.dueDate}" /> </td>
                        </tr>
                    </table>
				</td>
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

