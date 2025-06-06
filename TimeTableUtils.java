package basicWeb;

import java.util.*;

public class TimeTableUtils {
    public static final Map<String, String> PERIOD_TO_TIME = new HashMap<>();
    static {
        PERIOD_TO_TIME.put("1A", "09:00"); PERIOD_TO_TIME.put("1B", "09:30");
        PERIOD_TO_TIME.put("2A", "10:00"); PERIOD_TO_TIME.put("2B", "10:30");
        PERIOD_TO_TIME.put("3A", "11:00"); PERIOD_TO_TIME.put("3B", "11:30");
        PERIOD_TO_TIME.put("4A", "12:00"); PERIOD_TO_TIME.put("4B", "12:30");
        PERIOD_TO_TIME.put("5A", "13:00"); PERIOD_TO_TIME.put("5B", "13:30");
        PERIOD_TO_TIME.put("6A", "14:00"); PERIOD_TO_TIME.put("6B", "14:30");
        PERIOD_TO_TIME.put("7A", "15:00"); PERIOD_TO_TIME.put("7B", "15:30");
        PERIOD_TO_TIME.put("8A", "16:00"); PERIOD_TO_TIME.put("8B", "16:30");
        PERIOD_TO_TIME.put("9A", "17:00"); PERIOD_TO_TIME.put("9B", "17:30");
        PERIOD_TO_TIME.put("10A", "18:00"); PERIOD_TO_TIME.put("10B", "18:30");
        PERIOD_TO_TIME.put("11A", "19:00"); PERIOD_TO_TIME.put("11B", "19:30");
        PERIOD_TO_TIME.put("12A", "20:00"); PERIOD_TO_TIME.put("12B", "20:30");

        // 숫자만도 지원
        PERIOD_TO_TIME.put("1", "09:00"); PERIOD_TO_TIME.put("2", "10:00");
        PERIOD_TO_TIME.put("3", "11:00"); PERIOD_TO_TIME.put("4", "12:00");
        PERIOD_TO_TIME.put("5", "13:00"); PERIOD_TO_TIME.put("6", "14:00");
        PERIOD_TO_TIME.put("7", "15:00"); PERIOD_TO_TIME.put("8", "16:00");
        PERIOD_TO_TIME.put("9", "17:00"); PERIOD_TO_TIME.put("10", "18:00");
        PERIOD_TO_TIME.put("11", "19:00"); PERIOD_TO_TIME.put("12", "20:00");
    }

    public static int getDayIndex(String day) {
        switch(day) {
            case "월": return 0; case "화": return 1; case "수": return 2;
            case "목": return 3; case "금": return 4; case "토": return 5; case "일": return 6;
            default: return -1;
        }
    }

    public static int getHour(String t) {
        return Integer.parseInt(t.split(":")[0]);
    }

    public static int getMinute(String t) {
        String[] p = t.split(":");
        return (p.length > 1) ? Integer.parseInt(p[1]) : 0;
    }

    public static int getMinutesFromTime(String t) {
        return getHour(t) * 60 + getMinute(t);
    }

    // ---- TimeSlot ----
    public static class TimeSlot {
        private final String day;
        private final String startTime;
        private final String endTime;
        public TimeSlot(String day, String startTime, String endTime) {
            this.day = day; this.startTime = startTime; this.endTime = endTime;
        }
        public String getDay() { return day; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
    }

    // ---- 강의시간 문자열(교시, 시간 혼합) → TimeSlot 리스트로 파싱 ----
    public static List<TimeSlot> parseTimeSlots(String lectureTime) {
        List<TimeSlot> slots = new ArrayList<>();
        if (lectureTime == null || lectureTime.trim().isEmpty()) return slots;

        Map<String, List<String>> dayToPeriods = parsePeriodFormat(lectureTime);
        if (!dayToPeriods.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : dayToPeriods.entrySet()) {
                String day = entry.getKey();
                List<String> periods = entry.getValue();
                periods.sort(Comparator.comparingInt(p -> getMinutesFromTime(PERIOD_TO_TIME.getOrDefault(p, "00:00"))));

                List<List<String>> groups = new ArrayList<>();
                List<String> cur = new ArrayList<>();
                for (int i = 0; i < periods.size(); i++) {
                    if (cur.isEmpty() || isConsecutivePeriods(cur.get(cur.size() - 1), periods.get(i))) {
                        cur.add(periods.get(i));
                    } else {
                        groups.add(new ArrayList<>(cur));
                        cur.clear(); cur.add(periods.get(i));
                    }
                }
                if (!cur.isEmpty()) groups.add(cur);

                for (List<String> group : groups) {
                    String start = PERIOD_TO_TIME.get(group.get(0));
                    String end = add30min(PERIOD_TO_TIME.get(group.get(group.size() - 1)));
                    if (start != null && end != null) {
                        slots.add(new TimeSlot(day, start, end));
                    }
                }
            }
            return slots;
        }

        // 일반 시간 형식 파싱 ("월 09:00~10:30" 등)
        String[] regex = {
            "([월화수목금토일])\\s*(\\d{2}):(\\d{2})\\s*[~\\-]\\s*(\\d{2}):(\\d{2})"
        };
        String s = lectureTime.replaceAll("[()\\[\\]]", "");
        for (String pattern : regex) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(s);
            while (m.find()) {
                String day = m.group(1);
                String start = m.group(2) + ":" + m.group(3);
                String end = m.group(4) + ":" + m.group(5);
                slots.add(new TimeSlot(day, start, end));
            }
        }
        return slots;
    }

    private static Map<String, List<String>> parsePeriodFormat(String text) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        String[] parts = text.replaceAll("[()\\[\\]]", "").split("[,\\s]+");
        String day = null;
        for (String part : parts) {
            if (part.matches("[월화수목금토일]")) {
                day = part;
                map.putIfAbsent(day, new ArrayList<>());
            } else if (day != null && PERIOD_TO_TIME.containsKey(part)) {
                map.get(day).add(part);
            }
        }
        return map;
    }

    private static boolean isConsecutivePeriods(String p1, String p2) {
        String t1 = PERIOD_TO_TIME.get(p1), t2 = PERIOD_TO_TIME.get(p2);
        if (t1 == null || t2 == null) return false;
        return Math.abs(getMinutesFromTime(t2) - getMinutesFromTime(t1)) == 30;
    }

    private static String add30min(String t) {
        if (t == null) return null;
        String[] s = t.split(":");
        int h = Integer.parseInt(s[0]), m = Integer.parseInt(s[1]) + 30;
        if (m >= 60) { h++; m -= 60; }
        return String.format("%02d:%02d", h, m);
    }

    public static boolean isTimeConflict(TimeSlot s1, TimeSlot s2) {
        if (!s1.day.equals(s2.day)) return false;
        int s1s = getMinutesFromTime(s1.startTime), s1e = getMinutesFromTime(s1.endTime);
        int s2s = getMinutesFromTime(s2.startTime), s2e = getMinutesFromTime(s2.endTime);
        return !(s1e <= s2s || s2e <= s1s);
    }

    // ✅ 수정된 부분: 두 자릿수 교시도 인식
    public static int slotToRow(String slot) {
        if (slot == null || slot.length() < 2)
            throw new IllegalArgumentException("잘못된 시간 슬롯 형식: " + slot);

        String periodPart = slot.substring(0, slot.length() - 1); // "10A" → "10"
        char half = slot.charAt(slot.length() - 1);               // "10A" → 'A'

        int period;
        try {
            period = Integer.parseInt(periodPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("교시 숫자 형식 오류: " + slot);
        }

        if (period < 1 || period > 12 || (half != 'A' && half != 'B'))
            throw new IllegalArgumentException("올바르지 않은 시간 슬롯: " + slot);

        return (period - 1) * 2 + (half == 'A' ? 1 : 2);
    }
}
