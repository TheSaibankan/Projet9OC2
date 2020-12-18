package com.dummy.myerp.consumer.dao.impl.db.rowmapper.comptabilite;

import com.dummy.myerp.consumer.dao.impl.cache.JournalComptableDaoCache;
import com.dummy.myerp.model.bean.comptabilite.SequenceEcritureComptable;
import org.springframework.jdbc.core.RowMapper;

import javax.swing.tree.TreePath;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SequenceEcritureComptableRM implements RowMapper<SequenceEcritureComptable> {

    private final JournalComptableDaoCache journalComptableDaoCache = new JournalComptableDaoCache();

    @Override
    public SequenceEcritureComptable mapRow(ResultSet resultSet, int i) throws SQLException {
        SequenceEcritureComptable sequenceEcritureComptable = new SequenceEcritureComptable();
        sequenceEcritureComptable.setAnnee(resultSet.getInt("annee"));
        sequenceEcritureComptable.setDerniereValeur(resultSet.getInt("derniere_valeur"));
        return sequenceEcritureComptable;
    }
}

