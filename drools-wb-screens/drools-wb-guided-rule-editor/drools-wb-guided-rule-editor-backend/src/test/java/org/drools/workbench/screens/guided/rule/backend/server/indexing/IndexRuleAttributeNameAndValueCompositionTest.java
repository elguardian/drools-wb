/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.workbench.screens.guided.rule.backend.server.indexing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.lucene.search.Query;
import org.drools.workbench.screens.guided.rule.type.GuidedRuleDRLResourceTypeDefinition;
import org.guvnor.common.services.project.categories.Decision;
import org.junit.Test;
import org.kie.workbench.common.services.refactoring.backend.server.BaseIndexingTest;
import org.kie.workbench.common.services.refactoring.backend.server.TestIndexer;
import org.kie.workbench.common.services.refactoring.backend.server.query.builder.SingleTermQueryBuilder;
import org.kie.workbench.common.services.refactoring.model.index.terms.valueterms.ValueSharedPartIndexTerm;
import org.kie.workbench.common.services.refactoring.service.PartType;
import org.uberfire.ext.metadata.io.KObjectUtil;
import org.uberfire.java.nio.file.Path;

@NotThreadSafe
public class IndexRuleAttributeNameAndValueCompositionTest extends BaseIndexingTest<GuidedRuleDRLResourceTypeDefinition> {

    @Test
    public void testIndexDrlRuleAttributeNameAndValues() throws IOException, InterruptedException {
        //Add test files
        final Path path = basePath.resolve( "drl1.rdrl" );
        final String drl = loadText( "drl1.rdrl" );
        ioService().write( path,
                           drl );

        Thread.sleep( 5000 ); //wait for events to be consumed from jgit -> (notify changes -> watcher -> index) -> lucene index

        List<String> index = Arrays.asList(KObjectUtil.toKCluster(basePath.getFileSystem()).getClusterId());

        //DRL defining a RuleFlow-Group named myRuleFlowGroup. This should match drl5.drl
        //This checks whether there is a Rule Attribute "ruleflow-group" and its Value is "myRuleflowGroup"
        {
            final Query query = new SingleTermQueryBuilder( new ValueSharedPartIndexTerm( "myRuleFlowGroup", PartType.RULEFLOW_GROUP) )
                    .build();
            searchFor(index, query, 1, path);
        }

        //DRL defining a RuleFlow-Group named myAgendaGroup. This should *NOT* match drl5.drl
        {
            final Query query = new SingleTermQueryBuilder( new ValueSharedPartIndexTerm( "myAgendaGroup", PartType.RULEFLOW_GROUP) )
                    .build();
            searchFor(index, query, 0);
        }

    }

    @Override
    protected TestIndexer getIndexer() {
        return new TestGuidedRuleDrlFileIndexer();
    }

    @Override
    protected GuidedRuleDRLResourceTypeDefinition getResourceTypeDefinition() {
        return new GuidedRuleDRLResourceTypeDefinition(new Decision());
    }

    @Override
    protected String getRepositoryName() {
        return this.getClass().getSimpleName();
    }

}
