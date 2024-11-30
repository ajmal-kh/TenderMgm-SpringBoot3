package com.wings.tender;

import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.Scanner;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wings.tender.model.Bidding;
import com.wings.tender.model.LoginDTO;
import com.wings.tender.repo.UserRepo;

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
class TenderApplicationTests {

	@Autowired
	private UserRepo userRepo;

	private MockMvc mockMvc;

	public static final String TOKEN_APPROVER_1 = "token_approver_1";
	public static final String TOKEN_BIDDER_1 = "token_bidder_1";
	public static final String TOKEN_BIDDER_2 = "token_bidder_2";
	public static final String ID_USER_1 = "id_user_1";
	public static final String ID_USER_2 = "id_user_2";
	public static final String ID_BIDDING_1 = "id_bidding_1";
	public static final String ID_BIDDING_2 = "id_bidding_2";
	
	@Autowired
	WebApplicationContext context;
	
	@BeforeEach
	void setMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}
	
	@Test
	void a_testFailedloginAttempt() throws Exception{
		LoginDTO loginData = new LoginDTO("a@gmail.com","wrongpass");
		mockMvc.perform(post("/login")
				.content(toJson(loginData)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andReturn();
	}

	@Test
	void b_testSuccessloginAttemptBidder() throws Exception{
		LoginDTO loginData = new LoginDTO("a@gmail.com","a@123");
		MvcResult result = mockMvc.perform(post("/login")
				.content(toJson(loginData)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
		
		JSONObject obj = new JSONObject(result.getResponse().getContentAsString());
		assert obj.has("jwt");
		assert obj.getInt("status")==200;
		saveDataToFileSystem(TOKEN_BIDDER_1,obj.getString("jwt"));
		
		LoginDTO loginData1 = new LoginDTO("b@gmail.com","a@123");
		MvcResult result1 = mockMvc.perform(post("/login")
				.content(toJson(loginData1)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
		
		JSONObject obj1 = new JSONObject(result1.getResponse().getContentAsString());
		assert obj1.has("jwt");
		assert obj1.getInt("status")==200;
		saveDataToFileSystem(TOKEN_BIDDER_2,obj1.getString("jwt"));
	}
	
	@Test
	void c_testSuccessloginAttemptApprover() throws Exception{
		LoginDTO loginData = new LoginDTO("c@gmail.com","a@123");
		MvcResult result = mockMvc.perform(post("/login")
				.content(toJson(loginData)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
		
		JSONObject obj = new JSONObject(result.getResponse().getContentAsString());
		assert obj.has("jwt");
		assert obj.getInt("status")==200;
		saveDataToFileSystem(TOKEN_APPROVER_1,obj.getString("jwt"));
		
	}
	
	@Test
	void d_checkSuccessBiddingAdding() throws Exception{
		Bidding bidding = new Bidding(2608,1400000.2,2.6);
		MvcResult result = mockMvc.perform(post("/bidding/add")
				.content(toJson(bidding))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_1))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(201)).andReturn();
		
		Bidding bidding1 = new Bidding(3123,17000000.0,3.1);
		MvcResult result1 = mockMvc.perform(post("/bidding/add")
				.content(toJson(bidding1))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_2))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(201)).andReturn();
		
		JSONObject obj = new JSONObject(result.getResponse().getContentAsString());
		assert obj.has("id");
		assert Objects.equals(obj.getInt("biddingId"),2608);
		assert Objects.equals(obj.getString("dateOfBidding"),gettime());
		assert Objects.equals(obj.getString("status"),"pending");
		
		JSONObject obj1 = new JSONObject(result1.getResponse().getContentAsString());
		assert obj1.has("id");
		assert Objects.equals(obj1.getInt("biddingId"),3123);
		assert Objects.equals(obj1.getDouble("bidAmount"),17000000.0);
		assert Objects.equals(obj1.getInt("bidderId"),2);
		
		saveDataToFileSystem(ID_BIDDING_1,obj.getString("id"));
		saveDataToFileSystem(ID_BIDDING_2,obj1.getString("id"));
		
	}
	
	@Test
	void e_checkFailedBiddingAdding() throws Exception{
		Bidding bidding = new Bidding(26081,1400000.2,2.6);
		mockMvc.perform(post("/bidding/add")
				.content(toJson(bidding))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized()).andReturn();
		
		mockMvc.perform(post("/bidding/add")
				.content(toJson(bidding))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_1))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(201)).andReturn();
	

		
	}
	
	@Test
	void f_getSuccessBiddingCheckTest() throws Exception{
		
		
		 mockMvc.perform(get("/bidding/list?bidAmount=15000000")
				 .contentType(MediaType.APPLICATION_JSON_VALUE)
					.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_APPROVER_1)))
					
					.andExpect(MockMvcResultMatchers.status().isOk())
					.andExpect(jsonPath("$.[0].id",Matchers.is(2)))
					.andExpect(jsonPath("$.[0].biddingId",Matchers.is(3123)))
					.andExpect(jsonPath("$.[0].projectName",containsStringIgnoringCase("Metro Phase V 2024")))
					.andExpect(jsonPath("$.[0].bidAmount",Matchers.is(17000000.0)))
					.andExpect(jsonPath("$.[0].yearsToComplete",Matchers.is(3.1)))
					.andExpect(jsonPath("$.[0].dateOfBidding",containsStringIgnoringCase(gettime())))
					.andExpect(jsonPath("$.[0].status",containsStringIgnoringCase("pending")))
					.andExpect(jsonPath("$.[0].bidderId",Matchers.is(2)));
			

		
	}
	
	@Test
	void g_getFailedBiddingCheckTest() throws Exception{
		
		mockMvc.perform(get("/bidding/list?bidAmount=31000000")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_2)))
				.andExpect(MockMvcResultMatchers.status().is(400));
			
	}
	
	@Test
	void h_updateSuccessBiddingwithDetailsCheck() throws Exception{
		Bidding bidding = new Bidding("approved");
		MvcResult result = mockMvc.perform(patch("/bidding/update/"+getDataFromFileSystem(ID_BIDDING_1))
				.content(toJson(bidding))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_APPROVER_1))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(200)).andReturn();
		
		JSONObject response = new JSONObject(result.getResponse().getContentAsString());
		assert response.has("id");
		assert Objects.equals(response.getString("status"),"approved");
		assert Objects.equals(response.getInt("biddingId"),2608);
		
	}
	
	@Test
	void i_updateFailedBiddingwithDetailsCheck() throws Exception{
		Bidding bidding = new Bidding("approved");
		MvcResult result = mockMvc.perform(patch("/bidding/update/"+getDataFromFileSystem(ID_BIDDING_2))
				.content(toJson(bidding))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_2))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(403)).andReturn();
		
		MvcResult result1 = mockMvc.perform(patch("/bidding/update/8")
				.content(toJson(bidding))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_APPROVER_1))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(400)).andReturn();
		
	}
	
	@Test
	void j_deleteBiddingWithNoAccess() throws Exception{
		
		mockMvc.perform(delete("/bidding/delete/"+getDataFromFileSystem(ID_BIDDING_1))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_2))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(403)).andReturn();
		
		mockMvc.perform(delete("/bidding/delete/8")
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_1))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().is(400)).andReturn();	
		
	}
	
	@Test
	void k_deleteBiddingWithAccessBidder() throws Exception{
		
		mockMvc.perform(delete("/bidding/delete/"+getDataFromFileSystem(ID_BIDDING_1))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_BIDDER_1))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(204)).andReturn();
		
	}
	
	@Test
	void l_deleteBiddingWithAccessApprover() throws Exception{
		
		mockMvc.perform(delete("/bidding/delete/"+getDataFromFileSystem(ID_BIDDING_2))
				.header("Authorization", "Bearer "+getDataFromFileSystem(TOKEN_APPROVER_1))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is(204)).andReturn();	
	}
	private String gettime() {
		// TODO Auto-generated method stub
		String x= String.valueOf(System.currentTimeMillis());
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		long milliSeconds = Long.parseLong(x);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	private Object getDataFromFileSystem(String key) throws Exception{
		// TODO Auto-generated method stub
		try {
			File myObj = new File("temp.txt");
			Scanner myReader = new Scanner(myObj);
			StringBuilder builder = new StringBuilder();
			while(myReader.hasNextLine()) {
				builder.append(myReader.nextLine());
			}
			myReader.close();
			JSONObject jsonObject = new JSONObject(builder.toString());
			return jsonObject.get(key);
		} catch(FileNotFoundException | JSONException e) {
			throw new Exception("Data not found");
		}
	}

	private void saveDataToFileSystem(Object key, Object value) throws Exception{
		// TODO Auto-generated method stub
		try {
			JSONObject jsonObject = new JSONObject();
			StringBuilder builder = new StringBuilder();
			try {
				File myObj = new File("temp.txt");
				Scanner myReader = new Scanner(myObj);
				while(myReader.hasNextLine()) {
					builder.append(myReader.nextLine());
				}
				myReader.close();
				if(!builder.toString().isEmpty())
					jsonObject = new JSONObject(builder.toString());
			} catch(FileNotFoundException | JSONException e) {
				e.printStackTrace();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter("temp.txt"));
			jsonObject.put((String) key, value);
			writer.write(jsonObject.toString());
			writer.close();
		}catch (JSONException | IOException e) {
			throw new Exception("Data not saved");
		}
	}


	private byte[] toJson(Object r) throws Exception {
		// TODO Auto-generated method stub
		ObjectMapper map = new ObjectMapper();
		return map.writeValueAsString(r).getBytes();
	}
}
