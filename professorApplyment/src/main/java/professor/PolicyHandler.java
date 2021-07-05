package professor;

import professor.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired ProfessorApplymentRepository professorApplymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseCanceled_CancelApplyment(@Payload CourseCanceled courseCanceled){

        if(!courseCanceled.validate()) return;
        
        System.out.println("\n\n##### listener CancelApplyment : " + courseCanceled.toJson() + "\n\n");

        // mrfusion add ----------------------------------- Start
        ProfessorApplyment professorApplyment = professorApplymentRepository.findByCourseNo(courseCanceled.getCourseNo());

        professorApplymentRepository.delete(professorApplyment);
        // mrfusion add ----------------------------------- End
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseRegisted_ReceiveCourseNotice(@Payload CourseRegisted courseRegisted){

        if(!courseRegisted.validate()) return;
        
        System.out.println("\n\n##### listener ReceiveCourseNotice : " + courseRegisted.toJson() + "\n\n");

        // mrfusion add ----------------------------------- Start
        ProfessorApplyment professorApplyment = new ProfessorApplyment();
        professorApplyment.setCourseNo(courseRegisted.getCourseNo());

        professorApplymentRepository.save(professorApplyment);
        // mrfusion add ----------------------------------- End
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
