package if3t.exceptions;

public class AddRecipeException extends Exception 
{
	private static final long serialVersionUID = 1880310344965988787L;
	
	public AddRecipeException() {
		super();
	}

	public AddRecipeException(String message) {
		super(message);
	}
}
