package if3t.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.springframework.beans.factory.annotation.Autowired;

import if3t.models.ParametersPOJO;
import if3t.services.CreateRecipeService;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Parameters.ParametersValidator.class)
public @interface Parameters
{
	String message() default "error.parameters.generic.notvalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    ValidationMode mode() default ValidationMode.TRIGGER;
    
    public enum ValidationMode { TRIGGER, ACTION }
	
	class ParametersValidator implements ConstraintValidator<Parameters, List<ParametersPOJO>>
	{
		@Autowired
		private CreateRecipeService crService;
		private ValidationMode mode;
		private static final String UNCHEKED_RADIO = "unchecked_radio_button";
		private static final String UNCHEKED_CHECKBOX = "unchecked_checkbox_button";
		private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
		private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
		private static final Pattern EMAIL_PATTERN = Pattern.compile("^[-a-z0-9~!$%^&*_=+}{\'?]+(.[-a-z0-9~!$%^&*_=+}{\'?]+)*@([a-z0-9_][-a-z0-9_]*(.[-a-z0-9_]+)*.(aero|arpa|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro|travel|mobi|[a-z][a-z])|([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}))(:[0-9]{1,5})?$");
		
		@Override
		public void initialize(Parameters annotation) 
		{
			this.mode = annotation.mode();
		}

		@Override
		public boolean isValid(List<ParametersPOJO> params, ConstraintValidatorContext context) 
		{		
			String realName;
			int numRadioOptions = 0;
			int numChoosenRadio = 0;
			int numCheckboxes = 0;
			int numChoosenCheckboxes = 0;
			
			for (ParametersPOJO par : params)
			{
				if (par.getId() == null)
				{
					context.disableDefaultConstraintViolation();
			        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.id.null").addConstraintViolation();
			        return false;
				}
				
				if (par.getName() == null)
				{
					context.disableDefaultConstraintViolation();
			        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.name.null").addConstraintViolation();
			        return false;
				}
				
				if (par.getType() == null)
				{
					context.disableDefaultConstraintViolation();
			        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.type.null").addConstraintViolation();
			        return false;
				}
				
				if (par.getValue() == null)
				{
					context.disableDefaultConstraintViolation();
			        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.value.null").addConstraintViolation();
			        return false;
				}
				
				
				switch (par.getType().toLowerCase())
				{
					case "radio":
					{
						realName = (mode == ValidationMode.TRIGGER) ? crService.readParameterTrigger(par.getId()).getName() : crService.readParameterAction(par.getId()).getName();
						
						if (!par.getName().equals(realName) && !par.getName().equals(UNCHEKED_RADIO))
						{
							context.disableDefaultConstraintViolation();
					        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.radio.mismatch").addConstraintViolation();
					        return false;
						}
						
						numRadioOptions++;
						
						if (!par.getValue().equals(UNCHEKED_RADIO)) {
							numChoosenRadio++;
						}
						
						break;
					}
					case "checkbox":
					{
						realName = (mode == ValidationMode.TRIGGER) ? crService.readParameterTrigger(par.getId()).getName() : crService.readParameterAction(par.getId()).getName();
						
						if (!par.getName().equals(realName) && !par.getName().equals(UNCHEKED_CHECKBOX))
						{
							context.disableDefaultConstraintViolation();
					        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.checkbox.mismatch").addConstraintViolation();
					        return false;
						}
						
						numCheckboxes++;
						
						if (!par.getValue().equals(UNCHEKED_CHECKBOX)) {
							numChoosenCheckboxes++;
						}
						
						break;
					}
					case "email": 
					{					
					
						if (!EMAIL_PATTERN.matcher(par.getValue()).matches())
						{
							context.disableDefaultConstraintViolation();
					        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.email.notvalid").addConstraintViolation();
					        return false;
						}
							
						break;
					}
					case "date":
					{			
						Date date = null;
							
						try
						{
							date = DATE_FORMAT.parse(par.getValue());
								
							if (!par.getValue().equals(DATE_FORMAT.format(date))) {
						        date = null;
						    }
						}
						catch (ParseException e)
						{
							date = null;
						}
							
						if (date == null) 
						{	
							context.disableDefaultConstraintViolation();
					        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.date.notvalid").addConstraintViolation();
					        return false;
						}
						
						break;
					}
					case "time":
					{			
						Date time = null;
							
						try
						{
							time = TIME_FORMAT.parse(par.getValue());
							
							if (!par.getValue().equals(TIME_FORMAT.format(time))) {
						        time = null;
						    }
						}
						catch (ParseException e)
						{
							time = null;
						}
							
						if (time == null)
						{
							context.disableDefaultConstraintViolation();
					        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.time.notvalid").addConstraintViolation();
					        return false;
						}
						
						break;
					}
					case "number":
					{					
						try
						{
							Integer.parseInt(par.getValue());
						}
						catch (NumberFormatException e)
						{
							context.disableDefaultConstraintViolation();
						    context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.number.notvalid").addConstraintViolation();
						    return false;
						}
						
						break;
					}
					case "text":
					case "textarea":
					{
						break;
					}
					default:
					{
						System.out.println(par.getType());
						context.disableDefaultConstraintViolation();
					    context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.type.notvalid").addConstraintViolation();
					    return false;
					}
				}
			}
			
			if (numRadioOptions > 0 && numChoosenRadio != 1)
			{
				context.disableDefaultConstraintViolation();
		        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.radio.notvalid").addConstraintViolation();
		        return false;
			}
			
			if (numCheckboxes > 0 && numChoosenCheckboxes == 0)
			{
				context.disableDefaultConstraintViolation();
		        context.buildConstraintViolationWithTemplate("error." + mode.toString().toLowerCase() + ".parameters.checkbox.notvalid").addConstraintViolation();
		        return false;
			}
			
			return true;
		}
	}
}