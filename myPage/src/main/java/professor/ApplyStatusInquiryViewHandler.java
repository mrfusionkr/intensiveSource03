package professor;

import professor.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ApplyStatusInquiryViewHandler {


    @Autowired
    private ApplyStatusInquiryRepository applyStatusInquiryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenProfessorApplyed_then_CREATE_1 (@Payload ProfessorApplyed professorApplyed) {
        try {

            if (!professorApplyed.validate()) return;

            // view 객체 생성
            ApplyStatusInquiry applyStatusInquiry = new ApplyStatusInquiry();
            // view 객체에 이벤트의 Value 를 set 함
            applyStatusInquiry.setCourseNo(professorApplyed.getCourseNo());
            applyStatusInquiry.setProfessorNo(professorApplyed.getProfessorNo());
            applyStatusInquiry.setProfessorNm(professorApplyed.getProfessorNm());
            applyStatusInquiry.setStatus("Applyed");
            // view 레파지 토리에 save
            applyStatusInquiryRepository.save(applyStatusInquiry);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenApplymentCanceled_then_UPDATE_1(@Payload ApplymentCanceled applymentCanceled) {
        try {
            if (!applymentCanceled.validate()) return;
            
            // view 객체 조회

            List<ApplyStatusInquiry> applyStatusInquiryList = applyStatusInquiryRepository.findByCourseNo(applymentCanceled.getCourseNo());
            for(ApplyStatusInquiry applyStatusInquiry : applyStatusInquiryList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                applyStatusInquiry.setStatus("Canceled");
                // view 레파지 토리에 save
                applyStatusInquiryRepository.save(applyStatusInquiry);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenProfessorConfirmed_then_UPDATE_2(@Payload ProfessorConfirmed professorConfirmed) {
        try {
            if (!professorConfirmed.validate()) return;

            // view 객체 조회
            List<ApplyStatusInquiry> applyStatusInquiryList = applyStatusInquiryRepository.findByProfessorNo(professorConfirmed.getProfessorNo());  // mrfusion Modify
            for(ApplyStatusInquiry applyStatusInquiry : applyStatusInquiryList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                applyStatusInquiry.setStatus("Confirmed");
                // view 레파지 토리에 save
                applyStatusInquiryRepository.save(applyStatusInquiry);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenCourseCompleted_then_UPDATE_3(@Payload CourseCompleted courseCompleted) {
        try {
            if (!courseCompleted.validate()) return;

            // view 객체 조회
            List<ApplyStatusInquiry> applyStatusInquiryList = applyStatusInquiryRepository.findByCourseNo(courseCompleted.getCourseNo());
            for(ApplyStatusInquiry applyStatusInquiry : applyStatusInquiryList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                applyStatusInquiry.setStatus("Completed");
                // view 레파지 토리에 save
                applyStatusInquiryRepository.save(applyStatusInquiry);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

