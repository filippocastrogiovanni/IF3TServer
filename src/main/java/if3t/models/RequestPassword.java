package if3t.models;

public class RequestPassword {
	private String currentPassword;
	private String newPassword;
	/**
	 * @return the currentPassword
	 */
	public String getCurrentPassword() {
		return currentPassword;
	}
	/**
	 * @param currentPassword the currentPassword to set
	 */
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}
	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	
}
