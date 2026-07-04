package com.managant.backend.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonMembershipRepository extends JpaRepository<PersonMembershipEntity, Long> {
  List<PersonMembershipEntity> findByPersonId(Long personId);

  // Capacity counts should ignore inactive people.
  @Query("select count(distinct m.personId) "
      + "from PersonMembershipEntity m "
      + "join PersonEntity p on p.id = m.personId "
      + "where m.roleId = :roleId and p.active = true")
  long countDistinctActivePeopleByRoleId(@Param("roleId") Long roleId);

  @Query("select count(distinct m.personId) "
      + "from PersonMembershipEntity m "
      + "join PersonEntity p on p.id = m.personId "
      + "where m.roleId = :roleId and m.areaId = :areaId and p.active = true")
  long countDistinctActivePeopleByRoleIdAndAreaId(
      @Param("roleId") Long roleId,
      @Param("areaId") Long areaId
  );

  boolean existsByPersonIdAndAreaIdAndRoleId(Long personId, Long areaId, Long roleId);
  boolean existsByRoleId(Long roleId);
  void deleteByPersonIdAndAreaIdAndRoleId(Long personId, Long areaId, Long roleId);
}
