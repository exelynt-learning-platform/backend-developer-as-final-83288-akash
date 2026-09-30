package com.bookingSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ResTfulResourceBookingSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(ResTfulResourceBookingSystemApplication.class, args);
		String startupMsg = "Resource Booking System Started";
		printInBox(startupMsg);
	}

	private static void printInBox(String text) {
		char TOP_LEFT = '╔';
		char TOP_RIGHT = '╗';
		char BOTTOM_LEFT = '╚';
		char BOTTOM_RIGHT = '╝';
		char HORIZONTAL = '═';
		char VERTICAL = '║';

		int textLength = text.length() + 2;
		System.out.println(TOP_LEFT + String.valueOf(HORIZONTAL).repeat(textLength) + TOP_RIGHT);
		System.out.println(VERTICAL + " " + text + " " + VERTICAL);
		System.out.println(BOTTOM_LEFT + String.valueOf(HORIZONTAL).repeat(textLength) + BOTTOM_RIGHT);
	}

}
