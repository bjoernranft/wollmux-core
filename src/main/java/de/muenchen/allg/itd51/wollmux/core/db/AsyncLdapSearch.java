package de.muenchen.allg.itd51.wollmux.core.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncLdapSearch
{
  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLdapSearch.class);
  private List<Map<String, String>> searchQuery = new ArrayList<>();
  private DatasourceJoiner dj = null;

  public AsyncLdapSearch(List<Map<String, String>> searchQuery, DatasourceJoiner dj)
  {
    this.searchQuery = searchQuery;
    this.dj = dj;
  }

  public CompletableFuture<QueryResults> runLdapSearchAsync()
  {
    return this.asyncLdapSearch;
  }

  private CompletableFuture<QueryResults> asyncLdapSearch = CompletableFuture.supplyAsync(() -> {
    QueryResults results = null;

    try
    {
      if (searchQuery == null || dj == null)
        return null;

      results = Search.search(searchQuery.get(0), dj);
    } catch (TimeoutException | IllegalArgumentException e)
    {
      LOGGER.error("", e);
    }

    return results;
  });

}
