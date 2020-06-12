package org.infy.mail.batch;

import javax.mail.internet.MimeMessage;

import org.infy.mail.batch.model.Employee;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Value("${spring.mail.username}")
	private String sender;

	@Value("${org.infy.mail.batch.data}")
	public String data;
	
	@Value("${org.infy.mail.batch.attachment}")
	private String attachment;
	
	@Value("${org.infy.mail.batch.campaign.attachment}")
	private String campaignAttachment;
	
	@Value("${org.infy.mail.batch.notifications.email}")
	private String email;
	
	@Value("${notificationType}")
	private String notificationType;


	@Bean
	public FlatFileItemReader<Employee> reader() {
		FlatFileItemReader<Employee> reader = new FlatFileItemReader<>();
		reader.setResource(new FileSystemResource(data));
		reader.setLinesToSkip(1);
		reader.setLineMapper(new DefaultLineMapper<Employee>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames(new String[] {"fullname", "code", "email"} );
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<Employee>(){{
				setTargetType(Employee.class);
			}});
		}});
		return reader;
	}
	
	@Bean
	public EmployeeICountItemProcessor processor() {
		if (notificationType.equalsIgnoreCase("todo"))
			return new EmployeeICountItemProcessor(sender, attachment);
		else
			return new EmployeeICountItemProcessor(sender, campaignAttachment);
	}
	
	
	@Bean
	public MailBatchItemWriter writer() {
		MailBatchItemWriter writer = new MailBatchItemWriter();
		return writer;
	}

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener(email);
    }

    @Bean
    public Job importUserJob() {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Employee, MimeMessage> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
   
}
