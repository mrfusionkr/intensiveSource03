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
    @Autowired SmsHistoryRepository smsHistoryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseCompleted_SendSms(@Payload CourseCompleted courseCompleted){

        if(!courseCompleted.validate()) return;
        
        System.out.println("\n\n##### listener SendSms : " + courseCompleted.toJson() + "\n\n");

        // mrfusion Add --------------------------------------------- Start
        SmsHistory smsHistory = new SmsHistory();
        smsHistory.setPhoneNumber(courseCompleted.getPhoneNumber());
        smsHistory.setContents("교수로 임명 되었습니다.");

        smsHistoryRepository.save(smsHistory);
        // mrfusion Add --------------------------------------------- End

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverProfessorApplyed_SendSms(@Payload ProfessorApplyed professorApplyed){

        if(!professorApplyed.validate()) return;
        
        System.out.println("\n\n##### listener SendSms : " + professorApplyed.toJson() + "\n\n");

        // mrfusion Add --------------------------------------------- Start
        SmsHistory smsHistory = new SmsHistory();
        smsHistory.setPhoneNumber(professorApplyed.getPhoneNumber());
        smsHistory.setContents("교수 신청이 등록 되었습니다.");

        smsHistoryRepository.save(smsHistory);
        // mrfusion Add --------------------------------------------- End
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverApplymentCanceled_SendSms(@Payload ApplymentCanceled applymentCanceled){

        if(!applymentCanceled.validate()) return;

        System.out.println("\n\n##### listener SendSms : " + applymentCanceled.toJson() + "\n\n");

        // mrfusion Add --------------------------------------------- Start
        SmsHistory smsHistory = new SmsHistory();
        smsHistory.setPhoneNumber(applymentCanceled.getPhoneNumber());
        smsHistory.setContents("교수 신청이 취소 되었습니다.");

        smsHistoryRepository.save(smsHistory);
        // mrfusion Add --------------------------------------------- End
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
