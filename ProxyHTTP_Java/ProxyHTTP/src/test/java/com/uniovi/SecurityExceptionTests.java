package com.uniovi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;

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
import com.proxy.model.Configuration;
import com.proxy.model.SecurityException;
import com.proxy.model.UserConfiguration;
import com.proxy.repository.SecurityExceptionRepository;
import com.proxy.services.ConfigurationService;

@SpringBootTest(classes = HttpProxyApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
public class SecurityExceptionTests {
	
	private SecurityExceptionRepository securityExceptionRepositoryMock = Mockito.mock(SecurityExceptionRepository.class);

	@Autowired
	private ConfigurationService configurationService;
	
	private List<SecurityException> securityExceptions;
	
	@Before
	public void setUp() {
		configurationService.setSecurityExceptionRepository(securityExceptionRepositoryMock);
		
		setValuesToDefault();
		
		Mockito.when(securityExceptionRepositoryMock.save(any(SecurityException.class))).thenAnswer(new Answer<SecurityException>() {
			    @Override
			    public SecurityException answer(InvocationOnMock invocation) throws Throwable {
			    	Object[] args = invocation.getArguments();
			    	SecurityException secExc = (SecurityException) args[0];
			    	addExceptionToList(secExc);
			    	return secExc;
			    }
		});
		
		Mockito.when(securityExceptionRepositoryMock.findByEmail(any(String.class))).thenAnswer(new Answer<SecurityException>() {
		    @Override
		    public SecurityException answer(InvocationOnMock invocation) throws Throwable {
		    	Object[] args = invocation.getArguments();
		    	String email = (String) args[0];
		    	SecurityException secExc = findSecExcByEmail(email);
		    	return secExc;
		    }
		});
	}
	
	@AfterEach
	public void afterEachTest() {
		setValuesToDefault();
	}
	
	@Test
	@WithMockUser(username = "username@email.com", password = "password")
	public void testAddNewValidSecurityException() {
		String email = "username@email.com";
		configurationService.saveSecurityException("www.test.com");
		SecurityException secExc = securityExceptionRepositoryMock.findByEmail(email); 
		assertEquals(secExc.getHostsException(), "www.test.com");
	}
	
	@Test
	@WithMockUser(username = "username@email.com", password = "password")
	public void testAddAnAlreadySavedSecurityException() {
		String email = "username@email.com";
		
		configurationService.saveSecurityException("www.test.com");
		SecurityException secExc = securityExceptionRepositoryMock.findByEmail(email); 
		assertEquals(secExc.getHostsException(), "www.test.com");
		
		// Add a Host that is already saved as Security Exception, so ignore it.
		configurationService.saveSecurityException("www.test.com");
		secExc = securityExceptionRepositoryMock.findByEmail(email); 
		
		Configuration config = configurationService.getConfigOfUser();
		assertEquals(config.getHostExceptions().size(), 1);
		assertEquals(secExc.getHostsException(), "www.test.com");
	}
	
	@Test
	@WithMockUser(username = "username@email.com", password = "password")
	public void testObtainSecurityExceptions() {
		configurationService.saveSecurityException("www.test.com");
		configurationService.saveSecurityException("www.test2.com");
		
		Configuration config = configurationService.getConfigOfUser();
		assertEquals(config.getHostExceptions().size(), 2);
		assertEquals(config.getHostExceptions().get(0), "www.test.com");
		assertEquals(config.getHostExceptions().get(1), "www.test2.com");
	}
	
	/*
	 * 
	 * =======> Private methods. <=======
	 * 
	 * */
	private void addExceptionToList(SecurityException secExc) {
		securityExceptions.add(secExc);
	}
	
	private void setValuesToDefault() {
		securityExceptions = new ArrayList<SecurityException>();
		defaultUserConfiguration();
	}
	
	private SecurityException findSecExcByEmail(String email) {
		for (SecurityException secExc: securityExceptions) {
    		if (secExc.getEmail().equalsIgnoreCase(email)) {
    			return secExc;
    		}
    	}
		return null;
	}
	
	private void defaultUserConfiguration() {
		UserConfiguration.getInstance().setConfiguration(new Configuration());
	}
}
