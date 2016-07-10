package if3t;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.NotLoggedInException;


@ControllerAdvice  
@RestController  
public class MyExceptionHandler {  
  
    @ResponseStatus(value = HttpStatus.FORBIDDEN)  
    @ExceptionHandler(value = ChannelNotAuthorizedException.class)  
    public Response handleChannelException(ChannelNotAuthorizedException e){
    	return new Response(e.getMessage(), 403);
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = NotLoggedInException.class)  
    public Response handleLogInException(NotLoggedInException e){
    	return new Response(e.getMessage(), 400);
    }
  
    @ResponseStatus(value = HttpStatus.NOT_FOUND) 
    @ExceptionHandler(value = Exception.class)  
    public Response handleException(Exception e){
    	return new Response(e.getMessage(), 404);
    }  
}  
