package if3t.exceptions;

public class ChannelNotAuthorizedException extends Exception{

	private static final long serialVersionUID = 601195891602762377L;
	
	public ChannelNotAuthorizedException(){
		super();
	}
	
	public ChannelNotAuthorizedException(String message){
		super(message);
	}
}
