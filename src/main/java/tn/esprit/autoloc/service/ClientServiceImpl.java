package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.ClientRepository;

import java.util.List;

/**
 * Implémentation du service Client.
 *
 * <p>L'entité Client possède : idClient, nom, prenom, email, numPermis, reservations.</p>
 *
 * <p>Règles métier :</p>
 * <ul>
 *   <li>create : refuse un idClient non nul</li>
 *   <li>create : refuse un nom ou un email vide</li>
 *   <li>findById : lève ResourceNotFoundException si absent</li>
 *   <li>update : copie nom, prenom, email, numPermis ; conserve les réservations</li>
 *   <li>deleteById : vérifie l'existence avant suppression</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final ClientRepository clientRepository;

    @Override
    public Client create(Client client) {
        if (client.getIdClient() != null) {
            throw new IllegalArgumentException(
                "Un nouveau client ne doit pas avoir d'identifiant"
            );
        }
        if (client.getNom() == null || client.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom du client est obligatoire"
            );
        }
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                "L'email du client est obligatoire"
            );
        }
        return clientRepository.save(client);
    }

    @Override
    public Client findById(Long id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    @Override
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client update(Long id, Client client) {
        Client existant = findById(id);

        if (client.getNom() == null || client.getNom().isBlank()) {
            throw new IllegalArgumentException(
                "Le nom du client est obligatoire"
            );
        }

        existant.setNom(client.getNom());
        existant.setPrenom(client.getPrenom());
        existant.setEmail(client.getEmail());
        existant.setNumPermis(client.getNumPermis());
        // La collection reservations est conservée (gérée via Reservation).

        return clientRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client", id);
        }
        clientRepository.deleteById(id);
    }
}
