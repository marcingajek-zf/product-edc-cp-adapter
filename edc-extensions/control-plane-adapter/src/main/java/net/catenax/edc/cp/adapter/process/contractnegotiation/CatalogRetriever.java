/*
 * Copyright (c) 2022 ZF Friedrichshafen AG
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Contributors:
 * ZF Friedrichshafen AG - Initial API and Implementation
 *
 */

package net.catenax.edc.cp.adapter.process.contractnegotiation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import net.catenax.edc.cp.adapter.exception.ExternalRequestException;
import org.eclipse.dataspaceconnector.api.datamanagement.catalog.service.CatalogService;
import org.eclipse.dataspaceconnector.spi.query.Criterion;
import org.eclipse.dataspaceconnector.spi.query.QuerySpec;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;

@RequiredArgsConstructor
public class CatalogRetriever {
  private final CatalogService catalogService;

  public Catalog getEntireCatalog(String providerUrl) {
    return getEntireCatalog(providerUrl, null);
  }

  public Catalog getEntireCatalog(String providerUrl, String assetId) {
    int limit = 50;
    int offset = 0;

    Catalog catalogResult = getCatalog(providerUrl, getQuerySpec(limit, offset, assetId));
    boolean reachedLastPage = catalogResult.getContractOffers().size() < limit;

    while (!reachedLastPage) {
      offset += limit;
      Catalog catalog = getCatalog(providerUrl, getQuerySpec(limit, offset, assetId));
      catalogResult.getContractOffers().addAll(catalog.getContractOffers());
      reachedLastPage = catalog.getContractOffers().size() < limit;
    }

    return catalogResult;
  }

  public Catalog getCatalog(String providerUrl, QuerySpec querySpec) {
    try {
      return catalogService.getByProviderUrl(providerUrl, querySpec).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new ExternalRequestException("Could not retrieve contract offer.", e);
    }
  }

  private QuerySpec getQuerySpec(int limit, int offset, String assetId) {
    List<Criterion> filters =
        Objects.isNull(assetId)
            ? Collections.emptyList()
            : List.of(new Criterion("asset:prop:id", "=", assetId));
    return QuerySpec.Builder.newInstance().offset(offset).filter(filters).limit(limit).build();
  }
}
