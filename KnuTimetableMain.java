package basicWeb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * KNU 시간표 관리 프로그램 메인 클래스
 */
public class KnuTimetableMain extends JFrame {
    
    public KnuTimetableMain() {
        initializeUI();
    }
    
    private void initializeUI() {
        // Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setTitle("KNU 시간표 관리 시스템");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 메인 패널 설정
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // 헤더 패널
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // 중앙 버튼 패널
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // 하단 정보 패널
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }
    
    /**
     * 헤더 패널 생성
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(600, 80));
        headerPanel.setLayout(new BorderLayout());
        
        // 타이틀
        JLabel titleLabel = new JLabel("KNU 시간표 관리 시스템", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // 서브타이틀
        JLabel subtitleLabel = new JLabel("경북대학교 강의 시간표 작성 및 관리", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(220, 220, 220));
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    /**
     * 중앙 버튼 패널 생성
     */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 시간표 관리 버튼
        JButton timetableBtn = createStyledButton(
            "📅 시간표 관리", 
            "시간표를 생성하고 관리합니다",
            new Color(52, 152, 219)
        );
        timetableBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openTimetableGUI();
            }
        });
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(timetableBtn, gbc);
        
        // 강의 검색 버튼
        JButton searchBtn = createStyledButton(
            "🔍 강의 검색", 
            "교과과정에서 강의를 검색합니다",
            new Color(46, 204, 113)
        );
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLectureCrawler();
            }
        });
        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(searchBtn, gbc);
        
        // 과목 검색 버튼
        JButton subjectBtn = createStyledButton(
            "📚 과목 검색", 
            "과목명으로 강의를 검색합니다",
            new Color(155, 89, 182)
        );
        subjectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSubjectSearch();
            }
        });
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(subjectBtn, gbc);
        
        // 도움말 버튼
        JButton helpBtn = createStyledButton(
            "❓ 도움말", 
            "프로그램 사용법을 확인합니다",
            new Color(230, 126, 34)
        );
        helpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
        gbc.gridx = 1; gbc.gridy = 1;
        centerPanel.add(helpBtn, gbc);
        
        return centerPanel;
    }
    
    /**
     * 스타일이 적용된 버튼 생성
     */
    private JButton createStyledButton(String title, String description, Color bgColor) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setPreferredSize(new Dimension(220, 80));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        // 타이틀 라벨
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        // 설명 라벨
        JLabel descLabel = new JLabel(description, SwingConstants.CENTER);
        descLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        descLabel.setForeground(new Color(240, 240, 240));
        
        // 버튼에 라벨 추가
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(titleLabel, BorderLayout.CENTER);
        buttonPanel.add(descLabel, BorderLayout.SOUTH);
        
        button.add(buttonPanel);
        
        // 호버 효과
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;
            Color hoverColor = bgColor.darker();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    /**
     * 하단 정보 패널 생성
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(52, 73, 94));
        footerPanel.setPreferredSize(new Dimension(600, 40));
        footerPanel.setLayout(new BorderLayout());
        
        JLabel infoLabel = new JLabel("KNU Timetable Management System v1.0 | 경북대학교 컴퓨터학부", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(189, 195, 199));
        footerPanel.add(infoLabel, BorderLayout.CENTER);
        
        return footerPanel;
    }
    
    /**
     * 시간표 GUI 열기
     */
    private void openTimetableGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TimetableGUI();
            }
        });
    }
    
    /**
     * 강의 크롤러 열기
     */
    private void openLectureCrawler() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                KnuLectureCrawlerGUI crawler = new KnuLectureCrawlerGUI(0);
                crawler.setVisible(true);
            }
        });
    }
    
    /**
     * 과목 검색 열기
     */
    private void openSubjectSearch() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 임시 TimetableGUI 생성 (과목 검색은 시간표 참조가 필요)
                TimetableGUI tempTimetable = new TimetableGUI();
                SubjectSearchWindow searchWindow = new SubjectSearchWindow(tempTimetable);
                searchWindow.setVisible(true);
            }
        });
    }
    
    /**
     * 도움말 표시
     */
    private void showHelp() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("📋 KNU 시간표 관리 시스템 사용법\n\n");
        helpText.append("🔹 시간표 관리\n");
        helpText.append("• 새로운 시간표를 생성하고 관리할 수 있습니다\n");
        helpText.append("• '시간표 추가' 버튼으로 교과과정에서 과목을 선택할 수 있습니다\n");
        helpText.append("• '과목 추가' 버튼으로 과목명을 검색하여 추가할 수 있습니다\n");
        helpText.append("• 시간표의 과목을 클릭하면 삭제할 수 있습니다\n\n");
        
        helpText.append("🔹 강의 검색\n");
        helpText.append("• 개설연도, 학년, 학기를 선택하여 교과과정을 검색합니다\n");
        helpText.append("• 원하는 과목을 선택한 후 상세 정보를 확인할 수 있습니다\n\n");
        
        helpText.append("🔹 과목 검색\n");
        helpText.append("• 과목명으로 직접 검색하여 강의를 찾을 수 있습니다\n");
        helpText.append("• 검색 결과에서 원하는 강의를 선택하여 시간표에 추가할 수 있습니다\n\n");
        
        helpText.append("🔹 주요 기능\n");
        helpText.append("• 같은 과목은 동일한 색상으로 표시됩니다\n");
        helpText.append("• 시간 겹침 및 중복 과목 검사 기능이 있습니다\n");
        helpText.append("• 교시 형태의 시간 정보를 자동으로 변환합니다\n\n");
        
        helpText.append("⚠️ 주의사항\n");
        helpText.append("• 크롬 드라이버가 프로그램과 같은 폴더에 있어야 합니다\n");
        helpText.append("• 인터넷 연결이 필요합니다 (강의 정보 크롤링)");
        
        JTextArea textArea = new JTextArea(helpText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        textArea.setBackground(new Color(248, 249, 250));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(
            this, 
            scrollPane, 
            "도움말", 
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * 메인 메서드
     */
    public static void main(String[] args) {
        // 시스템 Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 폰트 설정
        UIManager.put("Button.foreground", Color.BLACK);
        
        // 메인 창 실행
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new KnuTimetableMain();
            }
        });
    }
}