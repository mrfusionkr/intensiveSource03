package professor;

public class ProfessorApplyed extends AbstractEvent {

    private Long id;
    private Long courseNo;
    private Long professorNo;
    private String professorNm;
    private String professorSpec;
    private Integer Career;
    private Integer age;
    private String phoneNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(Long courseNo) {
        this.courseNo = courseNo;
    }
    public Long getProfessorNo() {
        return professorNo;
    }

    public void setProfessorNo(Long professorNo) {
        this.professorNo = professorNo;
    }
    public String getProfessorNm() {
        return professorNm;
    }

    public void setProfessorNm(String professorNm) {
        this.professorNm = professorNm;
    }
    public String getProfessorSpec() {
        return professorSpec;
    }

    public void setProfessorSpec(String professorSpec) {
        this.professorSpec = professorSpec;
    }
    public Integer getCareer() {
        return Career;
    }

    public void setCareer(Integer Career) {
        this.Career = Career;
    }
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}