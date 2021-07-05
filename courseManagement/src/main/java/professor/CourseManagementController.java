package professor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import java.text.SimpleDateFormat;
//import java.util.Date;

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

        // Circuit Breaker 발생을 위한 임시 로직 ----------------------------- Start
        /*
        try {
            SimpleDateFormat defaultSimpleDateFormat = new SimpleDateFormat("YYYYMMddHHmmssSSS");
            System.out.println("@@@@@@@@@@@@@@@@@sleepTimeBefore(" + request.getParameter("professorNm") + "): " +  defaultSimpleDateFormat.format(new Date()) );

            Long lSleepTime = (long)(800 + Math.random() * 300);
            System.out.println("@@@@@@@@@@@@@@@@@sleepTime: " + Long.toString(lSleepTime) );
            Thread.sleep(lSleepTime);

            System.out.println("@@@@@@@@@@@@@@@@@sleepTimeEnd(" + request.getParameter("professorNm") + "): " + defaultSimpleDateFormat.format(new Date()) );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        // Circuit Breaker 발생을 위한 임시 로직 ----------------------------- End
       
       CourseManagement courseManagement = courseManagementRepository.findByCourseNo(courseNo);

       //System.out.println("@@@@@@@@@@@@@@@@@courseManagement.courseNo@" + Long.toString(courseManagement.getCourseNo()));
 
        // Circuit Breaker 발생을 위한 임시 주석처리
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
