package com.dummy.myerp.consumer;

import com.dummy.myerp.consumer.dao.contrat.ComptabiliteDao;
import com.dummy.myerp.model.bean.comptabilite.EcritureComptable;
import com.dummy.myerp.model.bean.comptabilite.JournalComptable;
import com.dummy.myerp.technical.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "applicationContext.xml")
public class ComptabiliteDaoImplTestIT {

    private static EcritureComptable ecritureComptable;

    @Autowired
    ComptabiliteDao comptabiliteDao;


    @Before
    public void initializeEcriture() {
        ecritureComptable = new EcritureComptable();
        ecritureComptable.setLibelle("Test1");
        ecritureComptable.setJournal(new JournalComptable("TT", "JournalTest"));
        ecritureComptable.setDate(new Date());
        ecritureComptable.setReference("TT-2021/00001");
    }

    @Test
    public void writeEcriture() {
        try {
            comptabiliteDao.insertEcritureComptable(ecritureComptable);
        } catch (Exception e) {
            fail("Cette exception ne devrait pas se produire : Échec de l'insertion de l'écriture");
        }

    }

    @Test
    public void getEcriture() {
        List<EcritureComptable> allEcritures = comptabiliteDao.getListEcritureComptable();

        EcritureComptable ecritureComptableRecup = getEcritureWithCatch();
        getEcritureWithCatch();
        assertNotNull(ecritureComptableRecup);

        assertTrue(allEcritures.contains(ecritureComptable));
        assertEquals(ecritureComptableRecup, ecritureComptable);
    }

    @Test
    public void updateEcriture() {
        EcritureComptable ecritureComptableRecup = getEcritureWithCatch();
        assertNotNull(ecritureComptableRecup);

        ecritureComptableRecup.setLibelle("LibelleUpdate");
        comptabiliteDao.updateEcritureComptable(ecritureComptableRecup);

        assertEquals("LibelleUpdate", Objects.requireNonNull(getEcritureWithCatch()).getLibelle());
    }

    @Test
    public void deleteEcriture() {
        int id = getEcritureWithCatch().getId();
        comptabiliteDao.deleteEcritureComptable(id);

    }

    private EcritureComptable getEcritureWithCatch() {
        try {
            return comptabiliteDao.getEcritureComptableByRef("TT-2021/00001");
        } catch (NotFoundException e) {
            fail("Cette exception ne devrait pas se produire = Requête GET renvoie NotFoundException");
            return null;
        }

    }


}
