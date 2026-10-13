package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.EmployeRepository;

import java.util.List;

/**
 * Implémentation du service Employe.
 *
 * <p>L'entité Employe possède : idEmploye, nom, prenom, role (RoleEmploye), agence.
 * Le "poste" correspond au champ {@code role} dans le modèle JPA.</p>
 *
 * <p>Règles métier :</p>
 * <ul>
 *   <li>create : refuse un idEmploye non nul</li>
 *   <li>create : refuse un nom ou un prénom vide</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : copie nom, prenom, role, agence ; ne modifie pas l'identifiant</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements IEmployeService {

    private final EmployeRepository employeRepository;

    @Override
    public Employe create(Employe employe) {
        if (employe.getIdEmploye() != null) {
            throw new IllegalArgumentException(
                "Un nouvel employé ne doit pas avoir d'identifiant"
            );
        }
        if (employe.getNom() == null || employe.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom de l'employé est obligatoire"
            );
        }
        if (employe.getPrenom() == null || employe.getPrenom().isBlank()) {
            throw new IllegalArgumentException(
                "Le prénom de l'employé est obligatoire"
            );
        }
        return employeRepository.save(employe);
    }

    @Override
    public Employe findById(Long id) {
        return employeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employe", id));
    }

    @Override
    public List<Employe> findAll() {
        return employeRepository.findAll();
    }

    @Override
    public Employe update(Long id, Employe employe) {
        Employe existant = findById(id);

        if (employe.getNom() == null || employe.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom de l'employé est obligatoire"
            );
        }

        existant.setNom(employe.getNom());
        existant.setPrenom(employe.getPrenom());
        existant.setRole(employe.getRole());
        existant.setAgence(employe.getAgence());

        return employeRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employe", id);
        }
        employeRepository.deleteById(id);
    }
}
