package if3t.exceptions;

public class PartialUpdateException extends Exception 
{
	private static final long serialVersionUID = -8223927007530740856L;
	
	public PartialUpdateException() {
		super();
	}
	
	public PartialUpdateException(String message) {
		super(message);
	}
}
