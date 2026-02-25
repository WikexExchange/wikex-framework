package com.xxl.job.admin.core.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie utility class
 * 
 * Handles saving, retrieving and deleting cookies.
 *
 * @author william 2015-12-12 18:01:06
 */
public class CookieUtil {

	// Default cache time, unit: seconds, set to max integer (practically very long)
	private static final int COOKIE_MAX_AGE = Integer.MAX_VALUE;
	// Cookie path, root path
	private static final String COOKIE_PATH = "/";

	/**
	 * Save cookie
	 *
	 * @param response   HttpServletResponse object
	 * @param key        cookie key/name
	 * @param value      cookie value
	 * @param ifRemember if true, cookie will persist for a long time, else session
	 *                   cookie
	 */
	public static void set(HttpServletResponse response, String key, String value, boolean ifRemember) {
		int age = ifRemember ? COOKIE_MAX_AGE : -1;
		set(response, key, value, null, COOKIE_PATH, age, true);
	}

	/**
	 * Save cookie (with detailed options)
	 *
	 * @param response   HttpServletResponse object
	 * @param key        cookie key/name
	 * @param value      cookie value
	 * @param domain     cookie domain, nullable
	 * @param path       cookie path
	 * @param maxAge     max age in seconds, 0 means delete
	 * @param isHttpOnly whether cookie is HttpOnly
	 */
	private static void set(HttpServletResponse response, String key, String value, String domain, String path,
			int maxAge, boolean isHttpOnly) {
		Cookie cookie = new Cookie(key, value);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(isHttpOnly);
		response.addCookie(cookie);
	}

	/**
	 * Retrieve cookie value by key
	 *
	 * @param request HttpServletRequest object
	 * @param key     cookie key/name
	 * @return cookie value or null if not found
	 */
	public static String getValue(HttpServletRequest request, String key) {
		Cookie cookie = get(request, key);
		if (cookie != null) {
			return cookie.getValue();
		}
		return null;
	}

	/**
	 * Retrieve cookie object by key
	 *
	 * @param request HttpServletRequest object
	 * @param key     cookie key/name
	 * @return Cookie object or null if not found
	 */
	private static Cookie get(HttpServletRequest request, String key) {
		Cookie[] arr_cookie = request.getCookies();
		if (arr_cookie != null && arr_cookie.length > 0) {
			for (Cookie cookie : arr_cookie) {
				if (cookie.getName().equals(key)) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * Remove cookie by setting maxAge=0
	 *
	 * @param request  HttpServletRequest object
	 * @param response HttpServletResponse object
	 * @param key      cookie key/name to remove
	 */
	public static void remove(HttpServletRequest request, HttpServletResponse response, String key) {
		Cookie cookie = get(request, key);
		if (cookie != null) {
			set(response, key, "", null, COOKIE_PATH, 0, true);
		}
	}

}
