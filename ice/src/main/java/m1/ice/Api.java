package m1.ice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Api {

	@GetMapping(value = "/getCommits/{url}")
	private String test(){
	
		return "TEST OK";
		
	}
	
}
