package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상세 강의 정보를 표시하는 새 창 (상세 정보 크롤링 개선)
 */
public class DetailedLectureWindow extends JFrame {
    private JTable detailTable;
    private DefaultTableModel detailTableModel;
    private JLabel statusLabel;
    private List<DetailedSubject> detailedSubjects;
    private TimetableGUI timetableGUI; // 기존 시간표 참조
    
    // 과목별 색깔 저장 (같은 과목은 같은 색깔)
    private static final Map<String, Color> SUBJECT_COLORS = new HashMap<>();
    
    // 추가된 과목들 저장 (중복 검사용)
    private List<DetailedSubject> addedSubjects = new ArrayList<>();

    // 교시를 시간으로 변환하는 맵
    private static final Map<String, String> PERIOD_TO_TIME = new HashMap<>();
    
    static {
        // KNU 교시 시간표
        PERIOD_TO_TIME.put("1A", "09:00");
        PERIOD_TO_TIME.put("1B", "09:30");
        PERIOD_TO_TIME.put("2A", "10:00");
        PERIOD_TO_TIME.put("2B", "10:30");
        PERIOD_TO_TIME.put("3A", "11:00");
        PERIOD_TO_TIME.put("3B", "11:30");
        PERIOD_TO_TIME.put("4A", "12:00");
        PERIOD_TO_TIME.put("4B", "12:30");
        PERIOD_TO_TIME.put("5A", "13:00");
        PERIOD_TO_TIME.put("5B", "13:30");
        PERIOD_TO_TIME.put("6A", "14:00");
        PERIOD_TO_TIME.put("6B", "14:30");
        PERIOD_TO_TIME.put("7A", "15:00");
        PERIOD_TO_TIME.put("7B", "15:30");
        PERIOD_TO_TIME.put("8A", "16:00");
        PERIOD_TO_TIME.put("8B", "16:30");
        PERIOD_TO_TIME.put("9A", "17:00");
        PERIOD_TO_TIME.put("9B", "17:30");
        PERIOD_TO_TIME.put("10A", "18:00");
        PERIOD_TO_TIME.put("10B", "18:30");
        
        // 숫자만 있는 경우도 처리
        PERIOD_TO_TIME.put("1", "09:00");
        PERIOD_TO_TIME.put("2", "10:00");
        PERIOD_TO_TIME.put("3", "11:00");
        PERIOD_TO_TIME.put("4", "12:00");
        PERIOD_TO_TIME.put("5", "13:00");
        PERIOD_TO_TIME.put("6", "14:00");
        PERIOD_TO_TIME.put("7", "15:00");
        PERIOD_TO_TIME.put("8", "16:00");
        PERIOD_TO_TIME.put("9", "17:00");
        PERIOD_TO_TIME.put("10", "18:00");
    }

    public DetailedLectureWindow() {
        setTitle("상세 강의 정보");
        setSize(1400, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        detailedSubjects = new ArrayList<>();
        initComponents();
    }

    public DetailedLectureWindow(TimetableGUI timetableGUI) {
        this();
        this.timetableGUI = timetableGUI;
    }

    private void initComponents() {
        // 상단 상태 라벨
        statusLabel = new JLabel("상세 정보를 불러오는 중...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.NORTH);

        // 상세 정보 테이블 헤더 (체크박스 열 추가)
        String[] detailHeaders = {
            "선택", "교과목명", "교과목코드", "담당교수", "강의시간", "강의실", "호실번호", "필수", "설계", "학점"
        };

        // 테이블 모델 생성
        detailTableModel = new DefaultTableModel(detailHeaders, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; // 체크박스 열
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 체크박스 열만 편집 가능
            }
        };

        // 테이블 생성 및 설정
        detailTable = new JTable(detailTableModel);
        detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailTable.setRowHeight(25);

        // 테이블 렌더러 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 교과목명과 강의시간을 제외한 모든 열을 중앙 정렬
        for (int i = 1; i < detailTable.getColumnCount(); i++) {
            if (i != 1 && i != 4) { // 교과목명(1)과 강의시간(4)은 왼쪽 정렬 유지
                detailTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // 열 너비 설정
        detailTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // 선택
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 교과목명
        detailTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 교과목코드
        detailTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 담당교수
        detailTable.getColumnModel().getColumn(4).setPreferredWidth(250); // 강의시간
        detailTable.getColumnModel().getColumn(5).setPreferredWidth(150); // 강의실
        detailTable.getColumnModel().getColumn(6).setPreferredWidth(100); // 호실번호
        detailTable.getColumnModel().getColumn(7).setPreferredWidth(50);  // 필수
        detailTable.getColumnModel().getColumn(8).setPreferredWidth(50);  // 설계
        detailTable.getColumnModel().getColumn(9).setPreferredWidth(50);  // 학점

        JScrollPane scrollPane = new JScrollPane(detailTable);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel();
        
        JButton confirmBtn = new JButton("시간표에 추가");
        confirmBtn.addActionListener(e -> addSelectedToTimetable());
        buttonPanel.add(confirmBtn);
        
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 선택된 강의들을 시간표에 추가
     */
    private void addSelectedToTimetable() {
        List<DetailedSubject> selectedSubjects = getSelectedSubjects();
        
        if (selectedSubjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "시간표에 추가할 강의를 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 기존 시간표가 없으면 새로 생성
        if (timetableGUI == null) {
            timetableGUI = new TimetableGUI();
        }

        // 중복 및 시간 겹침 검사
        List<String> conflicts = checkConflicts(selectedSubjects);
        if (!conflicts.isEmpty()) {
            StringBuilder message = new StringBuilder("다음과 같은 문제가 있어 추가할 수 없습니다:\n\n");
            for (String conflict : conflicts) {
                message.append("• ").append(conflict).append("\n");
            }
            JOptionPane.showMessageDialog(this, message.toString(), "추가 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 강의 추가
        int addedCount = 0;
        StringBuilder debugInfo = new StringBuilder("디버깅 정보:\n");
        
        for (DetailedSubject subject : selectedSubjects) {
            debugInfo.append("\n과목: ").append(subject.getName());
            debugInfo.append("\n강의시간: '").append(subject.getLectureTime()).append("'");
            
            if (addSubjectToTimetable(subject)) {
                addedSubjects.add(subject); // 추가된 과목 목록에 저장
                addedCount++;
                debugInfo.append(" -> 추가 성공");
            } else {
                debugInfo.append(" -> 추가 실패");
            }
        }
        
        // 디버깅 정보 출력
        System.out.println(debugInfo.toString());
        
        if (addedCount > 0) {
            JOptionPane.showMessageDialog(this, addedCount + "개의 강의가 시간표에 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "추가할 수 있는 강의가 없습니다.\n\n" + debugInfo.toString(), 
                "디버깅 정보", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * 중복 및 시간 겹침 검사
     */
    private List<String> checkConflicts(List<DetailedSubject> selectedSubjects) {
        List<String> conflicts = new ArrayList<>();
        
        for (DetailedSubject newSubject : selectedSubjects) {
            // 1. 같은 과목 중복 검사
            for (DetailedSubject addedSubject : addedSubjects) {
                if (newSubject.getName().equals(addedSubject.getName())) {
                    conflicts.add("'" + newSubject.getName() + "' 과목이 이미 시간표에 있습니다.");
                    break;
                }
            }
            
            // 2. 시간 겹침 검사
            List<TimeSlot> newTimeSlots = parseTimeSlots(newSubject.getLectureTime());
            for (DetailedSubject addedSubject : addedSubjects) {
                List<TimeSlot> addedTimeSlots = parseTimeSlots(addedSubject.getLectureTime());
                
                for (TimeSlot newSlot : newTimeSlots) {
                    for (TimeSlot addedSlot : addedTimeSlots) {
                        if (isTimeConflict(newSlot, addedSlot)) {
                            conflicts.add("'" + newSubject.getName() + "'과 '" + addedSubject.getName() + 
                                         "'의 강의시간이 겹칩니다. (" + newSlot.getDay() + "요일)");
                        }
                    }
                }
            }
        }
        
        // 3. 선택된 과목들 간의 겹침 검사
        for (int i = 0; i < selectedSubjects.size(); i++) {
            for (int j = i + 1; j < selectedSubjects.size(); j++) {
                DetailedSubject subject1 = selectedSubjects.get(i);
                DetailedSubject subject2 = selectedSubjects.get(j);
                
                // 같은 과목 검사
                if (subject1.getName().equals(subject2.getName())) {
                    conflicts.add("선택한 과목 중 '" + subject1.getName() + "'이 중복되었습니다.");
                    continue;
                }
                
                // 시간 겹침 검사
                List<TimeSlot> slots1 = parseTimeSlots(subject1.getLectureTime());
                List<TimeSlot> slots2 = parseTimeSlots(subject2.getLectureTime());
                
                for (TimeSlot slot1 : slots1) {
                    for (TimeSlot slot2 : slots2) {
                        if (isTimeConflict(slot1, slot2)) {
                            conflicts.add("선택한 '" + subject1.getName() + "'과 '" + subject2.getName() + 
                                         "'의 강의시간이 겹칩니다. (" + slot1.getDay() + "요일)");
                        }
                    }
                }
            }
        }
        
        return conflicts;
    }

    /**
     * 시간 겹침 검사
     */
    private boolean isTimeConflict(TimeSlot slot1, TimeSlot slot2) {
        if (!slot1.getDay().equals(slot2.getDay())) {
            return false; // 다른 요일이면 겹치지 않음
        }

        try {
            int start1 = getMinutesFromTime(slot1.getStartTime());
            int end1 = getMinutesFromTime(slot1.getEndTime());
            int start2 = getMinutesFromTime(slot2.getStartTime());
            int end2 = getMinutesFromTime(slot2.getEndTime());

            // 시간 겹침 검사: 한 강의의 끝이 다른 강의의 시작보다 늦고,
            // 다른 강의의 끝이 한 강의의 시작보다 늦으면 겹침
            return !(end1 <= start2 || end2 <= start1);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 시간을 분으로 변환
     */
    private int getMinutesFromTime(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return hour * 60 + minute;
    }

    /**
     * 과목별 고정 색깔 가져오기
     */
    private Color getSubjectColor(String subjectName) {
        if (!SUBJECT_COLORS.containsKey(subjectName)) {
            // 새로운 과목이면 새로운 색깔 생성
            Color newColor = generateDistinctColor(SUBJECT_COLORS.size());
            SUBJECT_COLORS.put(subjectName, newColor);
            System.out.println("새 과목 색깔 생성: " + subjectName + " -> " + newColor);
        }
        return SUBJECT_COLORS.get(subjectName);
    }

    /**
     * 구별되는 색깔 생성
     */
    private Color generateDistinctColor(int index) {
        // 미리 정의된 구별되는 색깔들
        Color[] predefinedColors = {
            new Color(255, 182, 193), // Light Pink
            new Color(173, 216, 230), // Light Blue
            new Color(144, 238, 144), // Light Green
            new Color(255, 218, 185), // Peach
            new Color(221, 160, 221), // Plum
            new Color(255, 255, 224), // Light Yellow
            new Color(255, 192, 203), // Pink
            new Color(176, 196, 222), // Light Steel Blue
            new Color(152, 251, 152), // Pale Green
            new Color(255, 228, 196), // Bisque
            new Color(216, 191, 216), // Thistle
            new Color(255, 239, 213), // Papaya Whip
        };
        
        if (index < predefinedColors.length) {
            return predefinedColors[index];
        } else {
            // 미리 정의된 색깔이 부족하면 랜덤 생성
            return new Color(
                (int)(Math.random() * 200) + 55,
                (int)(Math.random() * 200) + 55,
                (int)(Math.random() * 200) + 55
            );
        }
    }

    /**
     * 선택된 강의들 반환
     */
    private List<DetailedSubject> getSelectedSubjects() {
        List<DetailedSubject> selectedSubjects = new ArrayList<>();
        
        for (int i = 0; i < detailTableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) detailTableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                if (i < detailedSubjects.size()) {
                    selectedSubjects.add(detailedSubjects.get(i));
                }
            }
        }
        
        return selectedSubjects;
    }

    /**
     * 강의시간 문자열을 TimeSlot 리스트로 파싱 (교시 형태 지원)
     */
    private List<TimeSlot> parseTimeSlots(String lectureTime) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        
        if (lectureTime == null || lectureTime.trim().isEmpty()) {
            return timeSlots;
        }
        
        System.out.println("원본 강의시간: '" + lectureTime + "'");
        
        // 교시 형태 파싱: "화 8B,9A,9B,목 8B,9A,9B" 또는 "금 6A,6B,금 7A,7B,금 8A,8B,금 9A,9B"
        Map<String, List<String>> dayToPeriods = parsePeriodFormat(lectureTime);
        
        if (!dayToPeriods.isEmpty()) {
            // 교시 형태로 파싱 성공
            for (Map.Entry<String, List<String>> entry : dayToPeriods.entrySet()) {
                String day = entry.getKey();
                List<String> periods = entry.getValue();
                
                if (!periods.isEmpty()) {
                    System.out.println(day + "요일 교시 목록: " + periods);
                    
                    // 연속된 교시들을 그룹화하여 처리
                    List<List<String>> periodGroups = groupConsecutivePeriods(periods);
                    
                    for (List<String> group : periodGroups) {
                        if (!group.isEmpty()) {
                            String startTime = convertPeriodToTime(group.get(0));
                            String endTime = convertPeriodToEndTime(group.get(group.size() - 1));
                            
                            if (startTime != null && endTime != null) {
                                timeSlots.add(new TimeSlot(day, startTime, endTime));
                                System.out.println("교시 그룹 파싱: " + day + " " + startTime + "~" + endTime + 
                                                  " (교시: " + group + ")");
                            }
                        }
                    }
                }
            }
        } else {
            // 기존 시간 형태 파싱 시도
            timeSlots = parseTraditionalTimeFormat(lectureTime);
        }
        
        return timeSlots;
    }

    /**
     * 연속된 교시들을 그룹화
     * 예: [1A, 1B, 2A, 3A, 3B] -> [[1A, 1B, 2A], [3A, 3B]]
     */
    private List<List<String>> groupConsecutivePeriods(List<String> periods) {
        List<List<String>> groups = new ArrayList<>();
        if (periods.isEmpty()) return groups;
        
        // 교시를 정렬
        periods.sort((a, b) -> {
            String timeA = PERIOD_TO_TIME.get(a);
            String timeB = PERIOD_TO_TIME.get(b);
            if (timeA == null || timeB == null) return 0;
            return timeA.compareTo(timeB);
        });
        
        List<String> currentGroup = new ArrayList<>();
        currentGroup.add(periods.get(0));
        
        for (int i = 1; i < periods.size(); i++) {
            String prevPeriod = periods.get(i - 1);
            String currentPeriod = periods.get(i);
            
            if (isConsecutivePeriods(prevPeriod, currentPeriod)) {
                // 연속된 교시면 현재 그룹에 추가
                currentGroup.add(currentPeriod);
            } else {
                // 연속되지 않으면 새 그룹 시작
                groups.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroup.add(currentPeriod);
            }
        }
        
        // 마지막 그룹 추가
        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }
        
        System.out.println("교시 그룹화 결과: " + groups);
        return groups;
    }

    /**
     * 두 교시가 연속되는지 확인
     */
    private boolean isConsecutivePeriods(String period1, String period2) {
        String time1 = PERIOD_TO_TIME.get(period1);
        String time2 = PERIOD_TO_TIME.get(period2);
        
        if (time1 == null || time2 == null) return false;
        
        try {
            int minutes1 = getMinutesFromTime(time1);
            int minutes2 = getMinutesFromTime(time2);
            
            // 30분 차이면 연속된 교시로 간주
            return Math.abs(minutes2 - minutes1) == 30;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 교시 형태 파싱: "화 8B,9A,9B,목 8B,9A,9B" 또는 "금 6A,6B,금 7A,7B,금 8A,8B,금 9A,9B"
     * 같은 요일이 반복되는 경우도 처리
     * "화 2B,3A,3B,목 2B,3A,3B"처럼 요일없이 나오는 경우도 바로 이전 요일을 따라간다!
     */
    private Map<String, List<String>> parsePeriodFormat(String lectureTime) {
    	System.out.println(lectureTime);
        Map<String, List<String>> dayToPeriods = new HashMap<>();
        // 괄호 제거
        String cleanTime = lectureTime.replaceAll("[()\\[\\]]", "").trim();
        System.out.println("정리된 강의시간: '" + cleanTime + "'");

        String[] parts = cleanTime.split(",");
        String currentDay = null;
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            // "화 2B" 또는 "목 3A"처럼 요일+공백+교시
            if (part.matches("^[월화수목금토일]\\s*\\d+[AB]?$")) {
                String[] arr = part.split("\\s+");
                currentDay = arr[0];
                String period = arr.length > 1 ? arr[1] : "";
                if (!dayToPeriods.containsKey(currentDay)) dayToPeriods.put(currentDay, new ArrayList<>());
                if (!period.isEmpty()) dayToPeriods.get(currentDay).add(period);
            }
            // "화" 한글 한 글자만 등장하면(요일만)
            else if (part.matches("^[월화수목금토일]$")) {
                currentDay = part;
                if (!dayToPeriods.containsKey(currentDay)) dayToPeriods.put(currentDay, new ArrayList<>());
            }
            // "2B" "3A" 처럼 교시만 오면 직전 currentDay 사용
            else if (part.matches("^\\d+[AB]?$")) {
                if (currentDay != null) {
                    dayToPeriods.get(currentDay).add(part);
                }
            }
            // 혹시 몰라서 기타 케이스 처리
            else {
                System.out.println("parsePeriodFormat 기타 예외: " + part);
            }
        }
        System.out.println("파싱 결과: " + dayToPeriods);
        return dayToPeriods;
    }

    /**
     * 기존 시간 형태 파싱
     */
    private List<TimeSlot> parseTraditionalTimeFormat(String lectureTime) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        
        // 기존 로직 유지
        String cleanTime = lectureTime.replaceAll("[()\\[\\]]", "");
        String[] timeParts = cleanTime.split("[,;]");
        
        for (String timePart : timeParts) {
            timePart = timePart.trim();
            if (timePart.isEmpty()) continue;
            
            try {
                TimeSlot slot = parseIndividualTimeSlot(timePart);
                if (slot != null) {
                    timeSlots.add(slot);
                    System.out.println("시간 파싱 성공: " + slot.getDay() + " " + slot.getStartTime() + "~" + slot.getEndTime());
                }
            } catch (Exception e) {
                System.err.println("시간 파싱 오류: " + timePart + " - " + e.getMessage());
            }
        }
        
        return timeSlots;
    }

    /**
     * 교시를 시작 시간으로 변환
     */
    private String convertPeriodToTime(String period) {
        String time = PERIOD_TO_TIME.get(period);
        System.out.println("교시 " + period + " -> 시작시간 " + time);
        return time;
    }

    /**
     * 교시를 종료 시간으로 변환 (30분 추가)
     */
    private String convertPeriodToEndTime(String period) {
        String startTime = PERIOD_TO_TIME.get(period);
        if (startTime == null) {
            System.out.println("교시 " + period + "에 대한 시간을 찾을 수 없음");
            return null;
        }
        
        try {
            String[] parts = startTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            
            minute += 30; // 30분 추가
            if (minute >= 60) {
                hour++;
                minute -= 60;
            }
            
            String endTime = String.format("%02d:%02d", hour, minute);
            System.out.println("교시 " + period + " -> 종료시간 " + endTime + " (시작: " + startTime + " + 30분)");
            return endTime;
        } catch (Exception e) {
            System.err.println("종료시간 계산 오류: " + period + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 개별 시간 슬롯 파싱 (기존 로직)
     */
    private TimeSlot parseIndividualTimeSlot(String timeStr) {
        String[] patterns = {
            "([월화수목금토일])\\s*([0-9]{1,2}):?([0-9]{0,2})\\s*[~\\-]\\s*([0-9]{1,2}):?([0-9]{0,2})",
            "([월화수목금토일])\\s*([0-9]{1,2}):?([0-9]{0,2})\\s*[~\\-]\\s*([0-9]{1,2}):?([0-9]{0,2})",
            "([월화수목금토일])\\s+([0-9]{1,2}):([0-9]{2})\\s*[~\\-]\\s*([0-9]{1,2}):([0-9]{2})"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(timeStr);
            
            if (m.find()) {
                String day = m.group(1);
                String startHour = m.group(2);
                String startMin = m.group(3).isEmpty() ? "00" : m.group(3);
                String endHour = m.group(4);
                String endMin = m.group(5).isEmpty() ? "00" : m.group(5);
                
                String startTime = startHour + ":" + startMin;
                String endTime = endHour + ":" + endMin;
                
                return new TimeSlot(day, startTime, endTime);
            }
        }
        
        return null;
    }

    /**
     * 강의를 시간표에 추가 (실제 시간 정보 포함)
     */
    private boolean addSubjectToTimetable(DetailedSubject subject) {
        List<TimeSlot> timeSlots = parseTimeSlots(subject.getLectureTime());
        
        if (timeSlots.isEmpty()) {
            System.out.println("시간 정보가 없는 과목: " + subject.getName());
            return false;
        }
        
        boolean added = false;
        Color subjectColor = getSubjectColor(subject.getName()); // 과목별 고정 색깔
        
        for (TimeSlot slot : timeSlots) {
            int dayIndex = getDayIndex(slot.getDay());
            if (dayIndex == -1) {
                System.out.println("지원하지 않는 요일: " + slot.getDay());
                continue;
            }
            
            int startHour = getHourFromTime(slot.getStartTime());
            int startMinute = getMinuteFromTime(slot.getStartTime());
            int endHour = getHourFromTime(slot.getEndTime());
            int endMinute = getMinuteFromTime(slot.getEndTime());
            
            if (startHour == -1 || endHour == -1) {
                System.out.println("시간 파싱 실패: " + slot.getStartTime() + "~" + slot.getEndTime());
                continue;
            }
            
            System.out.println("정확한 시간 정보: " + slot.getStartTime() + "~" + slot.getEndTime());
            
            // 시간표 범위 체크 (9시~18시)
            if (startHour < 9 || endHour > 18 || (startHour == endHour && startMinute >= endMinute)) {
                System.out.println("시간표 범위 벗어남 또는 잘못된 시간: " + slot.getStartTime() + "~" + slot.getEndTime());
                continue;
            }
            
            // 시간표 위치 계산 (30분 단위로 정확히 계산)
            int startSlot = (startHour - 9) * 2 + (startMinute >= 30 ? 1 : 0);
            int endSlot = (endHour - 9) * 2 + (endMinute > 0 ? 1 : 0);
            
            int row = startSlot + 1; // 1부터 시작
            int height = Math.max(1, endSlot - startSlot);
            
            System.out.println("시간표 위치: 열=" + (dayIndex + 1) + ", 행=" + row + ", 높이=" + height);
            System.out.println("계산 과정: 시작슬롯=" + startSlot + ", 종료슬롯=" + endSlot);
            System.out.println("시작시간=" + startHour + ":" + startMinute + ", 종료시간=" + endHour + ":" + endMinute);
            
            // 실제 시간 정보 포함하여 추가
            String timeInfo = slot.getStartTime() + "~" + slot.getEndTime();
            timetableGUI.addSubjectToTableWithTime(subject.getName(), subject.getProfessor(), subject.getClassroom(), dayIndex + 1, row, height, subjectColor, timeInfo);
            added = true;
            
            System.out.println("시간표에 추가됨: " + subject.getName() + " - " + slot.getDay() + " " + timeInfo + " (색깔: " + subjectColor + ")");
        }
        
        return added;
    }

    /**
     * 요일을 인덱스로 변환
     */
    private int getDayIndex(String day) {
        switch (day) {
            case "월": return 0;
            case "화": return 1;
            case "수": return 2;
            case "목": return 3;
            case "금": return 4;
            case "토": return 5;
            case "일": return 6;
            default: return -1;
        }
    }

    /**
     * 시간 문자열에서 시간 추출 (예: "16:30" -> 16)
     */
    private int getHourFromTime(String time) {
        try {
            if (time.contains(":")) {
                return Integer.parseInt(time.split(":")[0]);
            }
            return Integer.parseInt(time);
        } catch (NumberFormatException e) {
            System.err.println("시간 파싱 오류: " + time);
            return -1;
        }
    }

    /**
     * 시간 문자열에서 분 추출 (예: "16:30" -> 30)
     */
    private int getMinuteFromTime(String time) {
        try {
            if (time.contains(":")) {
                String[] parts = time.split(":");
                return parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            }
            return 0;
        } catch (NumberFormatException e) {
            System.err.println("분 파싱 오류: " + time);
            return 0;
        }
    }

    /**
     * 상세 정보 데이터 설정
     */
    public void setDetailData(List<Object[]> detailData) {
        SwingUtilities.invokeLater(() -> {
            detailTableModel.setRowCount(0);
            detailedSubjects.clear();
            
            for (Object[] row : detailData) {
                // 체크박스를 맨 앞에 추가
                Object[] newRow = new Object[row.length + 1];
                newRow[0] = false; // 체크박스 초기값
                System.arraycopy(row, 0, newRow, 1, row.length);
                detailTableModel.addRow(newRow);
                
                // DetailedSubject 객체 생성 (실제 데이터에서)
                if (row.length >= 9) {
                    DetailedSubject subject = new DetailedSubject(
                        "", "", "", // year, semester, division
                        (String) row[1], // code
                        (String) row[0], // name
                        row[6].toString().equals("O"), // isRequired
                        row[7].toString().equals("O"), // isDesign
                        (String) row[8], // credit
                        (String) row[2], // professor
                        (String) row[3], // lectureTime
                        (String) row[4], // classroom
                        (String) row[5]  // roomNumber
                    );
                    detailedSubjects.add(subject);
                }
            }
            statusLabel.setText("상세 정보 로드 완료: " + detailData.size() + "개 강의");
        });
    }

    /**
     * 상태 메시지 업데이트
     */
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    /**
     * 시간 슬롯 클래스
     */
    private static class TimeSlot {
        private String day;
        private String startTime;
        private String endTime;

        public TimeSlot(String day, String startTime, String endTime) {
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getDay() { return day; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
    }
}