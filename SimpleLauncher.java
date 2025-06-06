package basicWeb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 간단한 런처 (바로 시간표 GUI 실행)
 */
public class SimpleLauncher {
    
    public static void main(String[] args) {
        // Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            } 	
        }
        
        // 폰트 및 UI 설정
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Label.font", new Font("맑은 고딕", Font.PLAIN, 12));
        UIManager.put("Button.font", new Font("맑은 고딕", Font.PLAIN, 12));
        UIManager.put("Table.font", new Font("맑은 고딕", Font.PLAIN, 11));
        
        // 스플래시 화면 표시 (선택사항)
        showSplashScreen();
        
        // 메인 시간표 GUI 실행
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TimetableGUI();
            }
        });
    }
    
    /**
     * 스플래시 화면 표시
     */
    private static void showSplashScreen() {
        final JWindow splash = new JWindow();
        splash.setSize(400, 250);
        splash.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(70, 130, 180));
        panel.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94), 2));
        
        // 로고/타이틀
        JLabel titleLabel = new JLabel("KNU 시간표 관리", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 20, 20, 20));
        
        // 로딩 메시지
        JLabel loadingLabel = new JLabel("프로그램을 시작하는 중...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        loadingLabel.setForeground(new Color(220, 220, 220));
        
        // 버전 정보
        JLabel versionLabel = new JLabel("v1.0 | 경북대학교 컴퓨터학부", SwingConstants.CENTER);
        versionLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(189, 195, 199));
        versionLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(loadingLabel, BorderLayout.CENTER);
        panel.add(versionLabel, BorderLayout.SOUTH);
        
        splash.add(panel);
        splash.setVisible(true);
        
        // 2초 후 스플래시 화면 닫기
        Timer timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                splash.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}