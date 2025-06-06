package basicWeb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;


public class TimetableGUI extends JFrame {
    private JPanel timetablePanel;
    private GridBagConstraints gbc;
    private JButton addButton;
    private JButton importButton;
    private Map<JButton, SubjectInfo> subjectInfoMap;

    // --- 색상 자동 할당을 위한 필드 추가 ---
    private final List<Color> colorPalette = new ArrayList<>();
    private final Map<String, Color> subjectColorMap = new HashMap<>();
    private int nextColorIndex = 0;
    // --- ---

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

        // --- 초기화 로직 추가 ---
        initializeColorPalette();
        subjectInfoMap = new HashMap<>();
        // --- ---

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
    
    // --- 색상 팔레트 초기화 메서드 ---
    private void initializeColorPalette() {
        colorPalette.add(new Color(173, 216, 230)); // Light Blue
        colorPalette.add(new Color(255, 182, 193)); // Light Pink
        colorPalette.add(new Color(144, 238, 144)); // Light Green
        colorPalette.add(new Color(255, 255, 224)); // Light Yellow
        colorPalette.add(new Color(221, 160, 221)); // Plum (Lavender-ish)
        colorPalette.add(new Color(255, 218, 185)); // Peach Puff
        colorPalette.add(new Color(175, 238, 238)); // Pale Turquoise
        colorPalette.add(new Color(240, 230, 140)); // Khaki
    }

    // --- 과목명에 따라 색상을 할당하거나 기존 색상을 반환하는 메서드 ---
    private synchronized Color getSubjectColor(String subjectName) {
        if (subjectColorMap.containsKey(subjectName)) {
            return subjectColorMap.get(subjectName);
        } else {
            Color newColor = colorPalette.get(nextColorIndex);
            subjectColorMap.put(subjectName, newColor);
            nextColorIndex = (nextColorIndex + 1) % colorPalette.size(); // 다음 인덱스로 이동 (순환)
            return newColor;
        }
    }
    // --- ---

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
        addSubjectSlot(name, prof, place, col, row, height, color, null);
    }

    public void addSubjectToTableWithTime(String name, String prof, String place, int col, int row, int height, Color color, String timeInfo) {
        addSubjectSlot(name, prof, place, col, row, height, color, timeInfo);
    }

    public synchronized boolean addSubjectSlot(String name, String prof, String place, int col, int row, int height, Color ignoredColor, String timeInfo) {
        // --- 색상을 외부에서 받지 않고 내부적으로 자동 할당 ---
        Color subjectColor = getSubjectColor(name);
        // --- ---
        
        List<SubjectInfo> pieces = new ArrayList<>();
        pieces.add(new SubjectInfo(name, prof, place, col, row, height, ""));

        List<JButton> buttonsToRemove = new ArrayList<>();
        for (Map.Entry<JButton, SubjectInfo> entry : subjectInfoMap.entrySet()) {
            SubjectInfo existingInfo = entry.getValue();
            if (existingInfo.getName().equals(name) && existingInfo.getCol() == col) {
                buttonsToRemove.add(entry.getKey());
                pieces.add(existingInfo);
            }
        }

        for (JButton button : buttonsToRemove) {
            timetablePanel.remove(button);
            subjectInfoMap.remove(button);
        }

        pieces.sort(Comparator.comparingInt(SubjectInfo::getRow));

        LinkedList<SubjectInfo> mergedBlocks = new LinkedList<>();
        for (SubjectInfo piece : pieces) {
            if (mergedBlocks.isEmpty() || mergedBlocks.getLast().getRow() + mergedBlocks.getLast().getHeight() < piece.getRow()) {
                mergedBlocks.add(new SubjectInfo(piece.name, piece.professor, piece.classroom, piece.col, piece.row, piece.height, ""));
            } else {
                SubjectInfo lastBlock = mergedBlocks.getLast();
                int mergedHeight = (piece.getRow() + piece.getHeight()) - lastBlock.getRow();
                lastBlock.height = mergedHeight;
            }
        }
        
        for (SubjectInfo block : mergedBlocks) {
            if (isTimeOverlapped(block.getCol(), block.getRow(), block.getHeight())) {
                 System.err.println("Error: Merged subject " + name + " overlaps with another subject.");
                return false;
            }
        }
        
        for (SubjectInfo block : mergedBlocks) {
            String newTimeInfo = calculateTimeInfo(block.getRow(), block.getHeight());
            // --- 자동 할당된 색상(subjectColor)을 사용 ---
            addSubject(block.getName(), block.getProfessor(), block.getClassroom(), block.getCol(), block.getRow(), block.getHeight(), subjectColor, newTimeInfo);
        }

        timetablePanel.revalidate();
        timetablePanel.repaint();
        return true;
    }

    private void addSubject(String name, String prof, String place, int col, int row, int height, Color color, String timeInfo) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridheight = height;

        JButton subject = new JButton("<html><center><b>" + name + "</b><br>" + prof + "<br>" + place + "</center></html>");
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
        Set<String> uniqueSubjectNames = new HashSet<>();
        for (SubjectInfo info : subjectInfoMap.values()) {
            uniqueSubjectNames.add(info.getName());
        }
        int count = 0;
        if(uniqueSubjectNames.contains(subjectName)) {
            // Count how many different days this subject is on
            Set<Integer> days = new HashSet<>();
            for(SubjectInfo info : subjectInfoMap.values()){
                if(info.getName().equals(subjectName)){
                    days.add(info.getCol());
                }
            }
            return days.size();
        }
        return 0;
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
        
        // --- 과목 삭제 시 색상 맵에서도 제거 ---
        subjectColorMap.remove(subjectName);
        // --- ---
        
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
        SwingUtilities.invokeLater(TimetableGUI::new);
    }
}
