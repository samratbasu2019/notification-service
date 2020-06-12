package org.infy.mail.batch;

import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.infy.mail.batch.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

public class EmployeeICountItemProcessor implements ItemProcessor<Employee, MimeMessage> {

	private static final Logger log = LoggerFactory.getLogger(EmployeeICountItemProcessor.class);

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private VelocityEngine engine;
	private String sender;
	private String attachment;

	@Value("${notificationType}")
	private String notificationType;

	public EmployeeICountItemProcessor(String sender, String attachment) {
		this.sender = sender;
		this.attachment = attachment;
	}

	@Override
	public MimeMessage process(Employee employee) throws Exception {
		MimeMessage message = mailSender.createMimeMessage();
		log.info("Notification Type : " + notificationType);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		Map<String, Object> model = new HashMap<>();
		model.put("name", employee.getFullname());
		model.put("code", employee.getCode());
		helper.setFrom(sender);
		helper.setTo(employee.getEmail());
		helper.setCc(sender);
		if (notificationType.equalsIgnoreCase("todo")) {
			helper.setSubject(VelocityEngineUtils.mergeTemplateIntoString(engine, "email-subject.vm", "UTF-8", model));
			helper.setText(VelocityEngineUtils.mergeTemplateIntoString(engine, "email-body.vm", "UTF-8", model));
		} else {
			helper.setSubject(
					VelocityEngineUtils.mergeTemplateIntoString(engine, "email-subject-campaign.vm", "UTF-8", model));
			helper.setText(
					VelocityEngineUtils.mergeTemplateIntoString(engine, "email-body-campaign.vm", "UTF-8", model));
		}
		log.info("Preparing message for: " + employee.getEmail());

		FileSystemResource file = new FileSystemResource(attachment);
		helper.addAttachment(file.getFilename(), file);

		return message;
	}

}
