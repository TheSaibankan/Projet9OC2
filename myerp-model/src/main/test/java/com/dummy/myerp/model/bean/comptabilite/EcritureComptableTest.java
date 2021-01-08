package com.dummy.myerp.model.bean.comptabilite;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


public class EcritureComptableTest {

    private LigneEcritureComptable createLigne(Integer pCompteComptableNumero, String pDebit, String pCredit) {
        BigDecimal vDebit = pDebit == null ? null : new BigDecimal(pDebit);
        BigDecimal vCredit = pCredit == null ? null : new BigDecimal(pCredit);
        String vLibelle = ObjectUtils.defaultIfNull(vDebit, BigDecimal.ZERO)
                                     .subtract(ObjectUtils.defaultIfNull(vCredit, BigDecimal.ZERO)).toPlainString();
        return new LigneEcritureComptable(new CompteComptable(pCompteComptableNumero),
                                                                    vLibelle,
                                                                    vDebit, vCredit);
    }

    @Test
    @DisplayName("Vérifie le total crédit et débit de l'écriture")
    public void getTotalCreditAndDebit() {
        EcritureComptable ecritureComptable = new EcritureComptable();
        LigneEcritureComptable creditA = createLigne(1, null, "200");
        LigneEcritureComptable creditB = createLigne(1, null, "201");
        LigneEcritureComptable debitA = createLigne(1, "201", null);
        ecritureComptable.getListLigneEcriture().addAll(Arrays.asList(creditA, creditB, debitA));

        assertEquals(ecritureComptable.getTotalCredit(), new BigDecimal(401));
        assertEquals(ecritureComptable.getTotalDebit(), new BigDecimal(201));
    }


    @Test
    @DisplayName("Vérifie si l'écriture est équilibré")
    public void isEquilibree() {
        EcritureComptable ecritureComptable = new EcritureComptable();

        ecritureComptable.setLibelle("Equilibrée");
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, "200", null));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, null, "200"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, null, "500"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, "500", null));
        assertTrue(ecritureComptable.isEquilibree());

        ecritureComptable.getListLigneEcriture().clear();
        ecritureComptable.setLibelle("Non équilibrée");
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, "10", null));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(1, "20", "1"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, null, "30"));
        ecritureComptable.getListLigneEcriture().add(this.createLigne(2, "1", "2"));
        assertFalse(ecritureComptable.isEquilibree());
    }

    @Test
    @DisplayName("Vérifie si toString() renvoie les bonnes valeurs")
    public void verifyToString() {
        Date actualDate = new Date();

        EcritureComptable ecritureComptable = new EcritureComptable();
        ecritureComptable.setId(1);
        ecritureComptable.setDate(actualDate);
        ecritureComptable.setLibelle("TestEcritureToString");

        assertEquals("EcritureComptable{id=1, " +
                "journal=null, " +
                "reference='null', " +
                "date="+actualDate+", " +
                "libelle='TestEcritureToString', " +
                "totalDebit=0, totalCredit=0, listLigneEcriture=[\n\n]}", ecritureComptable.toString());
    }

}
