package com.ssh.smartServiceHub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.jwt.secret=6fbd370b58ba873a7d4d9c282fc13f19d53a3bcaba28af61b2640f5374f1aa42",
		"app.jwt.expiration-ms=900000",
		"app.jwt.refreshExpirationMs=2592000000"
})
class SmartServiceHubApplicationTests {

	@Test
	void contextLoads() {
	}

}
