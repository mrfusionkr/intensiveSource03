package professor;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.net.ConnectException;
import java.util.Date;

@Entity
@Table(name="ProfessorEvaluation_table")
public class ProfessorEvaluation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long courseNo;
    private Long professorNo;
    private String professorNm;
    private Integer score;
    private Boolean successFlag;
    private String phoneNumber;

    @PreUpdate
    public void onPreUpdate() throws Exception{
        

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        // mrfusion Modify -------------------------------------------- Start
        /*
        professor.external.CourseManagement courseManagement = new professor.external.CourseManagement();
        // mappings goes here
        Application.applicationContext.getBean(professor.external.CourseManagementService.class)
            .completeCourse(courseManagement);
        */

        // 담당교수 확정 조건이 충족되지 않으면 확정하지 않는다.
        System.out.print("getSuccessFlag() = " + this.getSuccessFlag().toString());
        if (getSuccessFlag() == false){
            return;
        }

        try{
            //professor.external.CourseManagement courseManagement = new professor.external.CourseManagement();
            // mappings goes here
            boolean isUpdateYn = ProfessorEvaluationApplication.applicationContext.getBean(professor.external.CourseManagementService.class)
                .completeCourse(getCourseNo(), getProfessorNo(), getProfessorNm(), getPhoneNumber());

            if (isUpdateYn == false){
                throw new Exception ("교과등록의 담당교수 정보가 업데이트 되지 않음");
            }

            ProfessorConfirmed professorConfirmed = new ProfessorConfirmed();
            BeanUtils.copyProperties(this, professorConfirmed);
            professorConfirmed.publishAfterCommit();


        } catch (ConnectException ce){
            throw new Exception ("교과 등록 서비스 연결 실패");
        } catch (Exception e){
            throw new Exception ("교과 등록 서비스 실행 실패");
        }

        
        // mrfusion Modify -------------------------------------------- End
    }

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
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
    public Boolean getSuccessFlag() {
        return successFlag;
    }

    public void setSuccessFlag(Boolean successFlag) {
        this.successFlag = successFlag;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }




}
