package professor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 @RestController
 public class CourseManagementController {

    // mrfusion Add  ----------------------------------------------------------- Start
    @Autowired
    CourseManagementRepository courseManagementRepository;

    @RequestMapping(value = "/courseManagements/completeCourse",
       method = RequestMethod.GET,
       produces = "application/json;charset=UTF-8")
    public boolean completeCourse(HttpServletRequest request, HttpServletResponse response) {
       boolean status = false;

       System.out.println("@@@@@@@@@@@@@@@@@courseNo@" + request.getParameter("courseNo"));
       System.out.println("@@@@@@@@@@@@@@@@@professorNm@" + request.getParameter("professorNm"));

       Long courseNo = Long.valueOf(request.getParameter("courseNo"));
       Long professorNo = Long.valueOf(request.getParameter("professorNo"));
       
       CourseManagement courseManagement = courseManagementRepository.findByCourseNo(courseNo);

       System.out.println("@@@@@@@@@@@@@@@@@courseManagement.courseNo@" + Long.toString(courseManagement.getCourseNo()));
 
       if(courseManagement.getProfessorNo() == null || courseManagement.getProfessorNo() != professorNo){
            System.out.println("과목에 교수정보 저장");
            courseManagement.setProfessorNo(professorNo);
            courseManagement.setProfessorNm(request.getParameter("professorNm"));
            courseManagement.setPhoneNumber(request.getParameter("phoneNumber"));
            courseManagement.setSuccessFlag(true);

            courseManagementRepository.save(courseManagement);

            status = true;
       }

       return status;
       // mrfusion Add  ----------------------------------------------------------- End
    }

 }
