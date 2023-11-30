package com.truefmartin.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.AbstractMap;
import java.util.ArrayList;


@Controller
public class QueryController {

    @GetMapping("/query")
    public String queryForm(Model model) {
        model.addAttribute("queryobject", new QueryObject());
        return "input";
    }

    @GetMapping("/query/header")
    public void queryHeaderForm(Model model) {
        model.addAttribute("queryobject", new QueryObject());
    }

    @PostMapping("/query")
    public String querySubmit(@ModelAttribute QueryObject inQuery, Model model, HttpServletResponse response) {
        var qh = new QueryHandler();
        var results = qh.Query(qh.splitArgs(inQuery.getQuery()));
        if (results == null) {
            model.addAttribute("queryobject", inQuery);
            model.addAttribute("error", qh.error);
            return "errors";
        }

        var links = new ArrayList<ResultObject>();
        for (var res :
                results) {
            links.add(new ResultObject(res));
        }
        model.addAttribute("queryobject", inQuery);
        model.addAttribute("results", links);

        StringBuilder sb = new StringBuilder();
        links.forEach(sb::append);

        var cookie = new Cookie("last-results", sb.toString());
        cookie.setPath("/");
        cookie.setMaxAge(60*60);
        response.addCookie(cookie);

        return "result";
    }

    public String queryPost(QueryObject inQuery, Model model, HttpServletResponse response) {
        var qh = new QueryHandler();
        var results = qh.Query(qh.splitArgs(inQuery.getQuery()));
        if (results == null) {
            model.addAttribute("queryobject", inQuery);
            model.addAttribute("error", qh.error);
            return "errors";
        }

        var links = new ArrayList<ResultObject>();
        for (var res :
                results) {
            links.add(new ResultObject(res));
        }
        model.addAttribute("queryobject", inQuery);
        model.addAttribute("results", links);

        StringBuilder sb = new StringBuilder();
        links.forEach(sb::append);

        var cookie = new Cookie("last-results", sb.toString());
        cookie.setPath("/");
        cookie.setMaxAge(60*60);
        response.addCookie(cookie);

        return "result";
    }
//
//    @PostMapping("/results/cached")
//    public String queryCached(@ModelAttribute QueryObject inQuery, Model model) {
//        QueryHandler qh = new QueryHandler();
//        var results = qh.Query(qh.splitArgs(inQuery.getQuery()));
//
//
//        if (results == null) {
//            model.addAttribute("queryobject", inQuery);
//            model.addAttribute("error", qh.error);
//            return "errors";
//        }
//        var links = new ArrayList<ResultObject>();
//        for (var res :
//                results) {
//            links.add(new ResultObject(res));
//        }
//        model.addAttribute("queryobject", inQuery);
//
//        model.addAttribute("results", links);
//        return "result";
//    }

    @PostMapping("/results/cached")
    public String usedCachedResults(@CookieValue(name = "last-results", defaultValue = "") String lastResults, Model model) {
        var links = new ArrayList<ResultObject>();
        for (var res :
                lastResults.split(" ")) {
            var resultSplit = res.split(",");
            links.add(new ResultObject(new AbstractMap.SimpleEntry<>(resultSplit[0], Integer.parseInt(resultSplit[1]))));
        }
        if (links.isEmpty()) {
            return "empty";
        }
        var qo = new QueryObject();
        qo.setQuery("Using cached results");
        model.addAttribute("queryobject", qo);

        model.addAttribute("results", links);
        return "result";
    }

    @GetMapping("/display")
    public String displayGet(@RequestParam("filename") String filename, Model model) {
        return "cachedfiles/" + filename.split("\\.")[0];
    }

    @GetMapping("/display-full")
    public String displayFullGet(@RequestParam("filename") String filename, Model model, HttpServletResponse response) {
        var cookie = new Cookie("last-file-name", filename);

        cookie.setPath("/");
        cookie.setMaxAge(60*60);
        response.addCookie(cookie);

        model.addAttribute("requestobject", new ResultObject(new AbstractMap.SimpleEntry<>(filename, 0)));
        return "visit-result";
    }

    @GetMapping("/display-full/cached")
    public String displayFullCached(@CookieValue(name = "last-file-name", defaultValue = "") String lastFileName, Model model) {
        if (lastFileName.isEmpty()) {
            return "empty";
        }
        model.addAttribute("requestobject", new ResultObject(new AbstractMap.SimpleEntry<>(lastFileName, 0)));
        return "visit-result";
    }

}