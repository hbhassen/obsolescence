package com.example.dashboardradar.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SemanticVersionComparatorTest {

    @Test
    void compareHandlesSemverOrdering() {
        assertThat(SemanticVersionComparator.compare("3.2.1", "3.2.0")).isGreaterThan(0);
        assertThat(SemanticVersionComparator.compare("3.2.0", "3.2.0")).isZero();
        assertThat(SemanticVersionComparator.compare("2.9", "3.0")).isLessThan(0);
    }

    @Test
    void compareHandlesNullAndTextualVersions() {
        assertThat(SemanticVersionComparator.compare(null, "1.0.0")).isLessThan(0);
        assertThat(SemanticVersionComparator.compare("1.0.0", null)).isGreaterThan(0);
        assertThat(SemanticVersionComparator.compare("v1.2.3", "1.2.3")).isZero();
    }
}
