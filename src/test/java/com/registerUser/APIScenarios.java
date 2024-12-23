package com.registerUser;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.restassured.response.Response;

public class APIScenarios {

	private static String tokenvalue;
	String loginTokenvalue;
	private String contactId;
	public static String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	public static String emailWithTime = "Akshay" + currentTime + "@faketest.com";

	@Test(priority = 1)
	public void postUsingHashMAP() {
		System.out.println("POST Request using HashMap");
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("firstName", "Akshay");
		dataMap.put("lastName", "test");
		dataMap.put("email", emailWithTime);
		dataMap.put("password", "test123");

		Response res = given().header("Accept", "application/json").header("Content-Type", "application/json")
				.body(dataMap).when().post("https://thinking-tester-contact-list.herokuapp.com/users");

		res.then().log().body();
		// get token
		tokenvalue = res.jsonPath().getString("token");
		System.out.println("Generated token is: " + tokenvalue);
		int statusCode = res.getStatusCode();
		Assert.assertEquals(statusCode, 201, "Statuscode code should be equal 201 Created");
	}

	@Test(priority = 2)
	public void getUserProfile() {
		System.out.println("GET Request using Profile");
		if (tokenvalue != null) {
			Response res = given().header("Authorization", "Bearer " + tokenvalue).when()
					.get("https://thinking-tester-contact-list.herokuapp.com/users/me");
			res.then().log().body();

			int statusCode = res.getStatusCode();
			Assert.assertEquals(statusCode, 200, "Statuscode code should be equal 200 OK");
		}
	}

	@Test(priority = 3)
	public void patchUpdateProfile() {
		System.out.println("PATCH Request to update user profile");
		if (tokenvalue != null) {
			HashMap<String, Object> updateData = new HashMap<String, Object>();
			updateData.put("firstName", "Richard");
			updateData.put("lastName", "Antony");
			updateData.put("email", emailWithTime);
			updateData.put("password", "test1234");

			Response res = given().header("Authorization", "Bearer " + tokenvalue)
					.header("Content-Type", "application/json").body(updateData).when()
					.patch("https://thinking-tester-contact-list.herokuapp.com/users/me");
			res.then().log().body();
			int statusCode = res.getStatusCode();
			Assert.assertEquals(statusCode, 200, "Statuscode code should be equal 200 OK");
		} else {
			System.out.println("Token not available. Please Run POST request");
		}
	}

	@Test(priority = 4)
	public void loginUser() {
		System.out.println("POST Request using HashMap LoginUser");
		HashMap<String, Object> loginData = new HashMap<String, Object>();
		loginData.put("email", emailWithTime);
		loginData.put("password", "test1234");

		Response res = given().header("Accept", "application/json").header("Content-Type", "application/json")
				.body(loginData).when().post("https://thinking-tester-contact-list.herokuapp.com/users/login");
		res.then().log().body();
		loginTokenvalue = res.jsonPath().getString("token");
		System.out.println("Generated token is: " + loginTokenvalue);
		int statusCode = res.getStatusCode();
		Assert.assertEquals(statusCode, 200, "Statuscode code should be equal 200 OK");
	}

	@Test(priority = 5)
	public void addContact() {
		System.out.println("POST request to add contact");
		if (loginTokenvalue != null) {
			HashMap<String, Object> contactData = new HashMap<String, Object>();
			contactData.put("firstName", "John");
			contactData.put("lastName", "Doe");
			contactData.put("birthdate", "1970-01-01");
			contactData.put("email", "jdoe@fake.com");
			contactData.put("phone", "8005555555");
			contactData.put("street1", "1 Main St.");
			contactData.put("street2", "Apartment A");
			contactData.put("city", "Anytown");
			contactData.put("stateProvince", "KS");
			contactData.put("postalCode", "12345");
			contactData.put("country", "USA");

			Response res = given().header("Authorization", "Bearer " + loginTokenvalue)
					.header("Content-Type", "application/json").body(contactData).when()
					.post("https://thinking-tester-contact-list.herokuapp.com/contacts");
			res.then().log().body();
			int statusCode = res.getStatusCode();
			Assert.assertEquals(statusCode, 201, "Statuscode code should be equal 201 Created");
		}
	}

	@Test(priority = 6)
	public void getContactList() {
		System.out.println("GET Request to retrieve contact list");

		// Ensure token is available
		if (loginTokenvalue != null) {
			// Send GET request to fetch the contact list
			Response res = given().header("Authorization", "Bearer " + loginTokenvalue)
					.header("Accept", "application/json").when()
					.get("https://thinking-tester-contact-list.herokuapp.com/contacts");

			// Log the response body for debugging
			res.then().log().body();

			// Validate the status code is 200 OK
			int statusCode = res.getStatusCode();
			Assert.assertEquals(statusCode, 200, "Status code should be 200 OK");

			// Example: Validate the response contains a list of contacts
			int contactsSize = res.jsonPath().getList("data").size();
			Assert.assertTrue(contactsSize > 0, "Contact list should not be empty");
			// Extract the _id value of the first contact and store it globally
			contactId = res.jsonPath().getString("[0]._id");
			System.out.println("Stored contact _id: " + contactId);
		} else {
			System.out.println("Token is not available. Please run the POST request first.");
		}
	}

	@Test(priority = 7)
	public void getContactById() {
		System.out.println("GET Request to retrieve a specific contact by ID: " + contactId);

		if (contactId != null) {
			// Send GET request using the stored _id value
			Response res = given().header("Authorization", "Bearer " + loginTokenvalue)
					.header("Accept", "application/json").when()
					.get("https://thinking-tester-contact-list.herokuapp.com/contacts/" + contactId);

			res.then().log().body();

			int statusCode = res.getStatusCode();
			Assert.assertEquals(statusCode, 200, "Status code should be 200 OK");

		} else {
			System.out.println("contactId is not available. Please run the getContactList() first.");
		}
	}

	@Test(priority = 8)
	public void updateContact() {
		if (contactId != null && loginTokenvalue != null) {
			HashMap<String, Object> requestPayload = new HashMap<String, Object>();
			requestPayload.put("firstName", "Amy");
			requestPayload.put("lastName", "Miller");
			requestPayload.put("birthdate", "1992-02-02");
			requestPayload.put("email", "amiller@fake.com");
			requestPayload.put("phone", "8005554242");
			requestPayload.put("street1", "13 School St.");
			requestPayload.put("street2", "Apt. 5");
			requestPayload.put("city", "Washington");
			requestPayload.put("stateProvince", "QC");
			requestPayload.put("postalCode", "A1A1A1");
			requestPayload.put("country", "Canada");

			Response res = given().header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + loginTokenvalue).body(requestPayload).when()
					.put("https://thinking-tester-contact-list.herokuapp.com/contacts/" + contactId);
			res.then().log().body();
			int statusCode = res.getStatusCode();
			Assert.assertEquals(statusCode, 200, "Status code should be 200 OK");
		}
	}

	@Test(priority = 9)
	public void patchContact() {
		System.out.println("PATCH Request to update contact: "+contactId);

		// Ensure contactId and token are available
		if (contactId != null && loginTokenvalue != null) {
			HashMap<String, Object> patchUpdate = new HashMap<String, Object>();
			patchUpdate.put("firstName", "Anna");

			Response res = given().header("Authorization", "Bearer " + loginTokenvalue)
					.header("Content-Type", "application/json").body(patchUpdate) // Send the HashMap as the request
																					// body
					.when().patch("https://thinking-tester-contact-list.herokuapp.com/contacts/" + contactId);

			// Log the response body for debugging
			res.then().log().body();
			int statusCode = res.getStatusCode();
			Assert.assertEquals(statusCode, 200, "Status code should be 200 OK");
		}
	}

	@Test(priority = 10)
	public void logoutUser() {
	    System.out.println("POST Request to log out user");

	    // Ensure token is available
	    if (loginTokenvalue != null) {
	        Response res = given()
	                .header("Authorization", "Bearer " + loginTokenvalue)  // Include the Bearer token
	                .when()
	                .post("https://thinking-tester-contact-list.herokuapp.com/users/logout");

	        res.then().log().body();

	        int statusCode = res.getStatusCode();
	        Assert.assertEquals(statusCode, 200, "Status code should be 200 OK");
	    }
	}
	

}
