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
                <td style="color: #333; font-size:13px; padding: 21px 10px 12px 16px;line-height: 18px;">  Hi ${user.firstName},</td>
            </tr>
            <tr>
				<td style="padding: 25px 9px 11px 30px; display:block;line-height: 18px;"> 
				   <strong style="font-weight:800"> ${currentUser.firstName} ${currentUser.lastName} </strong> has added the Action Item ${actionItem.actionItemId} in ${meeting.schedule.project?.name} for meeting commenced on
                	<g:formatDate format="dd-MM-yyyy" date="${meeting.scheduleDate}" /> at ${meeting.startTime}.
				</td>
			</tr>
           <tr>
				<td style="padding-top: 20px;padding-left: 20px;">Thanks</td>
		  </tr>
		   <tr>
				<td style="padding:6px 20px 10px 19px;">Team Oodles</td>
		  </tr>
		  
		  <tr>
                <td style="color: #333;padding:21px 0px 4px 0px;line-height: 24px;text-align: center;font-weight: 800;font-size: 22px;">Action Items Per User</td>
            </tr>       
		  <tr>
		  
        </tbody>
    </table>
</body>

</html>

