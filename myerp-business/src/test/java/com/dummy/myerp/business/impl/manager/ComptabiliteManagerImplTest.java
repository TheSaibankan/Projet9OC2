package com.dummy.myerp.business.impl.manager;

import com.dummy.myerp.business.contrat.BusinessProxy;
import com.dummy.myerp.business.impl.AbstractBusinessManager;
import com.dummy.myerp.business.impl.TransactionManager;
import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.consumer.dao.contrat.DaoProxy;
import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.model.bean.comptabilite.LigneEcritureComptable;
import com.dummy.myerp.technical.exception.FunctionalException;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
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

        when(comptabiliteDaoMock.getEcritureComptableByRef("TestRef")).thenReturn(vEcritureComptable);

        EcritureComptable ecritureComptableComparaison = new EcritureComptable();
        ecritureComptableComparaison.setLibelle("Libelle2");
        ecritureComptableComparaison.setJournal(new JournalComptable("AC", "Achat"));
        ecritureComptableComparaison.setDate(new Date());
        addValidLines(ecritureComptableComparaison);
        ecritureComptableComparaison.setReference("AC-2020/00001");

        assertThrows(FunctionalException.class, () -> manager.checkEcritureComptable(ecritureComptableComparaison));

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
