package if3t.exceptions;

public class NotFoundRecipeException extends Exception 
{
	private static final long serialVersionUID = 1L;
	
	public NotFoundRecipeException() {
		super();
	}
	
	public NotFoundRecipeException(String message) {
		super(message);
	}
}
