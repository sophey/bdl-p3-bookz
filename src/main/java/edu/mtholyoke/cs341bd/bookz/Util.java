package edu.mtholyoke.cs341bd.bookz;

import javax.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author jfoley
 */
public class Util {
  @Nullable
  public static String join(@Nullable String[] array) {
    if(array == null) return null;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      if(i > 0) sb.append(' ');
      sb.append(array[i]);
    }
    return sb.toString();
  }

  // just use EST rather than something fancier; TBD -- how might we configure this for different users.
  private static ZoneId EST = TimeZone.getTimeZone("EST").toZoneId();

  public static String dateToEST(long millis) {
    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), EST);

    String yyyymmdd = localDateTime.getYear()+"-"+localDateTime.getMonth().getValue()+"-"+ localDateTime.getDayOfMonth();
    String hhmm = String.format("%02d:%02d",  localDateTime.getHour(), localDateTime.getMinute());

    return yyyymmdd+" "+hhmm;
  }

  public static String getAfterIfStartsWith(String prefix, String input) {
    if(input.startsWith(prefix)) {
      return input.substring(prefix.length());
    }
    return null;
  }

  public static String encodeParametersInURL(Map<String, String> params, String url) {
		StringBuilder output = new StringBuilder();
		output.append(url);
		output.append('?');

		try {
		  int index = 0;
			for (Map.Entry<String, String> kv : params.entrySet()) {
			  if(index > 0) output.append('&');
				output.append(URLEncoder.encode(kv.getKey(), "UTF-8"));
				output.append('=');
				output.append(URLEncoder.encode(kv.getValue(), "UTF-8"));
				index++;
			}
		} catch (UnsupportedEncodingException uee) {
			// This should never happen, because "UTF-8" is always installed in Java.
			throw new AssertionError(uee);
		}
		return output.toString();
	}
}
