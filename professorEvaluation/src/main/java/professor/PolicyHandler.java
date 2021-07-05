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
    @Autowired ProfessorEvaluationRepository professorEvaluationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverProfessorApplyed_RegistProfessorApply(@Payload ProfessorApplyed professorApplyed){

        if(!professorApplyed.validate()) return;
        
        System.out.println("\n\n##### listener RegistProfessorApply : " + professorApplyed.toJson() + "\n\n");
        //System.out.println("\n\n##### listener RegistProfessorApply(getCourseNo) : " + professorApplyed.getCourseNo() + "\n\n");
        //System.out.println("\n\n##### listener RegistProfessorApply(getProfessorNo) : " + professorApplyed.getProfessorNo() + "\n\n");
        //System.out.println("\n\n##### listener RegistProfessorApply(getProfessorNm) : " + professorApplyed.getProfessorNm() + "\n\n");
        //System.out.println("\n\n##### listener RegistProfessorApply(getPhoneNumber) : " + professorApplyed.getPhoneNumber() + "\n\n");
        
        ProfessorEvaluation professorEvaluation = new ProfessorEvaluation();
        professorEvaluation.setCourseNo(professorApplyed.getCourseNo());
        professorEvaluation.setProfessorNo(professorApplyed.getProfessorNo());
        professorEvaluation.setProfessorNm(professorApplyed.getProfessorNm());
        professorEvaluation.setPhoneNumber(professorApplyed.getPhoneNumber());

        professorEvaluationRepository.save(professorEvaluation);               
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverApplymentCanceled_CancelConfirmProfessor(@Payload ApplymentCanceled applymentCanceled){

        if(!applymentCanceled.validate()) return;
        
        System.out.println("\n\n##### listener CancelConfirmProfessor : " + applymentCanceled.toJson() + "\n\n");

        ProfessorEvaluation professorEvaluation = professorEvaluationRepository.findByCourseNo(applymentCanceled.getCourseNo());   

        professorEvaluationRepository.delete(professorEvaluation);
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
