package professor;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="CourseManagement_table")
public class CourseManagement {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long courseNo;
    private String title;
    private Date dueDate;
    private String courseInfo;
    private Boolean successFlag;
    private Long professorNo;
    private String professorNm;
    private String phoneNumber;

    @PostPersist
    public void onPostPersist(){
        CourseRegisted courseRegisted = new CourseRegisted();
        BeanUtils.copyProperties(this, courseRegisted);
        courseRegisted.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        CourseCompleted courseCompleted = new CourseCompleted();
        BeanUtils.copyProperties(this, courseCompleted);
        courseCompleted.publishAfterCommit();
    }

    @PostRemove
    public void onPostRemove(){
        CourseCanceled courseCanceled = new CourseCanceled();
        BeanUtils.copyProperties(this, courseCanceled);
        courseCanceled.publishAfterCommit();        
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
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    public String getCourseInfo() {
        return courseInfo;
    }

    public void setCourseInfo(String courseInfo) {
        this.courseInfo = courseInfo;
    }
    public Boolean getSuccessFlag() {
        return successFlag;
    }

    public void setSuccessFlag(Boolean successFlag) {
        this.successFlag = successFlag;
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
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
