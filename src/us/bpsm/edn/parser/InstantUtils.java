/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.parser.ParsedInstant;

public class InstantUtils {
    private static final Pattern INSTANT = Pattern.compile("(\\d\\d\\d\\d)(?:-(\\d\\d)(?:-(\\d\\d)(?:[T](\\d\\d)(?::(\\d\\d)(?::(\\d\\d)(?:[.](\\d{1,9}))?)?)?)?)?)?(?:[Z]|([-+])(\\d\\d):(\\d\\d))?");
    private static final byte[] DAYS_IN_MONTH = new byte[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int TZ_LIMIT = 23;
    private static final TimeZone[] TZ_CACHE;
    private static final int NANOSECS_PER_MILLISEC = 1000000;
    private static final TimeZone GMT;
    private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMAT;

    static ParsedInstant parse(String value) {
        Matcher m = INSTANT.matcher(value);
        if (!m.matches()) {
            throw new EdnSyntaxException("Can't parse \"" + value + "\"");
        }
        int years = Integer.parseInt(m.group(1));
        int months = InstantUtils.parseIntOrElse(m.group(2), 1);
        int days = InstantUtils.parseIntOrElse(m.group(3), 1);
        int hours = InstantUtils.parseIntOrElse(m.group(4), 0);
        int minutes = InstantUtils.parseIntOrElse(m.group(5), 0);
        int seconds = InstantUtils.parseIntOrElse(m.group(6), 0);
        int nanoseconds = InstantUtils.parseNanoseconds(m.group(7));
        int offsetSign = InstantUtils.parseOffsetSign(m.group(8));
        int offsetHours = InstantUtils.parseIntOrElse(m.group(9), 0);
        int offsetMinutes = InstantUtils.parseIntOrElse(m.group(10), 0);
        if (months < 1 || 12 < months) {
            throw new EdnSyntaxException(String.format("'%02d' is not a valid month in '%s'", months, value));
        }
        if (days < 1 || InstantUtils.daysInMonth(months, InstantUtils.isLeapYear(years)) < days) {
            throw new EdnSyntaxException(String.format("'%02d' is not a valid day in '%s'", days, value));
        }
        if (hours < 0 || 23 < hours) {
            throw new EdnSyntaxException(String.format("'%02d' is not a valid hour in '%s'", hours, value));
        }
        if (minutes < 0 || 59 < minutes) {
            throw new EdnSyntaxException(String.format("'%02d' is not a valid minute in '%s'", minutes, value));
        }
        if (seconds < 0 || (minutes == 59 ? 60 : 59) < seconds) {
            throw new EdnSyntaxException(String.format("'%02d' is not a valid second in '%s'", seconds, value));
        }
        assert (0 <= nanoseconds && nanoseconds <= 999999999);
        assert (-1 <= offsetSign && offsetSign <= 1);
        if (offsetHours < 0 || 23 < offsetHours) {
            throw new EdnSyntaxException(String.format("'%02d' is not a valid offset hour in '%s'", offsetHours, value));
        }
        if (offsetMinutes < 0 || 59 < offsetMinutes) {
            throw new EdnSyntaxException(String.format("'%02d' is not a valid offset minute in '%s'", offsetMinutes, value));
        }
        return new ParsedInstant(years, months, days, hours, minutes, seconds, nanoseconds, offsetSign, offsetHours, offsetMinutes);
    }

    static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    static int daysInMonth(int month, boolean isLeapYear) {
        int i = month - 1 + 12 * (isLeapYear ? 1 : 0);
        return DAYS_IN_MONTH[i];
    }

    private static int parseOffsetSign(String s) {
        if (s == null) {
            return 0;
        }
        return "-".equals(s) ? -1 : 1;
    }

    static int parseNanoseconds(String s) {
        if (s == null) {
            return 0;
        }
        if (s.length() < 9) {
            return Integer.parseInt(s + "000000000".substring(s.length()));
        }
        return Integer.parseInt(s);
    }

    private static int parseIntOrElse(String s, int alternative) {
        if (s == null) {
            return alternative;
        }
        return Integer.parseInt(s);
    }

    static Timestamp makeTimestamp(ParsedInstant pi) {
        GregorianCalendar c = InstantUtils.makeCalendar(pi);
        Timestamp ts = new Timestamp(c.getTimeInMillis() / 1000L * 1000L);
        ts.setNanos(pi.nanoseconds);
        return ts;
    }

    static Date makeDate(ParsedInstant pi) {
        return InstantUtils.makeCalendar(pi).getTime();
    }

    static GregorianCalendar makeCalendar(ParsedInstant pi) {
        TimeZone tz = InstantUtils.getTimeZone(pi.offsetSign, pi.offsetHours, pi.offsetMinutes);
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.set(1, pi.years);
        cal.set(2, pi.months - 1);
        cal.set(5, pi.days);
        cal.set(11, pi.hours);
        cal.set(12, pi.minutes);
        cal.set(13, pi.seconds);
        int millis = pi.nanoseconds / 1000000;
        cal.set(14, millis);
        return cal;
    }

    private static TimeZone getTimeZone(int offsetSign, int offsetHours, int offsetMinutes) {
        if (offsetMinutes == 0 && offsetHours <= 23) {
            int i = offsetHours * (offsetSign < 0 ? -1 : 1) + 23;
            return TZ_CACHE[i];
        }
        Object[] arrobject = new Object[3];
        arrobject[0] = offsetSign > 0 ? "+" : "-";
        arrobject[1] = offsetHours;
        arrobject[2] = offsetMinutes;
        String tzID = String.format("GMT%s%02d:%02d", arrobject);
        TimeZone tz = TimeZone.getTimeZone(tzID);
        return tz;
    }

    public static String calendarToString(GregorianCalendar cal) {
        String s = String.format("%1$tFT%1$tT.%1$tL%1$tz", cal);
        assert (Pattern.matches(".*[-+][0-9]{4}$", s));
        int n = s.length();
        return s.substring(0, n - 2) + ":" + s.substring(n - 2);
    }

    public static String dateToString(Date date) {
        GregorianCalendar c = new GregorianCalendar(GMT);
        c.setTime(date);
        String s = InstantUtils.calendarToString(c);
        assert (s.endsWith("+00:00"));
        return s.substring(0, s.length() - 6) + "-00:00";
    }

    public static String timestampToString(Timestamp ts) {
        return TIMESTAMP_FORMAT.get().format(ts) + String.format(".%09d-00:00", ts.getNanos());
    }

    static {
        TimeZone[] tzs = new TimeZone[47];
        for (int h = -23; h <= 23; ++h) {
            tzs[h + 23] = TimeZone.getTimeZone(String.format("GMT%+03d:00", h));
        }
        TZ_CACHE = tzs;
        GMT = TimeZone.getTimeZone("GMT");
        TIMESTAMP_FORMAT = new ThreadLocal<SimpleDateFormat>(){

            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                f.setTimeZone(GMT);
                return f;
            }
        };
    }

}

