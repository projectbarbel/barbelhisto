package com.projectbarbel.histo;

import com.projectbarbel.histo.journal.functions.JournalUpdateStrategyEmbedding.JournalUpdateCase;

public interface UpdateCaseAware {
    JournalUpdateCase getActualCase();
}
