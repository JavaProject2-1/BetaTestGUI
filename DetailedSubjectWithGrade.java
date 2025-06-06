package basicWeb;

/**
 * 학년 정보가 포함된 상세 교과목 정보 클래스
 */
public class DetailedSubjectWithGrade extends DetailedSubject {
    private String grade;

    public DetailedSubjectWithGrade(String year, String semester, String division, String code, String name, 
                                   boolean isRequired, boolean isDesign, String credit, 
                                   String professor, String lectureTime, String classroom, String roomNumber,
                                   String grade) {
        super(year, semester, division, code, name, isRequired, isDesign, credit, 
              professor, lectureTime, classroom, roomNumber);
        this.grade = grade;
    }

    public String getGrade() { 
        return grade; 
    }

    @Override
    public String getFormattedInfo() {
        return String.format("%s (%s학년, %s학기) - %s, 교수: %s, 강의시간: %s, 강의실: %s, 호실번호: %s",
                getCode(), grade, getSemester(), getName(), getProfessor(), getLectureTime(), getClassroom(), getRoomNumber());
    }
}
