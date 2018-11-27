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
				   Following Action Items has assigned to you.
				</td>
			</tr>
            <%--<tr>
				<td style="padding: 25px 9px 11px 30px; display:block;line-height: 18px;"> 
				   <strong style="font-weight:800"> ${currentUser.firstName} ${currentUser.lastName} </strong> has added the Action Items to you.
				</td>
			</tr>
           
            --%><tr>
				<td style="padding: 25px 12px 11px 12px; display:block;"> 
				    <table style="margin:0 auto;border-collapse: collapse;background-color: #f9f9f9;">
				    	<thaed>
				    		<tr>
                       			<th style="width:20%;text-align:left;border:1px solid #ccc;padding:6px;"><b>Project Name</b></th>
                       			<th style="width:15%;text-align:left;border:1px solid #ccc;padding:6px;"><b>Action Item ID</b></th>
                       			<th style="width:20%;text-align:left;border:1px solid #ccc;padding:6px;"><b>Person Responsible</b></th>
                       			<th style="width:15%;text-align:left;border:1px solid #ccc;padding:6px;"><b>Description</b></th>
                       			<th style="width:15%;text-align:left;border:1px solid #ccc;padding:6px;"><b>Comments</b></th>
                       			<th style="width:15%;text-align:left;border:1px solid #ccc;padding:6px;"><b>Due Date</b></th>
                   			</tr>
                   			</thaed> 
                   		<g:each var="ai" in="${groupedActionItemsforMailMap}">  
                   			<tbody>    
  						 	<tr>
  						 		<td style="text-align:left;border:1px solid #ccc;padding:6px;">${ai.meeting.schedule.project?.name}</td>
							 	<td style="text-align:left;border:1px solid #ccc;padding:6px;">${ai.actionItemId}</td>
							 	<td style="text-align:left;border:1px solid #ccc;padding:6px;">
							 	<g:each var="pr" in="${ai.personResponsible}">
							 	${pr.firstName} ${pr.lastName}<br>
							 	</g:each>
							 	</td>
							 	<td style="text-align:left;border:1px solid #ccc;padding:6px;">${ai.description}</td>
							 	<td style="text-align:left;border:1px solid #ccc;padding:6px;">${ai.comments}</td>
							 	<td style="text-align:left;border:1px solid #ccc;padding:6px;"><g:formatDate format="dd-MM-yyyy" date="${ai.dueDate}" /></td>
							 	
						 	</tr> 
						 	</tbody> 
			          	</g:each>
		       		</table>
				</td>
			</tr>
		  <tr>
				<td style="padding-top: 20px;padding-left: 20px;">Thanks</td>
		  </tr>
		   <tr>
				<td style="padding:6px 20px 10px 19px;">Team Oodles</td>
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

