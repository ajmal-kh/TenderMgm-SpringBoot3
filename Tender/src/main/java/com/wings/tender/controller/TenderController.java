package com.wings.tender.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wings.tender.model.Bidding;
import com.wings.tender.model.LoginDTO;
import com.wings.tender.model.User;
import com.wings.tender.repo.BiddingRepo;
import com.wings.tender.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
public class TenderController {

	@Autowired
	AuthenticationManager authManager;
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	BiddingRepo biddingRepo;
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public ResponseEntity<Object> login(@RequestBody LoginDTO loginDTO){
		try {
			authManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword(),null));
		}catch(Exception ex) {
			throw new BadCredentialsException("Bad Credentials");
		}
		String token = jwtUtil.genJwtToken(loginDTO.getEmail());
		
		Map<String,Object> jwtResponse = new HashMap<>();
		jwtResponse.put("jwt", token);
		jwtResponse.put("status", 200);
		return new ResponseEntity<>(jwtResponse,HttpStatus.OK);
	}
	
	@PostMapping("/bidding/add")
	@PreAuthorize("hasAuthority('BIDDER')")
	public ResponseEntity<Object> addBidding(@Valid @RequestBody Bidding bid) throws Exception{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		bid.setBidderId(user.getId());
		
		bid.setDateOfBidding(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
		
		Bidding savedBid = null;
				try {
				 savedBid = biddingRepo.save(bid);
				}catch(DataIntegrityViolationException ex) {
					throw new DataIntegrityViolationException(ex.getMessage());
				}
		
		return new ResponseEntity<>(savedBid,HttpStatus.CREATED);
	}
	
	@GetMapping("/bidding/list")
	@PreAuthorize("hasAnyAuthority('BIDDER','APPROVER')")
	public ResponseEntity<Object> getBidding(@RequestParam("bidAmount") Double bidAmount){
		List<Bidding> biddings = biddingRepo.findByBidAmountGreaterThanEqual(bidAmount);
		if(biddings.size()==0)
			return new ResponseEntity<>("no data available",HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(biddings,HttpStatus.OK);
	}
	
	@PatchMapping("/bidding/update/{id}")
	@PreAuthorize("hasAuthority('APPROVER')")
	public ResponseEntity<Object> updateBidStatus(@PathVariable("id") Integer id, @RequestBody Bidding bidding){
		Optional<Bidding> bid = biddingRepo.findById(id);
		if(bid.isEmpty())
			return new ResponseEntity<>("no data available",HttpStatus.BAD_REQUEST);
		Bidding dbBidding = bid.get();
		dbBidding.setStatus(bidding.getStatus());
		Bidding savedBid = biddingRepo.save(dbBidding);
		return new ResponseEntity<>(savedBid,HttpStatus.OK);
	}
	

	@DeleteMapping("/bidding/delete/{id}")
	@PreAuthorize("hasAnyAuthority('BIDDER','APPROVER')")
	public ResponseEntity<Object> deleteBid(@PathVariable("id") Integer id){
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<Bidding> bid = biddingRepo.findById(id);
		if(bid.isEmpty())
			return new ResponseEntity<>("not found",HttpStatus.BAD_REQUEST);
		Bidding dbBidding = bid.get();
		
		if(user.getRole().getRolename().equals("APPROVER") || dbBidding.getBidderId()==user.getId()){
			biddingRepo.deleteById(id);
			return new ResponseEntity<>("deleted successfully",HttpStatus.NO_CONTENT);
		}
			
		return new ResponseEntity<>("you don't have permission",HttpStatus.FORBIDDEN);
		
	}
}
