/*
 *  Copyright (c) 2022 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Initial API and Implementation
 *
 */

package net.catenax.edc.tests;

import java.util.stream.Stream;

import io.cucumber.java.en.Given;
import net.catenax.edc.tests.data.Asset;

public class AssetStepDefs {

  @Given("{connector} has no assets")
  public void hasNoAssets(Connector connector) throws Exception {

    final DataManagementAPI api = connector.getDataManagementAPI();

    Stream<Asset> assets = api.getAllAssets();
    for (Asset asset : assets.toArray(Asset[]::new)) {
      api.deleteAsset(asset.getId());
    }
  }

}
