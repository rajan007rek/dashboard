
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<link rel="stylesheet"
	href="https://fonts.googleapis.com/icon?family=Material+Icons">

</head>
<body>
	<table class="table-border" style="color: #333;">
		<tbody>
			<tr>
				<td style="width: 100%; background: #ddd" style="padding:10px">
					<img
					src="http://www.oodlestechnologies.com/assets/dashboard/front/logo.png">
				</td>
			</tr>
			
			<tr>
				<td style="padding: 20px;">Hi ${user.firstName},
				</td>
			</tr>
			<tr>
				<td style="padding-left: 20px; padding-bottom: 14px;">Following
					are the action items assigned to you during code review meeting of
					project ${meeting.schedule.project?.name} conducted on <g:formatDate
						format="dd-MM-yyyy" date="${meeting.scheduleDate}" /> at ${meeting.startTime}
				</td>
			</tr>
			<tr>
				<td>

					<table class="table table-border">
						<tbody>
							<tr>
								<th>S.no</th>
								<th>Description</th>
								<th>Due Date</th>
							</tr>
							<g:each var="actionItem" in="${actionItemList}">
								<tr>
									<td>
										${actionItem.actionItemId}
									</td>
									<td>
										${actionItem.description}
									</td>
									<td><g:formatDate format="dd-MM-yyyy"
											date="${actionItem.dueDate}" /></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</td>
			</tr>

			<tr>
				<td
					style="background: #ddd; border-top: 5px solid #0099cc; padding: 15px 0; text-align: center">
					Copyright 2009-<g:formatDate format="yyyy" date="${new Date()}" />
					OodlesTechnologies. All rights reserved.
				</td>
			</tr>
		</tbody>
	</table>
</body>
</html>

