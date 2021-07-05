package professor;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="ProfessorApplyment_table")
public class ProfessorApplyment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long courseNo;
    private Long professorNo;
    private String professorNm;
    private String professorSpec;
    private Integer career;
    private Integer age;
    private String phoneNumber;

    @PostPersist
    public void onPostPersist(){
        ProfessorApplyed professorApplyed = new ProfessorApplyed();
        BeanUtils.copyProperties(this, professorApplyed);
        professorApplyed.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        ProfessorApplyed professorApplyed = new ProfessorApplyed();
        BeanUtils.copyProperties(this, professorApplyed);
        professorApplyed.publishAfterCommit();
    }

    @PostRemove
    public void onPostRemove(){
        ApplymentCanceled applymentCanceled = new ApplymentCanceled();
        BeanUtils.copyProperties(this, applymentCanceled);
        applymentCanceled.publishAfterCommit();
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
    public String getProfessorSpec() {
        return professorSpec;
    }

    public void setProfessorSpec(String professorSpec) {
        this.professorSpec = professorSpec;
    }
    public Integer getCareer() {
        return career;
    }

    public void setCareer(Integer career) {
        this.career = career;
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
