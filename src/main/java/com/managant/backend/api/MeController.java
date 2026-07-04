package com.managant.backend.api;

import com.managant.backend.api.dto.ViewerDto;
import com.managant.backend.auth.AuthTokenFilter;
import com.managant.backend.auth.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

  @GetMapping
  public ViewerDto me(HttpServletRequest request) {
    var viewer = (CurrentUser) request.getAttribute(AuthTokenFilter.ATTR_CURRENT_USER);
    if (viewer == null) {
      throw new SecurityException("No autenticado");
    }

    return new ViewerDto(
        String.valueOf(viewer.userId()),
        viewer.username(),
        String.valueOf(viewer.personId()),
        viewer.isAdmin()
    );
  }
}
