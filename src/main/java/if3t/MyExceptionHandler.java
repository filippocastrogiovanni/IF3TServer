package if3t;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import if3t.exceptions.ActionChannelNotAuthorizedException;
import if3t.exceptions.TriggerChannelNotAuthorizedException;


@ControllerAdvice  
@RestController  
public class MyExceptionHandler {  
  
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason="Trigger channel not authorized")  
    @ExceptionHandler(value = TriggerChannelNotAuthorizedException.class)  
    public void handleTriggerException(TriggerChannelNotAuthorizedException e){
    }
    
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason="Action channel not authorized")  
    @ExceptionHandler(value = ActionChannelNotAuthorizedException.class)  
    public void handleChannelException(ActionChannelNotAuthorizedException e){
    }  
  
    @ExceptionHandler(value = Exception.class)  
    public String handleException(Exception e){
    	return e.getMessage();
    }  
}  
