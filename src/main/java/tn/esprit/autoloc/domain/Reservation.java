package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReservation;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut; // EN_ATTENTE, CONFIRMEE, ANNULEE, TERMINEE

    /**
     * Côté PROPRIÉTAIRE : Reservation porte la FK client_id.
     * FetchType.LAZY : le Client n'est chargé que si on y accède.
     * Pas de cascade : un Client existe indépendamment de la Reservation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    /**
     * Côté PROPRIÉTAIRE : Reservation porte la FK vehicule_id.
     * FetchType.LAZY : le Vehicule n'est chargé que si on y accède.
     * Pas de cascade : un Vehicule existe indépendamment de la Reservation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    /**
     * Côté PROPRIÉTAIRE de la relation Reservation <-> Contrat (@OneToOne).
     * La FK contrat_id est portée par la table reservation.
     * CascadeType.ALL : le cycle de vie du Contrat suit celui de la Reservation.
     * FetchType.LAZY : le Contrat n'est chargé que si on y accède.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "contrat_id", unique = true)
    private Contrat contrat;
}