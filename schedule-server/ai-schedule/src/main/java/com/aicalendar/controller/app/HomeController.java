package com.aicalendar.controller.app;

import com.aicalendar.dto.response.HomeResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.HomeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 首页控制器（按当前登录用户聚合数据）
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * 获取当前登录用户的首页聚合数据
     */
    @GetMapping
    public Result<HomeResponse> getHomeData(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        HomeResponse response = homeService.getHomeData(userId);
        return Result.success("查询成功", response);
    }
}
