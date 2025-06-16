package org.qualifaizebackendapi.repository;

import org.qualifaizebackendapi.model.SoftDeletable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface SoftDeletableRepository<T extends SoftDeletable, ID> extends JpaRepository<T, ID> {

    // Soft delete methods
    @Modifying(clearAutomatically = true)
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.deleted = false")
    int softDeleteById(@Param("id") ID id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids AND e.deleted = false")
    int softDeleteByIds(@Param("ids") Collection<ID> ids);

    // Find active (non-deleted) entities
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAllActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    Optional<T> findActiveById(@Param("id") ID id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.deleted = false")
    List<T> findActiveByIds(@Param("ids") Collection<ID> ids);

    // Pageable versions
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    Page<T> findAllActive(Pageable pageable);

    // Count methods
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
    long countActive();

    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    boolean existsActiveById(@Param("id") ID id);

    // Find deleted entities
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findAllDeleted();

    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    Page<T> findAllDeleted(Pageable pageable);

    // Restore deleted entities
    @Modifying(clearAutomatically = true)
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null WHERE e.id = :id AND e.deleted = true")
    int restoreById(@Param("id") ID id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null WHERE e.id IN :ids AND e.deleted = true")
    int restoreByIds(@Param("ids") Collection<ID> ids);

    // Hard delete (permanently delete)
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM #{#entityName} e WHERE e.id = :id")
    int hardDeleteById(@Param("id") ID id);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM #{#entityName} e WHERE e.deleted = true AND e.deletedAt < :beforeDate")
    int hardDeleteOldSoftDeleted(@Param("beforeDate") OffsetDateTime beforeDate);
}