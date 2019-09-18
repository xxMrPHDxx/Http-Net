package com.mrphd.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class URLUtility {

	public static final Map<String, String> CHAR_TO_HEX;
	public static final Map<String, String> HEX_TO_CHAR;
	
	private static final String HEX = "0123456789ABCDEF";

	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String NUMBERS = "0123456789";
	private static final String ALLOWED_CHARACTERS = "_";
	
	static {
		final Map<String, String> char_to_hex = new HashMap<String, String>();
		final Map<String, String> hex_to_char = new HashMap<String, String>();
		
		final String chars = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
		int i = 32;
		String s;
		for(final String c : chars.split("")) {
			int j = i++;
			s = "";
			while(j > 0) {
				s = HEX.charAt(j % 16) + s;
				j = j / 16;
			}
			char_to_hex.put(c, s);
			hex_to_char.put(s, c);
		}
		
		CHAR_TO_HEX = Collections.unmodifiableMap(char_to_hex);
		HEX_TO_CHAR = Collections.unmodifiableMap(hex_to_char);
	}
	
	private static boolean isLetters(final String s) {
		return LETTERS.contains(s.toUpperCase());
	}
	
	private static boolean isNumbers(final String s) {
		return NUMBERS.contains(s.toUpperCase());
	}
	
	private static boolean shouldNotDecode(final String s) {
		return isLetters(s) || isNumbers(s) || ALLOWED_CHARACTERS.contains(s);
	}
	
	public static String encodeUrlComponent(final String url) {
		final StringBuilder sb = new StringBuilder();
		for(final char sc : url.toCharArray()) {
			final String s = String.valueOf(sc);
			sb.append(shouldNotDecode(s) ? s : ("%" + CHAR_TO_HEX.get(s)));
		}
		return sb.toString();
	}
	
}
