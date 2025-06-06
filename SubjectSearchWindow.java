package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SubjectSearchWindow extends JFrame {
    private JTextField yearField;
    private JComboBox<String> semesterCombo;
    private JTextField subjectNameField;
    private JButton searchBtn;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private TimetableGUI timetableGUI;
    private List<DetailedSubject> searchResults;

    public SubjectSearchWindow(TimetableGUI timetableGUI) {
        this(timetableGUI, null);
    }

    public SubjectSearchWindow(TimetableGUI timetableGUI, String initialSubjectName) {
        this.timetableGUI = timetableGUI;
        this.searchResults = new ArrayList<>();
        setTitle("과목 검색");
        setSize(1400, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        if (initialSubjectName != null && !initialSubjectName.trim().isEmpty()) {
            subjectNameField.setText(initialSubjectName);
            searchSubjects();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("개설연도:"));
        yearField = new JTextField("2025", 6);
        searchPanel.add(yearField);

        searchPanel.add(new JLabel("학기:"));
        semesterCombo = new JComboBox<>(new String[]{"1학기", "2학기", "계절학기(하계)", "계절학기(동계)"});
        searchPanel.add(semesterCombo);

        searchPanel.add(new JLabel("과목명:"));
        subjectNameField = new JTextField(15);
        searchPanel.add(subjectNameField);

        searchBtn = new JButton("검색");
        searchBtn.addActionListener(e -> searchSubjects());
        searchPanel.add(searchBtn);

        statusLabel = new JLabel("과목명을 입력하고 검색하세요.");
        searchPanel.add(statusLabel);

        add(searchPanel, BorderLayout.NORTH);

        String[] headers = {
                "선택", "교과목명", "교과목코드", "담당교수", "강의시간", "강의실", "호실번호", "학년", "필수", "설계", "학점"
        };

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(25);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 2; i < resultTable.getColumnCount(); i++) {
            if (i != 1 && i != 4) {
                resultTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        resultTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        resultTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(7).setPreferredWidth(60);
        resultTable.getColumnModel().getColumn(8).setPreferredWidth(50);
        resultTable.getColumnModel().getColumn(9).setPreferredWidth(50);
        resultTable.getColumnModel().getColumn(10).setPreferredWidth(50);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton addBtn = new JButton("시간표에 추가");
        addBtn.addActionListener(e -> addSelectedToTimetable());
        buttonPanel.add(addBtn);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void searchSubjects() {
        String subjectName = subjectNameField.getText().trim();
        if (subjectName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "과목명을 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            searchBtn.setEnabled(false);
            tableModel.setRowCount(0);
            searchResults.clear();
            statusLabel.setText("검색 중...");

            try {
                String year = yearField.getText().trim();
                String semester = semesterCombo.getSelectedItem().toString();

                List<DetailedSubject> results = DetailedSubjectCrawler.searchDetailedSubjects(year, semester, subjectName);
                searchResults.addAll(results);

                SwingUtilities.invokeLater(() -> {
                    for (DetailedSubject subject : results) {
                        Object[] row = {
                                false,
                                subject.getName(),
                                subject.getCode(),
                                subject.getProfessor(),
                                subject.getLectureTime(),
                                subject.getClassroom(),
                                subject.getRoomNumber(),
                                subject.getYear(),
                                subject.isRequired() ? "O" : "",
                                subject.isDesign() ? "O" : "",
                                subject.getCredit()
                        };
                        tableModel.addRow(row);
                    }
                    statusLabel.setText("검색 완료: " + results.size() + "개 강의 발견");
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> statusLabel.setText("검색 중 오류 발생: " + ex.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> searchBtn.setEnabled(true));
            }
        }).start();
    }

    private void addSelectedToTimetable() {
        List<DetailedSubject> selectedSubjects = getSelectedSubjects();

        if (selectedSubjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "시간표에 추가할 강의를 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 선택된 과목들 중 중복된 과목명이 있는지 체크
        Set<String> selectedSubjectNames = new HashSet<>();
        List<String> duplicatedInSelection = new ArrayList<>();
        
        for (DetailedSubject subject : selectedSubjects) {
            String subjectName = subject.getName();
            if (selectedSubjectNames.contains(subjectName)) {
                if (!duplicatedInSelection.contains(subjectName)) {
                    duplicatedInSelection.add(subjectName);
                }
            } else {
                selectedSubjectNames.add(subjectName);
            }
        }
        
        // 한 번에 선택한 과목 중 중복이 있으면 경고
        if (!duplicatedInSelection.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("선택한 과목 중 중복된 과목이 있습니다:\n");
            for (String duplicatedName : duplicatedInSelection) {
                message.append("- ").append(duplicatedName).append("\n");
            }
            message.append("\n같은 과목은 하나만 선택해주세요.");
            
            JOptionPane.showMessageDialog(this,
                    message.toString(),
                    "중복 선택 오류",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 각 선택된 과목에 대해 개별적으로 처리
        boolean anySubjectAdded = false;
        
        for (DetailedSubject subject : selectedSubjects) {
            // 같은 교과목명이 이미 시간표에 있는지 체크
            if (timetableGUI.isSubjectAlreadyAdded(subject.getName())) {
                JOptionPane.showMessageDialog(this,
                        "'" + subject.getName() + "' 과목은 이미 시간표에 추가되어 있습니다.\n" +
                        "(교수: " + subject.getProfessor() + ", 시간: " + subject.getLectureTime() + ")\n\n" +
                        "같은 교과목명의 과목은 중복해서 추가할 수 없습니다.",
                        "중복 과목",
                        JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // 시간표에 추가 시도
            boolean subjectAddedSuccessfully = false;
            String lectureTime = subject.getLectureTime();
            String[] parts = lectureTime.split(",");
            String currentDay = null;
            
            // 모든 시간대가 가능한지 먼저 체크
            boolean canAddAllTimes = true;
            List<TimeSlot> timeSlots = new ArrayList<>();
            
            for (String part : parts) {
                part = part.trim();
                if (part.matches("^[월화수목금토일]\\s*\\d+[AB]$")) {
                    currentDay = part.substring(0, 1);
                    part = part.substring(1).trim();
                }

                if (currentDay == null) continue;

                try {
                    int col = dayToCol(currentDay);
                    int row = TimeTableUtils.slotToRow(part);
                    
                    if (timetableGUI.isTimeOverlapped(col, row, 1)) {
                        canAddAllTimes = false;
                        break;
                    }
                    
                    timeSlots.add(new TimeSlot(col, row, currentDay, part));
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this,
                            "시간 형식 오류: " + part + " (과목: " + subject.getName() + ")",
                            "오류",
                            JOptionPane.ERROR_MESSAGE);
                    canAddAllTimes = false;
                    break;
                }
            }
            
            if (!canAddAllTimes) {
                JOptionPane.showMessageDialog(this,
                        "'" + subject.getName() + "' 과목의 시간대에 이미 다른 수업이 있습니다!",
                        "시간 중복",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            // 모든 시간대가 가능하면 추가
            for (TimeSlot timeSlot : timeSlots) {
                timetableGUI.addSubjectToTable(
                        subject.getName(),
                        subject.getProfessor(),
                        subject.getClassroom(),
                        timeSlot.col, timeSlot.row, 1,
                        Color.CYAN
                );
            }
            
            subjectAddedSuccessfully = true;
            anySubjectAdded = true;
        }
        
        if (anySubjectAdded) {
            dispose();
        }
    }
    
    // 시간대 정보를 저장하는 내부 클래스
    private static class TimeSlot {
        int col, row;
        String day, timeStr;
        
        TimeSlot(int col, int row, String day, String timeStr) {
            this.col = col;
            this.row = row;
            this.day = day;
            this.timeStr = timeStr;
        }
    }

    private int dayToCol(String day) {
        switch (day) {
            case "월": return 1;
            case "화": return 2;
            case "수": return 3;
            case "목": return 4;
            case "금": return 5;
            default: return -1;
        }
    }

    private List<DetailedSubject> getSelectedSubjects() {
        List<DetailedSubject> selected = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                if (i < searchResults.size()) {
                    selected.add(searchResults.get(i));
                }
            }
        }
        return selected;
    }
}