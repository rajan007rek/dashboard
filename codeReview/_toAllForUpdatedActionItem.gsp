

<html>
<style>
table {
    font-family: arial, sans-serif;
    border-collapse: collapse;
    width: 100%;
}

td, th {
    border: 1px solid #dddddd;
    text-align: left;
    padding: 8px;
}

tr:nth-child(even) {
    background-color: #dddddd;
}
</style>
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
                <td style="color: #333; font-size:13px; padding:21px 0px 4px 15px;line-height: 18px;"> Hi ${personResponsible.firstName},</td>
            </tr>
            <tr>
                <td style="color: #333;padding: 25px 0px 11px 30px;">${currentUser.firstName} ${currentUser.lastName} has changed following fields in action item ${actionItem.actionItemId} added in ${project.name} for meeting
                commenced on <g:formatDate format="dd-MM-yyyy" date="${meeting.scheduleDate}" />.</td>
            </tr>
            <tr>
				<td style="padding: 25px 0px 11px 0px; display:block;"> 
				   <table style="margin:22px; text-align:left;border-collapse: collapse">
                <tr>
                    <th style="border: 1px solid #dddddd;padding: 5px;text-align: left; width:20%">Fields</th>
                    <th style="border: 1px solid #dddddd;padding: 5px;text-align: left; width:20%">Last</th>
                    <th style="border: 1px solid #dddddd;padding: 5px;text-align: left; width:60%">Current</th>
                </tr>
                <g:if test="${changeActionItem.description}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Description</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.description.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.description.current} </td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.comments}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Comments</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.comments.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.comments.current} </td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.dueDate}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Due Date</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.dueDate.previous}</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.dueDate.current}</td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.status}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Status</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.status.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.status.current} </td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.priority}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Priority</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.priority.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.priority.current} </td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.label}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Label</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.label.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.label.current} </td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.artifact}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Artifact</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.artifact.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.artifact.current} </td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.methodName}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Method Name</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.methodName.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.methodName.current} </td>
                    </tr>
                </g:if>
                <g:if test="${changeActionItem.lineNumber}">
                    <tr>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">Line Number</td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.lineNumber.previous} </td>
                        <td style="border: 1px solid #dddddd;padding: 5px;text-align: left;">${changeActionItem.lineNumber.current} </td>
                    </tr>
                </g:if>
                <%--<g:if test="${changeActionItem.personResponsible}">
                    <tr>
                        <td>Person Responsible</td>
                        <td>
                            <g:each var="person" in="${mailMap.personResponsible}">
                                ${person.firstName} ${person.lastName}
                                <br/>
                            </g:each>
                            <g:each var="person" in="${mailMap.previousPersonResponsible}">
                                ${person.firstName} ${person.lastName}
                                <br/>
                            </g:each>
                        </td>
                        <td>
                            <g:each var="person" in="${mailMap.personResponsible}">
                                ${person.firstName} ${person.lastName}
                                <br/>
                            </g:each>
                            <g:each var="person" in="${mailMap.newPersonResponsible}">
                                ${person.firstName} ${person.lastName}
                                <br/>
                            </g:each>
                        </td>
                    </tr>
                </g:if>
            --%></table>
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

