/**
 *# Copyright 2016 Infobip
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
package com.infobip.bitbucket;

import com.atlassian.bitbucket.commit.Commit;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.commit.CommitsRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageUtils;

import java.util.Iterator;

class ChangesetPageCrawler implements Iterator<Commit> {

    private final CommitService commitService;
    private final String branchName;
    private final Repository repository;
    private boolean hasReachedEnd;
    private Page<Commit> currentPage;
    private Iterator<Commit> currentPageIterator;

    public static ChangesetPageCrawler of(CommitService historyService,
                                          String branchName,
                                          Repository repository) {

        CommitsRequest request = new CommitsRequest.Builder(repository, branchName).build();

        Page<Commit> currentPage = historyService.getCommits(request, PageUtils.newRequest(0, 2));

        return new ChangesetPageCrawler(historyService, branchName, repository, currentPage);
    }

    private ChangesetPageCrawler(CommitService commitService, String branchName, Repository repository, Page<Commit> currentPage) {

        this.commitService = commitService;
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
    public Commit next() {

        return currentPageIterator.next();
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException();
    }

    private void getNextPageIterator() {

        if (hasReachedEnd) {
            return;
        }

        PageRequest nextPageRequest = currentPage.getNextPageRequest();

        if (nextPageRequest == null) {
            hasReachedEnd = true;
            return;
        }

        CommitsRequest request = new CommitsRequest.Builder(repository, branchName).build();

        currentPage = commitService.getCommits(request, nextPageRequest);

        if (currentPage.getSize() == 0) {
            hasReachedEnd = true;
            return;
        }

        currentPageIterator = currentPage.getValues().iterator();
    }
}
