package if3t.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import if3t.services.UserService;

@RestController
@CrossOrigin
public class UserController {
	
	@Autowired
	private UserService userService;
	
	/*
	@RequestMapping(value="/bookings", method=RequestMethod.GET)
	public List<Booking> getBookings() {
		return bookingService.getBookings();
	}
	
	@RequestMapping(value="/bookings/{day}/{month}/{year}", method=RequestMethod.GET)
	public List<Booking> getBookings(@PathVariable String day, @PathVariable String month, @PathVariable String year) {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, Integer.parseInt(year));
		date.set(Calendar.MONTH, Integer.parseInt(month)-1);
		date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
		String d = sdf.format(date.getTime());
		
		return bookingService.getDayBookings(d);
	}
	
	@RequestMapping(value="/bookings/{dayF}/{monthF}/{yearF}/{dayT}/{monthT}/{yearT}", method=RequestMethod.GET)
	public List<Booking> getBookings(@PathVariable String dayF, @PathVariable String monthF, @PathVariable String yearF, 
			@PathVariable String dayT, @PathVariable String monthT, @PathVariable String yearT) {
		
		Calendar date1 = Calendar.getInstance();
		date1.set(Calendar.YEAR, Integer.parseInt(yearF));
		date1.set(Calendar.MONTH, Integer.parseInt(monthF)-1);
		date1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayF));
		String d1 = sdf.format(date1.getTime());
		Calendar date2 = Calendar.getInstance();
		date2.set(Calendar.YEAR, Integer.parseInt(yearT));
		date2.set(Calendar.MONTH, Integer.parseInt(monthT)-1);
		date2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayT));
		String d2 = sdf.format(date2.getTime());
		
		return bookingService.getDaysBookings(d1, d2);
	}
	
	@RequestMapping(value="/bookings", method=RequestMethod.POST)
	public void createBooking(@RequestBody Booking b){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null) {
			User u = userService.getUserByUsername(auth.getName());
			if(u != null) {
				b.setUser(u);
				bookingService.addBooking(b);
			}
		}
	}
	
	@RequestMapping(value="/bookings/{id}", method=RequestMethod.DELETE)
	public void deleteBooking(@PathVariable Long id){
		Booking b = bookingService.getBooking(id);
		if(b != null) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			User u = userService.getUserByUsername(auth.getName());
			if(u != null) {
				if(u.getRole().equals(Role.ADMIN) || u.getId().equals(b.getUser().getId())) {
					bookingService.delBooking(b);
				}
			}
		}
	}
	*/
}
