package com.uniovi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.proxy.HttpProxyApplication;
import com.proxy.model.User;
import com.proxy.repository.UserRepository;
import com.proxy.services.UserService;

@SpringBootTest(classes = HttpProxyApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringJUnit4ClassRunner.class)
public class UserTests {
	
	private UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
	
	@Autowired
	private UserService userService;
	@Autowired
	private MockMvc mockMvc;
	
	private User validUser;
	private User invalidUser;
	private List<User> users;

	@Before
	public void setUp() {
		userService = new UserService(userRepositoryMock);
		
		setValuesToDefault();
		
		Mockito.when(userRepositoryMock.save(any(User.class))).thenAnswer(new Answer<User>() {
			    @Override
			    public User answer(InvocationOnMock invocation) throws Throwable {
			    	Object[] args = invocation.getArguments();
			    	User user = (User) args[0];
			    	addUserToList(user);
			    	return user;
			    }
		});
		
		Mockito.when(userRepositoryMock.findByEmail(any(String.class))).thenAnswer(new Answer<User>() {
		    @Override
		    public User answer(InvocationOnMock invocation) throws Throwable {
		    	Object[] args = invocation.getArguments();
		    	String email = (String) args[0];
		    	User user = findUserByEmail(email);
		    	return user;
		    }
		});
	}
	
	@AfterEach
	public void afterEachTest() {
		setValuesToDefault();
	}
	
	@Test
	public void testSignUpUserWithAnEmailAlreadyRegisteredInDB() {
		User user1 = userService.saveUser(validUser);
		User user2 = userService.saveUser(invalidUser);
		
		assertEquals(findUserByEmail(user1.getEmail()).getId(), validUser.getId());
		assertEquals(user2, null);
	}
	
	@Test
	public void testSignUpUserWithAnEmailNotRegisteredInDB() {
		User user1 = userService.saveUser(validUser);
		
		assertEquals(findUserByEmail(user1.getEmail()).getId(), validUser.getId());
	}
	
	@Test
	public void testIfPasswordWasEncryptedWhenUserWasSaved() {
		String passBeforeNotEncryptedYet = validUser.getPassword();
		User user = userService.saveUser(validUser);
		String passAfterAlreadyEncrypted = user.getPassword();
		
		assertNotEquals(passBeforeNotEncryptedYet, passAfterAlreadyEncrypted);
	}
	
	// Lo de debajo debería ser un test de integración de Spring-Security con la aplicación.
	@Test
	@WithMockUser(username = "email", password = "password")
	public void testLogoutUser() throws Exception {
		SecurityContext secContext = SecurityContextHolder.getContext();
		assertEquals("email", secContext.getAuthentication().getName());
		
		mockMvc.perform(get("/logout"));
		assertEquals(null, secContext.getAuthentication());
	}
	
	/*
	 * 
	 * =======> Private methods. <=======
	 * 
	 * */
	private void addUserToList(User user) {
		users.add(user);
	}
	
	private User findUserByEmail(String email) {
		for (User user : users) {
    		if (user.getEmail().equalsIgnoreCase(email)) {
    			return user;
    		}
    	}
		return null;
	}
	
	private void setValuesToDefault() {
		validUser = new User(new ObjectId(), "test@gmail.com", "nuevaPass12345678", "Pablo");
		invalidUser = new User(new ObjectId(), "test@gmail.com", "12345678nuevaPass", "Pepe");
		users = new ArrayList<User>();
	}

}
