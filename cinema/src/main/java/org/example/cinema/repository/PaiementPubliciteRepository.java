package org.example.cinema.repository;

import org.example.cinema.model.PaiementPublicite;
import org.example.cinema.model.Publicite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaiementPubliciteRepository extends JpaRepository<PaiementPublicite, Long> {

    List<PaiementPublicite> findByPubliciteOrderByDatePaiementDesc(Publicite publicite);

    List<PaiementPublicite> findByPubliciteIdOrderByDatePaiementDesc(Long publiciteId);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM PaiementPublicite p WHERE p.publicite.id = :publiciteId")
    BigDecimal getTotalPayeParPublicite(@Param("publiciteId") Long publiciteId);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM PaiementPublicite p WHERE p.publicite = :publicite")
    BigDecimal getTotalPaye(@Param("publicite") Publicite publicite);

    void deleteByPublicite(Publicite publicite);
}
