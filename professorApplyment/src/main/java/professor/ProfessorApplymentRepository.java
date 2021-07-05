package professor;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="professorApplyments", path="professorApplyments")
public interface ProfessorApplymentRepository extends PagingAndSortingRepository<ProfessorApplyment, Long>{

    ProfessorApplyment findByCourseNo(Long courseNo);

}
