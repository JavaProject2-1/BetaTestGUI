package basicWeb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TimetableGUI extends JFrame {
    private JPanel timetablePanel;
    private GridBagConstraints gbc;
    private JButton addButton;
    private JButton importButton;
    private Map<JButton, SubjectInfo> subjectInfoMap;

    public TimetableGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle("2025 시간표");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        subjectInfoMap = new HashMap<>();

        timetablePanel = new JPanel(new GridBagLayout());
        timetablePanel.setBackground(Color.WHITE);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        initTimetableGrid();

        JScrollPane scrollPane = new JScrollPane(timetablePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addButton = new JButton("과목 추가");
        addButton.addActionListener(e -> openSubjectSearchWindow());
        topPanel.add(addButton);

        topPanel.add(new JLabel(" | "));

        importButton = new JButton("시간표 추가");
        importButton.addActionListener(e -> openLectureCrawler());
        topPanel.add(importButton);

        return topPanel;
    }

    private void initTimetableGrid() {
        String[] days = {"월", "화", "수", "목", "금"};
        String[] times = {
            "9시", "10시", "11시", "12시", "13시", "14시",
            "15시", "16시", "17시", "18시", "19시", "20시", "21시"
        };

        gbc.gridx = 0;
        gbc.gridy = 0;
        timetablePanel.add(new JLabel(""), gbc);

        for (int i = 0; i < days.length; i++) {
            gbc.gridx = i + 1;
            gbc.gridy = 0;
            JLabel label = new JLabel(days[i], SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            timetablePanel.add(label, gbc);
        }

        for (int i = 0; i < times.length; i++) {
            int baseY = i * 2 + 1;
            gbc.gridx = 0;
            gbc.gridy = baseY;
            gbc.gridheight = 2;
            JLabel label = new JLabel(times[i], SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            timetablePanel.add(label, gbc);
            gbc.gridheight = 1;

            for (int j = 0; j < days.length; j++) {
                for (int k = 0; k < 2; k++) {
                    gbc.gridx = j + 1;
                    gbc.gridy = baseY + k;
                    JPanel cell = new JPanel();
                    cell.setPreferredSize(new Dimension(120, 40));
                    cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    timetablePanel.add(cell, gbc);
                }
            }
        }
    }

    private void openSubjectSearchWindow() {
        SwingUtilities.invokeLater(() -> new SubjectSearchWindow(TimetableGUI.this, null).setVisible(true));
    }

    private void openLectureCrawler() {
        SwingUtilities.invokeLater(() -> new KnuLectureCrawlerGUI(0, TimetableGUI.this).setVisible(true));
    }

    public void addSubjectToTable(String name, String prof, String place, int col, int row, int height, Color color) {
        String timeInfo = calculateTimeInfo(row, height);
        addSubject(name, prof, place, col, row, height, color, timeInfo);
        timetablePanel.revalidate();
        timetablePanel.repaint();
    }

    public void addSubjectToTableWithTime(String name, String prof, String place, int col, int row, int height, Color color, String timeInfo) {
        addSubject(name, prof, place, col, row, height, color, timeInfo);
        timetablePanel.revalidate();
        timetablePanel.repaint();
    }

    public boolean addSubjectSlot(String name, String prof, String place, int col, int row, int height, Color color, String timeInfo) {
        if (isTimeOverlapped(col, row, height)) return false;
        addSubject(name, prof, place, col, row, height, color, timeInfo);
        timetablePanel.revalidate();
        timetablePanel.repaint();
        return true;
    }

    private void addSubject(String name, String prof, String place, int col, int row, int height, Color color, String timeInfo) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridheight = height;

        JButton subject = new JButton("<html><b>" + name + "</b><br>" + prof + "<br>" + place + "</html>");
        subject.setOpaque(true);
        subject.setContentAreaFilled(true);
        subject.setBorderPainted(true);
        subject.setFocusPainted(false);
        subject.setBackground(color);
        subject.setForeground(Color.BLACK);
        subject.setFont(new Font("Dialog", Font.BOLD, 13));
        subject.setPreferredSize(new Dimension(120, height * 40));

        SubjectInfo info = new SubjectInfo(name, prof, place, col, row, height, timeInfo);
        subjectInfoMap.put(subject, info);

        subject.addActionListener(e -> showSubjectInfoAndConfirmDelete(subject, info));

        timetablePanel.add(subject, gbc);
        timetablePanel.setComponentZOrder(subject, 0);
        gbc.gridheight = 1;
    }

    private void showSubjectInfoAndConfirmDelete(JButton subjectButton, SubjectInfo info) {
        StringBuilder message = new StringBuilder();
        message.append("📚 과목 정보\n\n");
        message.append("과목명: ").append(info.getName()).append("\n");
        message.append("교수님: ").append(info.getProfessor()).append("\n");
        message.append("강의실: ").append(info.getClassroom()).append("\n");
        message.append("시간: ").append(info.getTimeInfo()).append("\n\n");

        int sameNameCount = countSameNameSubjects(info.getName());
        if (sameNameCount > 1) {
            message.append("※ '").append(info.getName()).append("' 과목이 ").append(sameNameCount).append("개 있습니다.\n");
            message.append("모든 '").append(info.getName()).append("' 과목을 시간표에서 삭제하시겠습니까?");
        } else {
            message.append("이 과목을 시간표에서 삭제하시겠습니까?");
        }

        int option = JOptionPane.showConfirmDialog(this, message.toString(), "과목 정보",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            removeSameNameSubjects(info.getName());
        }
    }

    private int countSameNameSubjects(String subjectName) {
        int count = 0;
        for (SubjectInfo info : subjectInfoMap.values()) {
            if (info.getName().equals(subjectName)) count++;
        }
        return count;
    }

    private void removeSameNameSubjects(String subjectName) {
        java.util.List<JButton> buttonsToRemove = new ArrayList<>();
        for (Map.Entry<JButton, SubjectInfo> entry : subjectInfoMap.entrySet()) {
            if (entry.getValue().getName().equals(subjectName)) {
                buttonsToRemove.add(entry.getKey());
            }
        }
        for (JButton button : buttonsToRemove) {
            timetablePanel.remove(button);
            subjectInfoMap.remove(button);
        }
        timetablePanel.revalidate();
        timetablePanel.repaint();
    }

    private String calculateTimeInfo(int row, int height) {
        int startHour = 9 + (row - 1) / 2;
        int startMinute = ((row - 1) % 2) * 30;
        int endRow = row + height - 1;
        int endHour = 9 + (endRow - 1) / 2;
        int endMinute = ((endRow - 1) % 2) * 30 + 30;
        if (endMinute == 60) { endHour += 1; endMinute = 0; }
        return String.format("%02d:%02d~%02d:%02d", startHour, startMinute, endHour, endMinute);
    }

    public boolean isTimeOverlapped(int col, int row, int height) {
        int newStart = row;
        int newEnd = row + height - 1;
        for (SubjectInfo info : subjectInfoMap.values()) {
            if (info.getCol() == col) {
                int oldStart = info.getRow();
                int oldEnd = oldStart + info.getHeight() - 1;
                if (!(newEnd < oldStart || oldEnd < newStart)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSubjectAlreadyAdded(String subjectName) {
        for (SubjectInfo info : subjectInfoMap.values()) {
            if (info.getName().equals(subjectName)) {
                return true;
            }
        }
        return false;
    }

    private static class SubjectInfo {
        private String name, professor, classroom, timeInfo;
        private int col, row, height;

        public SubjectInfo(String name, String professor, String classroom, int col, int row, int height, String timeInfo) {
            this.name = name;
            this.professor = professor;
            this.classroom = classroom;
            this.col = col;
            this.row = row;
            this.height = height;
            this.timeInfo = timeInfo;
        }

        public String getName() { return name; }
        public String getProfessor() { return professor; }
        public String getClassroom() { return classroom; }
        public String getTimeInfo() { return timeInfo; }
        public int getCol() { return col; }
        public int getRow() { return row; }
        public int getHeight() { return height; }
    }

    public static void main(String[] args) {
        UIManager.put("Button.foreground", Color.BLACK);
        SwingUtilities.invokeLater(() -> new TimetableGUI());
    }
}
