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

import com.atlassian.bitbucket.commit.*;
import com.atlassian.bitbucket.repository.*;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageUtils;

import java.util.Iterator;
import java.util.function.Function;

class CommitPageCrawler implements Iterator<Commit> {

    private final static int PAGE_REQUEST_LIMIT = 5;

    private final Function<PageRequest, Page<Commit>> pageProvider;

    private boolean hasReachedEnd;
    private Page<Commit> currentPage;
    private Iterator<Commit> currentPageIterator;

    public static CommitPageCrawler of(CommitService commitService,
                                       Repository repository,
                                       RefChange from) {

        Function<PageRequest, Page<Commit>> pageProvider = pageRequest -> {
            CommitsBetweenRequest request = new CommitsBetweenRequest.Builder(repository)
                    .include(from.getToHash())
                    .build();

            return commitService.getCommitsBetween(request, pageRequest);
        };

        Page<Commit> currentPage = pageProvider.apply(PageUtils.newRequest(0, PAGE_REQUEST_LIMIT));

        return new CommitPageCrawler(pageProvider, currentPage);
    }

    private CommitPageCrawler(Function<PageRequest, Page<Commit>> pageProvider,
                              Page<Commit> currentPage) {

        this.pageProvider = pageProvider;
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

        currentPage = pageProvider.apply(currentPage.getNextPageRequest());

        if (currentPage.getSize() == 0) {
            hasReachedEnd = true;
            return;
        }

        currentPageIterator = currentPage.getValues().iterator();
    }
}
