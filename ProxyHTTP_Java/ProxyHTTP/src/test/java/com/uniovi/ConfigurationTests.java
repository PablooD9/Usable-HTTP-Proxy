package com.uniovi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.proxy.HttpProxyApplication;
import com.proxy.controllers.ConfigurationController;
import com.proxy.model.Configuration;
import com.proxy.model.UserConfiguration;
import com.proxy.repository.ConfigurationRepository;
import com.proxy.services.ConfigurationService;

@SpringBootTest(classes = HttpProxyApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigurationTests {

	private ConfigurationRepository configurationRepositoryMock = Mockito.mock(ConfigurationRepository.class);

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ConfigurationController configurationController;

	private List<Configuration> configurations;

	@Before
	public void setUp() {
		configurationService.setConfRepository(configurationRepositoryMock);
		configurationController.setConfService(configurationService);

		setValuesToDefault();

		Mockito.when(configurationRepositoryMock.save(any(Configuration.class)))
				.thenAnswer(new Answer<Configuration>() {
					@Override
					public Configuration answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						Configuration configuration = (Configuration) args[0];
						addConfigurationToList(configuration);
						return configuration;
					}
				});

		Mockito.when(configurationRepositoryMock.findById(any(String.class)))
				.thenAnswer(new Answer<Optional<Configuration>>() {
					@Override
					public Optional<Configuration> answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						String email = (String) args[0];
						Optional<Configuration> config = findConfigByEmail(email);
						return config;
					}
				});
	}

	@Test
	public void testAddConfigurationOfAnonimousUser() {
		// We will check that the user's preferences are not saved in the database,
		// because user is not logged in.
		String os = "Windows";
		String browser = "Google Chrome";
		String securityHeaders = "header1,header2,header3,header4,header5";
		configurationController.savePreferences(os, browser, "true", "true", "true", "true", securityHeaders, "true");

		Optional<Configuration> configurationSavedAux = configurationRepositoryMock.findById("username@email.com");
		assertEquals(configurationSavedAux.isEmpty(), true); // Don't saved in DB.
		assertNotEquals(UserConfiguration.getInstance().getConfiguration(), null); // Although DB has not been updated,
																					// User Configuration should be.
	}

	@Test
	@WithMockUser(username = "username@email.com", password = "password")
	public void testAddConfigurationOfLoggedInUser() {
		String os = "Windows";
		String browser = "Google Chrome";
		String securityHeaders = "header1,header2,header3,header4,header5";
		configurationController.savePreferences(os, browser, "true", "true", "true", "true", securityHeaders, "true");

		// Configuration is saved in DB.
		Optional<Configuration> configurationSavedAux = configurationRepositoryMock.findById("username@email.com");
		assertEquals(configurationSavedAux.isEmpty(), false); // Saved in DB.
		assertNotEquals(UserConfiguration.getInstance().getConfiguration(), null); // Saved in Application.
	}

	@Test
	@WithMockUser(username = "username@email.com", password = "password")
	public void testObtainConfigurationOfUser() {
		String os = "Windows";
		String browser = "Google Chrome";
		String securityHeaders = "header1,header2,header3,header4,header5";
		configurationController.savePreferences(os, browser, "true", "true", "true", "true", securityHeaders, "true");

		// Configuration is saved in DB.
		Optional<Configuration> configurationSavedAux = configurationRepositoryMock.findById("username@email.com");
		Configuration configurationSaved = configurationSavedAux.get();
		checkConfigurationIsUpdated(configurationSaved, os, browser, securityHeaders, "true", "true", "true", "true",
				"true");
		// User Configuration has been updated too
		checkConfigurationIsUpdated(UserConfiguration.getInstance().getConfiguration(), os, browser, securityHeaders,
				"true", "true", "true", "true", "true");
	}

	@AfterEach
	public void afterEachTest() {
		setValuesToDefault();
	}

	/*
	 * 
	 * =======> Private methods. <=======
	 * 
	 */
	private void addConfigurationToList(Configuration config) {
		configurations.add(config);
	}

	private Optional<Configuration> findConfigByEmail(String email) {
		Optional<Configuration> optConfig = null;
		for (Configuration conf : configurations) {
			if (conf.getEmail().equalsIgnoreCase(email)) {
				optConfig = Optional.of(conf);
				return optConfig;
			}
		}
		return Optional.empty();
	}

	private void setValuesToDefault() {
		configurations = new ArrayList<Configuration>();
		defaultUserConfiguration();
	}

	private void defaultUserConfiguration() {
		UserConfiguration.getInstance().setConfiguration(new Configuration());
	}

	private void checkConfigurationIsUpdated(Configuration configuration, String os, String browser,
			String securityHeaders, String spHost, String malHost, String trackHost, String pornHost,
			String cookieHost) {
		assertNotEquals(configuration, null);
		assertEquals(configuration.getOS(), os);
		assertEquals(configuration.getBrowser(), browser);
		assertEquals(configuration.getSecurityHeaders(), securityHeaders);
		assertEquals(configuration.getCheckIfSpanishMaliciousHosts(), spHost);
		assertEquals(configuration.getCheckIfMaliciousHosts(), malHost);
		assertEquals(configuration.getCheckIfTrackersHosts(), trackHost);
		assertEquals(configuration.getCheckIfPornographicHosts(), pornHost);
		assertEquals(configuration.getCheckIfCookieHeader(), cookieHost);
	}
}
