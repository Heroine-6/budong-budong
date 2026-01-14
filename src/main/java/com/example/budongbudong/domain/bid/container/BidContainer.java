package com.example.budongbudong.domain.bid.container;

import com.example.budongbudong.domain.bid.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BidContainer {

    private final BidService bidService;
}
