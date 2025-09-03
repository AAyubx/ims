package com.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventoryManagementApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
		// All our beans should be properly configured
	}
}