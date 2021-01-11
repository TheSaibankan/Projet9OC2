package com.dummy.myerp.model.bean.comptabilite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.dummy.myerp.model.bean.comptabilite.CompteComptable.getByNumero;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompteComptableTest {

    @Test
    @DisplayName("Vérifie si toString() renvoie les bonnes valeurs")
    public void verifyToString() {
        CompteComptable compteComptable = new CompteComptable();
        compteComptable.setLibelle("CompteComptableTest");
        compteComptable.setNumero(12);

        assertEquals("CompteComptable{numero=12, libelle='CompteComptableTest'}", compteComptable.toString());
    }

    @Test
    @DisplayName("Vérifie la méthode de récupération par numéro")
    public void verifyGetNumero() {
        List<CompteComptable> compteComptableList = new ArrayList<>();
        CompteComptable compteComptableTest = new CompteComptable(15, "TestCompte");
        CompteComptable compteComptableTest2 = new CompteComptable(18, "TestCompte2");
        compteComptableList.add(compteComptableTest);
        compteComptableList.add(compteComptableTest2);
        compteComptableList.add(new CompteComptable(19, "TestCompte3"));

        assertEquals(getByNumero(compteComptableList, 15), compteComptableTest);
        assertNotEquals(getByNumero(compteComptableList, 22), compteComptableTest2);
    }
}
