package com.managant.backend.api;

import com.managant.backend.api.dto.AreaCategoryCreateRequest;
import com.managant.backend.api.dto.AreaCategoryDto;
import com.managant.backend.db.AreaCategoryEntity;
import com.managant.backend.db.AreaCategoryRepository;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/area-categories")
public class AreaCategoryController {

  private final AreaCategoryRepository categoryRepo;

  public AreaCategoryController(AreaCategoryRepository categoryRepo) {
    this.categoryRepo = categoryRepo;
  }

  @GetMapping
  public List<AreaCategoryDto> list(jakarta.servlet.http.HttpServletRequest request) {
    if (request.getAttribute(com.managant.backend.auth.AuthTokenFilter.ATTR_CURRENT_USER) == null) {
      throw new SecurityException("No autenticado");
    }

    return categoryRepo.findAll().stream()
        .sorted(Comparator.comparing(AreaCategoryEntity::getName))
        .map(c -> new AreaCategoryDto(String.valueOf(c.getId()), c.getName()))
        .toList();
  }

  @PostMapping
  public AreaCategoryDto create(@Valid @RequestBody AreaCategoryCreateRequest req) {
    if (categoryRepo.existsByNameIgnoreCase(req.name().trim())) {
      throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
    }

    var e = new AreaCategoryEntity();
    e.setName(req.name().trim());
    e = categoryRepo.save(e);
    return new AreaCategoryDto(String.valueOf(e.getId()), e.getName());
  }
}
