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

package org.eclipse.dataspaceconnector.core.security.hashicorp;

import org.eclipse.dataspaceconnector.spi.EdcException;

public class HashicorpVaultException extends EdcException {

  public HashicorpVaultException(String message) {
    super(message);
  }

  public HashicorpVaultException(String message, Throwable cause) {
    super(message, cause);
  }

  public HashicorpVaultException(Throwable cause) {
    super(cause);
  }

  public HashicorpVaultException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
