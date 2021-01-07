package com.dummy.myerp.business.impl.manager;

import com.dummy.myerp.business.impl.TransactionManager;
import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.*;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ComptabiliteManagerImplTest {

    private static ComptabiliteManagerImpl manager;

    //private static BusinessProxy businessProxyMock=mock(BusinessProxy.class);
    private static DaoProxy daoProxyMock=mock(DaoProxy.class);
    private static TransactionManager transactionManagerMock=mock(TransactionManager.class);
    private static ComptabiliteDao comptabiliteDaoMock=mock(ComptabiliteDao.class);

    @BeforeAll
    static void configureDao() {
        when(daoProxyMock.getComptabiliteDao()).thenReturn(comptabiliteDaoMock);
    }

    @BeforeEach
    void initializeTestBeans(){
        manager = spy(new ComptabiliteManagerImpl());
        manager.configure(null, daoProxyMock, transactionManagerMock);
    }

    @AfterEach
    void nullifyManager(){
        manager = null;
    }

    @Test
    @DisplayName("Ecriture comptable valide, ne devrait pas envoyer une exception")
    public void checkEcritureComptableUnit_shouldNotThrowFunctionnalException() {
        EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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
        EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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
            EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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
            EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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
    @DisplayName("Ecriture avec des valeurs négatives")
    public void negativeValues() {
        EcritureComptable ecritureComptable = getEmptyEcritureComptable();
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal("-100"), null));
        ecritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, null, new BigDecimal("-100")));
        manager.addReference(ecritureComptable);

        try {
            manager.checkEcritureComptableUnit(ecritureComptable);
        } catch (FunctionalException e) {
            fail("Cette exception en devrait pas se produire : valeurs négatives non-acceptées");
        }
    }

    @Test
    @DisplayName("Ecriture avec plus de 2 chiffres après la virgule")
    public void only2DigitsAfterDecimal() {
        EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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
        EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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

            EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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

            EcritureComptable ecritureComptable = getEmptyEcritureComptable();
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

    @Nested
    @DisplayName("Opération CRUD des écritures sans IT")
    class opCRUD{

        @Test
        @DisplayName("Sauvegarde d'une écriture")
        public void ecritureWrite() throws FunctionalException {

            EcritureComptable ecritureComptable = getEmptyEcritureComptable();
            addValidLines(ecritureComptable);
            ecritureComptable.setReference("AC-2021/00001");

            TransactionStatus transactionStatusMock = mock(TransactionStatus.class);
            doNothing().when(manager).checkEcritureComptable(any(EcritureComptable.class));
            when(transactionManagerMock.beginTransactionMyERP()).thenReturn(transactionStatusMock);
            when(daoProxyMock.getComptabiliteDao()).thenReturn(comptabiliteDaoMock);
            doNothing().when(comptabiliteDaoMock).insertEcritureComptable(any(EcritureComptable.class));
            doNothing().when(transactionManagerMock).commitMyERP(transactionStatusMock);
            doNothing().when(transactionManagerMock).rollbackMyERP(null);

            manager.insertEcritureComptable(ecritureComptable);

            verify(transactionManagerMock, atLeastOnce()).commitMyERP(transactionStatusMock);
            verify(transactionManagerMock, atLeastOnce()).rollbackMyERP(null);
        }

        @Test
        @DisplayName("Mise à jour d'une écriture")
        public void updateEcritureComptable() throws FunctionalException {

            EcritureComptable ecritureComptable = getFullEcritureComptable();

            TransactionStatus transactionStatusMock = Mockito.mock(TransactionStatus.class);
            Mockito.when(transactionManagerMock.beginTransactionMyERP()).thenReturn(transactionStatusMock);
            Mockito.when(daoProxyMock.getComptabiliteDao()).thenReturn(comptabiliteDaoMock);
            Mockito.doNothing().when(comptabiliteDaoMock).updateEcritureComptable(Mockito.any(EcritureComptable.class));
            Mockito.doNothing().when(transactionManagerMock).commitMyERP(transactionStatusMock);
            Mockito.doNothing().when(transactionManagerMock).rollbackMyERP(null);

            manager.updateEcritureComptable(ecritureComptable);

            Mockito.verify(transactionManagerMock, atLeastOnce()).commitMyERP(transactionStatusMock);
            Mockito.verify(transactionManagerMock, atLeastOnce()).rollbackMyERP(null);
        }

        @Test
        @DisplayName("Suppression d'une écriture")
        public void comptesRead() {

            EcritureComptable ecritureComptable = getFullEcritureComptable();
            ecritureComptable.setId(1);

            TransactionStatus transactionStatusMock = Mockito.mock(TransactionStatus.class);
            Mockito.when(transactionManagerMock.beginTransactionMyERP()).thenReturn(transactionStatusMock);
            Mockito.when(daoProxyMock.getComptabiliteDao()).thenReturn(comptabiliteDaoMock);
            Mockito.doNothing().when(comptabiliteDaoMock).deleteEcritureComptable(Mockito.any(Integer.class));
            Mockito.doNothing().when(transactionManagerMock).commitMyERP(transactionStatusMock);
            Mockito.doNothing().when(transactionManagerMock).rollbackMyERP(null);

            manager.deleteEcritureComptable(1);

            Mockito.verify(transactionManagerMock, atLeastOnce()).commitMyERP(transactionStatusMock);
            Mockito.verify(transactionManagerMock, atLeastOnce()).rollbackMyERP(null);
        }
    }

    private EcritureComptable getEmptyEcritureComptable() {
        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        ecritureComptable.setDate(new Date());
        ecritureComptable.setLibelle("Libelle");
        return ecritureComptable;
    }

    private EcritureComptable getFullEcritureComptable() {
        EcritureComptable ecritureComptable = getEmptyEcritureComptable();
        addValidLines(ecritureComptable);
        ecritureComptable.setReference("AC-2020/00001");
        return ecritureComptable;
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
