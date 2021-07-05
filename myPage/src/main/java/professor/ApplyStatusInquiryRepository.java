package professor;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplyStatusInquiryRepository extends CrudRepository<ApplyStatusInquiry, Long> {

    List<ApplyStatusInquiry> findByCourseNo(Long courseNo);    
    // mrfusion Add --------------------------------------------- Start
    List<ApplyStatusInquiry> findByProfessorNo(Long professorNo);
    
    void deleteByCourseNo(Long courseNo);
    void deleteByProfessorNo(Long professorNo);
    // mrfusion Add --------------------------------------------- End

}