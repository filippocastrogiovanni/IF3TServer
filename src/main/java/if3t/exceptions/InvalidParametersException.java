package if3t.exceptions;

public class InvalidParametersException extends Exception 
{
	private static final long serialVersionUID = -9205018582040543258L;

	public InvalidParametersException(){
		super();
	}

	public InvalidParametersException(String message){
		super(message);
	}
}
