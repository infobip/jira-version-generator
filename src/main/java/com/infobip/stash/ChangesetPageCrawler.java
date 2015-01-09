/**
 *# Copyright 2014 Infobip
 #
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 # http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 */
package com.infobip.stash;

import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.history.HistoryService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageUtils;

import java.util.Iterator;

/**
 * @author lpandzic
 */
public class ChangesetPageCrawler implements Iterator<Changeset> {

    private final HistoryService historyService;
    private final String branchName;
    private final Repository repository;
    private boolean hasReachedEnd;
    private Page<Changeset> currentPage;
    private Iterator<Changeset> currentPageIterator;

    public static ChangesetPageCrawler of(HistoryService historyService,
                                          String branchName,
                                          Repository repository) {

        Page<Changeset> currentPage = historyService.getChangesets(repository,
                                                                  branchName,
                                                                  null,
                                                                  PageUtils.newRequest(0, 2));

        return new ChangesetPageCrawler(historyService, branchName, repository, currentPage);
    }

    private ChangesetPageCrawler(HistoryService historyService, String branchName, Repository repository, Page<Changeset> currentPage) {

        this.historyService = historyService;
        this.branchName = branchName;
        this.repository = repository;
        hasReachedEnd = false;
        this.currentPage = currentPage;
        currentPageIterator = currentPage.getValues().iterator();
    }

    @Override
    public boolean hasNext() {

        if (hasReachedEnd) {
            return false;
        }

        if (currentPageIterator.hasNext()) {
            return true;
        }

        getNextPageIterator();

        return currentPageIterator.hasNext();
    }

    @Override
    public Changeset next() {

        return currentPageIterator.next();
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException();
    }

    void getNextPageIterator() {

        if (hasReachedEnd) {
            return;
        }

        PageRequest nextPageRequest = currentPage.getNextPageRequest();

        if (nextPageRequest == null) {
            hasReachedEnd = true;
            return;
        }

        currentPage = historyService.getChangesets(repository,
                                                   branchName,
                                                   null,
                                                   nextPageRequest);

        if (currentPage.getSize() == 0) {
            hasReachedEnd = true;
            return;
        }

        currentPageIterator = currentPage.getValues().iterator();
    }
}
