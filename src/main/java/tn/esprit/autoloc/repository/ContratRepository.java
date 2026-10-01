package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.autoloc.domain.Contrat;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
}
