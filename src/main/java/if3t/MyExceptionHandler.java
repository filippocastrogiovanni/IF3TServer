package if3t;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.InvalidParametersException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotLoggedInException;
import if3t.exceptions.WrongPasswordException;
import if3t.models.Response;


@ControllerAdvice  
@RestController
public class MyExceptionHandler 
{    
    @ResponseStatus(value = HttpStatus.FORBIDDEN)  
    @ExceptionHandler(value = ChannelNotAuthorizedException.class)  
    public Response handleChannelException(ChannelNotAuthorizedException e) {
    	return new Response(e.getMessage(), HttpStatus.FORBIDDEN.value());
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = NotLoggedInException.class)  
    public Response handleLogInException(NotLoggedInException e) {
    	return new Response(e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }
    
    //Cambiato 401 in 400 (lo segnalo perchè magari aveva un scopo preciso)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = InvalidParametersException.class)  
    public Response handleInvalidParametersException(InvalidParametersException e) {
    	return new Response(e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }
    
    @ResponseStatus(value = HttpStatus.CONFLICT)  
    @ExceptionHandler(value = WrongPasswordException.class)  
    public Response handlePasswordException(WrongPasswordException e) {
    	return new Response(e.getMessage(), HttpStatus.CONFLICT.value());
    }
    
    @ResponseStatus(value = HttpStatus.FORBIDDEN)  
    @ExceptionHandler(value = NoPermissionException.class)  
    public Response handleNoPermissionException(NoPermissionException e) {
    	return new Response(e.getMessage(), HttpStatus.FORBIDDEN.value());
    }
  
    //Cambiato 404 in 500 visto che cattura la generica Exception
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR) 
    @ExceptionHandler(value = Throwable.class)  
    public Response handleException(Throwable e) {
    	return new Response(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    } 
}  