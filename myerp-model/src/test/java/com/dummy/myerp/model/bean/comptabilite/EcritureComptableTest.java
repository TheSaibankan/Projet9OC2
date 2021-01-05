package com.dummy.myerp.model.bean.comptabilite;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;

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
    public void getTotalCredit() {
        EcritureComptable ecritureComptable = new EcritureComptable();
        LigneEcritureComptable creditA = createLigne(1, null, "200");
        LigneEcritureComptable creditB = createLigne(1, null, "201");
        LigneEcritureComptable debitA = createLigne(1, "201", null);
        ecritureComptable.getListLigneEcriture().addAll(Arrays.asList(creditA, creditB, debitA));
        assertEquals(ecritureComptable.getTotalCredit(), new BigDecimal(401));
    }

    @Test
    public void isEquilibree() {
        EcritureComptable vEcriture;
        vEcriture = new EcritureComptable();

        vEcriture.setLibelle("Equilibrée");
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "200", null));
        vEcriture.getListLigneEcriture().add(this.createLigne(1, null, "200"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, null, "500"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, "500", null));
        assertTrue(vEcriture.isEquilibree());

        vEcriture.getListLigneEcriture().clear();
        vEcriture.setLibelle("Non équilibrée");
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "10", null));
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "20", "1"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, null, "30"));
        vEcriture.getListLigneEcriture().add(this.createLigne(2, "1", "2"));
        assertFalse(vEcriture.isEquilibree());
    }

    @Test
    public void checkTotal(){
        EcritureComptable vEcriture = new EcritureComptable();
        vEcriture.getListLigneEcriture().add(this.createLigne(1, "250", null));
        vEcriture.getListLigneEcriture().add(this.createLigne(1, null, "250"));

        assertEquals(250, vEcriture.getTotalDebit().intValue());
        assertEquals(250, vEcriture.getTotalCredit().intValue());
    }

}
