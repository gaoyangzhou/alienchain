package org.alienchain.explorer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c)2019
 * Company: alienchain
 *
 * @author chen
 */

@Controller
public class HomeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping({"/","/index",""})
    public String index() {
        LOGGER.info("in index page.");
        return "index";
    }
}
