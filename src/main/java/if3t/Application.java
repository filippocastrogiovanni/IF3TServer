package if3t;

import java.security.Principal;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

@SpringBootApplication
@RestController
public class Application {
	
	private final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@CrossOrigin
	@RequestMapping(value = "/login")
	public Principal user(Principal user) {
		return user;
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowCredentials(true);
			}
		};
	}

	@Bean
    protected ServletContextListener listener() {

        return new ServletContextListener() {

            public void contextInitialized(ServletContextEvent sce) {
                log.info("Initialising Context...");
            }

            public final void contextDestroyed(ServletContextEvent sce) {

                log.info("Destroying Context...");

                try {
                    log.info("Calling MySQL AbandonedConnectionCleanupThread shutdown");
                    AbandonedConnectionCleanupThread.shutdown();

                } catch (InterruptedException e) {
                    log.error("Error calling MySQL AbandonedConnectionCleanupThread shutdown {}", e);
                }

                ClassLoader cl = Thread.currentThread().getContextClassLoader();

                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    Driver driver = drivers.nextElement();

                    if (driver.getClass().getClassLoader() == cl) {

                        try {
                            log.info("Deregistering JDBC driver {}", driver);
                            DriverManager.deregisterDriver(driver);

                        } catch (SQLException ex) {
                            log.error("Error deregistering JDBC driver {}", driver, ex);
                        }

                    } else {
                        log.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
                    }
                }
            }
        };
    }
	
	@Bean(name = "messageSource")
	public ReloadableResourceBundleMessageSource messageSource() 
	{
	    ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
	    messageBundle.setBasename("classpath:messages/messages");
	    messageBundle.setDefaultEncoding("UTF-8");
	    
	    return messageBundle;
	}
	
}
