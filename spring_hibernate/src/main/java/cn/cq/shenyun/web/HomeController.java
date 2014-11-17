package cn.cq.shenyun.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping({"/home"})
public class HomeController
{
	@RequestMapping({"/"})
	public Object Index(){
		ModelAndView mav=new ModelAndView("/home/main");
		return mav;
	}
}
