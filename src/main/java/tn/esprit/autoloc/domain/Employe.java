package tn.esprit.autoloc.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employe implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmploye;

    private String nom;
    private String prenom;

    @Enumerated(EnumType.STRING)
    private RoleEmploye role;

    @ManyToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;
}