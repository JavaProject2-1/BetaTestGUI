package basicWeb;

/**
 * 기본 교과목 정보를 담는 클래스
 */
public class Subject {
    private String year;
    private String semester;
    private String division;
    private String code;
    private String name;
    private boolean isRequired;
    private boolean isDesign;
    private String credit;

    public Subject(String year, String semester, String division, String code, String name, 
                  boolean isRequired, boolean isDesign, String credit) {
        this.year = year;
        this.semester = semester;
        this.division = division;
        this.code = code;
        this.name = name;
        this.isRequired = isRequired;
        this.isDesign = isDesign;
        this.credit = credit;
    }

    // Getters
    public String getYear() { return year; }
    public String getSemester() { return semester; }
    public String getDivision() { return division; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isRequired() { return isRequired; }
    public boolean isDesign() { return isDesign; }
    public String getCredit() { return credit; }

    @Override
    public String toString() {
        return String.format("%s학년 %s [%s] %s - %s, 필수: %s, 설계: %s, 학점: %s", 
            year, semester, division, code, name, isRequired, isDesign, credit);
    }
}
