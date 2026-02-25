package com.wikex.wikex.user.system;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class GeetestLib {

	protected final String verName = "4.0";
	protected final String sdkLang = "java";

	protected final String apiUrl = "https://gcaptcha4.geetest.com";

	protected final String registerUrl = "/register";
	protected final String validateUrl = "/validate";

	protected final String json_format = "1";

	public static final String fn_geetest_challenge = "geetest_challenge";

	public static final String fn_geetest_validate = "geetest_validate";

	public static final String fn_geetest_seccode = "geetest_seccode";

	private String captchaId = "ebd15c83cb7daecc963bc887b2c9cd76";

	private String privateKey = "26e11b2e8eda2ccbcf060756f2b845fd";

	private boolean newFailback = false;

	private String responseStr = "";

	public boolean debugCode = true;

	public String gtServerStatusSessionKey = "gt_server_status";

	public GeetestLib(String captchaId, String privateKey, boolean newFailback) {

		this.captchaId = captchaId;
		this.privateKey = privateKey;
		this.newFailback = newFailback;
	}

	public String getResponseStr() {

		return responseStr;

	}

	public String getVersionInfo() {

		return verName;

	}

	private String getFailPreProcessRes() {

		Long rnd1 = Math.round(Math.random() * 100);
		Long rnd2 = Math.round(Math.random() * 100);
		String md5Str1 = md5Encode(rnd1 + "");
		String md5Str2 = md5Encode(rnd2 + "");
		String challenge = md5Str1 + md5Str2.substring(0, 2);

		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject.put("success", 0);
			jsonObject.put("gt", this.captchaId);
			jsonObject.put("challenge", challenge);
			jsonObject.put("new_captcha", this.newFailback);

		} catch (JSONException e) {

			gtlog("json dumps error");

		}

		return jsonObject.toString();

	}

	private String getSuccessPreProcessRes(String challenge) {

		gtlog("challenge:" + challenge);

		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject.put("success", 1);
			jsonObject.put("gt", this.captchaId);
			jsonObject.put("challenge", challenge);

		} catch (JSONException e) {

			gtlog("json dumps error");

		}

		return jsonObject.toString();

	}

	public int preProcess(HashMap<String, String> data) {

		if (registerChallenge(data) != 1) {

			this.responseStr = this.getFailPreProcessRes();
			return 0;

		}

		return 1;

	}

	private int registerChallenge(HashMap<String, String> data) {

		try {
			String userId = data.get("user_id");
			String clientType = data.get("client_type");
			String ipAddress = data.get("ip_address");

			String getUrl = apiUrl + registerUrl + "?";
			String param = "gt=" + this.captchaId + "&json_format=" + this.json_format;

			if (userId != null) {
				param = param + "&user_id=" + userId;
			}
			if (clientType != null) {
				param = param + "&client_type=" + clientType;
			}
			if (ipAddress != null) {
				param = param + "&ip_address=" + ipAddress;
			}

			gtlog("GET_URL:" + getUrl + param);
			String result_str = readContentFromGet(getUrl + param);
			if (result_str == "fail") {

				gtlog("gtServer register challenge failed");
				return 0;

			}

			gtlog("result:" + result_str);
			JSONObject jsonObject = JSONObject.parseObject(result_str);
			String return_challenge = jsonObject.getString("challenge");

			gtlog("return_challenge:" + return_challenge);

			if (return_challenge.length() == 32) {

				this.responseStr = this.getSuccessPreProcessRes(this.md5Encode(return_challenge + this.privateKey));

				return 1;

			} else {

				gtlog("gtServer register challenge error");

				return 0;

			}
		} catch (Exception e) {

			gtlog(e.toString());
			gtlog("exception:register api");

		}
		return 0;
	}

	protected boolean objIsEmpty(Object gtObj) {

		if (gtObj == null) {

			return true;

		}

		if (gtObj.toString().trim().length() == 0) {

			return true;

		}

		return false;
	}

	private boolean resquestIsLegal(String challenge, String validate, String seccode) {

		if (objIsEmpty(challenge)) {

			return false;

		}

		if (objIsEmpty(validate)) {

			return false;

		}

		if (objIsEmpty(seccode)) {

			return false;

		}

		return true;
	}

	public int enhencedValidateRequest(String challenge, String validate, String seccode,
			HashMap<String, String> data) {

		if (!resquestIsLegal(challenge, validate, seccode)) {

			return 0;

		}

		gtlog("request legitimate");

		String userId = data.get("user_id");
		String clientType = data.get("client_type");
		String ipAddress = data.get("ip_address");

		String postUrl = this.apiUrl + this.validateUrl;
		String param = String.format("challenge=%s&validate=%s&seccode=%s&json_format=%s",
				challenge, validate, seccode, this.json_format);

		if (userId != null) {
			param = param + "&user_id=" + userId;
		}
		if (clientType != null) {
			param = param + "&client_type=" + clientType;
		}
		if (ipAddress != null) {
			param = param + "&ip_address=" + ipAddress;
		}

		gtlog("param:" + param);

		String response = "";
		try {

			if (validate.length() <= 0) {

				return 0;

			}

			if (!checkResultByPrivate(challenge, validate)) {

				return 0;

			}

			gtlog("checkResultByPrivate");

			response = readContentFromPost(postUrl, param);

			gtlog("response: " + response);

		} catch (Exception e) {

			e.printStackTrace();

		}

		String return_seccode = "";

		try {

			JSONObject return_map = JSONObject.parseObject(response);
			return_seccode = return_map.getString("seccode");
			gtlog("md5: " + md5Encode(return_seccode));

			if (return_seccode.equals(md5Encode(seccode))) {

				return 1;

			} else {

				return 0;

			}

		} catch (JSONException e) {

			gtlog("json load error");
			return 0;

		}

	}

	public int failbackValidateRequest(String challenge, String validate, String seccode) {

		gtlog("in failback validate");

		if (!resquestIsLegal(challenge, validate, seccode)) {
			return 0;
		}
		gtlog("request legitimate");

		return 1;
	}

	public void gtlog(String message) {
		if (debugCode) {
			System.out.println("gtlog: " + message);
		}
	}

	protected boolean checkResultByPrivate(String challenge, String validate) {
		String encodeStr = md5Encode(privateKey + "geetest" + challenge);
		return validate.equals(encodeStr);
	}

	private String readContentFromGet(String URL) throws IOException {

		URL getUrl = new URL(URL);
		HttpURLConnection connection = (HttpURLConnection) getUrl
				.openConnection();

		connection.setConnectTimeout(2000);
		connection.setReadTimeout(2000);

		connection.connect();

		if (connection.getResponseCode() == 200) {

			StringBuffer sBuffer = new StringBuffer();

			InputStream inStream = null;
			byte[] buf = new byte[1024];
			inStream = connection.getInputStream();
			for (int n; (n = inStream.read(buf)) != -1;) {
				sBuffer.append(new String(buf, 0, n, "UTF-8"));
			}
			inStream.close();
			connection.disconnect();

			return sBuffer.toString();
		} else {

			return "fail";
		}
	}

	private String readContentFromPost(String URL, String data) throws IOException {

		gtlog(data);
		URL postUrl = new URL(URL);
		HttpURLConnection connection = (HttpURLConnection) postUrl
				.openConnection();

		connection.setConnectTimeout(2000);
		connection.setReadTimeout(2000);
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		connection.connect();

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
		outputStreamWriter.write(data);
		outputStreamWriter.flush();
		outputStreamWriter.close();

		if (connection.getResponseCode() == 200) {

			StringBuffer sBuffer = new StringBuffer();

			InputStream inStream = null;
			byte[] buf = new byte[1024];
			inStream = connection.getInputStream();
			for (int n; (n = inStream.read(buf)) != -1;) {
				sBuffer.append(new String(buf, 0, n, "UTF-8"));
			}
			inStream.close();
			connection.disconnect();

			return sBuffer.toString();
		} else {

			return "fail";
		}
	}

	private String md5Encode(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}

}
