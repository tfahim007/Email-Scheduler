package com.tahmid.timer.job;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Component
public class EmailJob extends QuartzJobBean {
    private static final Logger logger = LoggerFactory.getLogger(EmailJob.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProperties mailProperties;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String recipientEmail = jobDataMap.getString("email");
        sendMail(mailProperties.getUsername(),recipientEmail,subject,body);
    }

    private void sendMail(String fromEmail, String toEmail, String subject, String body){
        logger.info("Sending Email");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
        try {
            messageHelper.setSubject(subject);
            messageHelper.setText(body,true);
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(toEmail);

            mailSender.send(message);

            logger.info("Email Sent");
        } catch (MessagingException e) {
            logger.info("Failed Sending an Email");
            throw new RuntimeException(e);
        }

    }
}
