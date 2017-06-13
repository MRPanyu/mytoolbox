package mrpanyu.mytoolbox.framework.utils;

import java.util.regex.Pattern;

/**
 * Utility class for glob style pattern matching
 * 
 * @author Panyu
 * 
 */
public abstract class GlobUtils {

	/**
	 * Matches a string with a glob pattern, the match is case sensitive
	 * 
	 * @param glob
	 *            the glob pattern
	 * @param str
	 *            the string to match
	 * @return match result
	 */
	public static boolean globMatches(String glob, String str) {
		return globMatches(glob, str, false);
	}

	/**
	 * Matches a string with a glob pattern.
	 * 
	 * @param glob
	 *            the glob pattern
	 * @param str
	 *            the string to match
	 * @param caseInsensitive
	 *            if the match should be case insensitive
	 * @return match result
	 */
	public static boolean globMatches(String glob, String str,
			boolean caseInsensitive) {
		String regex = globToRegex(glob);
		Pattern pattern = null;
		if (caseInsensitive) {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} else {
			pattern = Pattern.compile(regex);
		}
		return pattern.matcher(str).matches();
	}

	/**
	 * Converts a glob pattern to regular expression pattern.
	 * 
	 * @param glob
	 *            the glob pattern
	 * @return the regular expression pattern
	 */
	public static String globToRegex(String glob) {
		glob = glob.trim();
		int strLen = glob.length();
		StringBuilder sb = new StringBuilder(strLen);
		boolean escaping = false;
		int inCurlies = 0;
		for (char currentChar : glob.toCharArray()) {
			switch (currentChar) {
			case '*':
				if (escaping)
					sb.append("\\*");
				else
					sb.append(".*");
				escaping = false;
				break;
			case '?':
				if (escaping)
					sb.append("\\?");
				else
					sb.append('.');
				escaping = false;
				break;
			case '.':
			case '(':
			case ')':
			case '+':
			case '|':
			case '^':
			case '$':
			case '@':
			case '%':
				sb.append('\\');
				sb.append(currentChar);
				escaping = false;
				break;
			case '\\':
				if (escaping) {
					sb.append("\\\\");
					escaping = false;
				} else
					escaping = true;
				break;
			case '{':
				if (escaping) {
					sb.append("\\{");
				} else {
					sb.append('(');
					inCurlies++;
				}
				escaping = false;
				break;
			case '}':
				if (inCurlies > 0 && !escaping) {
					sb.append(')');
					inCurlies--;
				} else if (escaping)
					sb.append("\\}");
				else
					sb.append("}");
				escaping = false;
				break;
			case ',':
				if (inCurlies > 0 && !escaping) {
					sb.append('|');
				} else if (escaping)
					sb.append("\\,");
				else
					sb.append(",");
				break;
			default:
				escaping = false;
				sb.append(currentChar);
			}
		}
		return sb.toString();
	}

}
