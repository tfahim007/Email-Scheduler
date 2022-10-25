package com.tahmid.timer.controller;

import com.tahmid.timer.job.EmailJob;
import com.tahmid.timer.payload.EmailRequest;
import com.tahmid.timer.payload.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RestController
public class EmailSchedulerController {

    @Autowired
    private Scheduler scheduler;
    @GetMapping
    public String hello(){
        return "Hello";
    }
    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@RequestBody EmailRequest emailRequest){
        try{
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(),emailRequest.getTimeZone());
            if(dateTime.isBefore(ZonedDateTime.now())){
                EmailResponse emailResponse = new EmailResponse(false,"Time must be before schedule");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(emailResponse);
            }
            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail,dateTime);
            scheduler.scheduleJob(jobDetail,trigger);
            EmailResponse emailResponse = new EmailResponse(
                    true,jobDetail.getKey().getName(),jobDetail.getKey().getGroup(),
                    "Email Scheduled"
            );

            return ResponseEntity.ok(emailResponse);
        } catch (SchedulerException e) {
            log.error("Error while scheduling");
            EmailResponse emailResponse = new EmailResponse(false,"Error while Scheduling");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(emailResponse);
        }




    }

    private JobDetail buildJobDetail(EmailRequest emailRequest){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email",emailRequest.getEmail());
        jobDataMap.put("subject",emailRequest.getSubject());
        jobDataMap.put("body",emailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString())
                .withDescription("Send an email")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(),"email-triggers")
                .withDescription("Send Email Trigger")
                .startAt((Date.from(startAt.toInstant())))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

    }
}
