package if3t;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import if3t.exceptions.ChannelNotAuthorizedException;


@ControllerAdvice  
@RestController  
public class MyExceptionHandler {  
  
    @ResponseStatus(HttpStatus.FORBIDDEN)  
    @ExceptionHandler(value = ChannelNotAuthorizedException.class)  
    public String handleBaseException(ChannelNotAuthorizedException e){  
        return e.getMessage();  
    }  
  
    @ExceptionHandler(value = Exception.class)  
    public String handleException(Exception e){
    	return e.getMessage();
    }  
}  
