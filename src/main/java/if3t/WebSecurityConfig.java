package if3t;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
    private UserDetailsService userDetailsService;
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
  
        http
        	.httpBasic()
        	
        	.and()
        	.authorizeRequests()
        	.antMatchers(HttpMethod.POST, "/signup").permitAll()
        	.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        	.anyRequest().authenticated()
        	
        	.and()
        	.logout();
        
        http.csrf().disable();
        
        /*
        	.and()
	    	.addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
	    	.csrf().csrfTokenRepository(csrfTokenRepository());
	    */
    }
    
    /*
    private CsrfTokenRepository csrfTokenRepository() {
    	HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
    	repository.setHeaderName("X-XSRF-TOKEN");
    	return repository;
    }
    */

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	auth.userDetailsService(userDetailsService);
    }
}
