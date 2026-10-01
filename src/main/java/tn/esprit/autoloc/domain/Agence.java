package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
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

    @OneToMany(mappedBy = "agence")
    private List<Vehicule> vehicules;

    @OneToMany(mappedBy = "agence")
    private List<Employe> employes;
}