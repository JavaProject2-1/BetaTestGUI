package basicWeb;

/**
 * 다국어 지원을 위한 언어 관리 클래스
 */
public class LanguageChange {
    private static final String[] HEADERS_SUBJECTS_ADD = {
            "No", "개설연도", "개설학기", "학년", "교과구분", "개설대학", "개설학과", "강좌번호", "교과목명", "학점",
            "강의", "실습", "담당교수", "강의시간", "강의시간(실제시간)", "강의실", "호실번호", "수강정원", "수강신청",
            "수강꾸러미신청", "수강꾸러미신청가능여부", "대학원공통교과목여부", "비고", "평점"
    };
    
    private static final String[] HEADERS_TIMETABLE_ADD = {
    		"선택", "교과목명", "필수", "설계", "학점"
    };
    
    // 한국어 전체 헤더 (LectureCrawler용)
    private static final String[] HEADERS_KOREAN = {
            "No", "개설연도", "개설학기", "학년", "교과구분", "개설대학", "개설학과", "강좌번호", "교과목명", "학점",
            "강의", "실습", "담당교수", "강의시간", "강의시간(실제시간)", "강의실", "호실번호", "수강정원", "수강신청",
            "수강꾸러미신청", "수강꾸러미신청가능여부", "대학원공통교과목여부", "비고", "평점"
    };

    /**
     * 시퀀스에 따른 헤더 배열 반환
     */
    public static String[] getHeaders(int sequence) {
        if (sequence == 0) return HEADERS_TIMETABLE_ADD;
        else return HEADERS_SUBJECTS_ADD;
    }
    
    /**
     * 언어별 헤더 반환
     */
    public static String[] getHeaders(String language) {
        if ("Korean".equals(language)) {
            return HEADERS_KOREAN;
        }
        return HEADERS_SUBJECTS_ADD;
    }
    
    /**
     * GUI에 한국어 라벨 적용
     */
    public static void applyKoreanLabels(KnuLectureCrawlerGUI gui, int sequence) {
        gui.setTitle("KNU 강의 계획서 크롤러");
        gui.getSearchBtn().setText("검색");
        gui.getStatusLabel().setText("대기 중...");
        gui.getYearLabel().setText("개설연도:");
        gui.getSemesterLabel().setText("학기:");
        gui.getGradeLabel().setText("학년:");
        gui.getTableModel().setColumnIdentifiers(getHeaders(sequence));
    }
}
