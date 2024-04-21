package com.github.truefmartin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.truefmartin.JpaModel;
import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.views.DisplayRestaurantDishOrder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;


@Controller
public class QueryController {

    @GetMapping("/")
    public String home(Model model) {
        var teamAndWins = JpaModel.getTeamsAndWins();
        model.addAttribute("queryobject", new QueryObject());
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(teamAndWins);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("data", json);
        return "input";
    }

    @GetMapping("/query")
    public String queryForm(Model model) {
        model.addAttribute("queryobject", new QueryObject());
        return "input";
    }

    @PostMapping("/query")
    public String querySubmit(@ModelAttribute QueryObject inQuery, Model model, HttpServletResponse response) {
        return queryPost(inQuery, model, response);
    }

    @GetMapping("/query/header")
    public void queryHeaderForm(Model model) {
        model.addAttribute("queryobjectdup", new QueryObjectDup());
    }


    @PostMapping("/query/header")
    public String querySubmitHeader(@ModelAttribute QueryObjectDup inQuery, Model model, HttpServletResponse response) {
        var qo = new QueryObject();
        qo.setQuery(inQuery.getQuerydup());
        return queryPost(qo, model, response);
    }

    public String queryPost(QueryObject inQuery, Model model, HttpServletResponse response) {
        var qh = new QueryHandler();
        if (inQuery.getQuery().isEmpty()) {
            model.addAttribute("queryobject", inQuery);
            model.addAttribute("error", new Error("empty search field, please enter tokens"));
            return "errors";
        }
        var results = qh.Query(qh.splitArgs(inQuery.getQuery()));
        if (results == null) {
            model.addAttribute("queryobject", inQuery);
            return "no-results";
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
        var decodedLastResults = URLDecoder.decode(lastResults, StandardCharsets.UTF_8);
        var links = new ArrayList<ResultObject>();
        for (var res :
                decodedLastResults.split("_")) {
            var resultSplit = res.split("-");
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
    public String displayFullGet(@RequestParam("filename") String filename, @RequestParam("query") String query,
                                 Model model, HttpServletResponse response) {
        var encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        var cookie = new Cookie("last-file-name", encodedFilename);

        cookie.setPath("/");
        cookie.setMaxAge(60*60);
        response.addCookie(cookie);
        var q = new QueryObject();
        q.setQuery(query);
        model.addAttribute("queryobject", q);
        model.addAttribute("resultobject", new ResultObject(new AbstractMap.SimpleEntry<>(filename, 0)));
        return "visit-result";
    }

    @GetMapping("/display-full/cached")
    public String displayFullCached(@CookieValue(name = "last-file-name", defaultValue = "") String lastFileName, Model model) {
        if (lastFileName.isEmpty()) {
            return "empty";
        }

        model.addAttribute("requestobject",
                new ResultObject(new AbstractMap.SimpleEntry<>(
                        URLDecoder.decode(lastFileName, StandardCharsets.UTF_8), 0)));
        return "visit-result";
    }

}