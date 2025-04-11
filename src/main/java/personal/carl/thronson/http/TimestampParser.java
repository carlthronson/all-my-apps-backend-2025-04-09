package personal.carl.thronson.http;

import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampParser {

    public static void main(String[] args) throws Exception {
        for (String str : data) {
            OffsetDateTime timestamp = OffsetDateTime.now();
            OffsetDateTime result = extracted(str, timestamp);
            if (result.isEqual(timestamp)) {
                System.out.println("No change " + str);
            }
        }
    }

    public static OffsetDateTime extracted(String data) throws Exception {
        return extracted(data, OffsetDateTime.now());
    }

    static Pattern pattern = Pattern.compile("(\\d+)(\\s*)(\\w*)(\\s*)(\\w*)");

    public static OffsetDateTime extracted(String data, OffsetDateTime timestamp) throws Exception {
        Matcher matcher = pattern.matcher(data);
        if (!matcher.find()) {
            throw new Exception(data);
        } else if (matcher.groupCount() != 5) {
            throw new Exception(data);
        } else {
            Integer number = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(3);
            String directionQualifier = matcher.group(5);
            if (unit.endsWith("s")) {
                unit = unit.substring(0, unit.length() - 1);
            }
            switch (UNIT.valueOf(unit)) {
            case hour:
                timestamp = timestamp.minusHours(number);
                break;
            case minute:
                timestamp = timestamp.minusMinutes(number);
                break;
            case day:
                timestamp = timestamp.minusDays(number);
                break;
            case week:
                timestamp = timestamp.minusWeeks(number);
                break;
            default:
                System.out.println("Bad unit: " + unit);
                break;
            }
        }
//            for (int groupNumber = 1; groupNumber <= matcher.groupCount(); groupNumber++) {
//                System.out.println("group " + groupNumber + ": '" + matcher.group(groupNumber) + "'");
//            }
        return timestamp;
    }

    static enum UNIT {
        hour, minute, day, week
    }

    private static String[] data = { "9 hours ago", "9 hours ago", "23 minutes ago", "4 hours ago", "3 hours ago",
            "7 hours ago", "10 hours ago", "14 hours ago", "9 hours ago", "23 hours ago", "10 hours ago", "2 hours ago",
            "3 hours ago", "1 day ago", "2 hours ago", "1 day ago", "5 hours ago", "15 hours ago", "4 hours ago",
            "10 hours ago", "1 hour ago", "2 hours ago", "6 hours ago", "1 day ago", "7 hours ago", "1 hour ago",
            "10 hours ago", "10 hours ago", "10 hours ago", "10 hours ago", "10 hours ago", "10 hours ago",
            "10 hours ago", "10 hours ago", "19 hours ago", "18 hours ago", "21 hours ago", "1 day ago", "19 hours ago",
            "3 hours ago", "9 hours ago", "16 hours ago", "6 hours ago", "16 hours ago", "1 hour ago", "13 hours ago",
            "15 hours ago", "2 hours ago", "1 hour ago", "13 hours ago", "1 day ago", "21 hours ago", "21 hours ago",
            "3 hours ago", "7 hours ago", "6 hours ago", "10 hours ago", "7 hours ago", "1 hour ago", "4 hours ago",
            "1 hour ago", "2 hours ago", "1 day ago", "21 hours ago", "9 hours ago", "5 hours ago", "2 hours ago",
            "3 hours ago", "1 hour ago", "9 hours ago", "9 hours ago", "15 hours ago", "21 hours ago", "21 hours ago",
            "10 hours ago", "21 hours ago", "21 hours ago", "1 hour ago", "10 hours ago", "3 hours ago", "3 hours ago",
            "6 hours ago", "10 hours ago", "3 hours ago", "5 hours ago", "5 hours ago", "21 hours ago", "1 hour ago",
            "23 hours ago", "18 hours ago", "10 hours ago", "10 hours ago", "10 hours ago", "10 hours ago",
            "10 hours ago", "9 hours ago", "1 hour ago", "3 hours ago", "2 hours ago", "4 hours ago", "4 hours ago",
            "1 day ago", "3 hours ago", "10 hours ago", "10 hours ago", "10 hours ago", "17 hours ago", "15 hours ago",
            "2 hours ago", "2 hours ago", "14 hours ago", "10 hours ago", "10 hours ago", "41 minutes ago", "1 day ago",
            "2 hours ago", "24 minutes ago", "33 minutes ago", "1 hour ago", "22 hours ago", "1 day ago", "1 day ago",
            "4 hours ago", "10 hours ago", "5 hours ago", "21 hours ago", "9 hours ago", "10 hours ago", "2 hours ago",
            "16 hours ago", "4 hours ago", "23 hours ago", "1 day ago", "22 hours ago", "23 minutes ago",
            "20 hours ago", "9 hours ago", "10 hours ago", "9 hours ago", "10 hours ago", "10 hours ago",
            "18 hours ago", "9 hours ago", "3 hours ago", "13 hours ago", "33 minutes ago", "15 hours ago",
            "9 hours ago", "2 hours ago", "10 hours ago", "10 hours ago", "10 hours ago", "21 hours ago", "9 hours ago",
            "37 minutes ago", "23 minutes ago", "23 hours ago", "3 hours ago", "3 hours ago", "13 hours ago",
            "3 hours ago", "6 hours ago", "2 hours ago", "23 hours ago", "4 hours ago", "9 hours ago", "1 day ago",
            "1 day ago", "4 hours ago", "1 hour ago", "35 minutes ago", "3 hours ago", "2 hours ago", "1 hour ago",
            "7 minutes ago", "1 day ago", "5 hours ago", "4 hours ago", "9 hours ago", "13 hours ago", "1 hour ago",
            "9 hours ago", "11 hours ago", "12 hours ago", "1 hour ago", "1 day ago", "5 hours ago", "12 hours ago",
            "8 hours ago", "9 hours ago", "1 day ago", "52 minutes ago", "21 hours ago", "1 day ago", "1 hour ago",
            "6 hours ago", "1 day ago", "3 hours ago", "16 hours ago", "16 hours ago", "23 minutes ago", "22 hours ago",
            "2 hours ago", "6 hours ago", "3 hours ago", "9 hours ago", "3 hours ago", "19 hours ago", "8 hours ago",
            "23 hours ago", "5 hours ago", "1 hour ago", "53 minutes ago", "4 hours ago", "5 hours ago", "21 hours ago",
            "9 hours ago", "15 hours ago", "2 hours ago", "4 hours ago", "2 hours ago", "9 hours ago", "1 hour ago",
            "7 hours ago", "2 hours ago", "9 hours ago", "1 hour ago", "7 hours ago", "5 hours ago", "21 hours ago",
            "23 hours ago", "1 day ago", "5 hours ago", "37 minutes ago", "5 hours ago", "1 day ago", "2 hours ago",
            "1 day ago", "1 hour ago", "9 hours ago", "6 hours ago", "8 hours ago", "6 hours ago", "9 hours ago",
            "3 hours ago", "1 day ago", "15 hours ago" };
}
