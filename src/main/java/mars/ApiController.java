package mars;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ApiController {

	@RequestMapping(value="/hello", method=RequestMethod.GET)
	public ModelAndView greeting() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("msg", "First SpringMVC project");
		mv.setViewName("hello");
		return mv;
	}
}
