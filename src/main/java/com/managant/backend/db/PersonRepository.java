package com.managant.backend.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
  Optional<PersonEntity> findByCurp(String curp);
  boolean existsByCurpAndIdNot(String curp, Long id);

  @Query("select p from PersonEntity p "
      + "where p.active = true and (" 
      + "lower(p.firstNames) like lower(concat('%', :q, '%')) "
      + "or lower(p.lastName) like lower(concat('%', :q, '%')) "
      + "or lower(p.motherLastName) like lower(concat('%', :q, '%'))" 
      + ")")
  List<PersonEntity> searchActiveByName(@Param("q") String q);
}
