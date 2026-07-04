package com.managant.backend.auth;

import com.managant.backend.db.PersonMembershipRepository;
import com.managant.backend.db.PersonRepository;
import com.managant.backend.db.RoleRepository;
import com.managant.backend.db.SessionTokenRepository;
import com.managant.backend.db.UserAccountRepository;
import com.managant.backend.domain.Authz;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

  public static final String ATTR_CURRENT_USER = "currentUser";

  private final SessionTokenRepository sessionRepo;
  private final UserAccountRepository userRepo;
  private final PersonRepository personRepo;
  private final RoleRepository roleRepo;
  private final PersonMembershipRepository membershipRepo;

  public AuthTokenFilter(
      SessionTokenRepository sessionRepo,
      UserAccountRepository userRepo,
      PersonRepository personRepo,
      RoleRepository roleRepo,
      PersonMembershipRepository membershipRepo
  ) {
    this.sessionRepo = sessionRepo;
    this.userRepo = userRepo;
    this.personRepo = personRepo;
    this.roleRepo = roleRepo;
    this.membershipRepo = membershipRepo;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

    // Public endpoints
    return path.equals("/api/health")
        || path.equals("/api")
        || path.startsWith("/api/auth/login");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    String token = extractBearer(auth);

    if (token == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"No autenticado\"}");
      return;
    }

    var session = sessionRepo.findValidActiveByToken(token).orElse(null);
    if (session == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Sesión inválida o expirada\"}");
      return;
    }

    var user = userRepo.findById(session.getUserId()).orElse(null);
    if (user == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Sesión inválida\"}");
      return;
    }

    var person = personRepo.findById(user.getPersonId()).orElse(null);
    if (person == null || !person.isActive()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Sesión inválida\"}");
      return;
    }

    boolean isAdmin = Authz.personHasAdminRole(user.getPersonId(), roleRepo, membershipRepo);

    request.setAttribute(
        ATTR_CURRENT_USER,
        new CurrentUser(user.getId(), user.getUsername(), user.getPersonId(), isAdmin)
    );

    filterChain.doFilter(request, response);
  }

  private static String extractBearer(String header) {
    if (header == null) return null;
    String h = header.trim();
    if (h.length() < 8) return null;
    if (!h.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
    String token = h.substring(7).trim();
    return token.isEmpty() ? null : token;
  }
}
