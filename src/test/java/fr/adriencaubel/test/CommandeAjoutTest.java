package fr.adriencaubel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.*;

import fr.adriencaubel.entity.Commande;
import fr.adriencaubel.entity.LigneDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class CommandeAjoutTest {

	private void initializeDatabase(EntityManager em) {
		em.getTransaction().begin();

		Commande commande = new Commande();

		LigneDetail ligneDetail1 = new LigneDetail();
		LigneDetail ligneDetail2 = new LigneDetail();

		// Ajouter des deux côtés de la relation
		ligneDetail1.setCommande(commande);
		ligneDetail2.setCommande(commande);
		commande.getLigneDetails().add(ligneDetail1);
		commande.getLigneDetails().add(ligneDetail2);

		// Comme CASCADE.ALL seulement besoin de préciser la commande
		em.persist(commande);

		em.getTransaction().commit();
	}

	@Test
	public void testAddLigneDetailNotWorking() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = new LigneDetail();
		commande.getLigneDetails().add(ligneDetail); // Côté INVERSE uniquement (mappedBy)

		em.persist(ligneDetail); // La ligne est insérée mais commande_id reste NULL

		transaction.commit();

		// Vider le cache de premier niveau pour relire réellement la base
		em.clear();

		Commande commandeRelue = em.find(Commande.class, 1L);
		assertEquals(2, commandeRelue.getLigneDetails().size()); // La 3e ligne n'est pas rattachée
		assertNull(em.find(LigneDetail.class, ligneDetail.getId()).getCommande()); // FK NULL

		em.close();
		emf.close();
	}

	@Test
	public void testAddLigneDetailWorkingOwningSide() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = new LigneDetail();
		ligneDetail.setCommande(commande); // THIS ADDED : côté PROPRIÉTAIRE

		em.persist(ligneDetail); // La FK est bien écrite en base

		transaction.commit();

		// En base c'est correct... mais le modèle objet en mémoire est faux :
		// la collection n'a jamais été mise à jour
		assertEquals(2, commande.getLigneDetails().size());

		// Après rechargement depuis la base, on retrouve bien les 3 lignes
		em.clear();
		Commande commandeRelue = em.find(Commande.class, 1L);
		assertEquals(3, commandeRelue.getLigneDetails().size());

		em.close();
		emf.close();
	}

	@Test
	public void testAddLigneDetailWorkingConventional() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = new LigneDetail();
		ligneDetail.setCommande(commande); // THIS ADDED
		commande.getLigneDetails().add(ligneDetail); // THIS ADDED

		// Comme CASCADE.ALL, pas besoin de persist explicite sur la ligne
		transaction.commit();

		// Cohérent en mémoire...
		assertEquals(3, commande.getLigneDetails().size());

		// ... et en base
		em.clear();
		Commande commandeRelue = em.find(Commande.class, 1L);
		assertEquals(3, commandeRelue.getLigneDetails().size());

		em.close();
		emf.close();
	}

	/**
	 * Ici on justifie le choix de synchroniser les deux côté de la relation
	 * En effet en BDD on a bien 3 lignes mais lorsqu'on affiche commande.getLigneDetails().size() on en a que deux !
	 *
	 * Le premier test avec commande.getLigneDetails().add(ligneDetail); met FK à null
	 * d'après ce test, ligneDetail.setCommande(commande); n'est pas suffisant
	 * Donc il faut bien synchroniser les deux côté de la relation
	 */
	@Test
	public void testAddLigneDetailWorkingOwningSide_bug() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-bidirectional");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();

		initializeDatabase(em);

		transaction.begin();

		Commande commande = em.find(Commande.class, 1L);

		LigneDetail ligneDetail = new LigneDetail();
		ligneDetail.setCommande(commande); // THIS ADDED : côté PROPRIÉTAIRE

		em.persist(ligneDetail); // La FK est bien écrite en base

		em.flush(); // ou em.clear()

		System.out.println(commande.getLigneDetails().size());
		assertEquals(3, commande.getLigneDetails().size());

		transaction.commit();


		em.close();
		emf.close();
	}
}
