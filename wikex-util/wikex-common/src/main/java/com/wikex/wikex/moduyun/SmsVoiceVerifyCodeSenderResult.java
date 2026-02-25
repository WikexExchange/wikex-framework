package com.wikex.wikex.moduyun;

public class SmsVoiceVerifyCodeSenderResult {

/*
{
    "result": 0, 
    "errmsg": "", 
    "ext": "some msg", 
    "callid": "xxxx" 
}

*/
	public int result;
	public String errmsg;
	public String ext = "";
	public String callid;

	@Override
	public String toString() {
		if (0 == result) {
			return String.format(
					"SmsVoiceVerifyCodeSenderResult\nresult %d\nerrmsg %s\next %s\ncallid %s",
					result, errmsg, ext, callid);
		} else {
			return String.format(
					"SmsVoiceVerifyCodeSenderResult\nresult %d\nerrmsg %s\next %s",
					result, errmsg, ext);
		}
	}
}
