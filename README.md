![image](https://user-images.githubusercontent.com/70736001/123548542-1f039600-d7a0-11eb-9f09-e569ed7719e4.png)

### Repositories

- https://github.com/mrfusionkr/intensiveSource03.git



### Table of contents

- [서비스 시나리오]

  - [기능적 요구사항]

  - [비기능적 요구사항]

  - [Microservice명]

- [분석/설계]

- [구현]

  - [DDD 의 적용]

  - [폴리글랏 퍼시스턴스]

  - [동기식 호출 과 Fallback 처리]

  - [비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트]

- [운영]

  - [Deploy]

  - [Config Map]

  - [Circuit Breaker]
  
  - [Autoscale (HPA)]

  - [Zero-Downtime deploy (Readiness Probe)] 

  - [Self-healing (Liveness Probe)]



# 서비스 시나리오

### 기능적 요구 사항

```
• 교과 담당자는 과목을 등록 할 수 있다.(과목명, 내용, 기간)
• 과목이 등록되면 교수신청 항목이 등록되며 교수 신청이 가능하다.
• 교수는 과목에 대한 담당교수를 지원(신청) 할 수 있다.
• 담당교수 신청 된 항목에 대해서 평가 담당자는 담당 교수를 검토한다.
• 평가 담당자는 과목에 대한 담당 교수를 확정하여 결과를 등록 한다.
• 확정이 완료되면 과목에 대한 확정여부와 담당 교수정보가 등록(공지) 된다.
• 교과 담당자는 과목을 취소/변경 할 수 있다.
• 교과과목이 취소 되면 교수 신청 및 확정 정보도 취소 된다.
• 교수 신청 등록/등록취소/ 확정 시 담당 교수에게 SMS 를 발송한다.
• 교수는 개인별로 신청한 과목의 현황을 조회 할 수 있다.
※ 위 시나리오는 가상의 절차로, 실제 업무와 다를 수 있습니다.
```

### 비기능적 요구 사항

```
1. 트랜잭션
  - 담당 교수가 확정되면 과목의 현재 교수 정보와 확인 하여 불일치 하는 경우에만 확정이 처리 되고 담당교수 정보가 등록(공지) 된다. (Sync 호출)
2. 장애격리
  - 교수평가 기능이 수행되지 않더라도 교과등록, 담당교수 신청은 기능은 365일 24시간 받을 수 있어야 한다. Async (event-driven), Eventual Consistency
  - 담당교수 신청 기능이 과중되면 사용자를 잠시 동안 받지 않고 담당교수 신청을 잠시후에 하도록 유도한다. Circuit breaker, fallback
3. 성능
  - 교수는 My Page 화면에서 담당교수 신청 과목의 진행 상태를 확인 할 수 있어야 한다.CQRS - 조회전용 서비스
```

### Microservice명

```
교과등록 – courseManagement
담당교수신청 - professorApplyment
담당교수평가 - professorEvaluation
문자알림이력 - notification
진행현황조회 - myPage
```


# 분석/설계

### AS-IS 조직 (Horizontally-Aligned)

![1  AS-IS조직](https://user-images.githubusercontent.com/84000922/122162394-7b1c0f80-ceae-11eb-95c4-8952596bb623.png)




### TO-BE 조직 (Vertically-Aligned)

![image](https://user-images.githubusercontent.com/70736001/124527226-0e0df100-de40-11eb-87f0-eefbb80b6631.png)






### 이벤트 도출

![image](https://user-images.githubusercontent.com/70736001/124369844-45976480-dcab-11eb-98d0-cce84b32e0a3.png)





### 부적격 이벤트 탈락

![image](https://user-images.githubusercontent.com/70736001/124370045-9b6d0c00-dcad-11eb-99c4-ba94ef261469.png)

```
- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행
- 확정 이의 제기등록됨, 담당조교 등록 : 후행 시나리오라서 제외
- 교수등급 등록됨 : 속성 정보여서 제외
- 교수신청메뉴선택됨, 등급현황 조회됨 : UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외 
```




### 액터, 커맨드 부착하여 읽기 좋게

![image](https://user-images.githubusercontent.com/70736001/124370049-a889fb00-dcad-11eb-97d4-c268f71ddd22.png)




### 어그리게잇으로 묶기

![image](https://user-images.githubusercontent.com/70736001/124370051-bfc8e880-dcad-11eb-82d8-e3ba4cb3bdfd.png)



```
- 교과관리, 담당교수신청, 담당교수확정은 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌
```




### 바운디드 컨텍스트로 묶기

![image](https://user-images.githubusercontent.com/70736001/124370058-da02c680-dcad-11eb-9b0a-082768424a01.png)

```
도메인 서열 분리
- Core Domain: 교과관리, 담당교수신청: 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 교과관리의 배포주기는 1개월 1회 미만, 담당교수신청의 배포주기는 1주일 1회 미만
 - Supporting Domain: 담당교수확정 : 경쟁력을 내기 위한 서비스이며, SLA 수준은 연간 70% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함. 
- General Domain: Notification : 알림서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)
```




### 완성된 1차 모형 (폴리시 부착, 이동 및 컨텍스트 매핑(점선은 Pub/Sub, 실선은 Req/Resp))

![image](https://user-images.githubusercontent.com/70736001/124370073-07e80b00-dcae-11eb-9d31-13bde9f7eebd.png)



### 1차 완성본에 대한 기능적 요구사항을 커버하는지 검증 (1/2)

![image](https://user-images.githubusercontent.com/70736001/124370104-90ff4200-dcae-11eb-97a5-b203d920849a.png)

```
1) 교과담당은 교과목을 등록한다. 교과목을 등록하면 담당교수신청공고가 접수된다.
2) 교수는 담당교수 신청을 등록한다. 담당교수신청이 등록되면 담당교수확정이 접수(등록)된다.
3) 평가부서는 담당교수확정을 등록한다. 담당교수확정이 등록되면 해당과목에 담당교수 정보가 등록(공지)된다.
```




### 1차 완성본에 대한 기능적 요구사항을 커버하는지 검증 (2/2)

![image](https://user-images.githubusercontent.com/70736001/124370163-2ac6ef00-dcaf-11eb-9a54-4696b2f62273.png)

```
1) 교과담당은 교과등록을 취소 할 수 있다. 
    교과등록이 취소되면 담당교수신청등록 및 담당교수확정도 취소된다.
2) 담당교수신청, 담당교수신청 취소, 담당교수확정 시 교수에게 SMS를 발송한다.
3) 교수는 담당교수신청현황을 조회 할 수 있다.
```




### 1차 완성본에 대한 비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/70736001/124370204-b0e33580-dcaf-11eb-8cb0-baaa5cafe791.png)

```
1. 트랜잭션
  - 담당교수확정결과가 등록되면 교과에 담당교수 정보가 등록되어야 한다. (Sync 호출)
2. 장애격리
  - 교과등록 기능이 수행되지 않더라도 담당교수신청 기능은 24시간 받을 수 있어야 한다. 
    Async (event-driven), Eventual Consistency
  - 담당교수신청 기능이 과중 되면 사용자를 잠시 동안 받지 않고 예약을 잠시후에 하도록 유도한다.
    Circuit breaker, fallback
3. 성능
  - 교수는 MyPage 화면에서 담당교수신청 상태를 확인 할 수 있어야 한다.CQRS - 조회전용 서비스
```




### 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/70736001/124370263-64e4c080-dcb0-11eb-9807-5e49c9a936a8.png)




# 구현:

(서비스 별 포트) 분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트 등으로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8085, 8088 이다)

```
cd courseManagement
mvn spring-boot:run

cd professorApplyment
mvn spring-boot:run 

cd professorEvaluation
mvn spring-boot:run  

cd notification
mvn spring-boot:run

cd myPage
mvn spring-boot:run

cd gateway
mvn spring-boot:run
```

## DDD 의 적용

- (Entity 예시) 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (아래 예시는 교과관리 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다.

```
package professor;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="CourseManagement_table")
public class CourseManagement {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long courseNo;
    private String title;
    private Date dueDate;
    private String courseInfo;
    private Boolean successFlag;
    private Long professorNo;
    private String professorNm;
    private String phoneNumber;

    @PostPersist
    public void onPostPersist(){
        CourseRegisted courseRegisted = new CourseRegisted();
        BeanUtils.copyProperties(this, courseRegisted);
        courseRegisted.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        CourseCompleted courseCompleted = new CourseCompleted();
        BeanUtils.copyProperties(this, courseCompleted);
        courseCompleted.publishAfterCommit();
    }

    @PostRemove
    public void onPostRemove(){
        CourseCanceled courseCanceled = new CourseCanceled();
        BeanUtils.copyProperties(this, courseCanceled);
        courseCanceled.publishAfterCommit();        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(Long courseNo) {
        this.courseNo = courseNo;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    public String getCourseInfo() {
        return courseInfo;
    }

    public void setCourseInfo(String courseInfo) {
        this.courseInfo = courseInfo;
    }
    public Boolean getSuccessFlag() {
        return successFlag;
    }

    public void setSuccessFlag(Boolean successFlag) {
        this.successFlag = successFlag;
    }
    public Long getProfessorNo() {
        return professorNo;
    }

    public void setProfessorNo(Long professorNo) {
        this.professorNo = professorNo;
    }
    public String getProfessorNm() {
        return professorNm;
    }

    public void setProfessorNm(String professorNm) {
        this.professorNm = professorNm;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
```
- (Repository 예시) Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package professor;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="courseManagements", path="courseManagements")
public interface CourseManagementRepository extends PagingAndSortingRepository<CourseManagement, Long>{
    CourseManagement findByCourseNo(Long courseNo);
}
```

- 적용 후 REST API 의 테스트

1.교과관리 서비스에서 교과목 등록(Command-POST)
```
http POST http://localhost:8081/courseManagements courseNo=1 title=국어 courseInfo=국어과목 dueDate=2021-07-01
http GET http://localhost:8081/courseManagements/1
```
![image](https://user-images.githubusercontent.com/70736001/123549913-f8e0f480-d7a5-11eb-83af-369db3133772.png)

1-1.교과관리 서비스에서 담당교수신청공고 등록 >> 담당교수신청 DB에 교과정보가 자동 등록됨(Async-Policy)
```
http GET http://localhost:8082/professorApplyments
```
![image](https://user-images.githubusercontent.com/70736001/123550049-8fadb100-d7a6-11eb-8461-dd82fe2067ae.png)


2.담당교수신청 서비스에서 담당교수 신청 등록(Command-PATCH)
```
http PATCH http://localhost:8082/professorApplyments/1 professorNo=201 professorNm=P01 phoneNumber=000-0000-0001 age=40 career=15 professorSpec=TEST
http GET http://localhost:8082/professorApplyments/1
```
![image](https://user-images.githubusercontent.com/70736001/123550122-021e9100-d7a7-11eb-8b5a-286d0796c6ea.png)

2-1. 담당교수신청 서비스에서 담당교수 신청 등록 >> 담당교수평가 DB에 신청정보 자동 등록됨(Async-Policy)
```
http GET http://localhost:8083/professorEvaluations/2
```
![image](https://user-images.githubusercontent.com/70736001/123550169-4ca00d80-d7a7-11eb-82fd-92f87146a808.png)

2-2. 담당교수신청 서비스에서 담당교수 신청 등록 >> nofication DB에 SMS 발송이력 자동 등록됨(Async-Policy)
```
http GET http://localhost:8084/smsHistories/2
```
![image](https://user-images.githubusercontent.com/70736001/123550267-bddfc080-d7a7-11eb-8ff1-b1e8a6d12944.png)


3.담당교수평가 서비스에서 평가결과 등록(Command-PATCH)
```
http PATCH http://localhost:8083/professorEvaluations/2 successFlag=true score=85
```
![image](https://user-images.githubusercontent.com/70736001/123551557-4dd43900-d7ad-11eb-8d81-0bb60ab65a05.png)

3-1. 담당교수신청 서비스에서 담당교수 신청 등록 >> 교과관리 DB에 담당교수 정보가 자동 등록됨(Sync-Req/Res)
```
http GET http://localhost:8081/courseManagements/1
```
![image](https://user-images.githubusercontent.com/70736001/123550383-29299280-d7a8-11eb-8363-0c66a3727aaa.png)

3-2. 담당교수신청 서비스에서 담당교수 신청 등록 >> 교과관리 서비스 Down(CTRL+C) 시 담당교수평가 서비스의 평가결과 등록도 실패
```
http PATCH http://localhost:8083/professorEvaluations/2 professorNo=203 successFlag=true score=90
```
![image](https://user-images.githubusercontent.com/70736001/123551489-f635cd80-d7ac-11eb-9349-e7a2ae0b4ac4.png)

4.MyPage-진행현황 조회(CQRS)
```
http GET http://localhost:8085/applyStatusInquiries/2
```
![image](https://user-images.githubusercontent.com/70736001/123550676-a7d2ff80-d7a9-11eb-8bd2-77841e0acf53.png)


5.Gateway-담당교수확정 진행 현황 조회(Gateway 8088 포트로 진입점 통일)
```
http GET http://localhost:8088/applyStatusInquiries/2
```
![image](https://user-images.githubusercontent.com/70736001/123550572-224f4f80-d7a9-11eb-8c9a-2701b308293f.png)
![image](https://user-images.githubusercontent.com/70736001/123550597-31360200-d7a9-11eb-8e75-2173e86c5851.png)

## 폴리글랏 퍼시스턴스

(H2DB, HSQLDB 사용) Notification(문자알림) 서비스는 문자알림 이력이 많이 쌓일 수 있으므로 자바로 작성된 관계형 데이터베이스인 HSQLDB를 사용하기로 하였다. 이를 위해 pom.xml 파일에 아래 설정을 추가하였다.

```
# pom.xml
<dependency>
	<groupId>org.hsqldb</groupId>
	<artifactId>hsqldb</artifactId>
	<scope>runtime</scope>
</dependency>
```
HSQL DB에 SMS 발송이력 저장됨
```
http GET http://localhost:8084/smsHistories/2
```
![image](https://user-images.githubusercontent.com/70736001/123551654-bb806500-d7ad-11eb-9121-7f1493042b92.png)


- 교과관리, 담당교수신청, 담당교수평가 등 나머지 서비스는 H2 DB를 사용한다.
```
<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<scope>runtime</scope>
</dependency>
```

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 교수확정결과등록(교수평가)->담당교수정보등록(교과관리) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- (동기호출-Req)낙찰자정보 등록 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 
```
# (ProfessorEvaluation) CourseManagementService.java
package professor.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="courseManagement", url="http://localhost:8081", fallback=CourseCreationServiceFallback.class)
public interface CourseManagementService {
    @RequestMapping(method= RequestMethod.GET, path="/courseManagements/completeCourse")
    public boolean completeCourse(@RequestParam("courseNo") Long courseNo
                            , @RequestParam("professorNo") Long professNo
                            , @RequestParam("professorNm") String professNm
                            , @RequestParam("phoneNumber") String phoneNumber
    );
}
```

- (Fallback) 담당교수정보 등록 서비스가 정상적으로 호출되지 않을 경우 Fallback 처리
```
# (ProfessorEvaluation) CourseCreationServiceFallback.java
package professor.external;

import org.springframework.stereotype.Component;

@Component
public class CourseCreationServiceFallback implements CourseManagementService{

    @Override
    public boolean completeCourse (Long courseNo, Long professorNo, String professorNm, String phoneNumber){
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@@@@@@@@@@@@@Circuit Breaker is Running!! Fallback Returned Value.@@@@@@@@@@@@@@@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        return false;
    }
}
```

```
feign:
  hystrix:
    enabled: true
```

- (동기호출-Res) 담당교수 선정결과 등록 서비스 (정상 호출)
```
# (CourseManagement) CourseManagementController.java
package professor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 @RestController
 public class CourseManagementController {
    @Autowired
    CourseManagementRepository courseManagementRepository;

    @RequestMapping(value = "/courseManagements/completeCourse",
       method = RequestMethod.GET,
       produces = "application/json;charset=UTF-8")
    public boolean completeCourse(HttpServletRequest request, HttpServletResponse response) {
       boolean status = false;

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
    }
 }
```

- (동기호출-PreUpdate) 평가결과가 등록 된 직후(@PostUpdate) 담당교수정보 등록을 요청하도록 처리 (담당교수가 이미 있거나, 동일한 사람이면 이후 로직 스킵)
```
# ProfessorEvaluation.java (Entity)
    @PreUpdate
    public void onPreUpdate() throws Exception{
        // 담당교수 확정 조건이 충족되지 않으면 확정하지 않는다.
        System.out.print("getSuccessFlag() = " + this.getSuccessFlag().toString());
        if (getSuccessFlag() == false){
            return;
        }

        try{
            boolean isUpdateYn = ProfessorEvaluationApplication.applicationContext.getBean(professor.external.CourseManagementService.class)
                .completeCourse(getCourseNo(), getProfessorNo(), getProfessorNm(), getPhoneNumber());

            if (isUpdateYn == false){
                throw new Exception ("교과등록의 담당교수 정보가 업데이트 되지 않음");
            }

            ProfessorConfirmed professorConfirmed = new ProfessorConfirmed();
            BeanUtils.copyProperties(this, professorConfirmed);
            professorConfirmed.publishAfterCommit();
        } catch (ConnectException ce){
            throw new Exception ("교과 등록 서비스 연결 실패");
        } catch (Exception e){
            throw new Exception ("교과 등록 서비스 실행 실패");
        }
    }
```

- (동기호출-테스트) 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 입찰관리 시스템이 장애가 나면 입찰심사 등록도 못 한다는 것을 확인:

```
#교과관리(courseManagement) 서비스를 잠시 내려놓음 (ctrl+c)

#담당교수확정결과 등록 : Fail
http PATCH http://localhost:8083/professorEvaluations/2 successFlag=true score=85

#교과관리 서비스 재기동 및 교과목 재등록
cd courseManagement
mvn spring-boot:run
http POST http://localhost:8081/courseManagements courseNo=1 title=국어 courseInfo=국어과목 dueDate=2021-07-01

#심사결과 등록 : Success
http PATCH http://localhost:8083/professorEvaluations/2 successFlag=true score=85
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


교과목이 등록된 후에 담당교수신청 시스템에 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 담당교수신청 시스템의 처리를 위하여 교과등록 트랜잭션이 블로킹 되지 않도록 처리한다.
 
- (Publish) 이를 위하여 교과등록 기록을 남긴 후에 곧바로 등록 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
@Entity
@Table(name="CourseManagement_table")
public class CourseManagement {

 ...
    @PostPersist
    public void onPostPersist(){
        CourseRegisted courseRegisted = new CourseRegisted();
        BeanUtils.copyProperties(this, courseRegisted);
        courseRegisted.publishAfterCommit();
    }
```
- (Subscribe-등록) 담당교수신청 서비스에서는 교과등록됨 이벤트를 수신하면 담당교수신청 번호를 등록하는 정책을 처리하도록 PolicyHandler를 구현한다:

```
@Service
public class PolicyHandler{

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseRegisted_ReceiveCourseNotice(@Payload CourseRegisted courseRegisted){

        if(!courseRegisted.validate()) return;
        
        System.out.println("\n\n##### listener ReceiveCourseNotice : " + courseRegisted.toJson() + "\n\n");
	
        ProfessorApplyment professorApplyment = new ProfessorApplyment();
        professorApplyment.setCourseNo(courseRegisted.getCourseNo());

        professorApplymentRepository.save(professorApplyment);        
    }

```
- (Subscribe-취소) 담당교수신청 서비스에서는 교과가 취소됨 이벤트를 수신하면 담당교수신청 정보를 삭제하는 정책을 처리하도록 PolicyHandler를 구현한다:
  
```
@Service
public class PolicyHandler{
    @Autowired ProfessorApplymentRepository professorApplymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCourseCanceled_CancelApplyment(@Payload CourseCanceled courseCanceled){

        if(!courseCanceled.validate()) return;
        
        System.out.println("\n\n##### listener CancelApplyment : " + courseCanceled.toJson() + "\n\n");
        ProfessorApplyment professorApplyment = professorApplymentRepository.findByCourseNo(courseCanceled.getCourseNo());

        professorApplymentRepository.delete(professorApplyment);
    }

```

- (장애격리) 교과관리, 담당교수신청 시스템은 담당교수평가 시스템과 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에, 담당교수평가 시스템이 유지보수로 인해 잠시 내려간 상태라도 교과관리, 담당교수신청 서비스에 영향이 없다:
```
# 담당교수평가 서비스 (professorEvaluation) 를 잠시 내려놓음 (ctrl+c)

#교괴등록 : Success
http POST http://localhost:8081/courseManagements courseNo=1 title=국어 courseInfo=국어과목 dueDate=2021-07-01
#담당교수신청 등록 : Success
http PATCH http://localhost:8082/professorApplyments/1 professorNo=201 professorNm=P01 phoneNumber=000-0000-0001 age=40 career=15 professorSpec=TEST

#교과등록에서 교과목 등록 및 담당교수 신청 등록 확인
http GET http://localhost:8081/courseManagements/1     # 교과목 등록 됨확인
http GET http://localhost:8082/professorApplyments/1   # 담당교수신청 등록 됨확인

#담당교수평가 서비스 기동
cd professorEvaluation
mvn spring-boot:run

#담당교수확정 등록 : Success
http PATCH http://localhost:8083/professorEvaluations/2 successFlag=true score=85

#교과관리에서 담당교수정보 갱신 여부 확인
http GET http://localhost:8081/courseManagements/1     # 담당교수정보 갱신됨 확인
```

# 운영:

컨테이너화된 마이크로서비스의 자동 배포/조정/관리를 위한 쿠버네티스 환경 운영

## Deploy

- GitHub 와 연결 후 로컬빌드를 진행 진행
```
	cd personal	
	git clone --recurse-submodules https://github.com/mrfusionkr/intensiveSource03.git
	
	cd intensiveSource03
	cd personal/intensiveSource03/courseManagement
	mvn package
	
	cd personal/intensiveSource03/professorApplyment
	mvn package
	
	cd personal/intensiveSource03/professorEvaluation
	mvn package
	
	cd personal/intensiveSource03/notification
	mvn package
	
	
	cd personal/intensiveSource03/myPage
	mvn package
	
	
	cd personal/intensiveSource03/gateway
        mvn package
```
- namespace 등록 및 변경
```
kubectl config set-context --current --namespace=professor  --> professor namespace 로 변경
kubectl create ns professor
```
- 서비스별 이미지 정보 수정(deployment.yml)
```
image: user11skccacr.azurecr.io/coursemanagement:v1.0
image: user11skccacr.azurecr.io/professorapplyment:v1.1
image: user11skccacr.azurecr.io/professorEvaluation:v1.0
image: user11skccacr.azurecr.io/notification:v1.0
image: user11skccacr.azurecr.io/myPage:v1.0
image: user11skccacr.azurecr.io/courseManagement:v1.0

```

- ACR 컨테이너이미지 빌드
```
az acr build --registry user11skccacr --image user11skccacr.azurecr.io/coursemanagement:v1.0 .
```
![image](https://user-images.githubusercontent.com/70736001/124411005-95972980-dd86-11eb-8d0f-eb48c6cf50c5.png)
![image](https://user-images.githubusercontent.com/70736001/124411028-9f209180-dd86-11eb-85a3-c4c97d74fa73.png)


나머지 서비스에 대해서도 동일하게 등록을 진행함
```
az acr build --registry user11skccacr --image user11skccacr.azurecr.io/professorapplyment:v1.0 .
az acr build --registry user11skccacr --image user11skccacr.azurecr.io/professorevaluation:v1.0 .
az acr build --registry user11skccacr --image user11skccacr.azurecr.io/notification:v1.0  .
az acr build --registry user11skccacr --image user11skccacr.azurecr.io/mypage:v1.0  .
az acr build --registry user11skccacr --image user11skccacr.azurecr.io/gateway:v1.0 .
```
![image](https://user-images.githubusercontent.com/70736001/124413067-ddb84b00-dd8a-11eb-82fe-925d9f91fd33.png)


- 배포진행

1.bidding/BiddingExamination/kubernetes/deployment.yml 파일 수정 (BiddingManagement/BiddingParticipation/MyPage/Notification/gateway 동일)

![image](https://user-images.githubusercontent.com/70736001/124417678-a51d6f00-dd94-11eb-8b71-8468148ae227.png)

2.bidding/BiddingExamination/kubernetes/service.yaml 파일 수정 (BiddingManagement/BiddingParticipation/MyPage/Notification 동일)

![image](https://user-images.githubusercontent.com/70736001/124417692-b0709a80-dd94-11eb-9050-d0095b47f4e3.png)

3.bidding/gateway/kubernetes/service.yaml 파일 수정

![image](https://user-images.githubusercontent.com/70736001/124417721-c3836a80-dd94-11eb-91fd-a02e66a32d09.png)

4. 배포작업 수행
``` 
	cd courseManagement/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	cd ../../professorApplyment/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	cd ../../professorEvaluation/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../notification/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../myPage/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
	
	
	cd ../../gateway/kubernetes
	kubectl apply -f deployment.yml
	kubectl apply -f service.yaml
``` 

5. 배포결과 확인
``` 
kubectl get all
``` 
![image](https://user-images.githubusercontent.com/70736001/124417605-71424980-dd94-11eb-8621-b9d697754394.png)

- Kafka 설치
``` 
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh

kubectl --namespace kube-system create sa tiller 
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
helm init --service-account tiller

helm repo add incubator https://charts.helm.sh/incubator
helm repo update

kubectl create ns kafka
helm install --name my-kafka --namespace kafka incubator/kafka

kubectl get all -n kafka
``` 

- Topic 생성 및 확인
``` 
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --topic courseTP --create --partitions 1 --replication-factor 1
kubectl -n kafka exec my-kafka-0 -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --list
``` 
설치 후 서비스 재기동


## Config Map
professorevaluation:v1.2
ConfigMap을 사용하여 변경가능성이 있는 설정을 관리

- 담당교수평가(professorEvaluation) 서비스에서 동기호출(Req/Res방식)로 연결되는 교과관리(courseManagement) 서비스 url 정보 일부를 ConfigMap을 사용하여 구현

- 파일 수정
  - 담당교수평가 소스 (professorEvaluation/src/main/java/professor/external/CourseManagementService.java)

![image](https://user-images.githubusercontent.com/70736001/124429342-ba03fd80-dda8-11eb-8475-c0515b897592.png)

- Yaml 파일 수정
  - application.yml (BiddingExamination/src/main/resources/application.yml)
  - deploy yml (BiddingExamination/kubernetes/deployment.yml)

![image](https://user-images.githubusercontent.com/70736001/124429408-d011be00-dda8-11eb-9943-9d5959e90033.png)
![image](https://user-images.githubusercontent.com/70736001/124429930-7958b400-dda9-11eb-8ae7-51b47ceafc46.png)


- Config Map 생성 및 생성 확인
```
kubectl create configmap professor-cm --from-literal=url=CourseManagement
kubectl get cm
```

![image](https://user-images.githubusercontent.com/70736001/124430071-aad17f80-dda9-11eb-98a5-8ce2d99ca685.png)

```
kubectl get cm professor-cm -o yaml
```

![image](https://user-images.githubusercontent.com/70736001/124430231-e0766880-dda9-11eb-80df-24e2a77ca228.png)

```
kubectl get pod
```

![image](https://user-images.githubusercontent.com/70736001/124430351-06037200-ddaa-11eb-87b4-3e463c7e9dde.png)

- Req/Rep 테스트
```
http PATCH http://52.231.9.211:8080/professorEvaluations/1 successFlag=true score=85
http GET http://52.231.9.211:8080/courseManagements/1
```
![image](https://user-images.githubusercontent.com/70736001/124430565-4bc03a80-ddaa-11eb-80d6-6e53b8acba1e.png)
![image](https://user-images.githubusercontent.com/70736001/124430940-bffade00-ddaa-11eb-8067-4caceec745b1.png)


## Circuit Breaker
professorevaluation:v1.5
coursemanagement:v1.8
서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함
시나리오는 당당교수확정등록(담당교수평가:ProfessorEvaluation)--> 담당교수정보덩록(교과관리:CourseManagement) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 담당교수정보등록이 과도할 경우 CB 를 통하여 장애격리.


- Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 1000ms가 넘어서기 시작하면 CB 작동하도록 설정

**application.yml (ProfessorEvaluation)**
```
feign:
  hystrix:
    enabled: true

hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 1000
```
![image](https://user-images.githubusercontent.com/70736001/124458124-9603e480-ddc7-11eb-9392-73b116d52d7e.png)

- 피호출 서비스(교과관리:coursemanagement) 의 임의 부하 처리 - 800ms에서 증감 300ms 정도하여 800~1100 ms 사이에서 발생하도록 처리
CourseManagementController.java
```
req/res를 처리하는 피호출 function에 sleep 추가

	try {
            Long lSleepTime = (long)(800 + Math.random() * 300);
            Thread.sleep(lSleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
```
![image](https://user-images.githubusercontent.com/70736001/124458375-d9f6e980-ddc7-11eb-878d-3e4a2043fe0a.png)

- req/res 호출하는 위치가 onPostUpdate에 있어 실제로 Data Update가 발생하지 않으면 호출이 되지 않는 문제가 있어 siege를 2개 실행하여 Update가 지속적으로 발생하게 처리 함
```
siege -c2 –t20S  -v --content-type "application/json" 'http://professorEvaluation:8080/professorEvaluations/3 PATCH {"courseNo":"1","professorNo":"204","professorNm":"P04","score":"80","successFlag":"true"}'
siege -c2 –t20S  -v --content-type "application/json" 'http://professorEvaluation:8080/professorEvaluations/3 PATCH {"courseNo":"1","professorNo":"205","professorNm":"P05","score":"85","successFlag":"true"}'
```
![image](https://user-images.githubusercontent.com/70736001/124458694-3f4ada80-ddc8-11eb-82ab-243837a012a5.png)
![image](https://user-images.githubusercontent.com/70736001/124458794-61445d00-ddc8-11eb-8195-c4d2e3242007.png)



## Autoscale (HPA)
coursemanagement:v1.9
professorevaluation:v1.6(원복)
앞서 CB(Circuit breaker)는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.

- 리소스에 대한 사용량 정의(professor/courseManagement/kubernetes/deployment.yml)
![image](https://user-images.githubusercontent.com/70736001/122503960-49cd4c00-d034-11eb-8ab4-b322e7383cc0.png)

- Autoscale 설정 (request값의 20%를 넘어서면 Replica를 10개까지 동적으로 확장)
```
kubectl autoscale deployment coursemanagement --cpu-percent=20 --min=1 --max=10
```

- siege 생성 (로드제너레이터 설치)
```
kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: siege
  namespace: professor
spec:
  containers:
  - name: siege
    image: apexacme/siege-nginx
EOF
```
- 부하발생 (50명 동시사용자, 30초간 부하)
```
kubectl exec -it pod/siege -c siege -n professor -- /bin/bash
siege -c50 -t30S -v --content-type "application/json" 'http://courseManagement:8080/courseManagements POST {"courseNo":1,"title":"국어","courseInfo":"국어과목","dueDate":"2021-07-10"}'
```
- 모니터링 (부하증가로 스케일아웃되어지는 과정을 별도 창에서 모니터링)
```
watch kubectl get all
```
- 자동스케일아웃으로 Availablity 100% 결과 확인 (시간이 좀 흐른 후 스케일 아웃이 벌어지는 것을 확인, siege의 로그를 보아도 전체적인 성공률이 높아진 것을 확인함)

1.테스트전

![image](https://user-images.githubusercontent.com/70736001/124522323-d138fe80-de2d-11eb-9a50-79b3a0801f0e.png)

2.테스트후

![image](https://user-images.githubusercontent.com/70736001/124522328-d6964900-de2d-11eb-88ce-341a667119b3.png)

3.부하발생 결과

![image](https://user-images.githubusercontent.com/70736001/124522364-faf22580-de2d-11eb-93d7-35cb44ac9b93.png)


## Zero-Downtime deploy (Readiness Probe)
쿠버네티스는 각 컨테이너의 상태를 주기적으로 체크(Health Check)해서 문제가 있는 컨테이너는 서비스에서 제외한다.

- deployment.yml에 readinessProbe 설정 후 미설정 상태 테스트를 위해 주석처리함 
![image](https://user-images.githubusercontent.com/70736001/124522931-db5bfc80-de2f-11eb-9d9a-c4fce7046b00.png)

- deployment.yml에서 readinessProbe 미설정 상태로 siege 부하발생
```
kubectl exec -it pod/siege -c siege -n professor -- /bin/bash
siege -c50 -t30S -v --content-type "application/json" 'http://courseManagement:8080/courseManagements POST {"courseNo":1,"title":"국어","courseInfo":"국어과목","dueDate":"2021-07-10"}'
```
1.부하테스트 전

![image](https://user-images.githubusercontent.com/70736001/124522962-ffb7d900-de2f-11eb-9f27-7a7f2d707d51.png)

2.부하테스트 후

![image](https://user-images.githubusercontent.com/70736001/124522970-06465080-de30-11eb-94a8-2147ec982184.png)

3.생성중인 Pod 에 대한 요청이 들어가 오류발생

![image](https://user-images.githubusercontent.com/70736001/124522976-0cd4c800-de30-11eb-8cd5-de3602a86962.png)

- 정상 실행중인 biddingmanagement으로의 요청은 성공(201),비정상 적인 요청은 실패 확인

- hpa 설정에 의해 target 지수 초과하여 biddingmanagement scale-out 진행됨

- deployment.yml에 readinessProbe 설정 후 부하발생 및 Availability 100% 확인

![image](https://user-images.githubusercontent.com/70736001/124523280-67baef00-de31-11eb-874e-4f8924b31d5d.png)

1.부하테스트 전

![image](https://user-images.githubusercontent.com/70736001/124523293-70132a00-de31-11eb-8e2a-358646ff6b4e.png)

2.부하테스트 후

![image](https://user-images.githubusercontent.com/70736001/124523299-799c9200-de31-11eb-94b3-6edeb958150e.png)

3.readiness 정상 적용 후, Availability 100% 확인

![image](https://user-images.githubusercontent.com/70736001/124523304-8325fa00-de31-11eb-8968-b829a2bc8a3b.png)


## Self-healing (Liveness Probe)
쿠버네티스는 각 컨테이너의 상태를 주기적으로 체크(Health Check)해서 문제가 있는 컨테이너는 자동으로재시작한다.

- depolyment.yml 파일의 path 및 port를 잘못된 값으로 변경
  depolyment.yml(courseManagement/kubernetes/deployment.yml)
```
 livenessProbe:
    httpGet:
        path: '/coursemanagement/failed'
        port: 8090
      initialDelaySeconds: 30
      timeoutSeconds: 2
      periodSeconds: 5
      failureThreshold: 5
```




![image](https://user-images.githubusercontent.com/70736001/122506714-d75f6a80-d039-11eb-8bd0-223490797b58.png)

- liveness 설정 적용되어 컨테이너 재시작 되는 것을 확인
  Retry 시도 확인 (pod 생성 "RESTARTS" 숫자가 늘어나는 것을 확인) 

1.배포 전

![image](https://user-images.githubusercontent.com/70736001/124523695-16abfa80-de33-11eb-8b7d-582e1c37d9cb.png)

2.배포 후

![image](https://user-images.githubusercontent.com/70736001/124523692-11e74680-de33-11eb-94ab-c2e2249b5106.png)
