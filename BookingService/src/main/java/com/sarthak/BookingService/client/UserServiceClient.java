package com.sarthak.BookingService.client;

import com.sarthak.BookingService.dto.response.UsernameResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "User-Service")
public interface UserServiceClient {

    @PostMapping("/api/users/getUsername/list")
    public List<UsernameResponse> getUsernameByUserId(@RequestBody List<Long> userIdList);
}
