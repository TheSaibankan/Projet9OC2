package com.dummy.myerp.model.bean.comptabilite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.dummy.myerp.model.bean.comptabilite.JournalComptable.getByCode;
import static org.junit.jupiter.api.Assertions.*;

public class JournalComptableTest {

    @Test
    @DisplayName("Vérifie si toString() renvoie les bonnes valeurs")
    public void verifyToString() {
        JournalComptable journalComptable = getFullJournalComptable("AC", "JournalTest");

        assertEquals("JournalComptable{code='AC', libelle='JournalTest'}",
                journalComptable.toString());
    }

    @Test
    @DisplayName("Test de la méthode getByCode()")
    public void verifyGetByCode() {
        JournalComptable journalComptableTest1 = getFullJournalComptable("AA", "journalComptableTest1");
        JournalComptable journalComptableTest2 = getFullJournalComptable("AB", "journalComptableTest2");
        JournalComptable journalComptableTest3 = getFullJournalComptable("AC", "journalComptableTest3");

        List<JournalComptable> journalComptableList = new ArrayList<>();

        journalComptableList.add(journalComptableTest1);
        journalComptableList.add(journalComptableTest2);

        assertEquals(journalComptableTest1, getByCode(journalComptableList, "AA"));
        assertNotEquals(journalComptableTest2, getByCode(journalComptableList, "AA"));

        assertNull(getByCode(journalComptableList, "AC"));

    }

    private JournalComptable getFullJournalComptable(String code, String libelle) {
        JournalComptable journalComptable = new JournalComptable();
        journalComptable.setCode(code);
        journalComptable.setLibelle(libelle);
        return journalComptable;
    }

}
