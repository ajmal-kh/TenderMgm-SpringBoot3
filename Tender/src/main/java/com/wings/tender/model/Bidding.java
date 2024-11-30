package com.wings.tender.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bidding {

	public Bidding(int i, double d, double e) {
		// TODO Auto-generated constructor stub
		this.biddingId=i;
		bidAmount=d;
		yearsToComplete=e;
	}

	public Bidding(String status) {
		// TODO Auto-generated constructor stub
		this.status=status;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	
	@Column(unique=true)
	Integer biddingId;
	
	final String projectName="Metro Phase V 2024";
	
	@NotNull(message="bid amount cannot be null")
	Double bidAmount;
	
	Double yearsToComplete;
	
	String dateOfBidding;
	
	String status="pending";
	
	Integer bidderId;
}
