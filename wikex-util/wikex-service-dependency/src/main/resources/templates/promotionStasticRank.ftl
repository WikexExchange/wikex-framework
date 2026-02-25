<html>
	<head>
	 <meta charset="UTF-8">
	 <title>System Notification</title>
	</head>
	<style type="text/css">
	    table.gridtable {
	        font-family: verdana,arial,sans-serif;
	        font-size:11px;
	        color:#333333;
	        border-width: 1px;
	        border-color: #666666;
	        border-collapse: collapse;
	        width: 100%;
	    }
	    table.gridtable tr th {
	        border-width: 1px;
	        padding: 8px;
	        border-style: solid;
	        border-color: #666666;
	        background-color: #dedede;
	    }
	    table.gridtable td {
	        border-width: 1px !important;
	        padding: 8px;
	        border-style: solid !important;
	        border-color: #666666 !important;
	        background-color: #ffffff;
	        width: 25%;
	        text-align: center;
	    }
	</style>
	<body>
		<h3 style="text-align:center;">Invitation Ranking Report</h3>
	    <hr>
	    <div>
		    <h4>Top 20 by Number of Invitations:</h4>
		    <table class="gridtable">
		    	<tr><th>Rank</th><th>User</th><th>Number of Invitations</th><th>Robot</th></tr>
		    	<#list topInviteList as vo>
		    		<tr>
		    			<td>${vo_index+1}</td>
		    			<td>${vo.userIdentify}</td>
		    			<td>${vo.levelOne}</td>
		    			<#if vo.isRobot==0>
				        	<td>No</td>
				        <#else>
				        	<td style="color:#FF0000;">Yes</td>
				        </#if>
		    		</tr>
				</#list>
		    </table>
		</div>
		<br/><br/>
	</body>
</html>
