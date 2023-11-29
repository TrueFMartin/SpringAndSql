package com.truefmartin.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

import java.util.ArrayList;


@Controller
public class QueryController {

    @GetMapping("/query")
    public String greetingForm(Model model) {
        model.addAttribute("queryobject", new QueryObject());
        return "input";
    }

    @PostMapping("/query")
    public String greetingSubmit(@ModelAttribute QueryObject inQuery, Model model) {
        QueryHandler qh = new QueryHandler();
        var results = qh.Query(qh.splitArgs(inQuery.getQuery()));
        var links = new ArrayList<ResultObject>();
        for (var res :
                results) {
            links.add(new ResultObject(res));
        }
        model.addAttribute("queryobject", inQuery);

        model.addAttribute("results", links);
        return "result";
    }

}