/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.any23.extractor.html;

import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.ExtractionResult;
import org.apache.any23.extractor.ExtractorDescription;
import org.apache.any23.extractor.TagSoupExtractionResult;
import org.apache.any23.vocab.DCTERMS;
import org.apache.any23.vocab.REVIEW;
import org.apache.any23.vocab.VCARD;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.RDF;
import org.w3c.dom.Node;

import java.util.List;

import static org.apache.any23.extractor.html.HTMLDocument.TextField;

/**
 * Extractor for the <a href="http://microformats.org/wiki/hreview">hReview</a>
 * microformat.
 *
 * @author Gabriele Renzi
 */
public class HReviewExtractor extends EntityBasedMicroformatExtractor {

    private static final REVIEW  vREVIEW  = REVIEW.getInstance();
    private static final VCARD   vVCARD   = VCARD.getInstance();
    private static final DCTERMS vDCTERMS = DCTERMS.getInstance();

    @Override
    public ExtractorDescription getDescription() {
        return HReviewExtractorFactory.getDescriptionInstance();
    }

    @Override
    protected String getBaseClassName() {
        return "hreview";
    }

    @Override
    protected void resetExtractor() {
        // Empty.
    }

    @Override
    protected boolean extractEntity(Node node, ExtractionResult out) throws ExtractionException {
        BNode rev = getBlankNodeFor(node);
        out.writeTriple(rev, RDF.TYPE, vREVIEW.Review);
        final HTMLDocument fragment = new HTMLDocument(node);
        addRating(fragment, rev);
        addSummary(fragment, rev);
        addTime(fragment, rev);
        addType(fragment, rev);
        addDescription(fragment, rev);
        addItem(fragment, rev);
        addReviewer(fragment, rev);

        final TagSoupExtractionResult tser = (TagSoupExtractionResult) out;
        tser.addResourceRoot(
                DomUtils.getXPathListForNode(node),
                rev,
                this.getClass()
        );

        return true;
    }

    private void addType(HTMLDocument doc, Resource rev) {
        TextField value = doc.getSingularTextField("type");
        conditionallyAddStringProperty(
                value.source(),
                rev, vREVIEW.type, value.value()
        );
    }

    private void addReviewer(HTMLDocument doc, Resource rev) {
        List<Node> nodes = doc.findAllByClassName("reviewer");
        if (nodes.size() > 0) {
            Node node0 = nodes.get(0);
            addBNodeProperty(
                    node0,
                    rev, vREVIEW.reviewer, getBlankNodeFor(node0)
            );
        }
    }

    private void addItem(HTMLDocument root, BNode rev) throws ExtractionException {
        List<Node> nodes = root.findAllByClassName("item");
        for (Node node : nodes) {
            Resource item = findDummy(new HTMLDocument(node));
            addBNodeProperty(
                    node,
                    item, vREVIEW.hasReview, rev
            );
        }
    }

    private Resource findDummy(HTMLDocument item) throws ExtractionException {
        Resource blank = getBlankNodeFor(item.getDocument());
        TextField val = item.getSingularTextField("fn");
        conditionallyAddStringProperty(
                val.source(),
                blank, vVCARD.fn, val.value()
        );
        final TextField url = item.getSingularUrlField("url");
        conditionallyAddResourceProperty(blank, vVCARD.url, getHTMLDocument().resolveURI(url.value()));
        TextField pics[] = item.getPluralUrlField("photo");
        for (TextField pic : pics) {
            addURIProperty(blank, vVCARD.photo, getHTMLDocument().resolveURI(pic.value()));
        }
        return blank;
    }

    private void addRating(HTMLDocument doc, Resource rev) {
        HTMLDocument.TextField value = doc.getSingularTextField("rating");
        conditionallyAddStringProperty(
                value.source(), rev, vREVIEW.rating, value.value()
        );
    }

    private void addSummary(HTMLDocument doc, Resource rev) {
        TextField value = doc.getSingularTextField("summary");
        conditionallyAddStringProperty(
                value.source(),
                rev, vREVIEW.title, value.value()
        );
    }

    private void addTime(HTMLDocument doc, Resource rev) {
        TextField value = doc.getSingularTextField("dtreviewed");
        conditionallyAddStringProperty(
                value.source(),
                rev, vDCTERMS.date, value.value()
        );
    }

    private void addDescription(HTMLDocument doc, Resource rev) {
        TextField value = doc.getSingularTextField("description");
        conditionallyAddStringProperty(
                value.source(),
                rev, vREVIEW.text, value.value()
        );
    }

}