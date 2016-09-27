package if3t;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.http.HttpResponseException;

import org.springframework.web.bind.annotation.ExceptionHandler;

import if3t.exceptions.AddRecipeException;
import if3t.exceptions.ChannelNotAuthorizedException;
import if3t.exceptions.InvalidParametersException;
import if3t.exceptions.NoPermissionException;
import if3t.exceptions.NotFoundRecipeException;
import if3t.exceptions.NotLoggedInException;
import if3t.exceptions.PartialUpdateException;
import if3t.exceptions.WrongPasswordException;
import if3t.models.Response;

@ControllerAdvice  
@RestController
public class MyExceptionHandler 
{    
	@Autowired
	private MessageSource msgSource;
	private final static String VAL_ERROR = "There has been an error during the validation phase";
	private final static String GENERIC_VAL_ERROR = "There have been some errors during the validation phase";
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
	
    @ResponseStatus(value = HttpStatus.FORBIDDEN)  
    @ExceptionHandler(value = ChannelNotAuthorizedException.class)  
    public Response handleChannelException(ChannelNotAuthorizedException e) {
    	return new Response(e.getMessage(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = NotLoggedInException.class)  
    public Response handleLogInException(NotLoggedInException e) {
    	return new Response(e.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = AddRecipeException.class)  
    public Response handleAddRecipeException(AddRecipeException e){
    	return new Response(e.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = InvalidParametersException.class)  
    public Response handleInvalidParametersException(InvalidParametersException e) {
    	return new Response(e.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.CONFLICT)  
    @ExceptionHandler(value = WrongPasswordException.class)  
    public Response handlePasswordException(WrongPasswordException e) {
    	return new Response(e.getMessage(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.FORBIDDEN)  
    @ExceptionHandler(value = NoPermissionException.class)  
    public Response handleNoPermissionException(NoPermissionException e) {
    	return new Response(e.getMessage(), HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.NOT_FOUND)  
    @ExceptionHandler(value = NotFoundRecipeException.class)  
    public Response handleNotFoundRecipeException(NotFoundRecipeException e) {
    	return new Response(e.getMessage(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = PartialUpdateException.class)  
    public Response handlePartialUpdateException(PartialUpdateException e) {
    	return new Response(e.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)  
    @ExceptionHandler(value = HttpResponseException.class)  
    public Response handleHttpResponseException(HttpResponseException e) {
    	return new Response(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
  
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)  
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Response handleUpdateValidationException(MethodArgumentNotValidException e) 
    {
    	StringBuffer sb = new StringBuffer();
    	Locale currentLocale = LocaleContextHolder.getLocale();
    	BindingResult br = e.getBindingResult();
    	
    	for (FieldError err : br.getFieldErrors())
    	{
    		try
    		{
	    		sb.append(msgSource.getMessage(err.getDefaultMessage(), null, currentLocale));
	    		sb.append("<br>");
    		}
    		catch (NoSuchMessageException nsme)
    		{
    			sb.append(VAL_ERROR);
    			sb.append("<br>");
    		}
    	}
    	
    	sb.delete(sb.length() - 4, sb.length());
    	return new Response((sb.length() == 0) ? GENERIC_VAL_ERROR : sb.toString(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }
    
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR) 
    @ExceptionHandler(value = Throwable.class)  
    public Response handleException(Throwable t) 
    {
    	logger.error(t.getMessage(), t);
    	return new Response("", HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    } 
}  