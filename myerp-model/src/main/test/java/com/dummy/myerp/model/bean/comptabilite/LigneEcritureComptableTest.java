package com.dummy.myerp.model.bean.comptabilite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class LigneEcritureComptableTest {

    @Test
    @DisplayName("Vérifie si toString() renvoie les bonnes valeurs")
    public void verifyToString() {
        LigneEcritureComptable ligneEcritureComptable = new LigneEcritureComptable();
        ligneEcritureComptable.setCompteComptable(new CompteComptable(15, "TestCompte"));
        ligneEcritureComptable.setLibelle("TestLigne");
        ligneEcritureComptable.setCredit(new BigDecimal("500"));
        ligneEcritureComptable.setDebit(new BigDecimal("500"));

        Assertions.assertEquals("LigneEcritureComptable{compteComptable=CompteComptable{numero=15, libelle='TestCompte'}, " +
                "libelle='TestLigne', " +
                "debit=500, " +
                "credit=500}", ligneEcritureComptable.toString());
    }
}
