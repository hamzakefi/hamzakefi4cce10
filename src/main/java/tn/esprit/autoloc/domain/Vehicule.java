package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVehicule;

    private String immatriculation;
    private String marque;
    private String modele;
    private Double prixLocationJour;

    @Enumerated(EnumType.STRING)
    private StatutVehicule statut; // DISPONIBLE, LOUE, EN_MAINTENANCE

    /**
     * Côté PROPRIÉTAIRE de la relation Agence <-> Vehicule.
     * La FK agence_id est portée par la table vehicule.
     * FetchType.LAZY : l'Agence n'est chargée que si on y accède.
     * Pas de cascade : un Vehicule peut survivre à la suppression d'une Agence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id")
    private Agence agence;

    /**
     * Côté INVERSE de la relation Vehicule <-> Reservation.
     * mappedBy = "vehicule" correspond au champ dans Reservation.
     * FetchType.LAZY : les reservations ne sont chargées que si on y accède.
     */
    @OneToMany(mappedBy = "vehicule", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    /**
     * Côté INVERSE de la relation Vehicule <-> Maintenance.
     * mappedBy = "vehicule" correspond au champ dans Maintenance.
     * CascadeType.PERSIST : persister un Vehicule persiste ses Maintenances.
     * FetchType.LAZY : les maintenances ne sont chargées que si on y accède.
     */
    @OneToMany(
        mappedBy = "vehicule",
        fetch = FetchType.LAZY,
        cascade = CascadeType.PERSIST
    )
    @Builder.Default
    private List<Maintenance> maintenances = new ArrayList<>();

    /**
     * Côté PROPRIÉTAIRE de la relation ManyToMany Vehicule <-> Equipement.
     * Table de jointure : vehicule_equipement.
     * Set<Equipement> pour éviter les doublons.
     * FetchType.LAZY : les équipements ne sont chargés que si on y accède.
     * Pas de cascade : les équipements sont partagés et ne suivent pas le cycle de vie du vehicule.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "vehicule_equipement",
        joinColumns = @JoinColumn(name = "vehicule_id"),
        inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    @Builder.Default
    private Set<Equipement> equipements = new HashSet<>();
}
