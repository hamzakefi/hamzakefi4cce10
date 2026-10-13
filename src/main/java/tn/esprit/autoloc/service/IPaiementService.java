package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Paiement;

import java.util.List;

/**
 * Service Paiement en lecture seule.
 * Les opérations d'écriture passent par ContratServiceImpl (CascadeType.ALL).
 */
public interface IPaiementService {

    Paiement findById(Long id);

    List<Paiement> findAll();
}
