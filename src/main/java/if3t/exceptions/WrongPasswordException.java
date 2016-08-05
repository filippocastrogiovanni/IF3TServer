package if3t.exceptions;

public class WrongPasswordException extends Exception 
{
	private static final long serialVersionUID = -2313053063186723908L;

	public WrongPasswordException() {
		super();
	}
	
	public WrongPasswordException(String message) {
		super(message);
	}
}
