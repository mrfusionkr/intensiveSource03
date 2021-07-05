
package professor.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

// mrfusion Modify ----------------------------------------------------------------- Start

/*
@FeignClient(name="courseManagement", url="http://courseManagement:8080")
public interface CourseManagementService {
    @RequestMapping(method= RequestMethod.GET, path="/courseManagements")
    public void completeCourse(@RequestBody CourseManagement courseManagement);

}
*/

@FeignClient(name="courseManagement", url="http://localhost:8081", fallback=CourseCreationServiceFallback.class)
//@FeignClient(name="courseManagement", url="http://${api.url.course}:8080", fallback=CourseCreationServiceFallback.class)
public interface CourseManagementService {
    @RequestMapping(method= RequestMethod.GET, path="/courseManagements/completeCourse")
    public boolean completeCourse(@RequestParam("courseNo") Long courseNo
                            , @RequestParam("professorNo") Long professNo
                            , @RequestParam("professorNm") String professNm
                            , @RequestParam("phoneNumber") String phoneNumber
    );

}

// mrfusion Modify ----------------------------------------------------------------- End

