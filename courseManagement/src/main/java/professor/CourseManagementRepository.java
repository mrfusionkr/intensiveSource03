package professor;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="courseManagements", path="courseManagements")
public interface CourseManagementRepository extends PagingAndSortingRepository<CourseManagement, Long>{

    CourseManagement findByCourseNo(Long courseNo);

}
