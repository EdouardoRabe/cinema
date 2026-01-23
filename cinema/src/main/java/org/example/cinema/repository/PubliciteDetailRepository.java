package org.example.cinema.repository;

import org.example.cinema.model.PubliciteDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PubliciteDetailRepository extends JpaRepository<PubliciteDetail, Long> {

  
    List<PubliciteDetail> findByPubliciteId(Long publiciteId);

    List<PubliciteDetail> findBySeanceId(Long seanceId);

    @Query("SELECT pd FROM PubliciteDetail pd " +
           "JOIN pd.seance s " +
           "WHERE EXTRACT(YEAR FROM s.debut) = :annee " +
           "AND EXTRACT(MONTH FROM s.debut) = :mois")
    List<PubliciteDetail> findBySeanceMoisAnnee(@Param("annee") int annee, @Param("mois") int mois);
}
