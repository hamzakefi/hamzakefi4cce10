package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.EmployeRepository;

import java.util.List;

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
