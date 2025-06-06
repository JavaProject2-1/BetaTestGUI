package basicWeb;

/**
 * 상세 교과목 정보를 담는 클래스 (교수, 강의시간, 강의실 정보 추가)
 */
public class DetailedSubject extends Subject {
    private String professor;
    private String lectureTime;
    private String classroom;
    private String roomNumber;

    public DetailedSubject(String year, String semester, String division, String code, String name, 
                          boolean isRequired, boolean isDesign, String credit, 
                          String professor, String lectureTime, String classroom, String roomNumber) {
        super(year, semester, division, code, name, isRequired, isDesign, credit);
        this.professor = professor;
        this.lectureTime = lectureTime;
        this.classroom = classroom;
        this.roomNumber = roomNumber;
    }

    // Getters
    public String getProfessor() { return professor; }
    public String getLectureTime() { return lectureTime; }
    public String getClassroom() { return classroom; }
    public String getRoomNumber() { return roomNumber; }

    public String getFormattedInfo() {
        return String.format("%s (학년: %s) - %s, 교수: %s, 강의시간: %s, 강의실: %s, 호실번호: %s",
                getCode(), getYear(), getName(), professor, lectureTime, classroom, roomNumber);
    }
}
