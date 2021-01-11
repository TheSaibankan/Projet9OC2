package com.dummy.myerp.model.bean.comptabilite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SequenceEcritureComptableTest {

    @Test
    @DisplayName("Vérifie si toString() renvoie les bonnes valeurs")
    public void verifyToString() {
        SequenceEcritureComptable sequenceEcritureComptable = new SequenceEcritureComptable();
        sequenceEcritureComptable.setAnnee(2021);
        sequenceEcritureComptable.setDerniereValeur(91234);

        Assertions.assertEquals("SequenceEcritureComptable{annee=2021, derniereValeur=91234}", sequenceEcritureComptable.toString());
    }
}
