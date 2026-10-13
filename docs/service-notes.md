# AutoLoc — Atelier 4 : Notes de service

> Projet : AutoLoc · Spring Boot 4.1.1 · Java 17 · Spring Data JPA · MySQL · Lombok  
> Package racine : `tn.esprit.autoloc`

---

## A. Modes d'injection comparés

| Mode | Champ `final` | Dépendance visible | Utilisation hors Spring | SonarQube for IDE |
|---|---|---|---|---|
| **Par constructeur** | ✅ Oui | ✅ Oui (signature du constructeur) | ✅ Oui | ✅ Aucune alerte |
| **Par attribut** (`@Autowired`) | ❌ Non | ❌ Non (réflexion) | ❌ Non (NPE) | ⚠️ Règle S6813 |

Le TP impose l'injection par constructeur pour quatre raisons :

1. **Immuabilité** : le champ `private final` ne peut pas être réassigné après construction.
2. **Testabilité** : la classe est instanciable sans conteneur Spring (on passe un mock au constructeur).
3. **Détection des cycles** : une dépendance circulaire est signalée au démarrage, pas en cours d'exécution.
4. **Lombok** : `@RequiredArgsConstructor` génère le constructeur automatiquement pour tous les champs `final`.

---

## B. Services — Tableau récapitulatif

| Service | Repository injecté | Mode d'injection |
|---|---|---|
| `AgenceServiceImpl` | `AgenceRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `EmployeServiceImpl` | `EmployeRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `VehiculeServiceImpl` | `VehiculeRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `EquipementServiceImpl` | `EquipementRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `ClientServiceImpl` | `ClientRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `ReservationServiceImpl` | `ReservationRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `ContratServiceImpl` | `ContratRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `PaiementServiceImpl` | `PaiementRepository` | Constructeur (`@RequiredArgsConstructor`) |
| `MaintenanceServiceImpl` | `MaintenanceRepository` | Constructeur (`@RequiredArgsConstructor`) |

> Les repositories se nomment `XxxRepository` (sans préfixe `I`) conformément au code de l'Atelier 3.

---

## C. Diagnostic des erreurs du conteneur Spring

### Cas 1 — `NoSuchBeanDefinitionException`

```
No qualifying bean of type 'IContratService' available
```

**Causes :**
- Annotation `@Service` absente sur `ContratServiceImpl`.
- Classe placée en dehors du package scanné par `@SpringBootApplication` (`tn.esprit.autoloc`).

**Corrections :** ajouter `@Service` sur la classe ou déplacer le fichier dans le bon package.

### Cas 2 — `NoUniqueBeanDefinitionException`

```
expected single matching bean but found 2: emailNotificateur, smsNotificateur
```

**Solution A — `@Primary`** : annoter l'implémentation à utiliser par défaut.

```java
@Service
@Primary
public class EmailNotificateur implements INotificateur { ... }
```

**Solution B — `@Qualifier`** : préciser le bean au point d'injection.

```java
public MonService(@Qualifier("smsNotificateur") INotificateur notificateur) {
    this.notificateur = notificateur;
}
```

### Cas 3 — Dépendance circulaire

```
clientServiceImpl → reservationServiceImpl → clientServiceImpl
```

Spring ne peut pas construire les deux beans simultanément. L'injection par constructeur révèle le cycle au démarrage (fail-fast). Avec l'injection par attribut, Spring masquerait le problème via des proxies.

**Correction :** déplacer la logique partagée dans un troisième service sans dépendances croisées.

---

## D. Anomalies SonarQube for IDE

> L'analyse SonarQube for IDE est effectuée manuellement dans IntelliJ IDEA (clic droit sur le package → *Analyze with SonarQube for IDE*).

### Anomalie 1 — S6813 : injection par attribut

```java
@Autowired
private ContratRepository contratRepository; // ← déclenche S6813
```

**Correction :** champ `private final` + `@RequiredArgsConstructor`.

### Anomalie 2 — S112 : exception générique

```java
throw new RuntimeException("Contrat introuvable"); // ← déclenche S112
```

**Correction :** utiliser `ResourceNotFoundException` (classe métier héritant de `RuntimeException`).

### Anomalie 3 — état mutable dans un singleton

Un service `@Service` est un singleton partagé entre tous les threads. Un champ non `final` mutable crée un risque de concurrence :

```java
// Mauvais exemple
private Contrat dernierContratCree; // écrasé par un autre thread avant lecture
```

**Correction :** aucun champ d'état dans les services ; uniquement des dépendances `final`.

> **Note Equipement** : le champ `quantite` absent du modèle JPA empêche d'implémenter la règle "quantité négative".  
> **Note Maintenance** : le champ `cout` absent du modèle JPA empêche d'implémenter la règle "coût négatif".

---

## E. Questions de compréhension

**1. Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**  
Le mot-clé `new` n'apparaît pas dans le code applicatif. C'est le conteneur IoC de Spring qui instancie `ContratServiceImpl` au démarrage, via le constructeur généré par Lombok, et injecte `ContratRepository`.

**2. Pourquoi un contrôleur dépend-il de `IContratService` et non de `ContratServiceImpl` ?**  
En dépendant de l'interface, le contrôleur ne connaît que le contrat (les méthodes disponibles), pas l'implémentation. On peut remplacer `ContratServiceImpl` par un mock de test sans modifier le contrôleur. C'est le principe SOLID de dépendance vers l'abstraction.

**3. Pourquoi un service singleton doit-il rester sans état ?**  
Une seule instance est partagée entre tous les threads. Un champ mutable peut être écrasé par un autre thread avant lecture, produisant des résultats incohérents. Exemple : `private Contrat dernierCree;` sera lu par le thread A avec la valeur écrite par le thread B.

**4. Pourquoi `update` charge-t-il l'entité existante avant `save` ?**  
- **Protection de l'identifiant** : l'objet reçu peut avoir un `id` incorrect.  
- **Protection des relations** : sans chargement, la liste des paiements serait nulle, et `orphanRemoval = true` les supprimerait tous.  
- **Hibernate** : l'entité chargée est en état *managed* ; `save` génère un `UPDATE` ciblé, pas un `INSERT` inattendu.

**5. `@Component` vs `@Bean` · `@Primary` vs `@Qualifier` · pourquoi `IPaiementService` est-il en lecture seule ?**

| | `@Component` | `@Bean` |
|---|---|---|
| Placement | Sur la classe | Sur une méthode `@Configuration` |
| Scan automatique | ✅ | ❌ |
| Usage | Composants internes | Bibliothèques tierces, config complexe |

`@Primary` désigne le bean par défaut (global). `@Qualifier` choisit un bean spécifique au point d'injection (local).

`IPaiementService` est en lecture seule parce que les paiements sont liés à un contrat par `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`. Créer ou supprimer un paiement directement contournerait la gestion du `montantTotal` par `ContratServiceImpl` et violerait l'intégrité des données.
