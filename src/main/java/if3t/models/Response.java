package if3t.models;

public class Response {
	private String message;
	private Exception exception;
	private Integer code;
	
	public Response(String message, Integer code) {
		super();
		this.message = message;
		this.code = code;
	}
	
	/**
	 * @return the cause
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @param cause the cause to set
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}
	
	
}
