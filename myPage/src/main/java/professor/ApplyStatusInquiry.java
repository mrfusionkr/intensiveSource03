package professor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="ApplyStatusInquiry_table")
public class ApplyStatusInquiry {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long courseNo;
        private Long professorNo;
        private String professorNm;
        private String status;


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
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

}
