package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.domain.bid.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
}
