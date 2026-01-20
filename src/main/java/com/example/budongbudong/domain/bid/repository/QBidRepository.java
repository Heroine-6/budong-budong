package com.example.budongbudong.domain.bid.repository;

import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QBidRepository {

    Page<ReadMyBidsResponse> findMyBids(Long userId, String status, Pageable pageable);

}
