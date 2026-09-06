package com.aicalendar.controller;

import com.aicalendar.dto.response.HomeResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.HomeService;
import org.springframework.web.bind.annotation.*;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * 获取首页聚合数据
     */
    @GetMapping
    public Result<HomeResponse> getHomeData() {
        HomeResponse response = homeService.getHomeData();
        return Result.success("查询成功", response);
    }
}