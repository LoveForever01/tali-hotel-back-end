package com.vn.tali.hotel.securiry.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vn.tali.hotel.securiry.service.UserDetailsServiceImpl;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (header == null || header.isEmpty() || !header.startsWith("Bearer ")) {
				filterChain.doFilter(request, response);
				return;
			}

			// Get jwt token and validate
			final String jwt = header.split(" ")[1].trim();
			if (!jwtUtils.validateJwtToken(jwt)) {
				filterChain.doFilter(request, response);
				return;
			}
//			String jwt = parseJwt(request);
			if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
				String username = jwtUtils.getUserNameFromJwtToken(jwt);

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
		}

		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
		String jwt = jwtUtils.getJwtFromCookies(request);
		return jwt;
	}

//	private final JwtUtils jwtTokenUtil;
//	private final UserDetailsServiceImpl userRepo;
//
//	public AuthTokenFilter(JwtUtils jwtTokenUtil, UserDetailsServiceImpl userRepo) {
//		this.jwtTokenUtil = jwtTokenUtil;
//		this.userRepo = userRepo;
//	}
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//			throws ServletException, IOException {
//		// Get authorization header and validate
//		final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
//		if (header.isEmpty() || !header.startsWith("Bearer ")) {
//			chain.doFilter(request, response);
//			return;
//		}
//
//		// Get jwt token and validate
//		final String token = header.split(" ")[1].trim();
//		if (!jwtTokenUtil.validate(token)) {
//			chain.doFilter(request, response);
//			return;
//		}
//
//		// Get user identity and set it on the spring security context
//		UserDetails userDetails = userRepo.findByUsername(jwtTokenUtil.getUsername(token)).orElse(null);
//
//		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
//				userDetails == null ? List.of() : userDetails.getAuthorities());
//
//		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//		chain.doFilter(request, response);
//	}
}
