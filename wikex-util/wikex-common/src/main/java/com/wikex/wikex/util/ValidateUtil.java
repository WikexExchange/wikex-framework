package com.wikex.wikex.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class ValidateUtil {
	
	private static final String CARD_PATTERN = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";
	
	
	public static boolean isMobilePhone(String phone){
		if (StringUtils.isBlank(phone)) {
			return false;
		}
		if (isChinaPhoneLegal(phone)){
			return true;
		}else {
			return false;
		}
	}
	
	public static boolean isCard(String idNo){ 
		Pattern p2 = Pattern.compile(CARD_PATTERN);
		// System.out.println(p2.matcher(idNo).matches());
		return p2.matcher(idNo).matches();
	}

	public static boolean isnull(String str){
		if(str==null){
			return true;
		}
		if(str=="" || str.length()==0){
			return true;
		}
		return false;
	}
	
	public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
//		String regExp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-9])|(147))\\d{8}$";
		String regExp = "^((13[0-9])|(15[^4])|(14[579])|(16[6])|(19[89])|(18[0-9])|(17[0-9]))\\d{8}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	public static boolean isUrl(String url){
		String regExp = "^(http|https|ftp)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(url);
		return m.matches();
	}

	public static boolean isEmail(String email){
		String regExp = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	public static boolean isChineseName(String name){
		String regExp = "^[\\u4e00-\\u9fa5]+(·[\\u4e00-\\u9fa5]+)*$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(name);
		return m.matches();
	}
}
