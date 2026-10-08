package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agence implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgence;

    private String nom;
    private String adresse;
    private String telephone;

    /**
     * Côté INVERSE de la relation Agence <-> Vehicule (bidirectionnelle).
     * mappedBy = "agence" correspond au champ dans Vehicule.
     * FetchType.LAZY : les vehicules ne sont chargés que si on y accède.
     * Pas de cascade : un vehicule peut survivre à la suppression d'une agence.
     */
    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehicule> vehicules = new ArrayList<>();

    /**
     * Côté INVERSE de la relation Agence <-> Employe (bidirectionnelle).
     * mappedBy = "agence" correspond au champ dans Employe.
     * FetchType.LAZY : les employes ne sont chargés que si on y accède.
     * Pas de cascade : pas de suppression automatique des employés.
     */
    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employe> employes = new ArrayList<>();
}