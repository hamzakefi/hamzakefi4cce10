# AutoLoc — Atelier 4 : Notes de service

> Projet : AutoLoc — Spring Boot 4.1.1 / Java 17 / Spring Data JPA / Hibernate / MySQL / Lombok  
> Package racine : `tn.esprit.autoloc`  
> Date : 2026-10-13

---

## A. Modes d'injection comparés

### Tableau comparatif

| Mode | Champ `final` possible | Dépendance visible | Utilisation hors conteneur Spring | Signalement SonarQube for IDE |
|---|---|---|---|---|
| **Injection par constructeur** | ✅ Oui | ✅ Oui (dans la signature du constructeur) | ✅ Oui (instantiation normale) | ✅ Recommandé, aucun avertissement |
| **Injection par attribut** (`@Autowired`) | ❌ Non | ❌ Non (réflexion cachée) | ❌ Non (NPE si Spring absent) | ⚠️ Règle S6813 signalée |

### Pourquoi le TP impose l'injection par constructeur

1. **Immuabilité** : le champ `private final` garantit que la dépendance ne peut pas être réassignée après construction.
2. **Testabilité** : la classe peut être instanciée sans conteneur Spring en passant un mock au constructeur.
3. **Détection des cycles** : une dépendance circulaire entre services est détectée au démarrage de l'application (et non en cours d'exécution).
4. **Conformité SonarQube** : l'injection par attribut déclenche la règle S6813 ("Prefer constructor injection").
5. **Lombok** : `@RequiredArgsConstructor` génère automatiquement le constructeur pour tous les champs `final`, sans écrire le code manuellement.

---

## B. Services — Tableau des neuf services

| Service | Dépendances injectées | Mode d'injection | Justification |
|---|---|---|---|
| `AgenceServiceImpl` | `AgenceRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |
| `EmployeServiceImpl` | `EmployeRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |
| `VehiculeServiceImpl` | `VehiculeRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |
| `EquipementServiceImpl` | `EquipementRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |
| `ClientServiceImpl` | `ClientRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |
| `ReservationServiceImpl` | `ReservationRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |
| `ContratServiceImpl` | `ContratRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |
| `PaiementServiceImpl` | `PaiementRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, service en lecture seule |
| `MaintenanceServiceImpl` | `MaintenanceRepository` | Constructeur (`@RequiredArgsConstructor`) | Champ `final`, conforme S6813 |

> **Note** : les repositories du projet se nomment `XxxRepository` (sans préfixe `I`) conformément au code de l'Atelier 3. Les services s'y adaptent sans renommer les interfaces existantes.

---

## C. Messages d'erreur du conteneur Spring

### Cas 1 : `NoSuchBeanDefinitionException`

**Message type :**
```
No qualifying bean of type 'tn.esprit.autoloc.service.IContratService' available
```

**Causes probables :**

1. **Annotation `@Service` absente** sur `ContratServiceImpl`.  
   *Correction :* ajouter `@Service` en tête de classe.

2. **Classe placée hors du package scanné** par Spring Boot.  
   Spring Boot scanne automatiquement le package de la classe `@SpringBootApplication` et ses sous-packages.  
   Si `ContratServiceImpl` est dans un package extérieur (ex. `com.autre.service`), il ne sera pas détecté.  
   *Correction :* déplacer la classe dans `tn.esprit.autoloc.service` ou ajouter `@ComponentScan("com.autre.service")` sur la classe principale.

### Cas 2 : `NoUniqueBeanDefinitionException`

**Message type :**
```
No qualifying bean of type 'INotificateur' available:
expected single matching bean but found 2: emailNotificateur, smsNotificateur
```

**Solution A — `@Primary`**

Désigne le bean à utiliser par défaut lorsque plusieurs implémentations existent :

```java
@Service
@Primary
public class EmailNotificateur implements INotificateur { ... }

@Service
public class SmsNotificateur implements INotificateur { ... }
```

Spring injectera automatiquement `EmailNotificateur` partout où `INotificateur` est demandé.

**Solution B — `@Qualifier`**

Précise explicitement quel bean injecter sur chaque point d'injection :

```java
@Service
@RequiredArgsConstructor
public class MonService {

    private final INotificateur notificateur;

    public MonService(@Qualifier("smsNotificateur") INotificateur notificateur) {
        this.notificateur = notificateur;
    }
}
```

Ou, avec `@RequiredArgsConstructor` de Lombok et `@Qualifier` sur le paramètre :

```java
// On écrit le constructeur manuellement pour pouvoir annoter le paramètre
public MonService(@Qualifier("emailNotificateur") INotificateur notificateur) {
    this.notificateur = notificateur;
}
```

### Cas 3 : Dépendance circulaire

**Exemple :**
```
clientServiceImpl → reservationServiceImpl → clientServiceImpl
```

**Pourquoi cela pose problème :**  
Spring ne peut pas instancier `ClientServiceImpl` sans avoir d'abord instancié `ReservationServiceImpl`, et inversement. Le conteneur détecte ce cycle et refuse de démarrer l'application.

**Pourquoi l'injection par constructeur révèle le cycle au démarrage :**  
Avec l'injection par attribut (`@Autowired`), Spring peut créer les beans partiellement et les compléter plus tard (via des proxies). Avec l'injection par constructeur, les dépendances doivent toutes être résolues avant la construction : le cycle est donc détecté immédiatement, ce qui est le comportement souhaitable (fail-fast).

**Comment supprimer ce cycle :**

1. **Réorganiser les responsabilités** : identifier la méthode qui crée le cycle et la déplacer dans un troisième service (ex. `LocationServiceImpl`) qui dépend des deux autres sans que ceux-ci se dépendent mutuellement.
2. **Utiliser un événement applicatif** (`ApplicationEventPublisher`) pour découpler les deux services.
3. **Ne jamais utiliser `@Lazy`** comme rustine : cela masque le problème de conception sans le résoudre.

Dans le projet AutoLoc, aucune dépendance circulaire n'a été introduite : chaque service ne dépend que de son repository.

---

## D. Anomalies SonarQube for IDE

> **Note** : SonarQube for IDE (SonarLint) est un plugin IntelliJ IDEA. Les anomalies ci-dessous sont expliquées conformément au TP. Leur observation réelle nécessite le plugin installé dans IntelliJ et une analyse manuelle (clic droit → *Analyze with SonarQube for IDE*).

### Anomalie 1 — Règle S6813 : Injection par attribut (`@Autowired` sur un champ)

**Règle :** S6813 — *"Prefer constructor injection"*  
**Contexte :** si `ContratServiceImpl` utilisait `@Autowired` sur le champ au lieu de l'injection par constructeur.

```java
// Code problématique
@Autowired
private IContratRepository contratRepository; // ← S6813 déclenché
```

**Correction apportée :** utilisation de `@RequiredArgsConstructor` avec champ `private final`.

```java
// Code correct
@RequiredArgsConstructor
public class ContratServiceImpl implements IContratService {
    private final ContratRepository contratRepository;
}
```

### Anomalie 2 — Règle S112 : Exception générique (`RuntimeException`)

**Règle :** S112 — *"Generic exceptions should never be thrown"*  
**Contexte :** si `findById` lançait `new RuntimeException(...)` au lieu de `ResourceNotFoundException`.

```java
// Code problématique
throw new RuntimeException("Contrat introuvable"); // ← S112 déclenché
```

**Correction apportée :** utilisation de `ResourceNotFoundException` (classe métier spécialisée héritant de `RuntimeException`).

```java
// Code correct
throw new ResourceNotFoundException("Contrat", id);
```

### Anomalie 3 — Règle S6832 / bonne pratique : Champ mutable dans un service singleton

**Règle :** S6832 (ou alerte générale SonarQube) — *"Non-final fields in singleton beans"*  
**Contexte :** déclarer un champ d'état mutable dans un service (`@Service` = singleton Spring) peut provoquer des bugs de concurrence.

**Exemple de bug :**
```java
@Service
public class ContratServiceImpl implements IContratService {
    private Contrat dernierContratCree; // ← champ d'état mutable : BUG potentiel

    public Contrat create(Contrat contrat) {
        dernierContratCree = contratRepository.save(contrat);
        // En environnement multi-thread, un autre thread peut écraser dernierContratCree
        return dernierContratCree;
    }
}
```

**Correction apportée :** aucun champ d'état dans les services, uniquement des dépendances `final`.

> **Remarque sur Equipement** : le modèle JPA de l'Atelier 3 ne possède pas de champ `quantite`. La règle du TP "refuser une quantité négative" n'a pas pu être implémentée. Si ce champ est ajouté ultérieurement à l'entité, la validation devra être ajoutée dans `EquipementServiceImpl.create()` et `update()`.

> **Remarque sur Maintenance** : le modèle JPA de l'Atelier 3 ne possède pas de champ `cout`. La règle du TP "refuser un coût négatif" n'a pas pu être implémentée. Si ce champ est ajouté ultérieurement, la validation devra être ajoutée dans `MaintenanceServiceImpl.create()` et `update()`.

---

## E. Questions de compréhension

### Question 1 — Où est le `new` de `ContratServiceImpl` et qui l'exécute ?

Le mot-clé `new` n'apparaît **nulle part dans le code applicatif** pour instancier `ContratServiceImpl`.  
C'est le **conteneur IoC de Spring** (ApplicationContext) qui exécute ce `new` à son démarrage.  
Il détecte l'annotation `@Service`, instancie la classe via le constructeur généré par Lombok `@RequiredArgsConstructor`, et y injecte automatiquement le bean `ContratRepository`.  
L'objet ainsi créé est stocké dans le contexte Spring et partagé (singleton) entre tous les composants qui en dépendent.

### Question 2 — Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?

**Principe de Programmation Orientée Interface (POI) / Principe de ségrégation des dépendances :**

- En dépendant de l'interface `IContratService`, le contrôleur ne connaît que le **contrat** (les méthodes disponibles), pas l'implémentation.
- On peut remplacer `ContratServiceImpl` par une autre implémentation (ex. `ContratServiceMockImpl` pour les tests) **sans modifier le contrôleur**.
- Spring injecte automatiquement l'implémentation concrète (`ContratServiceImpl`) grâce à `@Service`.
- Cela respecte le principe SOLID "Dependency Inversion Principle" (D de SOLID).

### Question 3 — Pourquoi un service singleton doit-il rester sans état ? Donne un exemple de bug.

Un service annoté `@Service` est un **singleton** : une seule instance est créée et partagée entre tous les threads.  
Si le service conserve un état mutable, plusieurs requêtes concurrentes peuvent lire/modifier cet état simultanément, produisant des résultats incohérents.

**Exemple de bug :**
```java
@Service
public class ContratServiceImpl implements IContratService {
    private Long dernierIdCreé; // ← DANGER : état partagé entre threads

    @Override
    public Contrat create(Contrat contrat) {
        Contrat saved = contratRepository.save(contrat);
        dernierIdCreé = saved.getIdContrat(); // Thread A écrit
        // Thread B écrit par-dessus avant que Thread A ait fini de lire
        return saved;
    }
}
```

**Correction :** les services ne doivent contenir que des champs `final` pointant vers des dépendances injectées (repositories, autres services). Toute donnée temporaire doit être une variable locale de méthode, jamais un champ d'instance.

### Question 4 — Pourquoi `update` charge-t-il l'entité existante avant `save` au lieu de transmettre directement l'objet reçu ?

Plusieurs raisons critiques :

1. **Protection de l'identifiant** : l'entité reçue du client peut avoir un `idContrat` nul ou incorrect. En chargeant l'entité existante, on s'assure de toujours utiliser l'identifiant réel de la base.

2. **Protection des relations** : l'objet reçu ne contient pas forcément la liste des paiements. Passer directement cet objet à `save` avec `orphanRemoval = true` supprimerait tous les paiements existants. En chargeant d'abord `existant`, la collection `paiements` est préservée.

3. **Copie sélective** : on ne copie que les attributs réellement modifiables (`dateSignature`, `montantTotal`, `valide`), pas les métadonnées ou associations gérées par JPA.

4. **Gestion de session Hibernate** : l'entité chargée par `findById` est dans le contexte de persistance Hibernate (état *managed*). Appeler `save` sur cette entité produit un `UPDATE` SQL ciblé. Passer un objet détaché pourrait générer un `INSERT` ou un comportement inattendu.

### Question 5 — Différences entre les annotations et justification du service Paiement en lecture seule

**`@Component` vs `@Bean` :**

| | `@Component` | `@Bean` |
|---|---|---|
| **Placement** | Sur la classe elle-même | Sur une méthode dans une classe `@Configuration` |
| **Scan automatique** | ✅ Oui (classpath scanning) | ❌ Non (déclaration explicite) |
| **Contrôle** | Minimal (Spring instancie) | Total (le développeur écrit le code d'instanciation) |
| **Usage typique** | Services, repositories, composants internes | Beans tiers, configuration complexe, bibliothèques externes |

`@Service`, `@Repository`, `@Controller` sont des spécialisations de `@Component` avec une sémantique métier.

**`@Primary` vs `@Qualifier` :**

| | `@Primary` | `@Qualifier` |
|---|---|---|
| **Portée** | Globale (un seul bean par défaut pour le type) | Locale (choix par point d'injection) |
| **Placement** | Sur la classe d'implémentation | Sur le paramètre du constructeur / le champ injecté |
| **Usage** | Quand il y a un "meilleur choix évident" | Quand différents consommateurs ont besoin d'implémentations différentes |

**Pourquoi `IPaiementService` n'expose-t-il ni création, ni modification, ni suppression ?**

L'entité `Paiement` est dans une relation `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` avec `Contrat`.  
Cela signifie que :
- Créer un `Paiement` directement sans le rattacher à un `Contrat` violerait l'intégrité des données (clé étrangère `contrat_id`).
- Modifier ou supprimer un `Paiement` via un service dédié risque de désynchroniser le `Contrat` (incohérence du `montantTotal`).

Toutes les opérations d'écriture doivent donc passer par `ContratServiceImpl`, qui gère le cycle de vie complet (via `addPaiement`/`removePaiement` et la cascade JPA).  
`IPaiementService` en lecture seule permet de consulter les paiements sans risquer de corrompre la relation.

---

*Document généré dans le cadre de l'Atelier 4 du module ASI 26-27 — AutoLoc.*
