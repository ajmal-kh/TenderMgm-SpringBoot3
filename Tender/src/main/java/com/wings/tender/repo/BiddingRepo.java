package com.wings.tender.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wings.tender.model.Bidding;

@Repository
public interface BiddingRepo extends JpaRepository<Bidding,Integer>{

	List<Bidding> findByBidAmountGreaterThanEqual(Double bidAmount);
}
