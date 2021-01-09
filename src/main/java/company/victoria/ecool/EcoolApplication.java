package company.victoria.ecool;

import company.victoria.ecool.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EntityScan(basePackageClasses = {
        EcoolApplication.class,
        Jsr310JpaConverters.class
})
public class EcoolApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoolApplication.class, args);
    }

}
