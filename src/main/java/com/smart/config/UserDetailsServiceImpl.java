package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserDao;
import com.smart.entity.User;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserDao userDao;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user =	userDao.getUserByUserNmae(username);
		
		if(user == null) {
			throw new UsernameNotFoundException("Could not found user!!");
			
		}
		
		CustomUserDetails customUser = new CustomUserDetails(user);

		return customUser;
	}

}
