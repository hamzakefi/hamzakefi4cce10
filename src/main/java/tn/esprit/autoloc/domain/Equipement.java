package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipement;

    private String libelle;

    /**
     * Côté INVERSE de la relation ManyToMany Vehicule <-> Equipement.
     * mappedBy = "equipements" correspond exactement au champ dans Vehicule.
     * FetchType.LAZY : les vehicules ne sont chargés que si on y accède.
     * Pas de cascade : un Equipement est partagé entre plusieurs Vehicules.
     */
    @ManyToMany(mappedBy = "equipements", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Vehicule> vehicules = new HashSet<>();
}