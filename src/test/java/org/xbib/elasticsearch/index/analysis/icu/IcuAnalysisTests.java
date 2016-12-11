package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xbib.elasticsearch.MapperTestUtils.tokenFilterFactory;
import static org.xbib.elasticsearch.MapperTestUtils.tokenizerFactory;

/**
 *
 */
public class IcuAnalysisTests {

    @Test
    public void testDefaultsIcuAnalysis() throws IOException {

        TokenizerFactory tf = tokenizerFactory("icu_tokenizer");
        assertThat(tf, instanceOf(IcuTokenizerFactory.class));

        TokenFilterFactory filterFactory = tokenFilterFactory("icu_normalizer");
        assertThat(filterFactory, instanceOf(IcuNormalizerTokenFilterFactory.class));

        filterFactory = tokenFilterFactory("icu_folding");
        assertThat(filterFactory, instanceOf(IcuFoldingTokenFilterFactory.class));

        filterFactory = tokenFilterFactory("icu_transform");
        assertThat(filterFactory, instanceOf(IcuTransformTokenFilterFactory.class));

        Analyzer analyzer = MapperTestUtils.analyzer( "icu_collation");
        assertThat(analyzer, instanceOf(IcuCollationKeyAnalyzer.class));
    }
}
