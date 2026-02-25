<html>
<body>
<div>
    <h3>Welcome to ${name}!</h3><br/>
    <br/>Your account has been successfully created. Please click the link below to activate your account:<br/>
    <br/><a href="${host}/uc/register/active?key=${token}">Activate Account</a><br/>
    <br/>If you cannot click the link, or if the page takes too long to load, please copy the following URL and paste it into your browser:<br/>
    ${host}/uc/register/active?key=${token}
    <br/>Please keep this email safe.<br/>
    Your account details are as follows:<br/><br/>----------------------------<br/>
    Username: ${username}<br/>----------------------------<br/><br/>
    &nbsp;If you forget your password, you can reset it through the "Forgot Password" link on the login page.<br/><br/>
    <br/>${name} grows together with you. Thank you for registering!<br/>
    <br/>
</div>
</body>
</html>
