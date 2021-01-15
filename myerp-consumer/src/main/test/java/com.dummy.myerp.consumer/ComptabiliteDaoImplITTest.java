package com.dummy.myerp.consumer;

import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.model.bean.comptabilite.CompteComptable;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.model.bean.comptabilite.SequenceEcritureComptable;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"applicationContext.xml", "sqlContext.xml"})
public class ComptabiliteDaoImplITTest {

    private static EcritureComptable ecritureComptable;
    private static SequenceEcritureComptable sequenceEcritureComptable;

    @Autowired
    ComptabiliteDao comptabiliteDao;


    @BeforeAll
    static void initializeVariables() {
        ecritureComptable = new EcritureComptable();
        ecritureComptable.setLibelle("Test1");
        ecritureComptable.setJournal(new JournalComptable("AC", "Achat"));
        ecritureComptable.setDate(new Date());
        ecritureComptable.setReference("AC-2021/00001");

        sequenceEcritureComptable = new SequenceEcritureComptable();
        sequenceEcritureComptable.setAnnee(2055);
        sequenceEcritureComptable.setDerniereValeur(91234);


    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Opération CRUD avec EcritureComptable")
    public class EcritureComptableTest {

        @Test
        @Order(1)
        public void writeEcriture() {
            assertDoesNotThrow(() -> comptabiliteDao.insertEcritureComptable(ecritureComptable));
        }

        @Test
        @Order(2)
        public void getEcriture() throws NotFoundException {
            List<EcritureComptable> allEcritures = comptabiliteDao.getListEcritureComptable();

            EcritureComptable ecritureComptableRecup = comptabiliteDao.getEcritureComptableByRef("AC-2021/00001");
            assertNotNull(ecritureComptableRecup);

            assertNotNull(allEcritures.stream()
                    .filter(ecritureComptable1 -> "AC-2021/00001".equals(ecritureComptable1.getReference()))
                    .findAny()
                    .orElse(null));
            assertEquals(ecritureComptableRecup.getReference(), ecritureComptable.getReference());
        }

        @Test
        @Order(3)
        public void updateEcriture() throws NotFoundException {
            EcritureComptable ecritureComptableRecup = comptabiliteDao.getEcritureComptableByRef("AC-2021/00001");
            assertNotNull(ecritureComptableRecup);

            ecritureComptableRecup.setLibelle("LibelleUpdate");
            comptabiliteDao.updateEcritureComptable(ecritureComptableRecup);

            assertEquals("LibelleUpdate",
                    comptabiliteDao.getEcritureComptableByRef("AC-2021/00001").getLibelle());
        }

        @Test
        @Order(4)
        public void deleteEcriture() throws NotFoundException {
            int id = comptabiliteDao.getEcritureComptableByRef("AC-2021/00001").getId();
            comptabiliteDao.deleteEcritureComptable(id);

            assertThrows(NotFoundException.class, () -> comptabiliteDao.getEcritureComptable(id));

        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Opération CRUD avec SequenceEcritureComptable")
    public class SequenceComptableTest {

        @Test
        @Order(1)
        public void writeSequence() {
            assertDoesNotThrow(() -> comptabiliteDao.insertSequence(sequenceEcritureComptable, "AC"));
        }

        @Test
        @Order(2)
        public void getSequence() throws NotFoundException {
            SequenceEcritureComptable sequenceEcritureComptableTest =
                    comptabiliteDao.getSequenceEcritureComptable(2055, "AC");

            assertEquals(sequenceEcritureComptableTest.getAnnee(), sequenceEcritureComptable.getAnnee());
            assertEquals(sequenceEcritureComptableTest.getDerniereValeur(), sequenceEcritureComptable.getDerniereValeur());
        }

        @Test
        @Order(3)
        public void updateSequence() throws NotFoundException {
            SequenceEcritureComptable sequenceEcritureComptableTest =
                    comptabiliteDao.getSequenceEcritureComptable(2055, "AC");

            sequenceEcritureComptableTest.setDerniereValeur(20066);
            comptabiliteDao.updateSequenceEcritureComptable(sequenceEcritureComptableTest, "AC");

            assertDoesNotThrow(() -> comptabiliteDao.getSequenceEcritureComptable(2055, "AC"));
        }

        @Test
        @Order(4)
        public void deleteSequence() {
            comptabiliteDao.deleteSequenceEcritureComptable(2055, "AC", 20066);
            assertThrows(NotFoundException.class,
                    () -> comptabiliteDao.getSequenceEcritureComptable(2066, "AC"));
        }

    }

    @Test
    public void getListCompteComptable() {
        List<CompteComptable> compteComptableList = assertDoesNotThrow(() -> comptabiliteDao.getListCompteComptable());
        assertNotNull(compteComptableList);
    }

    @Test
    public void getListJournalComptable() {
        List<JournalComptable> journalComptableList = assertDoesNotThrow(() -> comptabiliteDao.getListJournalComptable());
        assertNotNull(journalComptableList);
    }


}
