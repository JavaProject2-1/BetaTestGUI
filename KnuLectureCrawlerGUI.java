package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * KNU 강의 계획서 크롤러 GUI 클래스
 */
public class KnuLectureCrawlerGUI extends JFrame {
    private JTextField yearField;
    private JComboBox<String> semesterCombo;
    private JComboBox<String> gradeCombo;
    private JButton searchBtn;
    private JButton detailBtn; // 상세 정보 버튼 추가
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JLabel yearLabel, semesterLabel, gradeLabel;
    private List<Subject> currentSubjects; // 현재 검색된 과목들을 저장
    private TimetableGUI timetableGUI; // 시간표 GUI 참조

    /**
     * 생성자
     */
    public KnuLectureCrawlerGUI(int sequence) {
        setTitle("KNU 강의 계획서 크롤러");
        setSize(1200, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 창을 닫아도 프로그램이 종료되지 않도록 변경
        setLocationRelativeTo(null);
        
        currentSubjects = new ArrayList<>();

        initComponents();
        LanguageChange.applyKoreanLabels(this, sequence);
    }
    
    /**
     * 시간표 GUI 참조를 받는 생성자
     */
    public KnuLectureCrawlerGUI(int sequence, TimetableGUI timetableGUI) {
        this(sequence);
        this.timetableGUI = timetableGUI;
    }

    /**
     * 컴포넌트 초기화
     */
    private void initComponents() {
        // 상단 패널 구성
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 테이블 모델 및 테이블 구성
        tableModel = createTableModel();
        resultTable = new JTable(tableModel);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);
        
        // 테이블 셀 렌더러 설정
        setupTableRenderers();
        
        // 검색 버튼 이벤트 설정
        setupSearchButtonAction();
        
        // 상세 정보 버튼 이벤트 설정
        setupDetailButtonAction();
        
        // 테이블 셀 클릭 이벤트 설정
        setupTableClickEvent();
        
        // 기본값 설정
        yearField.setText("2025");
    }
    
    /**
     * 상단 패널 생성
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 왼쪽 패널 (검색 조건들)
        JPanel leftPanel = new JPanel();
        
        yearLabel = new JLabel("개설연도:");
        leftPanel.add(yearLabel);
        yearField = new JTextField("", 6);
        leftPanel.add(yearField);

        gradeLabel = new JLabel("학년:");
        leftPanel.add(gradeLabel);
        gradeCombo = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        leftPanel.add(gradeCombo);

        semesterLabel = new JLabel("학기:");
        leftPanel.add(semesterLabel);
        semesterCombo = new JComboBox<>(new String[]{"1학기", "2학기", "계절학기(하계)", "계절학기(동계)"});
        leftPanel.add(semesterCombo);

        searchBtn = new JButton("검색");
        leftPanel.add(searchBtn);

        statusLabel = new JLabel("Ready");
        leftPanel.add(statusLabel);
        
        topPanel.add(leftPanel, BorderLayout.WEST);
        
        // 오른쪽 패널 (다음 버튼)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        detailBtn = new JButton("다음");
        detailBtn.setEnabled(false); // 초기에는 비활성화
        detailBtn.setPreferredSize(new Dimension(80, 30));
        rightPanel.add(detailBtn);
        
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    /**
     * 테이블 모델 생성
     */
    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(LanguageChange.getHeaders(0), 0) {
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
    }
    
    /**
     * 테이블 렌더러 설정
     */
    private void setupTableRenderers() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        SwingUtilities.invokeLater(() -> {
            for (int i = 1; i < resultTable.getColumnCount(); i++) {
                resultTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        });
    }
    
    /**
     * 검색 버튼 액션 설정
     */
    private void setupSearchButtonAction() {
        searchBtn.addActionListener(e -> new Thread(() -> {
            searchBtn.setEnabled(false);
            detailBtn.setEnabled(false);
            tableModel.setRowCount(0);
            currentSubjects.clear();
            statusLabel.setText("검색 중...");
            
            try {
                String yearFull = yearField.getText().trim();
                String grade = gradeCombo.getSelectedItem().toString();
                String semester = semesterCombo.getSelectedItem().toString();
                
                // 과목 정보 가져오기
                List<Subject> subjects = CrawlerExample.crawlCurriculumSubjects(yearFull, grade, semester);
                currentSubjects.addAll(subjects);
                
                // 테이블에 표시
                List<Object[]> rows = CrawlerExample.convertSubjectsToRows(subjects);
                for (Object[] row : rows) {
                    tableModel.addRow(row);
                }
                
                statusLabel.setText("검색 완료: 전체 " + rows.size() + "개 과목");
                detailBtn.setEnabled(true); // 검색 완료 후 다음 버튼 활성화
                
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("오류 발생: " + ex.getMessage());
            } finally {
                searchBtn.setEnabled(true);
            }
        }).start());
    }
    
    /**
     * 상세 정보 버튼 액션 설정
     */
    private void setupDetailButtonAction() {
        detailBtn.addActionListener(e -> new Thread(() -> {
            // 체크된 과목들 찾기
            List<Subject> selectedSubjects = getSelectedSubjects();
            
            if (selectedSubjects.isEmpty()) {
                JOptionPane.showMessageDialog(this, "상세 정보를 보려는 과목을 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // 시간표 GUI 참조 전달
            DetailedLectureWindow detailWindow = new DetailedLectureWindow(timetableGUI);
            detailWindow.setVisible(true);
            
            detailBtn.setEnabled(false);
            statusLabel.setText("선택한 " + selectedSubjects.size() + "개 과목의 상세 정보를 가져오는 중...");
            
            try {
                String yearFull = yearField.getText().trim();
                
                // 상세 정보 크롤링
                List<DetailedSubject> detailedSubjects = CrawlerExample.crawlSelectedSubjectsDetail(selectedSubjects, yearFull);
                
                // 상세 정보를 테이블 데이터로 변환
                List<Object[]> detailRows = CrawlerExample.convertDetailedSubjectsToRows(detailedSubjects);
                
                // 상세 정보 창에 데이터 설정
                detailWindow.setDetailData(detailRows);
                
                statusLabel.setText("상세 정보 수집 완료: " + detailedSubjects.size() + "개 강의");
                
            } catch (Exception ex) {
                ex.printStackTrace();
                detailWindow.updateStatus("오류 발생: " + ex.getMessage());
                statusLabel.setText("상세 정보 수집 중 오류 발생");
            } finally {
                detailBtn.setEnabled(true);
            }
        }).start());
    }
    
    /**
     * 체크된 과목들을 반환
     */
    private List<Subject> getSelectedSubjects() {
        List<Subject> selectedSubjects = new ArrayList<>();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                if (i < currentSubjects.size()) {
                    selectedSubjects.add(currentSubjects.get(i));
                }
            }
        }
        
        return selectedSubjects;
    }
    
    /**
     * 테이블 클릭 이벤트 설정
     */
    private void setupTableClickEvent() {
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = resultTable.rowAtPoint(evt.getPoint());
                int col = resultTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0 && col != 0) {
                    Object value = resultTable.getValueAt(row, col);
                    if (value != null) {
                        TableCellRenderer renderer = resultTable.getCellRenderer(row, col);
                        Component comp = renderer.getTableCellRendererComponent(resultTable, value, false, false, row, col);
                        Font font = comp.getFont();
                        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
                        int textWidth = (int) font.getStringBounds(value.toString(), frc).getWidth();
                        int columnWidth = resultTable.getColumnModel().getColumn(col).getWidth();
                        if (textWidth > columnWidth) {
                            JOptionPane.showMessageDialog(KnuLectureCrawlerGUI.this,
                                    value.toString(), "셀 내용 보기", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    // Getters
    public JTextField getYearField() { return yearField; }
    public JComboBox<String> getSemesterCombo() { return semesterCombo; }
    public JComboBox<String> getGradeCombo() { return gradeCombo; }
    public JButton getSearchBtn() { return searchBtn; }
    public JTable getResultTable() { return resultTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getStatusLabel() { return statusLabel; }
    public JLabel getYearLabel() { return yearLabel; }
    public JLabel getSemesterLabel() { return semesterLabel; }
    public JLabel getGradeLabel() { return gradeLabel; }

    /**
     * 메인 메소드
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KnuLectureCrawlerGUI(0).setVisible(true));
    }
}