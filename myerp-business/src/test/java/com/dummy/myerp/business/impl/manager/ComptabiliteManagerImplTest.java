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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Test des règles de gestion")
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
    @DisplayName("Ecriture comptable valide")
    public void checkEcritureComptableUnit() throws Exception {
        EcritureComptable vEcritureComptable = getEcritureComptable();
        vEcritureComptable.setReference("AC-2020/00001");
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                                                                                 null, null,
                                                                                 new BigDecimal(123)));
        manager.checkEcritureComptableUnit(vEcritureComptable);
    }

    @Test
    @DisplayName("Ecriture comptable vide")
    public void checkEcritureComptableUnitViolation() throws Exception {
        EcritureComptable vEcritureComptable = new EcritureComptable();
        assertThrows(FunctionalException.class,
                () -> manager.checkEcritureComptable(vEcritureComptable));
    }

    @Test
    @DisplayName("Ecriture comptable non-équilibré")
    public void checkEcritureComptableUnitRG2() throws Exception {
        EcritureComptable vEcritureComptable = getEcritureComptable();
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                                                                                 null, new BigDecimal(123),
                                                                                 null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                                                                                 null, null,
                                                                                 new BigDecimal(1234)));
        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(vEcritureComptable));
    }

    @Nested
    @DisplayName("Test des scénarios relatifs à la règle de gestion 3")
    class checkRG3 {
        @Test
        @DisplayName("Ecriture comptable sans ligne débit")
        public void checkEcritureComptableUnitRG3Debit() {
            EcritureComptable vEcritureComptable = getEcritureComptable();
            vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, new BigDecimal(123),
                    null));
            vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, new BigDecimal(123),
                    null));
            assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(vEcritureComptable));
        }
        @Test
        @DisplayName("Ecriture comptable sans ligne crédit")
        public void checkEcritureComptableUnitRG3Credit() {
            EcritureComptable vEcritureComptable = getEcritureComptable();
            vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, null,
                    new BigDecimal(123)));
            vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                    null, null,
                    new BigDecimal(123)));
            assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(vEcritureComptable));
        }
    }

    @Test
    @DisplayName("Ecriture avec plus de 2 chiffres après la virgule")
    public void only2DigitsAfterDecimal() {
        EcritureComptable vEcritureComptable = getEcritureComptable();
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(1),
                null, new BigDecimal(123.222),
                null));
        vEcritureComptable.getListLigneEcriture().add(new LigneEcritureComptable(new CompteComptable(2),
                null, null,
                new BigDecimal(123.567)));

        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(vEcritureComptable));
    }

    @Test
    @DisplayName("Ecriture dont la référence existe déjà")
    public void referenceMustBeUnique() throws NotFoundException {
        EcritureComptable vEcritureComptable = getEcritureComptable();
        addValidLines(vEcritureComptable);
        vEcritureComptable.setReference("AC-2020/00001");

        when(comptabiliteDaoMock.getEcritureComptableByRef("AC-2020/00001")).thenReturn(vEcritureComptable);

        EcritureComptable ecritureComptableComparaison = new EcritureComptable();
        ecritureComptableComparaison.setLibelle("Libelle2");
        ecritureComptableComparaison.setJournal(new JournalComptable("AC", "Achat"));
        ecritureComptableComparaison.setDate(new Date());
        addValidLines(ecritureComptableComparaison);
        ecritureComptableComparaison.setReference("AC-2020/00001");

        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableContext(ecritureComptableComparaison));
    }

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

            ecritureComptable.setReference("CQ-2020/00001");

            assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(ecritureComptable));
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

            assertThrows(FunctionalException.class, () -> manager.checkEcritureComptableUnit(ecritureComptable));
        }
    }


    @Nested
    @DisplayName("Test des scénarios relatifs aux références")
    class checkRef {
        @Test
        @DisplayName("Ajout de la séquence : séquence déjà existante")
        public void addRefWithCreatedSequence() throws NotFoundException {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2020, Calendar.JANUARY, 1);

            EcritureComptable ecritureComptable = new EcritureComptable();
            ecritureComptable.setJournal(new JournalComptable("BQ", "Banque"));
            ecritureComptable.setDate(calendar.getTime());
            ecritureComptable.setLibelle("Libelle");
            addValidLines(ecritureComptable);

            when(comptabiliteDaoMock.getSequenceEcritureComptable(2020, "BQ")).thenReturn(new SequenceEcritureComptable(2020,51));
            manager.addReference(ecritureComptable);

            assertEquals("BQ-2020/00052", ecritureComptable.getReference());
        }

        @Test
        @DisplayName("Ajout de la séquence : séquence inexistante")
        public void addRefWithNoSequenceCreated() throws NotFoundException {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2020, Calendar.JANUARY, 1);

            EcritureComptable ecritureComptable = new EcritureComptable();
            ecritureComptable.setJournal(new JournalComptable("BQ", "Banque"));
            ecritureComptable.setDate(calendar.getTime());
            ecritureComptable.setLibelle("Libelle");
            addValidLines(ecritureComptable);

            when(comptabiliteDaoMock.getSequenceEcritureComptable(2020, "BQ"))
                    .thenThrow(new NotFoundException("Séquence non trouvée"));
            manager.addReference(ecritureComptable);

            assertEquals("BQ-2020/00001", ecritureComptable.getReference());
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
