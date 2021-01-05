package com.dummy.myerp.business.impl.manager;

import com.dummy.myerp.business.contrat.BusinessProxy;
import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.business.impl.TransactionManager;
import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ComptabiliteManagerImplTest {

    private ComptabiliteManagerImpl manager = new ComptabiliteManagerImpl();

    private static BusinessProxy businessProxyMock=mock(BusinessProxy.class);
    private static DaoProxy daoProxyMock=mock(DaoProxy.class);
    private static TransactionManager transactionManagerMock=mock(TransactionManager.class);
    private static ComptabiliteDao comptabiliteDaoMock=mock(ComptabiliteDao.class);

    @BeforeAll
    static void initializeTestBeans(){
        AbstractBusinessManager.configure(businessProxyMock, daoProxyMock, transactionManagerMock);
        when(daoProxyMock.getComptabiliteDao()).thenReturn(comptabiliteDaoMock);
    }

    @Test
    @DisplayName("Ecriture comptable valide, ne devrait pas envoyer une exception")
    public void checkEcritureComptableUnit_shouldNotThrowFunctionnalException() {
        EcritureComptable ecritureComptable = getEcritureComptable();
        ecritureComptable.setReference("AC-2021/00001");
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                                                                                 null, null,
                                                                                 new BigDecimal(123)));
        try {
            manager.checkEcritureComptableUnit(ecritureComptable);
        } catch (FunctionalException e) {
            fail("Cette exception ne devrait pas se produire.", e);
        }
    }

    @Test
    @DisplayName("Ecriture comptable vide")
    public void checkEcritureComptableUnitViolation() {
        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(new EcritureComptable()));
    }

    @Test
    @DisplayName("Ecriture comptable non-équilibrée")
    public void checkEcritureComptableUnitRG2_shouldThrowFunctionalExceptionNonEquilibree() {
        EcritureComptable ecritureComptable = getEcritureComptable();
        LigneEcritureComptable ecritureDebit = new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null);
        LigneEcritureComptable ecritureCredit = new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal(1234));

        ecritureComptable.getListLigneEcriture().add(ecritureDebit);
        ecritureComptable.getListLigneEcriture().add(ecritureCredit);
        Throwable functionalException = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptable));
        assertEquals(functionalException.getMessage(), "L'écriture comptable n'est pas équilibrée.");

    }

    @Nested
    @DisplayName("Test des scénarios relatifs à la règle de gestion 3")
    class checkRG3 {
        @Test
        @DisplayName("Ecriture comptable sans ligne débit")
        public void checkEcritureComptableUnitRG3Debit() {
            EcritureComptable ecritureComptable = getEcritureComptable();
            ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, new BigDecimal(0),
                    null));
            ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, new BigDecimal(0),
                    null));
            Throwable functionalException = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptable));
            assertEquals(functionalException.getMessage(), "L'écriture comptable doit avoir au moins deux lignes : une ligne au débit et une ligne au crédit.");
        }
        @Test
        @DisplayName("Ecriture comptable sans ligne crédit")
        public void checkEcritureComptableUnitRG3Credit() {
            EcritureComptable ecritureComptable = getEcritureComptable();
            ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, null,
                    new BigDecimal(0)));
            ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, null,
                    new BigDecimal(0)));
            Throwable functionalException = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptable));
            assertEquals(functionalException.getMessage(), "L'écriture comptable doit avoir au moins deux lignes : une ligne au débit et une ligne au crédit.");
        }
    }

    @Test
    @DisplayName("Ecriture avec plus de 2 chiffres après la virgule")
    public void only2DigitsAfterDecimal() {
        EcritureComptable ecritureComptable = getEcritureComptable();
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal("123.567"),
                null));
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal("123.567")));

        Throwable functionalException = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptable));
        assertEquals(functionalException.getMessage(), "L'écriture comptable ne respecte pas les règles de gestion.");
    }

    @Test
    @DisplayName("Ecriture dont la référence existe déjà")
    public void referenceMustBeUnique() throws NotFoundException {
        EcritureComptable ecritureComptable = getEcritureComptable();
        addValidLines(ecritureComptable);
        ecritureComptable.setReference("AC-2021/00001");

        when(comptabiliteDaoMock.getEcritureComptableByRef("AC-2021/00001")).thenReturn(ecritureComptable);

        EcritureComptable ecritureComptableComparaison = new EcritureComptable();
        ecritureComptableComparaison.setId(56);
        ecritureComptableComparaison.setLibelle("Libelle2");
        ecritureComptableComparaison.setJournal(new JournalComptable("AC", "Achat"));
        ecritureComptableComparaison.setDate(new Date());
        addValidLines(ecritureComptableComparaison);
        ecritureComptableComparaison.setReference("AC-2021/00001");

        Throwable functionalException = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptableComparaison));
        assertEquals(functionalException.getMessage(), "Une autre écriture comptable existe déjà avec la même référence.");    }

    @Nested
    @DisplayName("Test de l'authenticité de la référence")
    class checkRefValidity {

        @Test
        @DisplayName("Ecriture dont la référence ne respecte pas le code journal fournis")
        public void referenceMustHaveCorrectValues() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2020, Calendar.JANUARY, 1);

            EcritureComptable ecritureComptable = getEcritureComptable();
            addValidLines(ecritureComptable);
            ecritureComptable.setJournal(new JournalComptable("BQ", "Banque"));
            ecritureComptable.setDate(calendar.getTime());
            ecritureComptable.setLibelle("Libelle");

            ecritureComptable.setReference("CQ-2021/00001");

            Throwable functionalException = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptable));
            assertEquals(functionalException.getMessage(), "La référence de l'écriture comptable ne correspond pas au code journal enregistré.");
        }
        @Test
        @DisplayName("Ecriture dont la référence ne respecte pas l'année fournis")
        public void referenceMustHaveCorrectValues2() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2020, Calendar.JANUARY, 1);

            EcritureComptable ecritureComptable = getEcritureComptable();
            addValidLines(ecritureComptable);
            ecritureComptable.setJournal(new JournalComptable("BQ", "Banque"));
            ecritureComptable.setDate(calendar.getTime());
            ecritureComptable.setLibelle("Libelle");

            ecritureComptable.setReference("BQ-2019/00001");

            Throwable functionalException = assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptable));
            assertEquals(functionalException.getMessage(), "La référence de l'écriture comptable ne correspond pas à l'année enregistrée.");
        }
    }


    @Nested
    @DisplayName("Test des scénarios relatifs aux références")
    class checkRef {
        @Test
        @DisplayName("Ajout de la séquence : séquence déjà existante")
        public void addRefWithCreatedSequence() throws NotFoundException {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2021, Calendar.JANUARY, 1);

            EcritureComptable ecritureComptable = new EcritureComptable();
            ecritureComptable.setJournal(new JournalComptable("BQ", "Banque"));
            ecritureComptable.setDate(calendar.getTime());
            ecritureComptable.setLibelle("Libelle");
            addValidLines(ecritureComptable);

            when(comptabiliteDaoMock.getSequenceEcritureComptable(2021, "BQ")).thenReturn(new SequenceEcritureComptable(2020,51));
            manager.addReference(ecritureComptable);

            assertEquals("BQ-2021/00052", ecritureComptable.getReference());
        }

        @Test
        @DisplayName("Ajout de la séquence : séquence inexistante")
        public void addRefWithNoSequenceCreated() throws NotFoundException {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2021, Calendar.JANUARY, 1);

            EcritureComptable ecritureComptable = new EcritureComptable();
            ecritureComptable.setJournal(new JournalComptable("BQ", "Banque"));
            ecritureComptable.setDate(calendar.getTime());
            ecritureComptable.setLibelle("Libelle");
            addValidLines(ecritureComptable);

            when(comptabiliteDaoMock.getSequenceEcritureComptable(2021, "BQ"))
                    .thenThrow(new NotFoundException("Séquence non trouvée"));
            manager.addReference(ecritureComptable);

            assertEquals("BQ-2021/00001", ecritureComptable.getReference());
        }
    }

    private EcritureComptable getEcritureComptable() {
        EcritureComptable vEcritureComptable = new EcritureComptable();
        vEcritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        vEcritureComptable.setDate(new Date());
        vEcritureComptable.setLibelle("Libelle");
        return vEcritureComptable;
    }

    private void addValidLines(EcritureComptable ecritureComptable) {
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123),
                null));
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal(123)));
    }


}
