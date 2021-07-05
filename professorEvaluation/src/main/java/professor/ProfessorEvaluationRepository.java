package professor;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="professorEvaluations", path="professorEvaluations")
public interface ProfessorEvaluationRepository extends PagingAndSortingRepository<ProfessorEvaluation, Long>{

    ProfessorEvaluation findByCourseNo(Long courseNo);  // mrfusion Add
}
