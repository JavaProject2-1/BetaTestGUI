package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    // 반드시 이 생성자 있어야 함!
    public SubjectSearchWindow(TimetableGUI timetableGUI) {
        this(timetableGUI, null);
    }
    // 이미 있었던 생성자
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                searchBtn.setEnabled(false);
                tableModel.setRowCount(0);
                searchResults.clear();
                statusLabel.setText("검색 중...");

                try {
                    String year = yearField.getText().trim();
                    String semester = semesterCombo.getSelectedItem().toString();

                    List<DetailedSubject> results = DetailedSubjectCrawler.searchDetailedSubjects(year, semester, subjectName);
                    searchResults.addAll(results);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
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
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            statusLabel.setText("검색 중 오류 발생: " + ex.getMessage());
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            searchBtn.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }

    // --- 핵심: 중복 체크 추가 ---
    private void addSelectedToTimetable() {
        List<DetailedSubject> selectedSubjects = getSelectedSubjects();

        if (selectedSubjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "시간표에 추가할 강의를 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (DetailedSubject subject : selectedSubjects) {
            String lectureTime = subject.getLectureTime(); // 예: "화 2B,3A,3B,목 1A,1B,2A"

            String[] parts = lectureTime.split(",");
            String currentDay = null;

            for (String part : parts) {
                part = part.trim();

                // 요일이 포함된 경우: "화 2B"
                if (part.matches("^[월화수목금토일]\\s*\\d+[AB]$")) {
                    currentDay = part.substring(0, 1);
                    part = part.substring(1).trim(); // 예: "2B"
                }

                if (currentDay == null) continue; // 요일이 없으면 무시

                try {
                    int col = dayToCol(currentDay);
                    int row = TimeTableUtils.slotToRow(part);

                    if (timetableGUI.isTimeOverlapped(col, row, 1)) {
                        JOptionPane.showMessageDialog(this, "이미 해당 시간에 다른 수업이 있습니다!", "중복", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    timetableGUI.addSubjectToTable(
                            subject.getName(),
                            subject.getProfessor(),
                            subject.getClassroom(),
                            col, row, 1,
                            Color.CYAN
                    );
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "시간 형식 오류: " + part, "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        dispose();
    }
    private int periodToRow(String period) {
        switch (period) {
            case "1A": return 1;
            case "1B": return 2;
            case "2A": return 3;
            case "2B": return 4;
            case "3A": return 5;
            case "3B": return 6;
            case "4A": return 7;
            case "4B": return 8;
            case "5A": return 9;
            case "5B": return 10;
            case "6A": return 11;
            case "6B": return 12;
            case "7A": return 13;
            case "7B": return 14;
            case "8A": return 15;
            case "8B": return 16;
            case "9A": return 17;
            case "9B": return 18;
            default: return -1;
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

    // "09:00" -> 1, "09:30" -> 2, ..., "17:00" -> 17
    private int timeToRow(String time) {
        String[] hm = time.split(":");
        int hour = Integer.parseInt(hm[0]);
        int min = Integer.parseInt(hm[1]);
        return (hour - 9) * 2 + (min == 30 ? 2 : 1);
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
